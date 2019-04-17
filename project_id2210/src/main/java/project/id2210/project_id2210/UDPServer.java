package project.id2210.project_id2210;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author ema
 */
public class UDPServer {

    private DatagramSocket socket;
    private final int PORT = 5001;

    public UDPServer() {
        try {
            socket = new DatagramSocket(PORT);
        } catch (SocketException ex) {
            System.err.print(ex);
            System.exit(1);
        }

    }

    private void waitForPackets() {
        while (true) {
            try {
                byte[] data = new byte[100];
                DatagramPacket receive = new DatagramPacket(data, data.length);
                socket.receive(receive);
                displayMessage("\nPacket received:"
                        + "\nFrom host: " + receive.getAddress()
                        + "\nHost port: " + receive.getPort()
                        + "\nLength: " + receive.getLength()
                        + "\nContaining:\n\t" + new String(receive.getData(), 0, receive.getLength()));
                sendPacketToClient(receive);
            } catch (IOException ex) {
                System.err.print(ex);
                System.exit(1);
            }
        }
    }

    private void sendPacketToClient(DatagramPacket receivePacket) throws IOException {
        displayMessage("Echo data to client...");
        DatagramPacket sendPacket = new DatagramPacket(
                receivePacket.getData(), receivePacket.getLength(),
                receivePacket.getAddress(), receivePacket.getPort());
        socket.send(sendPacket); // echo to client
        displayMessage("Packet sent\n");
    }

    private void displayMessage(final String messageToDisplay) {
        System.out.println("> " + messageToDisplay);
    }

    public static void main(String... args) {
        UDPServer server = new UDPServer();
        server.waitForPackets();
    }
}

