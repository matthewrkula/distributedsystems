import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server20Questions {
	
	Question rootQuestion;

	ServerSocket serverSocket;
	Socket socket;
	
	BufferedReader socketIn;
	PrintWriter socketOut;
	
	public void run() throws Exception {
		serverSocket = new ServerSocket(6001);
		
		socket = serverSocket.accept();
		socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOut = new PrintWriter(socket.getOutputStream(), true);
		
		socketOut.println("Welcome to matt's server");
		
		String str;
		while(true){
			str = socketIn.readLine();
			socketOut.println(str.toUpperCase());
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Server20Questions().run();
	}
}
