
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private ServerSocket serverSocket;
    private File rootDirectory;
    
    private Socket socket;

    private FileServer(File root, int port) throws IOException {
        rootDirectory = root;
        serverSocket = new ServerSocket(port);
    }

    private void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println("IOException occured while closing server socket.");
            }
        }
    }
    
    private void sendFileUsingBytes() throws IOException {
    	String line, name;
    	socket = serverSocket.accept();
    	System.out.println("Connection accepted, ready to send bytes.");
    	BufferedReader socketIn = new BufferedReader(new InputStreamReader(
    			socket.getInputStream()));
    	name = socketIn.readLine();
    	DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream());
    	File file = new File(rootDirectory, name);
    	DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
    	System.out.println("Sending " + name);
    	byte[] buffer = new byte[1024];
    	int read;
    	while((read = fileIn.read(buffer, 0, 1024)) > -1){
    		socketOut.write(buffer, 0, read);
    		socketOut.flush();
    	}
    	fileIn.close();
    }
    
	private void sendFile() throws IOException {
		socket = serverSocket.accept();
		System.out.println("Connection accepted!");
		BufferedReader socketIn = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		PrintWriter socketOut = new PrintWriter(socket.getOutputStream());
		String line, name;
		name = socketIn.readLine();
		File file = new File(rootDirectory, name);
		BufferedReader fileIn = new BufferedReader(new FileReader(file));
		System.out.println("Sending: " + name);
		while ((line = fileIn.readLine()) != null) {
			socketOut.println(line);
			socketOut.flush();
		}
		fileIn.close();
	}

    public void run() {
        while (true) {
            try {
//                sendFile();
            	sendFileUsingBytes();
            } catch (IOException ex) {
                System.err.println("IOException occured.  Closing connection.");
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                        System.out.println("Connection closed.");
                    } catch (IOException ex) {
                        System.err.println("IOException occured while closing connection.");
                    }
                }
            }
        }
    }    

    public static void main(String[] args) {
        System.setProperty("line.separator", "\r\n");
        if (args.length != 2) {
            System.err.println("Usage: java FileServer <root> <port>");
            System.exit(1);
        }
        FileServer server = null;
        try {
            File root = new File(args[0]);
            if (!root.exists()) {
                System.err.println("Root directory does not exist: " + args[0]);
                System.exit(1);
            }
            if (!root.isDirectory()) {
                System.err.println(args[0] + " is not a directory!");
                System.exit(1);
            }
            if (!root.canRead()) {
                System.err.println("Root directory is not readable: " + args[0]);
                System.exit(1);
            }
            int port = Integer.parseInt(args[1]);
            server = new FileServer(root, port);
            server.run();
        } catch (NumberFormatException ex) {
            System.err.println("Port agurment is not a number: " + args[1]);
        } catch (IOException ex) {
            System.err.println("IOException occured.");
        } finally {
            if (server != null) {
                server.close();
            }
        }
    }
}
