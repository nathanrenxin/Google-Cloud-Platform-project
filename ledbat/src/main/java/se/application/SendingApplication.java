package se.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.ledbat.LedbatSenderComp;
import se.kth.ledbat.msgs.Payload;
import se.kth.ledbat.util.LedbatConfig;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.util.ByteIdentifier;
import se.sics.ktoolbox.util.network.basic.BasicAddress;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;
import se.sics.util.RingTimer;

import java.util.List;

public class SendingApplication extends ComponentDefinition {
    Positive<Network> net = requires(Network.class);
    Positive<Timer> timerPort = requires(Timer.class);

    private int seconds;
    private int messageSize;
    private BasicAddress self;
    private BasicAddress server;
    private BasicHeader header;
    private  BasicContentMsg msg;
    private Payload pay;
    private int acks;
    private int secondsPassed;
    private int messageBuffer = 10;

    private static final Logger LOG = LoggerFactory.getLogger(SendingApplication.class);
    private String logPrefix = "";

    public SendingApplication(Init init) {

        seconds = init.seconds;
        messageSize = init.messageSize;
        self = init.self;
        server = init.server;
        acks = 0;
        subscribe(startHandler, control);
        subscribe(handleRingTimeout, timerPort);
        subscribe(msgHandler, net);
    }

    Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            header = new BasicHeader(self, server, Transport.UDP);
            byte id = 1;
            pay = new Payload(new ByteIdentifier(id), new byte[messageSize]);
            msg = new BasicContentMsg(header, pay);
            SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(0, 1000);
            SecondTimeout timeout = new SecondTimeout(spt);
            spt.setTimeoutEvent(timeout);
            trigger(spt, timerPort);
            for (int i = 0; i < messageBuffer; i++) {
                trigger(msg,net);
                pay.incrementID();
                msg = new BasicContentMsg(header, pay);
            }
        }
    };

    Handler<BasicContentMsg> msgHandler = new Handler<BasicContentMsg>() {
        @Override
        public void handle(BasicContentMsg event) {
            acks++;
        }
    };

    Handler<SecondTimeout> handleRingTimeout = new Handler<SecondTimeout>() {

        @Override
        public void handle(SecondTimeout timeout) {
            secondsPassed++;
            LOG.info("Received " + acks + " packages for effective bandwidth of " + acks * messageSize / 1024 + " KiB/s", logPrefix );
            for (int i = 0; i < acks; i++) {
                pay.incrementID();
                msg = new BasicContentMsg(header, pay);
                trigger(msg,net);
            }
            acks = 0;
            if(secondsPassed >= seconds) {
                Kompics.asyncShutdown();
            }
        }
    };

    public static class SecondTimeout extends Timeout {
        public SecondTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }

    public static class Init extends se.sics.kompics.Init<SendingApplication> {

        public final BasicAddress self;
        public final int seconds;
        public final int messageSize;
        public final BasicAddress server;

        public Init(BasicAddress self, BasicAddress server, int seconds, int messageSize) {
            this.self = self;
            this.server = server;
            this.seconds = seconds;
            this.messageSize = messageSize;
        }
    }

}
