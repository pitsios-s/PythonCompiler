//GROUP A , TEAM 10.
//AGGELOPOULOS ARXIMHDHS -> 3100007
//PAGONAS ATHANASIOS     -> 3100249
//PANAGOPOULOS NIKOLAOS  -> 3100143 
//PITSIOS STAMATIOS      -> 3100153
//SXISTOS IWANNHS        -> 3100178


import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.*;


public class SemanticsTest
{
  
  public static void main(String[] args)
  {
    
	try
    {
     
		Parser parser = new Parser(new Lexer(new PushbackReader(new FileReader(args[0].toString()), 1024)));

		SymbolTable symtable =  new SymbolTable();
    
		Start ast = parser.parse();
	 
		FirstVisitor first = new FirstVisitor(symtable);
		ast.apply(first);
     
		SecondVisitor second = new SecondVisitor(first.getTable(),first.getErrors());
		ast.apply(second);
	 
		
		//If you want to check all the variables and functions that have been declared , uncomment the code bellow.	
		
		/*
		
		System.out.println("VARIABLES :\n");
     
		for(String key : symtable.getVariables().keySet())
		{
			System.out.println("Variable name : " + symtable.getVariables().get(key).getName()+ " , Variable type : " + symtable.getVariables().get(key).getType());
		}
	 
		System.out.println("\n\nFUNCTIONS :\n");
     
		for(String key : symtable.getFunctions().keySet())
		{
			System.out.println("Function name : " + symtable.getFunctions().get(key).getName()+ " , Return type : " + symtable.getFunctions().get(key).getReturns());
		}
		
		*/
	}
	
    catch (Exception e)
    {
		e.printStackTrace();
    }
    
  }
  
}
