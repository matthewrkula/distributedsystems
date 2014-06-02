import java.math.BigInteger;


public class Question {
	String question;
	String answer;
	Question noChild;
	Question yesChild;
	
	long creatorId = -1;
	
	public Question(String question){
		this.question = question;
	}
	
	public Question(long id, String question, String answer){
		this.creatorId = id;
		this.question = question;
		this.answer = answer;
	}
	
	public Question(String question, Question no, Question yes){
		this.question = question;
		this.noChild = no;
		this.yesChild = yes;
	}
	
	public boolean isLeafNode(){
		return this.answer != null;
	}
	
	@Override
	public String toString() {
		if(this.answer == null){
			return this.question;
		}
		return "Is it " + answer + "?";
	}
}
