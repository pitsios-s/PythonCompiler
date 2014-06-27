import java.util.*;

/**
 * This class is used for our symbol table.
 */
public class SymbolTable
{
	/*
	 * This map will contain all the functions with their keys.
	 * A function key is the function's name together with the number of the function's arguments.
	 * For example the function add(x,y) has a key : "add_2".
	 */
	private HashMap<String , Function> functions;
	
	/*
	 * This map will contain all the variables with their keys.
	 * A variable key is the variable's name.
	 * For example the variable x = 5 has a key : "x".
	 */
	private HashMap<String , Variable> variables;
	
	
	
	
	/*
	 * The class default constructor.
	 */
	public SymbolTable()
	{
		this.functions = new HashMap<String , Function>();
		this.variables = new HashMap<String , Variable>();
	}
	
	
	
	
	/*
	 *	This function adds a record to the functions map.
	 */
	public void addFunction(String key , Function func)
	{
		functions.put(key , func);
	}
	
	/*
	 *	This function adds a record to the variables map.
	 */
	public void addVariable(String key , Variable var)
	{
		this.variables.put(key , var);
	}
	
	/*
	 *	This function returns the functions map.
	 */
	public HashMap<String , Function> getFunctions()
	{
		return this.functions;
	}
	
	/*
	 *	This function returns the variables map.
	 */
	public HashMap<String , Variable> getVariables()
	{
		return this.variables;
	}
	
}
