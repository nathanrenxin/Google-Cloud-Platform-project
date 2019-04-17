package project.id2210.project_id2210;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author ema
 */
public class UDPClient {

    private DatagramSocket socket;
    private final int PORT = 5001;
    private static Scanner scan;
    private final String HOST = "35.204.134.136";
    public UDPClient() {
        try {
            socket = new DatagramSocket();

        } catch (SocketException ex) {
            System.err.print(ex);
            System.exit(1);
        }
    }

    public void sendData(String message) {
        try {
            System.out.println("\nSending packet containing: " + message);
            byte[] data = message.getBytes();
            InetSocketAddress address = new InetSocketAddress(HOST, PORT);
            DatagramPacket sendPacket = new DatagramPacket(data,
                    data.length, address);
            socket.send(sendPacket);
            System.out.println("Packet sent");
        } catch (UnknownHostException ex) {
            System.err.print(ex);
            System.exit(1);
        } catch (IOException ex) {
            System.err.print(ex);
            System.exit(1);
        }
    }

    public void waitForPackets() {
        while (true) {
            try {
                byte[] data = new byte[100];
                DatagramPacket receive = new DatagramPacket(data, data.length);
                socket.receive(receive);
                displayMessage("\nPacket received:"
                        + "\nFrom host: " + receive.getAddress().getHostName()
                        + "\nHost port: " + receive.getPort()
                        + "\nLength: " + receive.getLength()
                        + "\nContaining: " + new String(receive.getData(), 0, receive.getLength()));
            } catch (IOException ex) {
                System.err.print(ex);
                System.exit(1);
            }
        }
    }

    private void displayMessage(final String messageToDisplay) {
        System.out.println( messageToDisplay);
    }

    public static void main(String... args) {
        UDPClient client = new UDPClient();
        scan = new Scanner(System.in);
        String message;
        System.out.println("Enter your message: ");
        do {
            message = scan.nextLine();
            client.sendData(message);
        } while (message.equals("stop"));

        client.waitForPackets();
    }
}

