package project.id2210.project_id2210;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPServer {

    private static ServerSocket server;
    private static final int PORT = 8080;
    private static Socket socket;
    private static DataOutputStream output;
    private static DataInputStream input;
    private static BufferedReader br;

    public static void main(String... args) {
        try {
            acceptConnection();
            getIOStreams();
            br = new BufferedReader(new InputStreamReader(System.in));
            String icmgMsg = "";
            String otgngMsg;
            while (!icmgMsg.equals("quite")) {
                icmgMsg = input.readUTF();
                System.out.println("Client>>" + icmgMsg);
                otgngMsg = br.readLine();
                output.writeUTF(otgngMsg);
                output.flush();
                System.out.println("VM>>: " + otgngMsg);
            }
            closeConnecion();
        } catch (IOException ex) {
            Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void acceptConnection() throws IOException {
        server = new ServerSocket(PORT);
        System.out.println("Waiting for connection....");
        socket = server.accept();
        System.out.println("Client connected ... ");
    }

    public static void getIOStreams() throws IOException {
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());
        System.out.println("Got I/O streams...");
    }

    public static void closeConnecion() throws IOException {
        System.out.println("Terminating connection...");
        server.close();
        socket.close();
        output.close();
        input.close();
        br.close();
    }
}

