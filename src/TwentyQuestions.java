import java.util.Scanner;


public class TwentyQuestions {
	
	Question rootQuestion;
	Scanner scanner;
	
	public void startServer(){
		rootQuestion = new Question(null, "Barack Obama");
		scanner = new Scanner(System.in);
		
		while(true){
			new GameSession().start(rootQuestion);
		}
	}
	
	public static void main(String[] args){
		new TwentyQuestions().startServer();
	}
	
	public class GameSession {
		
		Question lastQuestion;
		Question currentQuestion;
		boolean isPlaying = true;
		boolean lastWasYes = true;
		
		public void start(Question rootQuestion){
			print("***************");
			this.currentQuestion = rootQuestion;
			
			while(currentQuestion.answer == null){
				print(currentQuestion.toString());
				String input = scanner.nextLine().toLowerCase();
				if(input.equals("yes") || input.equals("y")){
					lastQuestion = currentQuestion;
					currentQuestion = currentQuestion.yesChild;
					lastWasYes = true;
				} else if(input.equals("no") || input.equals("n")){
					lastQuestion = currentQuestion;
					currentQuestion = currentQuestion.noChild;
					lastWasYes = false;
				}
			}
			
			print(currentQuestion.toString());
			String input = scanner.nextLine().toLowerCase();
			if(input.equals("yes") || input.equals("y")){
				print("I am so smart!");
			} else if(input.equals("no") || input.equals("n")){
				askForNewQuestion();
			}
		}
		
		private void askForNewQuestion(){
			print("Who were you thinking of?");
			String name = scanner.nextLine();
			print("Please enter a new question!");
			String question = scanner.nextLine();
			Question newAnswer = new Question(null, name);
			
			print("What is the answer for " + name + "?");
			String answer = scanner.nextLine().toLowerCase();
			
			Question newQuestion;
			newQuestion = (answer.equals("yes") || answer.equals("y")) ? 
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
		
		public void print(String string){
			System.out.println(string);
		}
	}
	
}
