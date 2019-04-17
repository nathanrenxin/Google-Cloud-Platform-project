package se.application;

import se.kth.ledbat.LedbatReceiverComp;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.ktoolbox.util.network.basic.BasicAddress;

public class ReceiverParent extends ComponentDefinition {

    public ReceiverParent(Init init) {
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component receiver = create(LedbatReceiverComp.class, init.init);

        connect(receiver.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<ReceiverParent> {

        public final BasicAddress self;
        public final LedbatReceiverComp.Init init;

        public Init(BasicAddress self, LedbatReceiverComp.Init init) {
            this.self = self;
            this.init = init;
        }
    }

}
