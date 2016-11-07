import java.util.ArrayList;
import java.io.*;
import java.nio.file.*;

public class Todo 
{
  private final ArrayList<String> todo;
  private String file;
  public Todo(String filename) {
  	todo = new ArrayList<String>();
  	file = filename;
  	if (Files.exists(Paths.get(file))) {
  		String line = null;

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                todo.add(line);
            }   

            bufferedReader.close();         
        } catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + file + "'");                
        } catch(IOException ex) {
            System.out.println("Error reading file '" + file + "'");                  
        }
  	}
  }
  
  public void add(String task) {
  	todo.add(task);

  	String fileName = file;

    try {
        FileWriter fileWriter = new FileWriter(fileName);

		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		for (String s : todo) {
			bufferedWriter.write(s);
			bufferedWriter.newLine();
		}

		bufferedWriter.close();
    } catch(IOException ex) {
		System.out.println("Error writing to file '"+ fileName + "'");
    }
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
