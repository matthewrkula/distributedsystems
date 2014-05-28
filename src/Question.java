
public class Question {
	String question;
	String answer;
	Question noChild;
	Question yesChild;
	
	
	public Question(String question){
		this.question = question;
	}
	
	public Question(String question, String answer){
		this.question = question;
		this.answer = answer;
	}
	
	public Question(String question, Question no, Question yes){
		this.question = question;
		this.noChild = no;
		this.yesChild = yes;
	}
	
	@Override
	public String toString() {
		if(this.answer == null){
			return this.question;
		}
		return "Is it " + answer + "?";
	}
}
