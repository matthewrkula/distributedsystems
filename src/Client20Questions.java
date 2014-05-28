import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Client20Questions {
	
	Socket socket;
	BufferedReader socketIn;
	PrintWriter socketOut;
	
	public void run() throws UnknownHostException, IOException{
		socket = new Socket("localhost", 6001);
		socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOut = new PrintWriter(socket.getOutputStream(), true);
		
		Scanner s = new Scanner(System.in);
		String input;
		System.out.println("*********");
		System.out.println(socketIn.readLine());
		System.out.println("*********");
		
		while(true){
			input = s.nextLine();
			socketOut.println(input);
			System.out.println(socketIn.readLine());
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException{
		new Client20Questions().run();
	}
}
