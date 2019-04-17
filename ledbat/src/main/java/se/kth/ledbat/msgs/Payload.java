package se.kth.ledbat.msgs;

import se.sics.kompics.util.ByteIdentifier;
import se.sics.kompics.util.Identifiable;
import se.sics.kompics.util.Identifier;

public class Payload implements Identifiable<ByteIdentifier> {
    private byte[] content;
    private ByteIdentifier byteIdentifier;

    public Payload (ByteIdentifier byteIdentifier, byte[] content) {
        this.byteIdentifier = byteIdentifier;
        this.content = content;
    }

    @Override
    public ByteIdentifier getId() {
        return byteIdentifier;
    }

    public void incrementID() {
        byte b = byteIdentifier.id;
        b++;
        byteIdentifier = new ByteIdentifier(b);
    }

    public byte[] getContent() {
        return content;
    }
}
