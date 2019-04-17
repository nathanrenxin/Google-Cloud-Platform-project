/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.kth.ledbat;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ledbat.msgs.LedbatMsg;
import se.kth.ledbat.util.Cwnd;
import se.kth.ledbat.util.LedbatConfig;
import se.kth.ledbat.util.RTTEstimator;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.test.NetworkTest;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.util.Identifiable;
import se.sics.kompics.util.Identifier;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.util.RingTimer;
import se.sics.util.RingTimer.Container;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class LedbatSenderComp extends ComponentDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(LedbatSenderComp.class);
  private String logPrefix = "";

  Positive<Timer> timerPort = requires(Timer.class);
  Negative<Network> incomingNetworkPort = provides(Network.class);
  Positive<Network> outgoingNetworkPort = requires(Network.class);

  private final Identifier dataId;
  private final Identifier senderId;
  private final Identifier receiverId;
  private final Cwnd cwnd;
  private final RTTEstimator rttEstimator;
  private final RingTimer ringTimer;
  private final LedbatConfig ledbatConfig;
  private UUID ringTimeout;
  private UUID statusTimeout;
  private LinkedList<BasicContentMsg> pendingData = new LinkedList<>();

  public LedbatSenderComp(Init init) {
    dataId = init.dataId;
    senderId = init.senderId;
    receiverId = init.receiverId;
    logPrefix = "<" + dataId + "," + senderId + "," + receiverId + ">";

    ledbatConfig = new LedbatConfig();
    cwnd = new Cwnd(ledbatConfig);
    rttEstimator = new RTTEstimator(ledbatConfig);
    ringTimer = new RingTimer(HardCodedConfig.windowSize, HardCodedConfig.maxTimeout);

    subscribe(handleStart, control);
    subscribe(handleIncomingAck, outgoingNetworkPort);
    subscribe(handleOutgoingMsg, incomingNetworkPort);
    subscribe(handleRingTimeout, timerPort);
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting...", logPrefix);
      scheduleRingTimeout(HardCodedConfig.windowSize);
    }
  };

  @Override
  public void tearDown() {
    cancelRingTimeout();
  }



  ClassMatchedHandler handleIncomingAck
    = new ClassMatchedHandler<LedbatMsg.Ack, BasicContentMsg<?, ?, LedbatMsg.Ack>>() {

      @Override
      public void handle(LedbatMsg.Ack payload, BasicContentMsg<?, ?, LedbatMsg.Ack> msg) {
        LOG.trace("{}received:{}", logPrefix, msg);
        Optional<Container> containerAux = ringTimer.cancelTimeout(payload.eventId);
        if (!containerAux.isPresent()) {
          LOG.trace("{}late:{}", logPrefix, payload.getId());
          return;
        }
        long now = System.currentTimeMillis();

        payload.ackDelay.receive(now);  //addded

        long rtt = payload.ackDelay.receive - payload.dataDelay.send;
        long dataDelay = payload.dataDelay.receive - payload.dataDelay.send;
        cwnd.ack(now, dataDelay, ledbatConfig.MSS);
        rttEstimator.update(rtt);
        ringTimer.cancelTimeout(payload.eventId);
        LOG.info("triggering msg inc");
        trigger(msg,incomingNetworkPort);
        trySend();
      }
    };

  Handler handleOutgoingMsg = new Handler<BasicContentMsg>() {
    @Override
    public void handle(BasicContentMsg msg) {
      LOG.trace("{}received:{}", logPrefix, msg);
      pendingData.add((BasicContentMsg) msg);
      trySend();
      return;
    }
  };

  Handler handleRingTimeout = new Handler<RingTimeout>() {

    @Override
    public void handle(RingTimeout timeout) {
      LOG.trace("{}ring timeout", logPrefix);
      List<Container> timeouts = ringTimer.windowTick();
      long now = System.currentTimeMillis();
      for (Container c : timeouts) {
        RingContainer rc = (RingContainer) c;
        LOG.debug("{}msg:{} timed out", logPrefix, rc.msg);
        cwnd.loss(now, rttEstimator.rto(), ledbatConfig.MSS);
        pendingData.addFirst(rc.msg);
        trySend();
      }
    }
  };

  private void trySend() {
    while (!pendingData.isEmpty() && cwnd.canSend(ledbatConfig.MSS)) {
      BasicContentMsg<?, ?, Identifiable> msg = pendingData.removeFirst();
      LOG.trace("{}sending:{}", logPrefix, msg);
      LedbatMsg.Data wrappedData = new LedbatMsg.Data(dataId, msg.extractValue());

      long now = System.currentTimeMillis();  //added lines
      wrappedData.dataDelay.send(now);

      BasicContentMsg ledbatMsg = new BasicContentMsg(msg.getHeader(), wrappedData);
      trigger(ledbatMsg, outgoingNetworkPort);
      cwnd.send(ledbatConfig.MSS);
      ringTimer.setTimeout(rttEstimator.rto(), new RingContainer(msg));
    }
  }

  private void scheduleRingTimeout(long period) {
    SchedulePeriodicTimeout st = new SchedulePeriodicTimeout(period, period);
    RingTimeout rt = new RingTimeout(st, Ledbat.senderTransferId(dataId, senderId, receiverId));
    st.setTimeoutEvent(rt);
    LOG.debug("{}schedule periodic ring timer", logPrefix);
    trigger(st, timerPort);
    ringTimeout = rt.getTimeoutId();
  }

  private void cancelRingTimeout() {
    CancelPeriodicTimeout cpt = new CancelPeriodicTimeout(ringTimeout);
    LOG.debug("{}cancel periodic ring timer", logPrefix);
    trigger(cpt, timerPort);
  }

  public static class Init extends se.sics.kompics.Init<LedbatSenderComp> {

    public final Identifier dataId;
    public final Identifier senderId;
    public final Identifier receiverId;

    public Init(Identifier dataId, Identifier senderId, Identifier receiverId) {
      this.dataId = dataId;
      this.senderId = senderId;
      this.receiverId = receiverId;
    }
  }

  public static class RingContainer implements RingTimer.Container {

    public final BasicContentMsg<?, ?, Identifiable> msg;

    public RingContainer(BasicContentMsg msg) {
      this.msg = msg;
    }

    @Override
    public Identifier getId() {
      return msg.extractValue().getId();
    }
  }

  public static class RingTimeout extends Timeout {
    public final Identifier transferId;
    
    public RingTimeout(SchedulePeriodicTimeout spt, Identifier transferId) {
      super(spt);
       this.transferId = transferId;
    }
    
    public Identifier transferId() {
      return transferId;
    }
  }

  public static class HardCodedConfig {

    public static int statusPeriod = 100;
    public static int windowSize = 50;
    public static int maxTimeout = 25000;
  }

}
