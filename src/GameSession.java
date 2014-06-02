import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession implements Runnable {

	Server20Questions server;
	Question lastQuestion, currentQuestion, rootQuestion;
	String name;

	boolean isPlaying = true, lastWasYes = true;

	Socket socket;
	BufferedReader socketIn;
	PrintWriter socketOut;
	
	public long id;

	public GameSession(long id, Server20Questions server, Socket socket,
			Question rootQuestion) {
		this.id = id;
		this.server = server;
		this.socket = socket;
		this.currentQuestion = rootQuestion;
		this.rootQuestion = rootQuestion;
		try {
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
		this.currentQuestion = rootQuestion;

		while (!currentQuestion.isLeafNode()) {
			print(currentQuestion.toString());
			String input = getInput().toLowerCase();

			if (answerIsYes(input)) {
				lastQuestion = currentQuestion;
				currentQuestion = currentQuestion.yesChild;
				lastWasYes = true;
			} else {
				lastQuestion = currentQuestion;
				currentQuestion = currentQuestion.noChild;
				lastWasYes = false;
			}
		}

		print(currentQuestion.toString());
		String input = getInput();
		if (answerIsYes(input)) {
			print("I am so smart!");
			this.server.notifyPlayer(this.name, currentQuestion);
		} else {
			askForNewQuestion();
		}
	}

	private void askForNewQuestion() {
		print("Who were you thinking of?");
		String name = getInput();
		print("Please enter a new question!");
		String question = getInput();
		Question newAnswer = new Question(id, null, name);

		print("What is the answer for " + name + "?");
		String answer = getInput();

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
			rootQuestion = newQuestion;
			this.server.setRootQuestion(newQuestion);
		}
	}

	public String getInput() {
		try {
			return socketIn.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void print(String string) {
		socketOut.println(string);
	}

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