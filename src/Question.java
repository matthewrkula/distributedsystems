import java.io.Serializable;


public class Question implements Serializable {
	String question;
	String answer;
	Question noChild;
	Question yesChild;
	
	transient GameSession createdBy = null;
	
	public Question(String question){
		this.question = question;
	}
	
	public Question(GameSession session, String question, String answer){
		this.createdBy = session;
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
