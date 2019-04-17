/*
 * Copyright (C) 2009 Swedish Institute of Computer Science (SICS) Copyright (C)
 * 2009 Royal Institute of Technology (KTH)
 *
 * KompicsToolbox is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ledbat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ClassMatchedHandler;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.util.Identifier;
import se.kth.ledbat.msgs.LedbatMsg;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class LedbatReceiverComp extends ComponentDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(LedbatReceiverComp.class);
  private String logPrefix = "";

  Negative<Network> incomingNetworkPort = provides(Network.class);
  Positive<Network> outgoingNetworkPort = requires(Network.class);

  private final Identifier dataId;
  private final Identifier senderId;
  private final Identifier receiverId;

  public LedbatReceiverComp(Init init) {
    dataId = init.dataId;
    senderId = init.senderId;
    receiverId = init.receiverId;
    logPrefix = "<" + dataId + "," + senderId + "," + receiverId + ">";

    subscribe(handleStart, control);
    subscribe(handleIncomingMsg, outgoingNetworkPort);
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting...", logPrefix);
    }
  };

  @Override
  public void tearDown() {
  }

  ClassMatchedHandler handleIncomingMsg
    = new ClassMatchedHandler<LedbatMsg.Data, BasicContentMsg<?, ?, LedbatMsg.Data>>() {

      @Override
      public void handle(LedbatMsg.Data payload, BasicContentMsg<?, ?, LedbatMsg.Data> msg) {

        long now = System.currentTimeMillis();  //added lines
        payload.dataDelay.receive(now);

        BasicContentMsg baseMsg = new BasicContentMsg(msg.getHeader(), payload.data);
        trigger(baseMsg, incomingNetworkPort);
        LedbatMsg.Ack ack = payload.answer();
        ack.ackDelay.send(now);
        trigger(msg.answer(ack), outgoingNetworkPort);
      }
    };

  public static class Init extends se.sics.kompics.Init<LedbatReceiverComp> {

    public final Identifier dataId;
    public final Identifier senderId;
    public final Identifier receiverId;

    public Init(Identifier dataId, Identifier senderId, Identifier receiverId) {
      this.dataId = dataId;
      this.senderId = senderId;
      this.receiverId = receiverId;
    }
  }
}
