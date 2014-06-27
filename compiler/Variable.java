/**
 * Every instance of this class represents a variable that was found in the program.
 */
public class Variable
{
	/*
	 * The variable's name.
	 */
	private String name;
	
	/*
	 * The variable's type. It can be "STRING" , "INTEGER" or "ARGUMENT"(if it is a function argument).
	 */
	private String type;
	
	/*
	 * The line that this variable was found.
	 */
	private int lineFound;
	
	
	
		
	/*
	 * The class default constructor.
	 */
	public Variable()
	{
		this.name = "";
		this.type = "";
		this.lineFound = -1;
	}
	
	
	
		
	/*
	 * Sets the name of the variable , to the value given as parameter.
	 */
	public void setName(String name)
	{	
		this.name = name;
	}
	
	
	/*
	 * Returns the variable's name.
	 */
	public String getName()
	{
		return this.name;
	}
	
	
	/*
	 * Sets the type of the variable , to the value given as parameter.
	 */
	public void setType(String type)
	{	
		this.type = type;
	}
	
	
	/*
	 * Returns the variable's type.
	 */
	public String getType()
	{
		return this.type;
	}
	
	
	/*
	 * Sets the line that this variable was found , to the value given as parameter.
	 */
	public void setLineFound(int line)
	{
		this.lineFound = line;
	}
	
	
	/*
	 * Returns the line that this variable was found.
	 */
	public int getLineFound()
	{
		return this.lineFound;
	}
	
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == null) return false;
		
		if(obj == this) return true;
		
		if(! (obj instanceof Variable) ) return false;
		
		Variable temp = (Variable)obj;
		
		return temp.getName().equals(this.getName());
	}

}
