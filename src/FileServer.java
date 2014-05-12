
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

    private ServerSocket serverSocket;
    private File rootDirectory;
    
    private Socket socket;
    private BufferedReader socketIn;
    private DataOutputStream socketOut;

    private FileServer(File root, int port) throws IOException {
        rootDirectory = root;
        serverSocket = new ServerSocket(port);
    }
    
    private void sendFileUsingBytes() throws IOException {
    	socket = serverSocket.accept();
    	System.out.println("Connection accepted, ready to send bytes.");
    	socketIn = new BufferedReader(new InputStreamReader(
    			socket.getInputStream()));
    	socketOut = new DataOutputStream(socket.getOutputStream());
    	
//    	while(true){
    		readRequest();
//    	}
    }
    
    private void readRequest() throws IOException{
    	String name = socketIn.readLine();
    	
    	if(name.equals("*")){
    		printAllFiles();
    		socketIn.close();
    		return;
    	}
    	downloadFile(name);
    }
    
    private void downloadFile(String name) throws IOException{
    	System.out.println("Looking for file named " + name);
    	File file = new File(rootDirectory, name);
    	DataInputStream fileIn = new DataInputStream(new FileInputStream(file));
    	byte[] buffer = new byte[4090];
    	int read = 0, total = 0;
    	
    	System.out.println("Sending " + name);
    	while((read = fileIn.read(buffer)) > -1){
    		socketOut.write(buffer, 0, read);
    		socketOut.flush();
    		total += read;
    	}
    	fileIn.close();
    	System.out.println("Sent " + total + " bytes.");
    }

    
    private void printAllFiles() throws IOException{
    	for(File file : rootDirectory.listFiles()){
    		System.out.println(" - " + file.getName());
    	}
    	socketOut.close();
    }

    public void run() {
        while (true) {
            try {
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
    

    private void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.err.println("IOException occured while closing server socket.");
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
