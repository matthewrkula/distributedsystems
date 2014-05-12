
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class FileClient {

    private Socket socket;
    private DataInputStream dataSocketIn;
    private PrintWriter socketOut;
    
    InetAddress server;
    int port;

    private FileClient(InetAddress server, int port) throws IOException {
    	this.server = server;
    	this.port = port;
        reloadSocket();
    }
    
    private void reloadSocket() throws IOException{
    	socket = new Socket(server, port);
        dataSocketIn = new DataInputStream(socket.getInputStream());
        socketOut = new PrintWriter(socket.getOutputStream());
    }

    private void close() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                System.err.println("IOException while closing socket.");
            }
        }
    }
    
    private void downloadFileUsingBytes(String name) throws IOException{
        BufferedOutputStream outputStream = new BufferedOutputStream(
        		new FileOutputStream(new File(name)));
        
        socketOut.println(name);
        socketOut.flush();
        byte[] buffer = new byte[4090];
        int read = 0;
        int total = 0;
        while ((read = dataSocketIn.read(buffer)) > -1) {
            outputStream.write(buffer, 0, read);
            total += read;
        }
        outputStream.close();
        System.out.println("Downloaded " + total + " bytes.");
    }
    
    public void run() throws IOException {
    	String name;
    	BufferedReader consoleIn =
				new BufferedReader(new InputStreamReader(System.in));
    	
    	System.out.print("What file do you want? ");
    	name = consoleIn.readLine();
    	
    	while(!name.equals("!")){
    		downloadFileUsingBytes(name);
    		reloadSocket();
    		System.out.print("What file do you want? ");
        	name = consoleIn.readLine();
    	}
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java FileClient <hostname> <port>");
            System.exit(1);
        }
        FileClient client = null;
        try {
            InetAddress s = InetAddress.getByName(args[0]);
            int p = Integer.parseInt(args[1]);
            client = null;
            client = new FileClient(s, p);
            client.run();
        } catch (UnknownHostException ex) {
            System.err.println("Could not find host: " + args[0]);
        } catch (NumberFormatException ex) {
            System.err.println("Port agurment is not a number: " + args[1]);
        } catch (IOException ex) {
            System.err.println("IOException occured.");
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
