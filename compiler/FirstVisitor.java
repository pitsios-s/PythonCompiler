import minipython.analysis.*;
import minipython.node.*;
import java.util.*;


/**
 * This class parses all the code for first time and adds all the variables and functions to the symbol table.
 * It also finds some errors that are associated with wrong declarations.
 */
public class FirstVisitor extends DepthFirstAdapter
{
	/*
	 * The symbol table.
	 */
    private SymbolTable table;
	
	/*
	 * The number of errors that were found during the first parse.
	 */
	private int errors;
	
	/*
	 * This list will be used to figure out which function contains a certain return statement.
	 */
	private ArrayList<String> keys ;
	
	
	
	
	/*
	 * The class constructor.
	 */
	public FirstVisitor(SymbolTable symtable)
	{
		this.table = symtable;
		this.errors = 0;
		keys = new ArrayList<String>();
	}
	
	
	
	
	//Assign an expression to an identifier.
	@Override
	public void inARule4Statement(ARule4Statement node)
    {
		//Get the identifier.
		AIdentifier id = (AIdentifier)node.getIdentifier();
		
		//Create a new variable object and set it's name and the line that was found.
		Variable variable = new Variable();
		variable.setName(id.getId().getText());
		variable.setLineFound(id.getId().getLine());
		
		//If the expression is just a value.
		if(node.getExpression() instanceof AValueExpression)
		{
			//get the value.
			AValueExpression value = (AValueExpression)node.getExpression();
		
			String type = "";
		
			//If value is a number...
			if(value.getValue() instanceof ANumberValue)
			{
				type = "INTEGER";
			}
			
			//else if value is a string...
			else if(value.getValue() instanceof AStringValue)
			{
				type = "STRING";
			}
		
			//Create the variable's key.
			String key = variable.getName();
			
			//Set the variable's type.
			variable.setType(type);
		
			boolean found = false;
		
			//check if the variable has already been stated 
			for(String temp_key : table.getVariables().keySet())
			{
				if(table.getVariables().get(temp_key).getName().equals(variable.getName()))
				{
					found = true;
				
					//if the variable has already been stated with a different type , show error.
					if(!(table.getVariables().get(temp_key).getType().equals(variable.getType())))
					{
					
						if(!(table.getVariables().get(temp_key).getType().equals("ARGUMENT")))
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+variable.getName()+"' in line:"+id.getId().getLine()+" , in column:"+id.getId().getPos()+" has already been declared with different type");
							break;
						}
						
						else
						{
							table.getVariables().get(temp_key).setType(type);
						}
					}
				}
			}
		
			//Add the new variable to the sumbol table.
			if(!found) this.table.addVariable(key , variable);
		}
		
		//Else the expression is an identifier.
		else if(node.getExpression() instanceof AIdExpression)
		{
			AIdExpression id_exp = (AIdExpression)node.getExpression();
			AIdentifier identifier = (AIdentifier)id_exp.getIdentifier();
			
			//Get the identifier.
			TId t_id = identifier.getId();
			
			//Get the identifier's name.
			String t_name = t_id.getText();
			
			String t_type = "";
			String left_type = "";
			
			
			boolean right_found = false;
			boolean left_found = false;
			
			//Search all the variables in the symbol table.
			for(String temp_key : table.getVariables().keySet())
			{
				//If the identifier is a variable that is already been declared...
				if(table.getVariables().get(temp_key).getName().equals(t_name)) 
				{
					right_found = true;
					t_type = table.getVariables().get(temp_key).getType();
				}
				
				//If the variable has already been declared...
				if(table.getVariables().get(temp_key).getName().equals(variable.getName()))
				{
					left_found = true;
					left_type = table.getVariables().get(temp_key).getType();
				}
			}
			
			//If the identifier does not correspond to an already declared variable , print error message.
			if(!right_found) 
			{
				System.out.println("\n\nerror "+(++errors)+": Variable '"+t_name+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
			}
			
			//If the variable at the left side is not declared and the variable on the right side has been declared...
			else if(!left_found && right_found)
			{
				//Sets the variable's at the left side type , to the type of the right variable.
				variable.setType(t_type);
				
				//Create the variable's key.
				String key = variable.getName();
				
				//Add the variable to the symbol table.
				this.table.addVariable(key , variable);
			}
			
			//Else if both of the variables have been declared...
			else if(left_found && right_found)
			{
				//If their type is not the same , print error message.
				if(!t_type.equals(left_type))
				{
						if(!(left_type.equals("ARGUMENT")))
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+variable.getName()+"' in line:"+id.getId().getLine()+" , in column:"+id.getId().getPos()+" has already been declared with different type");
						}
						
						else
						{
							table.getVariables().get(variable.getName()).setType(t_type);
						}
				}
			}
		}
		
		//Else the expression is an addition or a multiplication or a substraction or an expression with parentheses.
		else if( (node.getExpression() instanceof AAddExpression )||(node.getExpression() instanceof AMultExpression)||(node.getExpression() instanceof AMinusExpression ) ||(node.getExpression() instanceof AExpExpression ) )
		{
			//This stack will be used for the dfs algorithm , for all the nodes that are expressions.
			Stack<PExpression> non_terminals = new Stack<PExpression>();
			
			//In this list we are going to keep all the "leaf" nodes that are either variables , values , or function calls.
			ArrayList<PExpression> terminals = new ArrayList<PExpression>();
			
			//If the expression is an addition...
			if(node.getExpression() instanceof AAddExpression)
			{
				//Cast Expression to an Addition Expression
				AAddExpression add_exp = (AAddExpression)node.getExpression();
				
				//Add it's children to the stack.
				non_terminals.push(add_exp.getRight());
				non_terminals.push(add_exp.getLeft());
			}
			
			//If the expression is a multiplication...
			else if(node.getExpression() instanceof AMultExpression)
			{
				//Cast Expression to a Multiplication Expression
				AMultExpression mult_exp = (AMultExpression)node.getExpression();
				
				//Add it's children to the stack.
				non_terminals.push(mult_exp.getRight());
				non_terminals.push(mult_exp.getLeft());
			}
			
			//If the expression is a substraction...
			else if(node.getExpression() instanceof AMinusExpression)
			{
				//Cast Expression to a Substraction Expression
				AMinusExpression minus_exp = (AMinusExpression)node.getExpression();
				
				//Add it's children to the stack.
				non_terminals.push(minus_exp.getRight());
				non_terminals.push(minus_exp.getLeft());
			}
			
			//If the expression is an expression with parentheses.
			else if(node.getExpression() instanceof AExpExpression)
			{
				//Cast Expression to a Exp Expression
				AExpExpression par_exp = (AExpExpression)node.getExpression();
				
				//Add it's children to the stack.
				non_terminals.push(par_exp.getExpression());
			}
			
			//While there are more nodes...
			while(!non_terminals.isEmpty())
			{
				//Get the first from the stack.
				PExpression p_exp = non_terminals.pop(); 
				
				//If the node is another addition , multiplication , or substraction , or an expression with parentheses.
				if( p_exp instanceof AAddExpression || p_exp instanceof AMultExpression || p_exp instanceof AMinusExpression || p_exp instanceof AExpExpression  )  
				{
					//If the expression is an addition...
					if(p_exp instanceof AAddExpression)
					{
						//Cast Expression to an Addition Expression
						AAddExpression add = (AAddExpression)p_exp;
						
						//Add it's children to the stack.
						non_terminals.push(add.getRight());
						non_terminals.push(add.getLeft());
					}
			
					//If the expression is a multiplication...
					else if(p_exp instanceof AMultExpression)
					{
						//Cast Expression to a Multiplication Expression
						AMultExpression mult = (AMultExpression)p_exp;
						
						//Add it's children to the stack.
						non_terminals.push(mult.getRight());
						non_terminals.push(mult.getLeft());
					}
			
					//If the expression is a substraction...
					else if(p_exp instanceof AMinusExpression)
					{
						//Cast Expression to a Substraction Expression
						AMinusExpression minus = (AMinusExpression)p_exp;
						
						//Add it's children to the stack.
						non_terminals.push(minus.getRight());
						non_terminals.push(minus.getLeft());
					}	
					
					//If the expression is an expression with parentheses.
					else if(p_exp instanceof AExpExpression)
					{
						//Cast Expression to a Exp Expression
						AExpExpression par_exp = (AExpExpression)p_exp;
						
						//Add it's children to the stack.
						non_terminals.push(par_exp.getExpression());
					}
				}
				
				//Else if the node is a terminal symbol , add it to the terminals list.
				else
				{
					terminals.add(p_exp);
				}
				
			}
			
			//If the expression is correct.
			boolean correct = true;
			
			//Look all terminal symbols.
			for(int i = 0; i<terminals.size(); i++)
			{
				PExpression p_exp = terminals.get(i);
				
				//If the current symbol is a simple value.
				if(p_exp instanceof AValueExpression)
				{
					//Cast it to a value expression.
					AValueExpression val_exp = (AValueExpression)p_exp;
					
					//If this value is not a number , then print error message.
					if(!(val_exp.getValue() instanceof ANumberValue))
					{
						System.out.println("\n\nerror "+(++errors)+": Error in line "+variable.getLineFound()+ ". Arithmetic expression can not contain string values!");						
						correct = false;
						break;
					}
				}
				
				//Else if the current symbol is an identifier.
				else if(p_exp instanceof AIdExpression)
				{
					//Cast the expression to an identifier.
					AIdExpression id_exp = (AIdExpression)p_exp;
					AIdentifier a_id = (AIdentifier)id_exp.getIdentifier();
					TId t_id = a_id.getId();
					
					//If this variable has not been declared , print error message.
					if(!(table.getVariables().containsKey(t_id.getText().trim())))
					{
						System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						correct = false;
						break;
					}
					
					//Else if it has been declared...
					else
					{
						//Get the variable's type.
						String type = table.getVariables().get(t_id.getText().trim()).getType();
						
						//If the type is STRING , print error message.
						if(type.equals("STRING")) 
						{
							System.out.println("\n\nerror "+(++errors)+": Error in line "+variable.getLineFound()+ ". Arithmetic expression can not contain string values!");
							correct = false;
							break;
						}
					}
				}
				
				//Else if the expression is a function call.
				else if(p_exp instanceof AFuncExpression)
				{
					//Do nothing , we will deal with this case in the second visitor.
					correct = false;
				}
			}
			
			//If the arithmetics expression is correct...
			if(correct)
			{
				//If the variable has not already been declared , set the variable's type to INTEGER and add it to the symbol table.
				if(!(table.getVariables().containsKey(variable.getName())))
				{
					variable.setType("INTEGER");
					table.addVariable(variable.getName() , variable);
				}
				
				//Else if the variable is declared...
				else
				{
					//If it's type is STRING , print error message.
					if(table.getVariables().get(variable.getName()).getType().equals("STRING"))
					{
						System.out.println("\n\nerror "+(++errors)+": Incompatible types at line: "+variable.getLineFound()+ ". Expected STRING but found INTEGER");
					}
					
					else if(table.getVariables().get(variable.getName()).getType().equals("ARGUMENT"))
					{
						table.getVariables().get(variable.getName()).setType("INTEGER");
					}
				}
			}
			
		}
    }
	
	
	
	
	//Declaring a function.
	@SuppressWarnings("unchecked")
	@Override
	public void inAFuncFunction(AFuncFunction node)
    {
	
		//Clear the function key list.
		keys.clear();
		
        AIdentifier func_name = (AIdentifier)node.getIdentifier();
		
		//Getting the function's name.
		TId id = func_name.getId();
		String f_name = id.getText().trim();
		
		//Get the function's line.
		int f_line = id.getLine();
		
		//The total number of non-default arguments of the function.
		int non_default_args = 0;
		
		//The total number of default arguments of the function.
		int default_args = 0;
		
		//All the default arguments.
		ArrayList<Variable> d_args = new ArrayList<Variable>();
		
		//All the non default arguments.
		ArrayList<Variable> nd_args = new ArrayList<Variable>();
		
		//Get all the arguments of the function.
		LinkedList<AArgArgument> arguments = node.getArgument();
		
		//For each argument.
		for(int i = 0; i<arguments.size(); i++)
		{
			//Get the assign_value production for the first argument.
			LinkedList<AAssignValue> assignValue = arguments.get(i).getAssignValue();
			
			//Get all the rest arguments.
			LinkedList<AMoreArguments> more_args = arguments.get(i).getMoreArguments();
			
			//If the first argument has not a default value.
			if(assignValue.size() == 0)
			{
				//increase the number of non-default arguments.
				non_default_args++;
				
				//Create a new variable object for the first argument.
				Variable var = new Variable();
				
				AIdentifier a_id = (AIdentifier)arguments.get(i).getIdentifier();
				TId t_id = a_id.getId();
				
				//Set the variable's properties
				var.setName(t_id.getText().trim());
				var.setLineFound(t_id.getLine());
				var.setType("ARGUMENT");
				
				//add the variable to the non-default arguments list.
				nd_args.add(var);
				
				//Add the variable to the symbol table.
				table.addVariable(var.getName() , var);
			}
			
			//Else if the first argument has a default value.
			else
			{
				//increase the number of default arguments.
				default_args++;
			
				//Create a new variable object for the first argument.
				Variable var = new Variable();
				
				AIdentifier a_id = (AIdentifier)arguments.get(i).getIdentifier();
				TId t_id = a_id.getId();
				
				//Sets the variable's name and line.
				var.setName(t_id.getText().trim());
				var.setLineFound(t_id.getLine());
				
				//If the value is a number , set the type of the variable to "INTEGER"
				if(assignValue.get(i).getValue() instanceof ANumberValue)
				{
					var.setType("INTEGER");
				}
				
				//Else if the value is a string , set the type of the variable to "STRING"
				else
				{
					var.setType("STRING");
				}
				
				//add the variable to the default arguments list.
				d_args.add(var);
				
				//Add the variable to the symbol table.
				table.addVariable(var.getName() , var);
			}
			
			//For all the rest arguments...
			for(int j = 0; j<more_args.size(); j++)
			{
				//Get the assign_value production for the current argument.
				LinkedList<AAssignValue> values = more_args.get(j).getAssignValue();
				
				//If the current argument has not a default value.
				if(values.size() == 0)
				{	
					//increase the number of non-default arguments.
					non_default_args++;
					
					//Create a new variable object for the first argument.
					Variable var = new Variable();
					
					AIdentifier a_id = (AIdentifier)more_args.get(j).getIdentifier();
					TId t_id = a_id.getId();
					
					//Set the variable's properties
					var.setName(t_id.getText().trim());
					var.setLineFound(t_id.getLine());
					var.setType("ARGUMENT");
					
					//add the variable to the non-default arguments list.
					nd_args.add(var);
					
					//Add the variable to the symbol table.
					table.addVariable(var.getName() , var);
				}
				
				//Else if the current argument has a default value.
				else
				{
					//increase the number of default arguments.
					default_args++;
				
					//Create a new variable object for the first argument.
					Variable var = new Variable();
					
					AIdentifier a_id = (AIdentifier)more_args.get(j).getIdentifier();
					TId t_id = a_id.getId();
					
					//Sets the variable's name and line.
					var.setName(t_id.getText().trim());
					var.setLineFound(t_id.getLine());
					
					//If the value is a number , set the type of the variable to "INTEGER"
					if(values.get(0).getValue() instanceof ANumberValue)
					{
						var.setType("INTEGER");
					}
					
					//Else if the value is a string , set the type of the variable to "STRING"
					else
					{
						var.setType("STRING");
					}
					
					//add the variable to the default arguments list.
					d_args.add(var);
					
					//Add the variable to the symbol table.
					table.addVariable(var.getName() , var);
				}
			}
		}
		
		//The return variable of the function.
		Variable return_var = null;
		
		//If there is already a function with the same signature or not.
		boolean function_found = false;
		
		//For all the posssible function declarations...
		for(int i = non_default_args; i <= non_default_args + default_args; i++)
		{
			//Create the function's key.
			String function_key = f_name+"_"+String.valueOf(i);
			
			//If we have already declared a function with the same signature , print error message.
			if(table.getFunctions().containsKey(function_key))
			{
				int past_line = table.getFunctions().get(function_key).getLineFound();
				System.out.println("\n\nerror "+(++errors)+": Function '"+f_name+"' in line:"+id.getLine()+" has already been declared in line "+past_line);
				function_found = true;
			}
		}
		
		//If there is not a function with a same signature...
		if(!function_found)
		{
			
			//Add all the possible declarations of the function to the symbol table.
			for(int i = non_default_args; i <= non_default_args + default_args; i++)
			{
				//Create a new function object.
				Function function = new Function();
			
				//Sets the function's properties.
				function.setName(f_name);
				function.setLineFound(f_line);
				
				//The default return variable will be null.
				function.setReturnVariable(return_var);
				
				//The default return type will be void. If the function returns something , we will fix it in the return statement.
				function.setReturns("VOID");
				
			
				//Create the function's key.
				String function_key = f_name+"_"+String.valueOf(i);
				keys.add(function_key);
				
				//Set the rest of the function's properties.
				function.setNonDefaultArgs(non_default_args);
				function.setDefaultArgs(i - non_default_args);
				
				//Add the non-default arguments to the function.
				for(int k = 0; k < non_default_args; k++)
				{
					function.getArguments().add(nd_args.get(k));
				}
				
				//Add the default arguments to the function.
				if(default_args > 0)
				{
					for(int k = 0; k < Math.abs(i - non_default_args) ; k++)
					{
						function.getArguments().add(d_args.get(k));
					}
				}
				
				//Add the function to the symbol table.
				table.addFunction(function_key,function);
			}
		}
		
    }
	
	
	
	
	//A return statement.
	@Override
	public void inARule7Statement(ARule7Statement node)
	{
	
		//By default , the function's return type will be VOID.
		String return_type = "VOID";
		
		Variable return_var = null;
		
			
			//The expression is just a value.
			if(node.getExpression() instanceof AValueExpression)
			{
				//Cast it to a value expression.
				AValueExpression val_exp = (AValueExpression)node.getExpression();
				
				//If value is a number , return type will be INTEGER.
				if(val_exp.getValue() instanceof ANumberValue)
				{
					return_type = "INTEGER";
				}
				
				//Else if value is a string , return type will be STRING.
				else
				{
					return_type = "STRING";
				}
			}
			
			//Else if the expression is an identifier.
			else if(node.getExpression() instanceof AIdExpression)
			{
				//Cast the expression to an identifier expression.
				AIdExpression id_exp = (AIdExpression)node.getExpression();
				AIdentifier a_id = (AIdentifier)id_exp.getIdentifier();
				TId t_id = a_id.getId();
				
				//If the variable has not yet been declared , print error message.
				if(!table.getVariables().containsKey(t_id.getText().trim()))
				{
					System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
					return;
				}
				
				//Else if the variable has been declared...
				else
				{
					//Set the return type of the function to the variable's type.
					return_type = table.getVariables().get(t_id.getText().trim()).getType();
					
					//If the functions returns a variable that is an argument , set the return variable of the function , to this variable.
					if(return_type.equals("ARGUMENT")) return_var = table.getVariables().get(t_id.getText().trim());
				}
			}
		
			//Else the expression is an addition or a multiplication or a substraction or an expression in parentheses.
			else if( (node.getExpression() instanceof AAddExpression )||(node.getExpression() instanceof AMultExpression)||(node.getExpression() instanceof AMinusExpression ) || (node.getExpression() instanceof AExpExpression ) )
			{
				//This stack will be used for the dfs algorithm , for all the nodes that are expressions.
				Stack<PExpression> non_terminals = new Stack<PExpression>();
				
				//In this list we are going to keep all the "leaf" nodes that are either variables , values , or function calls.
				ArrayList<PExpression> terminals = new ArrayList<PExpression>();
				
				//If the expression is an addition...
				if(node.getExpression() instanceof AAddExpression)
				{
					//Cast Expression to an Addition Expression
					AAddExpression add_exp = (AAddExpression)node.getExpression();
					
					//Add it's children to the stack.
					non_terminals.push(add_exp.getRight());
					non_terminals.push(add_exp.getLeft());
				}
				
				//If the expression is a multiplication...
				else if(node.getExpression() instanceof AMultExpression)
				{
					//Cast Expression to a Multiplication Expression
					AMultExpression mult_exp = (AMultExpression)node.getExpression();
					
					//Add it's children to the stack.
					non_terminals.push(mult_exp.getRight());
					non_terminals.push(mult_exp.getLeft());
				}
				
				//If the expression is a substraction...
				else if(node.getExpression() instanceof AMinusExpression)
				{
					//Cast Expression to a Substraction Expression
					AMinusExpression minus_exp = (AMinusExpression)node.getExpression();
					
					//Add it's children to the stack.
					non_terminals.push(minus_exp.getRight());
					non_terminals.push(minus_exp.getLeft());
				}
				
				//If the expression is an expression with parentheses.
				else if(node.getExpression() instanceof AExpExpression)
				{
					//Cast Expression to a Exp Expression
					AExpExpression par_exp = (AExpExpression)node.getExpression();
					
					//Add it's children to the stack.
					non_terminals.push(par_exp.getExpression());
				}
				
				//While there are more nodes...
				while(!non_terminals.isEmpty())
				{
					//Get the first from the stack.
					PExpression p_exp = non_terminals.pop(); 
					
					//If the node is another addition , multiplication , or substraction.
					if( p_exp instanceof AAddExpression || p_exp instanceof AMultExpression || p_exp instanceof AMinusExpression || p_exp instanceof AExpExpression )  
					{
						//If the expression is an addition...
						if(p_exp instanceof AAddExpression)
						{
							//Cast Expression to an Addition Expression
							AAddExpression add = (AAddExpression)p_exp;
							
							//Add it's children to the stack.
							non_terminals.push(add.getRight());
							non_terminals.push(add.getLeft());
						}
				
						//If the expression is a multiplication...
						else if(p_exp instanceof AMultExpression)
						{
							//Cast Expression to a Multiplication Expression
							AMultExpression mult = (AMultExpression)p_exp;
							
							//Add it's children to the stack.
							non_terminals.push(mult.getRight());
							non_terminals.push(mult.getLeft());
						}
				
						//If the expression is a substraction...
						else if(p_exp instanceof AMinusExpression)
						{
							//Cast Expression to a Substraction Expression
							AMinusExpression minus = (AMinusExpression)p_exp;
							
							//Add it's children to the stack.
							non_terminals.push(minus.getRight());
							non_terminals.push(minus.getLeft());
						}	
						
						//If the expression is an expression with parentheses.
						else if(p_exp instanceof AExpExpression)
						{
							//Cast Expression to a Exp Expression
							AExpExpression par_exp = (AExpExpression)p_exp;
							
							//Add it's children to the stack.
							non_terminals.push(par_exp.getExpression());
						}
					}
					
					//Else if the node is a terminal symbol , add it to the terminals list.
					else
					{
						terminals.add(p_exp);
					}
				
				}
			
				//If the expression is correct.
				boolean correct = true;
			
				//Look all terminal symbols.
				for(int i = 0; i<terminals.size(); i++)
				{
					PExpression p_exp = terminals.get(i);
				
					//If the current symbol is a value.
					if(p_exp instanceof AValueExpression)
					{
						//Cast the expression to a value expression.
						AValueExpression val_exp = (AValueExpression)p_exp;
					
						//If the type of the value is not INTEGER , print error message.
						if(!(val_exp.getValue() instanceof ANumberValue))
						{
							AStringValue string_val = (AStringValue)val_exp.getValue();
							TString t_string = string_val.getString();
							System.out.println("\n\nerror "+(++errors)+": Error in line "+(t_string.getLine())+ ". Arithmetic expression can not contain string values!");						
							correct = false;
							return;
						}
					}
				
					//Else if the expression is an identifier...
					else if(p_exp instanceof AIdExpression)
					{
						//Cast the expression to an identifier expression.
						AIdExpression id_exp = (AIdExpression)p_exp;
						AIdentifier a_id = (AIdentifier)id_exp.getIdentifier();
						TId t_id = a_id.getId();
					
						//If this variable has not yet been declared , print error message.
						if(!(table.getVariables().containsKey(t_id.getText().trim())))
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
							correct = false;
							return;
						}
					
						//Else if the variable has been declared...
						else
						{
							//Get it's type.
							String type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the type is STRING , print error message.
							if(type.equals("STRING")) 
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+ (t_id.getLine()) + ". Arithmetic expression can not contain string values!");
								correct = false;
								return;
							}
							
							//If the variable that is an argument , is in an arithmetic expression , set the type of this argument to INTEGER.
							else if(type.equals("ARGUMENT")) table.getVariables().get(t_id.getText().trim()).setType("INTEGER");
						}
					}
				
					//Else if the expression is a function call.
					else if(p_exp instanceof AFuncExpression)
					{
						//Do nothing yet , we will deal with this in another visitor.
					}
				}
			
				//If the arithmetics expression is correct , then the return type will be INTEGER.
				if(correct)
				{
					return_type = "INTEGER";
				}
			
			}
			
			for(int i = 0; i < keys.size(); i++)
			{
				table.getFunctions().get(keys.get(i)).setReturns(return_type);
				table.getFunctions().get(keys.get(i)).setReturnVariable(return_var);
			}			
	}
	
	
	
		
	//Returns the number of errors that were found on this first visitor.
	public int getErrors()
	{
		return this.errors;
	}
	
	
	
	
	//Returns the filled symbol table
	public SymbolTable getTable()
	{
		return this.table;
	}

}
