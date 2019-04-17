package project.id2210.project_id2210;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private static final int PORT = 8080;
    private static final String HOST = "35.204.134.136";
    private static Socket socket;
    private static DataOutputStream output;
    private static DataInputStream input;
    private static BufferedReader br;

    public static void main(String... args) {
        try {
            connectToServer();
            getIOStreams();
            br = new BufferedReader(new InputStreamReader(System.in));
            String icmgMsg = "";
            String otgngMsg;

            while (!icmgMsg.equals("quite")) {
                otgngMsg = br.readLine();
                output.writeUTF(otgngMsg);
                output.flush();
                System.out.println("Client>>" + otgngMsg);
                icmgMsg = input.readUTF();
                System.out.println("VM>" + icmgMsg);

            }
            closeConnecion();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void getIOStreams() throws IOException {
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
        System.out.println("Got I/O streams...");
    }

    public static void connectToServer() throws IOException {
        System.out.println("Atempting to connect");
        socket = new Socket(InetAddress.getByName(HOST), PORT);
        System.out.println("Connected to: " + socket.getInetAddress().getHostName());

    }

    public static void closeConnecion() throws IOException {
        System.out.println("Terminating connection...");
        socket.close();
        output.close();
        input.close();
        br.close();
    }
}
