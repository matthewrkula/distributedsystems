import java.net.ServerSocket;
import java.net.Socket;


public class Server20Questions {
	
	Question rootQuestion;

	ServerSocket serverSocket;
	Socket acceptSocket;
	
	public void startServer() throws Exception {
		serverSocket = new ServerSocket(6001);
		rootQuestion = new Question(null, "Barack Obama");
		
		while(true){
			acceptSocket = serverSocket.accept();
			Runnable game = new GameSession(acceptSocket, rootQuestion);
			new Thread(game).start();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Server20Questions().startServer();
	}
}
