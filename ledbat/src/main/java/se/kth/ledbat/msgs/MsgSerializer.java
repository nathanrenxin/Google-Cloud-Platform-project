package se.kth.ledbat.msgs;

import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import se.kth.ledbat.Ledbat;
import se.kth.ledbat.util.OneWayDelay;
import se.sics.kompics.network.netty.serialization.Serializer;
import se.sics.kompics.network.netty.serialization.Serializers;
import se.sics.kompics.util.ByteIdentifier;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;

import javax.jws.Oneway;

public class MsgSerializer implements Serializer {

    private static final byte CONTENT = 0;
    private static final byte PAYLOAD = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;


    @Override
    public int identifier() {
        return 200;
    }

    @Override
    public void toBinary(Object o, ByteBuf buf) {
        if (o instanceof BasicContentMsg) {
            BasicContentMsg msg = (BasicContentMsg) o;
            buf.writeByte(CONTENT);
            Serializers.toBinary(msg.getHeader(), buf);
            Serializers.toBinary(msg.getContent(),buf);
        } else if(o instanceof Payload) {
            Payload pay = (Payload) o;
            buf.writeByte(PAYLOAD);
            buf.writeByte(pay.getId().id);
            buf.writeBytes(pay.getContent());
        } else if(o instanceof LedbatMsg.Data) {
            LedbatMsg.Data data = (LedbatMsg.Data) o;
            buf.writeByte(DATA);
            byte id = ((ByteIdentifier) data.getDataId()).id;
            buf.writeByte(id);
            buf.writeLong(data.dataDelay.send);
            buf.writeLong(data.dataDelay.receive);
            Serializers.toBinary(data.extractValue(),buf);
        } else if(o instanceof LedbatMsg.Ack) {
            LedbatMsg.Ack ack = (LedbatMsg.Ack) o;
            buf.writeByte(ACK);
            ByteIdentifier eventId = (ByteIdentifier) ack.eventId;
            ByteIdentifier dataId = (ByteIdentifier) ack.dataId;
            buf.writeByte(eventId.id);
            buf.writeByte(dataId.id);
            buf.writeLong(ack.dataDelay.send);
            buf.writeLong(ack.dataDelay.receive);
            buf.writeLong(ack.ackDelay.send);
            buf.writeLong(ack.ackDelay.receive);
        }

    }

    @Override
    public Object fromBinary(ByteBuf buf, Optional<Object> hint) {
        byte type = buf.readByte();
        switch(type) {
            case CONTENT:
                BasicHeader header = (BasicHeader) Serializers.fromBinary(buf, Optional.absent());
                BasicContentMsg msg = new BasicContentMsg(header,  Serializers.fromBinary(buf, Optional.absent()));
                return msg;
            case PAYLOAD:
                byte id = buf.readByte();
                int bytes = buf.readableBytes();
                byte[] content = new byte[bytes];
                for(int i = 0; i < bytes; i++) {
                    content[i] = buf.readByte();
                }
                return new Payload(new ByteIdentifier(id), content);
            case DATA:
                byte idD = buf.readByte();
                long send = buf.readLong();
                long receive = buf.readLong();
                Payload payload = (Payload) Serializers.fromBinary(buf, Optional.absent());
                LedbatMsg.Data data = new LedbatMsg.Data(new ByteIdentifier(idD), payload);
                data.dataDelay.send(send);
                data.dataDelay.receive(receive);
                return data;
            case ACK:
                OneWayDelay dataDelay = new OneWayDelay();
                byte eventId = buf.readByte();
                byte dataId = buf.readByte();
                dataDelay.send(buf.readLong());
                dataDelay.receive(buf.readLong());
                LedbatMsg.Ack ack = new LedbatMsg.Ack(new ByteIdentifier(eventId), new ByteIdentifier(dataId), dataDelay);
                ack.ackDelay.send(buf.readLong());
                ack.ackDelay.receive(buf.readLong());
                return ack;
        }
        return null;
    }
}
