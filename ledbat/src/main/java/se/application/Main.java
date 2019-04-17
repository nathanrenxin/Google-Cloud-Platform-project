package se.application;

import java.net.InetAddress;
import java.net.UnknownHostException;

import se.application.ReceiverParent;
import se.kth.ledbat.LedbatReceiverComp;
import se.kth.ledbat.LedbatSenderComp;
import se.kth.ledbat.msgs.LedbatMsg;
import se.kth.ledbat.msgs.MsgSerializer;
import se.kth.ledbat.msgs.NetSerializer;
import se.kth.ledbat.msgs.Payload;
import se.sics.kompics.Kompics;
import se.sics.kompics.network.netty.NettySerializer;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.kompics.util.ByteIdentifier;
import se.sics.ktoolbox.util.network.basic.BasicAddress;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;

public class Main {

    static {
        Serializers.register(new NetSerializer(), "netS");
        Serializers.register(new MsgSerializer(), "msgS");

        Serializers.register(BasicAddress.class, "netS");
        Serializers.register(BasicHeader.class, "netS");
        Serializers.register(BasicContentMsg.class, "msgS");
        Serializers.register(Payload.class, "msgS");
        Serializers.register(LedbatMsg.Data.class, "msgS");
        Serializers.register(LedbatMsg.Ack.class, "msgS");
    }

    public static void main(String[] args) {
        try {
            if (args.length == 2) { // start server
                InetAddress ip = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                byte b = 1;
                ByteIdentifier identifier = new ByteIdentifier(b);

                BasicAddress self = new BasicAddress(ip, port, identifier);
                LedbatReceiverComp.Init preInit = new LedbatReceiverComp.Init(identifier,identifier,identifier);
                ReceiverParent.Init init = new ReceiverParent.Init(self, preInit);
                Kompics.createAndStart(ReceiverParent.class, init, 2);
                System.out.println("Starting Server at " + self);
                Kompics.waitForTermination();
            } else if (args.length == 5) { // start client
                InetAddress myIp = InetAddress.getByName(args[0]);
                int myPort = Integer.parseInt(args[1]);
                byte b = 2;
                ByteIdentifier identifier = new ByteIdentifier(b);

                BasicAddress self = new BasicAddress(myIp, myPort, identifier);
                InetAddress serverIp = InetAddress.getByName(args[2]);
                int serverPort = Integer.parseInt(args[3]);
                int time = Integer.parseInt(args[4]);
                b = 1;
                BasicAddress server = new BasicAddress(serverIp, serverPort, new ByteIdentifier(b));

                LedbatSenderComp.Init preInit = new LedbatSenderComp.Init(identifier, identifier, new ByteIdentifier(b));
                SenderParent.Init init = new SenderParent.Init(self, server, time, preInit);
                Kompics.createAndStart(SenderParent.class, init);
                System.out.println("Starting client at" + self + " to " + server);
                Kompics.waitForTermination();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    System.exit(1);
                }
                Kompics.shutdown();
                System.exit(0);
            } else {
                System.err.println("Invalid number of parameters (2 for server, 4 for client)");
                System.exit(1);
            }

        } catch (UnknownHostException ex) {
            System.err.println(ex);
            System.exit(1);
        } catch (InterruptedException e) {
            System.exit(1);
        }
    }
}
