
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
    
    private void downloadFile(String name) throws IOException{
        byte[] buffer = new byte[2048];
        int read = 0, total = 0;
        
        // Send name of file
        socketOut.println(name);
        socketOut.flush();
        
        // Get the length of the file
        long length = dataSocketIn.readLong();
        
        if(length >= 0){
            BufferedOutputStream outputStream = new BufferedOutputStream(
            		new FileOutputStream(new File(name)));
	        while (total < length) {
	        	read = dataSocketIn.read(buffer);
	            outputStream.write(buffer, 0, read);
	            outputStream.flush();
	            total += read;
	            printProgress(total, length);
	        }
	        outputStream.close();
	        System.out.println("\nDownloaded " + total + " bytes.");
        } else {
        	System.out.println("File does not exist.");
        }
    }
    
    private void readAllFiles() throws IOException {
    	socketOut.println("*");
    	socketOut.flush();
    	
    	int length = dataSocketIn.readInt();
    	byte[] nameBuffer = new byte[1024];
    	
    	while(length > 0){
    		dataSocketIn.read(nameBuffer, 0, length);
    		StringBuilder builder = new StringBuilder();
    		for(int i=0; i < length; i++){
    			builder.append((char)nameBuffer[i]);
    		}
    		System.out.println(builder.toString());
    		length = dataSocketIn.readInt();
    	}
    }
    
    public void printProgress(int current, long total){
    	float frac = total / 20.0f;
    	
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("[");
    	for(int i=0; i < 20; i++){
    		buffer.append(frac * i < current ? "=" : " ");
    	}
    	buffer.append("]\r");

    	System.out.print(buffer.toString());
    }
    
    public void run() throws IOException {
    	String name;
    	BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
    	
    	System.out.print("What file do you want? ");
    	name = consoleIn.readLine();
    	
    	while(!name.equals("!")){
    		
    		if(name.equals("*")){
    			readAllFiles();
    		} else {
        		downloadFile(name);
    		}
    		System.out.print("What file do you want? ");
        	name = consoleIn.readLine();
    	}
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
