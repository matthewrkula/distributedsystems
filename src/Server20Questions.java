import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server20Questions {
	
	Question rootQuestion;

	ServerSocket serverSocket;
	Socket acceptSocket;
	
	String dataFileName = "database.dat";
	File file = new File(dataFileName);
	ObjectOutputStream objectOutputStream;
	ObjectInputStream objectInputStream;
	
	long idIncrementer = 0;
	
	ArrayList<GameSession> games;
	
	public void startServer() throws Exception {
		serverSocket = new ServerSocket(6001);
		games = new ArrayList<GameSession>();
		
		if(file.exists() && file.length() > 0) {
			System.out.println("data found!");
			objectInputStream = new ObjectInputStream(new FileInputStream(file));
			rootQuestion = (Question)objectInputStream.readObject();
			objectInputStream.close();
		} else {
			System.out.println("data not found!");
			rootQuestion = new Question(null, null, "Barack Obama");
		}
			
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
	
	public Question getRootQuestion(){
		return this.rootQuestion;
	}
	
	public synchronized void saveTreeToDisk(){
		try {
			objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
			objectOutputStream.writeObject(rootQuestion);
			objectOutputStream.flush();
			objectOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void notifyPlayer(String name, Question question){
		if(question.createdBy == null){		// Original root question
			return;
		}
		
		System.out.println("Notifying about " + name + " to " + question.createdBy.name);

		synchronized(games){
			for(GameSession game : games){
				if(game.equals(question.createdBy)){
					game.print(name + " just thought of " + question.answer + "!");
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
