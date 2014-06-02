import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server20Questions {
	
	Question rootQuestion;

	ServerSocket serverSocket;
	Socket acceptSocket;
	
	long idIncrementer = 0;
	
	ArrayList<GameSession> games;
	
	public void startServer() throws Exception {
		serverSocket = new ServerSocket(6001);
		rootQuestion = new Question(-1, null, "Barack Obama");
		games = new ArrayList<GameSession>();
		
		while(true){
			acceptSocket = serverSocket.accept();
			GameSession game = new GameSession(++idIncrementer, this, acceptSocket, rootQuestion);
			synchronized(games){
				games.add(game);
				System.out.println("New game #" + this.idIncrementer);
				System.out.println("Number of games going on: " + games.size());
			}
			new Thread(game).start();
		}
	}
	
	public void setRootQuestion(Question question){
		this.rootQuestion = question;
	}
	
	public void notifyPlayer(String name, Question question){
		System.out.println("Notifying about " + name + " to thread " + question.creatorId);
		if(question.creatorId <= 0){		// Original root question
			return;
		}
		
		synchronized(games){
			for(GameSession game : games){
				if(game.id == question.creatorId){
					game.print(name + " just thought of " + question.answer + "!");
					System.out.println("Found him!");
				}
			}
			System.out.println("Done notifying about " + name);
		}
	}
	
	public void quitGame(GameSession game){
		System.out.println(game.id + " quit!");
		synchronized(games){
			games.remove(game);
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Server20Questions().startServer();
	}
}
