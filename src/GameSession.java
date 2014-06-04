import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession implements Runnable {

	Server20Questions server;
	Question lastQuestion, currentQuestion;
	String name;

	boolean isPlaying = true, 
			lastWasYes = true,
			isBlocking = false;

	Socket socket;
	BufferedReader socketIn;
	PrintWriter socketOut;
	
	public long id;

	public GameSession(long id, Server20Questions server, Socket sock,
			Question rootQuestion) {
		this.id = id;
		this.server = server;
		this.currentQuestion = rootQuestion;
		this.socket = sock;
		try {
			socket.setSoTimeout(20*1000);
			socketIn = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			socketOut = new PrintWriter(socket.getOutputStream(), true);
		} catch (Exception e) {
			this.server.quitGame(this);
			System.out.println(e.toString());
		}
	}

	@Override
	public void run() {
		print("What is your name?");
		this.name = getInput();

		String input = "yes";
		while (answerIsYes(input)) {
			startGame();
			print("Would you like to play again, " + this.name + "?");
			input = getInput();
		}

		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			this.server.quitGame(this);
		}
	}

	public void startGame() {
		print("***************");
		this.currentQuestion = server.getRootQuestion();
		if(currentQuestion.isLeafNode()){
			synchronized(currentQuestion){
				this.currentQuestion = server.getRootQuestion();
			}
		}

		while (!currentQuestion.isLeafNode()) {
			print(currentQuestion.toString());
			String input = getInput().toLowerCase();

			lastWasYes = answerIsYes(input);
			lastQuestion = currentQuestion;
			Question tempQuestion = lastWasYes ? currentQuestion.yesChild : currentQuestion.noChild;
			if(tempQuestion.isLeafNode()){
				synchronized(tempQuestion){
					currentQuestion = lastWasYes ? currentQuestion.yesChild : currentQuestion.noChild;
				}
			} else {
				currentQuestion = tempQuestion;
			}
		}
		
		try {
			synchronized(currentQuestion){
				print(currentQuestion.toString());
				String input = getInputWhileBlocking();
				if (answerIsYes(input)) {
					print("I am so smart!");
					this.server.notifyPlayer(this.name, currentQuestion);
				} else {
					askForNewQuestion();
				}
			}
			this.server.saveTreeToDisk();
			
		} catch (IOException e){	// Catches the timeout
			System.out.println("Restarting game for thread #" + this.id);
			startGame();
		}
	}

	private void askForNewQuestion() throws IOException {
		print("Who are you thinking of?");
		String name = getInputWhileBlocking();
		print("Please enter a yes/no question to distinguish "
				+ currentQuestion.answer + " from " + name + "!");
		String question = getInputWhileBlocking();
		Question newAnswer = new Question(this, null, name);

		print("What is the answer for " + name + "?");
		String answer = getInputWhileBlocking();

		Question newQuestion;
		newQuestion = answerIsYes(answer) ? new Question(question,
				currentQuestion, newAnswer) : new Question(question, newAnswer,
				currentQuestion);

		if (lastQuestion != null) {
			if (lastWasYes) {
				lastQuestion.yesChild = newQuestion;
			} else {
				lastQuestion.noChild = newQuestion;
			}
		} else {
			this.server.setRootQuestion(newQuestion);
		}
	}

	/**
	 * Waits on input from the client.
	 * @return The line that was send from the client
	 */
	public String getInput() {
		String s = null;
		
		while(s == null){
			try {
				s = cleanString(socketIn.readLine());
			} catch (IOException e) {
				s = null;
			}
		}
		return s;
	}
	
	public String getInputWhileBlocking() throws IOException {
		return cleanString(socketIn.readLine());
	}
	
	/**
	 * Returns a string that accounts for all backspaces and the characters that should be removed.
	 */
	private String cleanString(String string){
		int index;
		while((index = string.indexOf("\b")) >= 0){
			string = string.substring(0, Math.max(0, index-1)) 
				   + string.substring(index+1);
		}
		return string;
	}
	
	/**
	 * Send message to the client.
	 * @param string - The line to send to the client.
	 */
	public void print(String string) {
		socketOut.println(string);
	}

	/**
	 * Check if a string input should be considered a 'yes'
	 * @param input - The string to check
	 * @return true if the answer is a 'yes'
	 */
	public boolean answerIsYes(String input) {
		return (input.toLowerCase().equals("yes") || input.toLowerCase()
				.equals("y"));
	}
	
	@Override
	public boolean equals(Object obj) {
		GameSession g = (GameSession)obj;
		return this.id == g.id;
	}
}