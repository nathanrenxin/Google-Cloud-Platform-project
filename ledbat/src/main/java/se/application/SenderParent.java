package se.application;

import se.kth.ledbat.LedbatSenderComp;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.netty.NettyInit;
import se.sics.kompics.network.netty.NettyNetwork;
import se.sics.ktoolbox.util.network.basic.BasicAddress;
import se.sics.kompics.timer.java.JavaTimer;
import se.sics.kompics.timer.Timer;


public class SenderParent extends ComponentDefinition {

    public SenderParent(SenderParent.Init init) {
        Component network = create(NettyNetwork.class, new NettyInit(init.self));
        Component sender = create(LedbatSenderComp.class, init.init);
        Component app = create(SendingApplication.class, new SendingApplication.Init(init.self, init.server, init.seconds, 10000));
        Component timer = create(JavaTimer.class, Init.NONE);

        connect(sender.getNegative(Network.class), network.getPositive(Network.class), Channel.TWO_WAY);
        connect(app.getNegative(Network.class), sender.getPositive(Network.class), Channel.TWO_WAY);
        connect(sender.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);
        connect(app.getNegative(Timer.class), timer.getPositive(Timer.class), Channel.TWO_WAY);
    }

    public static class Init extends se.sics.kompics.Init<SenderParent> {

        public final BasicAddress self;
        public final BasicAddress server;
        public final LedbatSenderComp.Init init;
        public final int seconds;

        public Init(BasicAddress self, BasicAddress server, int seconds, LedbatSenderComp.Init init) {
            this.self = self;
            this.server = server;
            this.seconds = seconds;
            this.init = init;
        }
    }
}
