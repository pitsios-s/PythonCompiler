import java.util.ArrayList;

/**
 * Every instance of this class represents a function that was found in the program.
 */
public class Function
{
	
	/*
	 * The function's name.
	 */
	private String name;
	
	/*
	 * The function's number of non-default arguments.
	 */
	private int non_default_args;
	
	/*
	 * The function's number of default arguments.
	 */
	private int default_args;
	
	/*
	 * The line that this function was found.
	 */
	private int lineFound;
	
	/*
	 * What the functions returns. Possible values are "STRING" , "INTEGER" , "ARGUMENT" or "VOID" if it has no return statement.
	 */
	private String returns;
	 
	/*
	 * The arguments of the function.
	 */
	private ArrayList<Variable> arguments;
	
	/*
	 * The variable that this function returns , or null if it does not return a variable. 
	 */
	private Variable return_variable;
	
	
	
	
	/*
	 * The class default constructor.
	 */
	public Function()
	{
		this.name = "";
		this.non_default_args = 0;
		this.default_args = 0;
		this.lineFound = 0;
		this.returns = "";
		this.arguments = new ArrayList<Variable>();
	}
	
	
	
		
	/*
	 * Sets the name of the function , to the value given as parameter.
	 */
	public void setName(String name)
	{	
		this.name = name;
	}
	
	
	/*
	 * Returns the function's name.
	 */
	public String getName()
	{
		return this.name;
	}
	
	
	/*
	 * Sets the number of the non-default arguments , to the value given as parameter.
	 */
	public void setNonDefaultArgs(int args)
	{
		this.non_default_args = args;
	}
	
	
	/*
	 * Returns the number of the non-default arguments.
	 */
	public int getNonDefaultArgs()
	{
		return this.non_default_args;
	}
	
	
	/*
	 * Sets the number of the default arguments , to the value given as parameter.
	 */
	public void setDefaultArgs(int args)
	{
		this.default_args = args;
	}
	
	
	/*
	 * Returns the number of the default arguments.
	 */
	public int getDefaultArgs()
	{
		return this.default_args;
	}
	
	
	/*
	 * Sets the line that this function was found , to the value given as parameter.
	 */
	public void setLineFound(int line)
	{
		this.lineFound = line;
	}
	
	
	/*
	 * Returns the line that this function was found.
	 */
	public int getLineFound()
	{
		return this.lineFound;
	}
	
	
	/*
	 * Sets the return type of this function , to the value given as parameter.
	 */
	public void setReturns(String type)
	{
		this.returns = type;
	}
	
	
	/*
	 * Returns the type of value that this function returns.
	 */
	public String getReturns()
	{
		return this.returns;
	}
	
	
	/*
	 * Returns the arguments.
	 */
	 public ArrayList<Variable> getArguments()
	 {
		return this.arguments;
	 }
	 
	 
	 /*
	  * Sets the return variable , to the value given as parameter. 
	  */
	 public void setReturnVariable(Variable var)
	 {
		this.return_variable = var;
	 }
	 
	 
	 /*
	  * Returns the return variable.
	  */
	 public Variable getReturnVariable()
	 {
		return this.return_variable;
	 }
	
}
