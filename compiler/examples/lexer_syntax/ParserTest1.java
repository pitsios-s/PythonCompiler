import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.Start;


public class ParserTest1
{
  public static void main(String[] args)
  {
for (int x=1 ; x< 13;x++){    
try
    {
      
	Parser parser = 
        new Parser(
        new Lexer(
        new PushbackReader(
        new FileReader("c"+x+".py"), 1024)));

      parser.parse();

	System.out.println("C"+x+" ok");
    }
    catch (Exception e)
    {
      System.out.println("C"+x+" " + e);
    }
}
  
  
for (int x=1 ; x< 11;x++){    
try
    {
      
	Parser parser = 
        new Parser(
        new Lexer(
        new PushbackReader(
        new FileReader("r"+x+".py"), 1024)));

      parser.parse();

	System.out.println("r"+x+" ok");
    }
    catch (Exception e)
    {
      System.out.println("r"+x+" " + e);
    }
}
  }
}

