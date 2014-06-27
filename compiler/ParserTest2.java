import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.Start;


public class ParserTest2
{
  public static void main(String[] args)
  {
    try
    {
         Parser parser = 
         new Parser(
         new Lexer(
         new PushbackReader(
         new FileReader(args[0].toString()), 1024)));

         Start ast = parser.parse();

         ast.apply(new ASTPrinter());
    }
    
    catch (Exception e)
    {
      System.err.println(e);
    }
    
  }
}
