import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameSession implements Runnable {
		
		Question lastQuestion, currentQuestion, rootQuestion;
		
		boolean isPlaying = true, lastWasYes = true;
		
		Socket socket;
		BufferedReader socketIn;
		PrintWriter socketOut;
		
		public GameSession(Socket socket, Question rootQuestion){
			this.socket = socket;
			this.currentQuestion = rootQuestion;
			this.rootQuestion = rootQuestion;
			try {
				socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				socketOut = new PrintWriter(socket.getOutputStream(), true);
			} catch (Exception e){
				System.out.println(e.toString());
			}
		}

		@Override
		public void run() {
			String input = "yes";
			while(answerIsYes(input)){
				startGame();
				input = getInput();
			}
		}
		
		public void startGame(){	
			print("***************");
			this.currentQuestion = rootQuestion;
			
			while(currentQuestion.answer == null){
				print(currentQuestion.toString());
				String input = getInput().toLowerCase();
				
				if(answerIsYes(input)){
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
			if(answerIsYes(input)){
				print("I am so smart!");
			} else {
				askForNewQuestion();
			}
		}
		
		private void askForNewQuestion(){
			print("Who were you thinking of?");
			String name = getInput();
			print("Please enter a new question!");
			String question = getInput();
			Question newAnswer = new Question(null, name);
			
			print("What is the answer for " + name + "?");
			String answer = getInput();
			
			Question newQuestion;
			newQuestion = answerIsYes(answer) ? 
					new Question(question, currentQuestion, newAnswer) :
					new Question(question, newAnswer, currentQuestion);
			
			if(lastQuestion != null){
				if(lastWasYes){
					lastQuestion.yesChild = newQuestion;
				} else {
					lastQuestion.noChild = newQuestion;
				}
			} else {
				rootQuestion = newQuestion;
			}
		}
		
		public String getInput(){
			try {
				return socketIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}
		
		public void print(String string){
			socketOut.println(string);
		}
		
		public boolean answerIsYes(String input){
			return (input.toLowerCase().equals("yes") || input.toLowerCase().equals("y"));
		}
	}