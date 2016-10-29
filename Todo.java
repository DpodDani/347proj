import java.util.ArrayList;

public class Todo 
{
  private final ArrayList<String> todo;
  
  public Todo() {
  	todo = new ArrayList<String>();
  }
  
  public void add(String task) {
  	todo.add(task);
  }

  public String getList() {
  	String output = "{";
  	int count=0;
  	for (String s : todo) {
  		if (count++ != 0) output+=","+s;
  		else output+=s;
  	}
  	output+="}";
  	return output;
  }
}
