import minipython.analysis.*;
import minipython.node.*;

import java.util.*;


public class SecondVisitor extends DepthFirstAdapter
{	
	/*
	 * The symbol table.
	 */
	private SymbolTable table;
	
	/*
	 * The number of errors that were found in all the program.
	 */
	private int errors = 0;
	
	
	
	
	/*
	 * The class constructor.
	 */
	public SecondVisitor(SymbolTable symtable,int errors)
	{
		this.table = symtable;
		this.errors=errors;
	}
	
	
	
	//Exiting the program...
	@Override
	public void outACmdsGoal(ACmdsGoal node)
	{
        System.out.println("\n\nTotal Errors: " + errors + "\n\n\n");
	}
	
	
	
	
	//If we want to print a statement.
	@SuppressWarnings("unchecked")
	@Override
	public void inARule3Statement(ARule3Statement node)
    {
		
		LinkedList<PExpression> print_list = new LinkedList<PExpression>();
	
		LinkedList<PMoreExpressions> moreArgs = new LinkedList<PMoreExpressions>(node.getMoreExpressions());
	
		print_list.add(node.getExp1());
	
		for(int i = 0; i < moreArgs.size(); i++)
		{
			print_list.add( ((AMoreExpressions)moreArgs.get(i)).getExpression());
		}
		
		for(int s = 0; s < print_list.size(); s++)
		{
			
			if(print_list.get(s) instanceof AValueExpression)
			{
				//Do nothing , it is correct!
			}
	
			else if(print_list.get(s) instanceof AIdExpression)
			{
			
				AIdExpression exp_id = (AIdExpression)print_list.get(s);
				AIdentifier id = (AIdentifier)exp_id.getIdentifier();
				
				boolean found = false;
		
				for(String key : table.getVariables().keySet())
				{
					if(table.getVariables().get(key).getName().equals(id.getId().getText()) && table.getVariables().get(key).getLineFound() < id.getId().getLine()) found = true;
				}
		
				if(!found)
				{	
					System.out.println("\n\nerror "+(++errors)+": Variable '"+id.getId().getText()+"' in line:"+id.getId().getLine()+" , in column:"+id.getId().getPos()+" undefined");
				}
			}
			
			else if(print_list.get(s) instanceof AFuncExpression)
			{
				//Cast the expression to a function call expression.
				AFuncExpression afunc_exp = (AFuncExpression)print_list.get(s);
			
				AFunctionCall afunc_call = (AFunctionCall)afunc_exp.getFunctionCall();
				
				//Get the identifier that the function call is assigned to.
				AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
				TId func_id = a_id.getId();
			
			//The number of arguments in the function call.
			int args_number = 0;
			
			//Get the argument list size.
			LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
			
			//The arguments that we use to the function call.
			ArrayList<PExpression> call_args = new ArrayList<PExpression>();
			
			//For all the objects in the argument list.
			for(int k = 0; k<args.size(); k++)
			{
				AArgList a_args = (AArgList)args.get(k);
				
				//Increase the number of arguments.
				args_number++;
				
				//Add the first argument to the function call arguments list.
				call_args.add(a_args.getExpression());
				
				//Get all the rest arguments , if any.
				ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
				
				//Add the number of more_args to the number of total arguments.
				args_number += more_args.size();
				
				//For any of these more_args...
				for(int j = 0; j < more_args.size(); j++)
				{
					//Cast it to a AMoreListArguments object.
					AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
					
					//Add the argument to the function call arguments list.
					call_args.add(a_more_args.getExpression());
				}
			}
			
			//Create the function's key.
			String key = func_id.getText().trim()+"_"+args_number;
			
			//If there in no function matching this key , print error message.
			if(!table.getFunctions().containsKey(key))
			{
				System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
			}
			
			//Else if there is such a function...
			else
			{
				//Get this function.
				Function function = table.getFunctions().get(key);
				
				//This variable shows if the association between the function arguments and the call arguments is correct.
				boolean arguments_correct = true;
			
				//For all the call arguments.
				for(int i = 0; i < call_args.size(); i++)
				{
					//The type of the current argument.
					String current_type = "";
					
					//The column that the current call argument was found.
					int column = -1;
					
					//If the current call argument is a constant...
					if(call_args.get(i) instanceof AValueExpression)
					{
						//Cast it to AValueExpression object.
						AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
						
						//If this constant is a number...
						if(a_val_exp.getValue() instanceof ANumberValue)
						{
							ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
							AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
							TInteger t_int = a_number.getInteger();
							column = t_int.getPos();
							current_type = "INTEGER";
						}
						
						//Else if the constant's type is a string...
						else
						{
							AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
							TString t_string = a_string_value.getString();
							column = t_string.getPos();
							current_type = "STRING";
						}
						
						
						//If the call argument and the associated function argument does not match...
						if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
						{
							//If the function argument's type is not "ARGUMENT" , print error message.
							if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
							{
								arguments_correct = false;
								System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
							}
						}
					}
					
					//Else if the current call argument is a variable...
					else if(call_args.get(i) instanceof AIdExpression)
					{
						//Find the token that corresponds to this variable.
						AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
						AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
						TId t_id = a_identifier.getId();
						
						//If this variable is not in the symbol table , print error message.
						if(!(table.getVariables().containsKey(t_id.getText().trim())))
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if there is such a variable but is declared later in the program , print error message.
						else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if the variable already exists...
						else
						{
							//The current arguments type , will be the variable's type.
							current_type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the call argument and the associated function argument does not match...
							if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
							{
								//If the function argument's type is not "ARGUMENT" , print error message.
								if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
								}
								
							}
						}
						
						
					}
					
					
				}
				
				//If the arguments are passed correctly , it is time to look for the function's return type.
				if(arguments_correct)
				{
					
					//If the function does not return anything , print error message.
					if(function.getReturns().equals("VOID"))
					{
						System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not print a function that returns VOID.");
					}
				}
				
			}
			}
			
			//Else the expression is an addition or a multiplication or a substraction or an expression with parentheses.
		else if( (print_list.get(s) instanceof AAddExpression )||(print_list.get(s) instanceof AMultExpression)||(print_list.get(s) instanceof AMinusExpression ) ||(print_list.get(s) instanceof AExpExpression ) )
		{
			//This stack will be used for the dfs algorithm , for all the nodes that are expressions.
			Stack<PExpression> non_terminals = new Stack<PExpression>();
			
			//In this list we are going to keep all the "leaf" nodes that are either variables , values , or function calls.
			ArrayList<PExpression> terminals = new ArrayList<PExpression>();
			
			//If the expression is an addition...
			if(print_list.get(s) instanceof AAddExpression)
			{
				//Cast Expression to an Addition Expression
				AAddExpression add_exp = (AAddExpression)print_list.get(s);
				
				//Add it's children to the stack.
				non_terminals.push(add_exp.getRight());
				non_terminals.push(add_exp.getLeft());
			}
			
			//If the expression is a multiplication...
			else if(print_list.get(s) instanceof AMultExpression)
			{
				//Cast Expression to a Multiplication Expression
				AMultExpression mult_exp = (AMultExpression)print_list.get(s);
				
				//Add it's children to the stack.
				non_terminals.push(mult_exp.getRight());
				non_terminals.push(mult_exp.getLeft());
			}
			
			//If the expression is a substraction...
			else if(print_list.get(s) instanceof AMinusExpression)
			{
				//Cast Expression to a Substraction Expression
				AMinusExpression minus_exp = (AMinusExpression)print_list.get(s);
				
				//Add it's children to the stack.
				non_terminals.push(minus_exp.getRight());
				non_terminals.push(minus_exp.getLeft());
			}
			
			//If the expression is an expression with parentheses.
			else if(print_list.get(s) instanceof AExpExpression)
			{
				//Cast Expression to a Exp Expression
				AExpExpression par_exp = (AExpExpression)print_list.get(s);
				
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
			
			//Look all terminal symbols.
			for(int z = 0; z<terminals.size(); z++)
			{
				PExpression p_exp = terminals.get(z);
				
				//If the current symbol is a simple value.
				if(p_exp instanceof AValueExpression)
				{
					//Cast it to a value expression.
					AValueExpression val_exp = (AValueExpression)p_exp;
					
					//If this value is not a number , then print error message.
					if(!(val_exp.getValue() instanceof ANumberValue))
					{
						AStringValue string_val = (AStringValue)val_exp.getValue();
						TString t_string = string_val.getString();
						System.out.println("\n\nerror "+(++errors)+": Error in line "+t_string.getLine()+ ". Arithmetic expression can not contain string values!");						
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
						break;
					}
					
					//Else if it has been declared...
					else
					{
						if(t_id.getLine() > table.getVariables().get(t_id.getText().trim()).getLineFound())
						{
							//Get the variable's type.
							String type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the type is STRING , print error message.
							if(type.equals("STRING")) 
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+t_id+ ". Arithmetic expression can not contain string values!");
								break;
							}
						}
						
						else
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" undefined");
						}
					}
				}
				
				//If the expression is a function call.
				else if(p_exp instanceof AFuncExpression)
				{
					//Cast the expression to a function call expression.
					AFuncExpression func_exp = (AFuncExpression)p_exp;
					AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
					TId func_id = a_id.getId();
					
					//The number of arguments in the function call.
					int args_number = 0;
					
					//Get the argument list size.
					LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
					
					//The arguments that we use to the function call.
					ArrayList<PExpression> call_args = new ArrayList<PExpression>();
					
					//For all the objects in the argument list.
					for(int i = 0; i<args.size(); i++)
					{
						AArgList a_args = (AArgList)args.get(i);
						
						//Increase the number of arguments.
						args_number++;
						
						//Add the first argument to the function call arguments list.
						call_args.add(a_args.getExpression());
						
						//Get all the rest arguments , if any.
						ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
						
						//Add the number of more_args to the number of total arguments.
						args_number += more_args.size();
						
						//For any of these more_args...
						for(int j = 0; j < more_args.size(); j++)
						{
							//Cast it to a AMoreListArguments object.
							AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
							
							//Add the argument to the function call arguments list.
							call_args.add(a_more_args.getExpression());
						}
					}
					
					//Create the function's key.
					String key = func_id.getText().trim()+"_"+args_number;
					
					//If there in no function matching this key , print error message.
					if(!table.getFunctions().containsKey(key))
					{
						System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
					}
					
					//Else if there is such a function...
					else
					{
						//Get this function.
						Function function = table.getFunctions().get(key);
						
						//This variable shows if the association between the function arguments and the call arguments is correct.
						boolean arguments_correct = true;
						
						//The return type of the function , if the function returns an argument that we don't know it's type.
						String temp_return_type = "";
					
						//For all the call arguments.
						for(int i = 0; i < call_args.size(); i++)
						{
							//The type of the current argument.
							String current_type = "";
							
							//The column that the current call argument was found.
							int column = -1;
							
							//If the current call argument is a constant...
							if(call_args.get(i) instanceof AValueExpression)
							{
								//Cast it to AValueExpression object.
								AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
								
								//If this constant is a number...
								if(a_val_exp.getValue() instanceof ANumberValue)
								{
									ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
									AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
									TInteger t_int = a_number.getInteger();
									column = t_int.getPos();
									current_type = "INTEGER";
								}
								
								//Else if the constant's type is a string...
								else
								{
									AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
									TString t_string = a_string_value.getString();
									column = t_string.getPos();
									current_type = "STRING";
								}
								
								
								//If the call argument and the associated function argument does not match...
								if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
								{
									//If the function argument's type is not "ARGUMENT" , print error message.
									if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
									{
										arguments_correct = false;
										System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
									}
									
									//Else if the function argument's type is "ARGUMENT"
									else
									{
										//If the function's return variable is not null...
										if(!(function.getReturnVariable() == null))
										{
											//If the current call argument , matches the return variable of the function...
											if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
											{
												//...set the return type of the function , to this argument's type.
												temp_return_type = current_type;
											}
										}
									}
								}
							}
							
							//Else if the current call argument is a variable...
							else if(call_args.get(i) instanceof AIdExpression)
							{
								//Find the token that corresponds to this variable.
								AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
								AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
								TId t_id = a_identifier.getId();
								
								//If this variable is not in the symbol table , print error message.
								if(!(table.getVariables().containsKey(t_id.getText().trim())))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if there is such a variable but is declared later in the program , print error message.
								else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if the variable already exists...
								else
								{
									//The current arguments type , will be the variable's type.
									current_type = table.getVariables().get(t_id.getText().trim()).getType();
								
									//If the call argument and the associated function argument does not match...
									if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
									{
										//If the function argument's type is not "ARGUMENT" , print error message.
										if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
										{
											arguments_correct = false;
											System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
										}
										
										//Else if the function argument's type is "ARGUMENT"
										else
										{
											//If the function's return variable is not null...
											if(!(function.getReturnVariable() == null))
											{
												//If the current call argument , matches the return variable of the function...
												if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
												{
													//...set the return type of the function , to this argument's type.
													temp_return_type = current_type;
												}
											}
										}
									}
								}
					
								
							}
							
							
						}
						
						//If the arguments are passed correctly , it is time to look for the function's return type.
						if(arguments_correct)
						{
							//If the function does not return anything , print error message.
							if(function.getReturns().equals("VOID"))
							{
								System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not assign VOID to a variable ");
							}
							
							//Else if the function returns an integer...
							else if(function.getReturns().equals("INTEGER"))
							{
								//Do nothing
							}
							
							//Else if the function returns an string...
							else if(function.getReturns().equals("STRING"))
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ ". Arithmetic expression can not contain string values!");
							}
							
							//Else if the return type is an argument.
							else if(function.getReturns().equals("ARGUMENT"))
							{
								//If the return type is String , print error message.
								if(temp_return_type.equals("STRING"))
								{
									System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ ". Arithmetic expression can not contain string values!");
								}
							}
						}
					
					}
				}
			}
			
		  }
			
	    }
   
    }
	
	
	
	
	//Assign an expression to an identifier.
	@SuppressWarnings("unchecked")
	@Override
	public void inARule4Statement(ARule4Statement node)
    {
		//Get the identifier.
		AIdentifier id = (AIdentifier)node.getIdentifier();
		
		//Create a new variable object and set it's name and the line that was found.
		Variable variable = new Variable();
		variable.setName(id.getId().getText());
		variable.setLineFound(id.getId().getLine());
		
		//If the expression is a function call.
		if(node.getExpression() instanceof AFuncExpression)
		{
			//Cast the expression to a function call expression.
			AFuncExpression func_exp = (AFuncExpression)node.getExpression();
			AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
			
			//Get the identifier that the function call is assigned to.
			AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
			TId func_id = a_id.getId();
			
			//The number of arguments in the function call.
			int args_number = 0;
			
			//Get the argument list size.
			LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
			
			//The arguments that we use to the function call.
			ArrayList<PExpression> call_args = new ArrayList<PExpression>();
			
			//For all the objects in the argument list.
			for(int i = 0; i<args.size(); i++)
			{
				AArgList a_args = (AArgList)args.get(i);
				
				//Increase the number of arguments.
				args_number++;
				
				//Add the first argument to the function call arguments list.
				call_args.add(a_args.getExpression());
				
				//Get all the rest arguments , if any.
				ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
				
				//Add the number of more_args to the number of total arguments.
				args_number += more_args.size();
				
				//For any of these more_args...
				for(int j = 0; j < more_args.size(); j++)
				{
					//Cast it to a AMoreListArguments object.
					AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
					
					//Add the argument to the function call arguments list.
					call_args.add(a_more_args.getExpression());
				}
			}
			
			//Create the function's key.
			String key = func_id.getText().trim()+"_"+args_number;
			
			//If there in no function matching this key , print error message.
			if(!table.getFunctions().containsKey(key))
			{
				System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
			}
			
			//Else if there is such a function...
			else
			{
				//Get this function.
				Function function = table.getFunctions().get(key);
				
				//This variable shows if the association between the function arguments and the call arguments is correct.
				boolean arguments_correct = true;
				
				//The return type of the function , if the function returns an argument that we don't know it's type.
				String temp_return_type = "";
			
				//For all the call arguments.
				for(int i = 0; i < call_args.size(); i++)
				{
					//The type of the current argument.
					String current_type = "";
					
					//The column that the current call argument was found.
					int column = -1;
					
					//If the current call argument is a constant...
					if(call_args.get(i) instanceof AValueExpression)
					{
						//Cast it to AValueExpression object.
						AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
						
						//If this constant is a number...
						if(a_val_exp.getValue() instanceof ANumberValue)
						{
							ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
							AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
							TInteger t_int = a_number.getInteger();
							column = t_int.getPos();
							current_type = "INTEGER";
						}
						
						//Else if the constant's type is a string...
						else
						{
							AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
							TString t_string = a_string_value.getString();
							column = t_string.getPos();
							current_type = "STRING";
						}
						
						
						//If the call argument and the associated function argument does not match...
						if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
						{
							//If the function argument's type is not "ARGUMENT" , print error message.
							if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
							{
								arguments_correct = false;
								System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
							}
							
							//Else if the function argument's type is "ARGUMENT"
							else
							{
								//If the function's return variable is not null...
								if(!(function.getReturnVariable() == null))
								{
									//If the current call argument , matches the return variable of the function...
									if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
									{
										//...set the return type of the function , to this argument's type.
										temp_return_type = current_type;
									}
								}
							}
						}
					}
					
					//Else if the current call argument is a variable...
					else if(call_args.get(i) instanceof AIdExpression)
					{
						//Find the token that corresponds to this variable.
						AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
						AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
						TId t_id = a_identifier.getId();
						
						//If this variable is not in the symbol table , print error message.
						if(!(table.getVariables().containsKey(t_id.getText().trim())))
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if there is such a variable but is declared later in the program , print error message.
						else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if the variable already exists...
						else
						{
							//The current arguments type , will be the variable's type.
							current_type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the call argument and the associated function argument does not match...
							if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
							{
								//If the function argument's type is not "ARGUMENT" , print error message.
								if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
								}
								
								//Else if the function argument's type is "ARGUMENT"
								else
								{
									//If the function's return variable is not null...
									if(!(function.getReturnVariable() == null))
									{
										//If the current call argument , matches the return variable of the function...
										if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
										{
											//...set the return type of the function , to this argument's type.
											temp_return_type = current_type;
										}
									}
								}
							}
						}
			
						
					}
					
					
				}
				
				//If the arguments are passed correctly , it is time to look for the function's return type.
				if(arguments_correct)
				{
					//If the function does not return anything , print error message.
					if(function.getReturns().equals("VOID"))
					{
						System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not assign VOID to a variable ");
					}
					
					//Else if the function returns an integer...
					else if(function.getReturns().equals("INTEGER"))
					{
						//If the variable is already declared...
						if(table.getVariables().containsKey(variable.getName()))
						{
							//If the variable's type is not integer , print error message.
							if(!(table.getVariables().get(variable.getName()).getType().equals("INTEGER")))
							{
								if(!(table.getVariables().get(variable.getName()).getType().equals("ARGUMENT")))
								{
									System.out.println("\n\nerror "+(++errors)+": Incompatible types at line: "+variable.getLineFound()+ ". Expected "+ table.getVariables().get(variable.getName()).getType() +" but found " + temp_return_type);
								}
								
								else
								{
									table.getVariables().get(variable.getName()).setType("INTEGER");
								}							
							}
						}
						
						//Else if the variable is not declared...
						else
						{
							//Set's it's type to integer.
							variable.setType("INTEGER");
							
							//Add this variable to the symbol table.
							table.addVariable(variable.getName() , variable);
						}
					}
					
					//Else if the function returns an string...
					else if(function.getReturns().equals("STRING"))
					{
						//If the variable is already declared...
						if(table.getVariables().containsKey(variable.getName()))
						{
							//If the variable's type is not string , print error message.
							if(!(table.getVariables().get(variable.getName()).getType().equals("STRING")))
							{
								if(!(table.getVariables().get(variable.getName()).getType().equals("ARGUMENT")))
								{
									System.out.println("\n\nerror "+(++errors)+": Incompatible types at line: "+variable.getLineFound()+ ". Expected "+ table.getVariables().get(variable.getName()).getType() +" but found " + temp_return_type);
								}
								
								else
								{
									table.getVariables().get(variable.getName()).setType("STRING");
								}
							}
						}
						
						//Else if the variable is not declared...
						else
						{
							//Set's it's type to string.
							variable.setType("STRING");
							
							//Add this variable to the symbol table.
							table.addVariable(variable.getName() , variable);
						}
					}
					
					//Else if the return type is an argument.
					else if(function.getReturns().equals("ARGUMENT"))
					{
						//If the variable is already declared...
						if(table.getVariables().containsKey(variable.getName()))
						{
							//If the variable's type does not match the function's return type , print error message.
							if(!(table.getVariables().get(variable.getName()).getType().equals(temp_return_type)))
							{
								if(!(table.getVariables().get(variable.getName()).getType().equals("ARGUMENT")))
								{
									System.out.println("\n\nerror "+(++errors)+": Incompatible types at line: "+variable.getLineFound()+ ". Expected "+ table.getVariables().get(variable.getName()).getType() +" but found " + temp_return_type);
								}
								
								else
								{
									table.getVariables().get(variable.getName()).setType(temp_return_type);
								}
							}
						}
						
						//Else if the variable is not declared...
						else
						{
							//Set's the variable's type to the function's return type.
							variable.setType(temp_return_type);
							
							//Add this variable to the symbol table.
							table.addVariable(variable.getName() , variable);
						}
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
			for(int z = 0; z<terminals.size(); z++)
			{
				PExpression p_exp = terminals.get(z);
				
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
				
				//If the expression is a function call.
				else if(p_exp instanceof AFuncExpression)
				{
					//Cast the expression to a function call expression.
					AFuncExpression func_exp = (AFuncExpression)p_exp;
					AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
					TId func_id = a_id.getId();
					
					//The number of arguments in the function call.
					int args_number = 0;
					
					//Get the argument list size.
					LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
					
					//The arguments that we use to the function call.
					ArrayList<PExpression> call_args = new ArrayList<PExpression>();
					
					//For all the objects in the argument list.
					for(int i = 0; i<args.size(); i++)
					{
						AArgList a_args = (AArgList)args.get(i);
						
						//Increase the number of arguments.
						args_number++;
						
						//Add the first argument to the function call arguments list.
						call_args.add(a_args.getExpression());
						
						//Get all the rest arguments , if any.
						ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
						
						//Add the number of more_args to the number of total arguments.
						args_number += more_args.size();
						
						//For any of these more_args...
						for(int j = 0; j < more_args.size(); j++)
						{
							//Cast it to a AMoreListArguments object.
							AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
							
							//Add the argument to the function call arguments list.
							call_args.add(a_more_args.getExpression());
						}
					}
					
					//Create the function's key.
					String key = func_id.getText().trim()+"_"+args_number;
					
					//If there in no function matching this key , print error message.
					if(!table.getFunctions().containsKey(key))
					{
						System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
					}
					
					//Else if there is such a function...
					else
					{
						//Get this function.
						Function function = table.getFunctions().get(key);
						
						//This variable shows if the association between the function arguments and the call arguments is correct.
						boolean arguments_correct = true;
						
						//The return type of the function , if the function returns an argument that we don't know it's type.
						String temp_return_type = "";
					
						//For all the call arguments.
						for(int i = 0; i < call_args.size(); i++)
						{
							//The type of the current argument.
							String current_type = "";
							
							//The column that the current call argument was found.
							int column = -1;
							
							//If the current call argument is a constant...
							if(call_args.get(i) instanceof AValueExpression)
							{
								//Cast it to AValueExpression object.
								AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
								
								//If this constant is a number...
								if(a_val_exp.getValue() instanceof ANumberValue)
								{
									ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
									AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
									TInteger t_int = a_number.getInteger();
									column = t_int.getPos();
									current_type = "INTEGER";
								}
								
								//Else if the constant's type is a string...
								else
								{
									AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
									TString t_string = a_string_value.getString();
									column = t_string.getPos();
									current_type = "STRING";
								}
								
								
								//If the call argument and the associated function argument does not match...
								if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
								{
									//If the function argument's type is not "ARGUMENT" , print error message.
									if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
									{
										arguments_correct = false;
										System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
									}
									
									//Else if the function argument's type is "ARGUMENT"
									else
									{
										//If the function's return variable is not null...
										if(!(function.getReturnVariable() == null))
										{
											//If the current call argument , matches the return variable of the function...
											if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
											{
												//...set the return type of the function , to this argument's type.
												temp_return_type = current_type;
											}
										}
									}
								}
							}
							
							//Else if the current call argument is a variable...
							else if(call_args.get(i) instanceof AIdExpression)
							{
								//Find the token that corresponds to this variable.
								AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
								AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
								TId t_id = a_identifier.getId();
								
								//If this variable is not in the symbol table , print error message.
								if(!(table.getVariables().containsKey(t_id.getText().trim())))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if there is such a variable but is declared later in the program , print error message.
								else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if the variable already exists...
								else
								{
									//The current arguments type , will be the variable's type.
									current_type = table.getVariables().get(t_id.getText().trim()).getType();
								
									//If the call argument and the associated function argument does not match...
									if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
									{
										//If the function argument's type is not "ARGUMENT" , print error message.
										if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
										{
											arguments_correct = false;
											System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
										}
										
										//Else if the function argument's type is "ARGUMENT"
										else
										{
											//If the function's return variable is not null...
											if(!(function.getReturnVariable() == null))
											{
												//If the current call argument , matches the return variable of the function...
												if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
												{
													//...set the return type of the function , to this argument's type.
													temp_return_type = current_type;
												}
											}
										}
									}
								}
					
								
							}
							
							
						}
						
						//If the arguments are passed correctly , it is time to look for the function's return type.
						if(arguments_correct)
						{
							//If the function does not return anything , print error message.
							if(function.getReturns().equals("VOID"))
							{
								correct = false;
								System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not use VOID in an arithmetic expression");
							}
							
							//Else if the function returns an integer...
							else if(function.getReturns().equals("INTEGER"))
							{
								correct = true;
							}
							
							//Else if the function returns an string...
							else if(function.getReturns().equals("STRING"))
							{
								correct = false;
								System.out.println("\n\nerror "+(++errors)+": Error in line "+variable.getLineFound()+ ". Arithmetic expression can not contain string values!");
							}
							
							//Else if the return type is an argument.
							else if(function.getReturns().equals("ARGUMENT"))
							{
								//If the return type is String , print error message.
								if(temp_return_type.equals("STRING"))
								{
									correct = false;
									System.out.println("\n\nerror "+(++errors)+": Error in line "+variable.getLineFound()+ ". Arithmetic expression can not contain string values!");
								}
							}
						}
						
					}
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
	
	
	
	
	//Call a function
	@SuppressWarnings("unchecked")
	@Override
	public void inARule6Statement(ARule6Statement node)
	{
			//Cast the expression to a function call expression.
			AFunctionCall afunc_call = (AFunctionCall)node.getFunctionCall();
			
			//Get the identifier that the function call is assigned to.
			AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
			TId func_id = a_id.getId();
			
			//The number of arguments in the function call.
			int args_number = 0;
			
			//Get the argument list size.
			LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
			
			//The arguments that we use to the function call.
			ArrayList<PExpression> call_args = new ArrayList<PExpression>();
			
			//For all the objects in the argument list.
			for(int i = 0; i<args.size(); i++)
			{
				AArgList a_args = (AArgList)args.get(i);
				
				//Increase the number of arguments.
				args_number++;
				
				//Add the first argument to the function call arguments list.
				call_args.add(a_args.getExpression());
				
				//Get all the rest arguments , if any.
				ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
				
				//Add the number of more_args to the number of total arguments.
				args_number += more_args.size();
				
				//For any of these more_args...
				for(int j = 0; j < more_args.size(); j++)
				{
					//Cast it to a AMoreListArguments object.
					AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
					
					//Add the argument to the function call arguments list.
					call_args.add(a_more_args.getExpression());
				}
			}
			
			//Create the function's key.
			String key = func_id.getText().trim()+"_"+args_number;
			
			//If there in no function matching this key , print error message.
			if(!table.getFunctions().containsKey(key))
			{
				System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
			}
			
			//Else if there is such a function...
			else
			{
				//Get this function.
				Function function = table.getFunctions().get(key);
			
				//For all the call arguments.
				for(int i = 0; i < call_args.size(); i++)
				{
					//The type of the current argument.
					String current_type = "";
					
					//The column that the current call argument was found.
					int column = -1;
					
					//If the current call argument is a constant...
					if(call_args.get(i) instanceof AValueExpression)
					{
						//Cast it to AValueExpression object.
						AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
						
						//If this constant is a number...
						if(a_val_exp.getValue() instanceof ANumberValue)
						{
							ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
							AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
							TInteger t_int = a_number.getInteger();
							column = t_int.getPos();
							current_type = "INTEGER";
						}
						
						//Else if the constant's type is a string...
						else
						{
							AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
							TString t_string = a_string_value.getString();
							column = t_string.getPos();
							current_type = "STRING";
						}
						
						
						//If the call argument and the associated function argument does not match...
						if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
						{
							//If the function argument's type is not "ARGUMENT" , print error message.
							if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
							{
								System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
							}
							
						}
					}
					
					//Else if the current call argument is a variable...
					else if(call_args.get(i) instanceof AIdExpression)
					{
						//Find the token that corresponds to this variable.
						AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
						AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
						TId t_id = a_identifier.getId();
						
						//If this variable is not in the symbol table , print error message.
						if(!(table.getVariables().containsKey(t_id.getText().trim())))
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if there is such a variable but is declared later in the program , print error message.
						else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if the variable already exists...
						else
						{
							//The current arguments type , will be the variable's type.
							current_type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the call argument and the associated function argument does not match...
							if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
							{
								//If the function argument's type is not "ARGUMENT" , print error message.
								if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
								{
									System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
								}			
							}
						}
					}
				}
			}
	}
	
	
	
	
	//In an IF STATEMENT.
	@SuppressWarnings("unchecked")
	@Override
	public void inARule1Statement(ARule1Statement node)
	{
		//Get the comparison object.
		ARule1Comparison a_comp = (ARule1Comparison)node.getComparison();
		
		//Get the expressions that are right and left of the comparison operator.
		PExpression left_expression   = a_comp.getExp1();
		PExpression right_expression  = a_comp.getExp2();
		
		
		
		
		/*
		 * First , we will check the expression on the left.
		 */
		
		//If left_expression is a constant.
		if(left_expression instanceof AValueExpression)
		{
			//Cast it to a value expression.
			AValueExpression val_exp = (AValueExpression)left_expression;
			
			//If this value is not a number , then print error message.
			if(!(val_exp.getValue() instanceof ANumberValue))
			{
				AStringValue a_val = (AStringValue)val_exp.getValue();
				TString t_string = a_val.getString();
				System.out.println("\n\nerror "+(++errors)+": Error in line "+t_string.getLine()+ " , column "+ t_string.getPos() +". IF statement can not contain string values!");						
			}
		}
		
		//else if left_expression is a variable.
		else if(left_expression instanceof AIdExpression)
		{
			//Cast the IdExpression into token.
			AIdExpression a_id_exp = (AIdExpression)left_expression;
			AIdentifier a_id = (AIdentifier)a_id_exp.getIdentifier();
			TId t_id = a_id.getId();
			
			if(table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() < t_id.getLine())
			{
				if(table.getVariables().get(t_id.getText().trim()).getType().equals("STRING"))
				{
					System.out.println("\n\nerror "+(++errors)+": Error in line "+t_id.getLine()+ " , column "+ t_id.getPos() +". IF statement can not contain string values!");						
				}
			}
			
			else
			{
				System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
			}
		}
		
		//else if the left_expression is a function call.
		else if(left_expression instanceof AFuncExpression)
		{
			//Cast the expression to a function call expression.
			AFuncExpression func_exp = (AFuncExpression)left_expression;
			AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
			
			//Get the identifier that the function call is assigned to.
			AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
			TId func_id = a_id.getId();
			
			//The number of arguments in the function call.
			int args_number = 0;
			
			//Get the argument list size.
			LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
			
			//The arguments that we use to the function call.
			ArrayList<PExpression> call_args = new ArrayList<PExpression>();
			
			//For all the objects in the argument list.
			for(int i = 0; i<args.size(); i++)
			{
				AArgList a_args = (AArgList)args.get(i);
				
				//Increase the number of arguments.
				args_number++;
				
				//Add the first argument to the function call arguments list.
				call_args.add(a_args.getExpression());
				
				//Get all the rest arguments , if any.
				ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
				
				//Add the number of more_args to the number of total arguments.
				args_number += more_args.size();
				
				//For any of these more_args...
				for(int j = 0; j < more_args.size(); j++)
				{
					//Cast it to a AMoreListArguments object.
					AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
					
					//Add the argument to the function call arguments list.
					call_args.add(a_more_args.getExpression());
				}
			}
			
			//Create the function's key.
			String key = func_id.getText().trim()+"_"+args_number;
			
			//If there in no function matching this key , print error message.
			if(!table.getFunctions().containsKey(key))
			{
				System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
			}
			
			//Else if there is such a function...
			else
			{
				//Get this function.
				Function function = table.getFunctions().get(key);
				
				//This variable shows if the association between the function arguments and the call arguments is correct.
				boolean arguments_correct = true;
				
				//The return type of the function , if the function returns an argument that we don't know it's type.
				String temp_return_type = "";
			
				//For all the call arguments.
				for(int i = 0; i < call_args.size(); i++)
				{
					//The type of the current argument.
					String current_type = "";
					
					//The column that the current call argument was found.
					int column = -1;
					
					//If the current call argument is a constant...
					if(call_args.get(i) instanceof AValueExpression)
					{
						//Cast it to AValueExpression object.
						AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
						
						//If this constant is a number...
						if(a_val_exp.getValue() instanceof ANumberValue)
						{
							ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
							AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
							TInteger t_int = a_number.getInteger();
							column = t_int.getPos();
							current_type = "INTEGER";
						}
						
						//Else if the constant's type is a string...
						else
						{
							AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
							TString t_string = a_string_value.getString();
							column = t_string.getPos();
							current_type = "STRING";
						}
						
						
						//If the call argument and the associated function argument does not match...
						if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
						{
							//If the function argument's type is not "ARGUMENT" , print error message.
							if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
							{
								arguments_correct = false;
								System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
							}
							
							//Else if the function argument's type is "ARGUMENT"
							else
							{
								//If the function's return variable is not null...
								if(!(function.getReturnVariable() == null))
								{
									//If the current call argument , matches the return variable of the function...
									if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
									{
										//...set the return type of the function , to this argument's type.
										temp_return_type = current_type;
									}
								}
							}
						}
					}
					
					//Else if the current call argument is a variable...
					else if(call_args.get(i) instanceof AIdExpression)
					{
						//Find the token that corresponds to this variable.
						AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
						AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
						TId t_id = a_identifier.getId();
						
						//If this variable is not in the symbol table , print error message.
						if(!(table.getVariables().containsKey(t_id.getText().trim())))
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if there is such a variable but is declared later in the program , print error message.
						else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if the variable already exists...
						else
						{
							//The current arguments type , will be the variable's type.
							current_type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the call argument and the associated function argument does not match...
							if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
							{
								//If the function argument's type is not "ARGUMENT" , print error message.
								if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
								}
								
								//Else if the function argument's type is "ARGUMENT"
								else
								{
									//If the function's return variable is not null...
									if(!(function.getReturnVariable() == null))
									{
										//If the current call argument , matches the return variable of the function...
										if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
										{
											//...set the return type of the function , to this argument's type.
											temp_return_type = current_type;
										}
									}
								}
							}
						}
			
						
					}
					
					
				}
				
				//If the arguments are passed correctly , it is time to look for the function's return type.
				if(arguments_correct)
				{
					//If the function does not return anything , print error message.
					if(function.getReturns().equals("VOID"))
					{
						System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not use VOID in a comparison");
					}
					
					//Else if the function returns an integer...
					else if(function.getReturns().equals("INTEGER"))
					{
						//Do nothing.
					}
					
					//Else if the function returns an string...
					else if(function.getReturns().equals("STRING"))
					{
						System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". IF statement can not contain string values!");						
					}
					
					//Else if the return type is an argument.
					else if(function.getReturns().equals("ARGUMENT"))
					{
						if(temp_return_type.equals("STRING"))
						{
							System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". IF statement can not contain string values!");						
						}
					}
				}
				
			}
		}
		
		else if(left_expression instanceof AAddExpression || left_expression instanceof AMultExpression || left_expression instanceof AMinusExpression || left_expression instanceof AExpExpression)
		{
			//This stack will be used for the dfs algorithm , for all the nodes that are expressions.
			Stack<PExpression> non_terminals = new Stack<PExpression>();
			
			//In this list we are going to keep all the "leaf" nodes that are either variables , values , or function calls.
			ArrayList<PExpression> terminals = new ArrayList<PExpression>();
			
			//If the expression is an addition...
			if(left_expression instanceof AAddExpression)
			{
				//Cast Expression to an Addition Expression
				AAddExpression add_exp = (AAddExpression)left_expression;
				
				//Add it's children to the stack.
				non_terminals.push(add_exp.getRight());
				non_terminals.push(add_exp.getLeft());
			}
			
			//If the expression is a multiplication...
			else if(left_expression instanceof AMultExpression)
			{
				//Cast Expression to a Multiplication Expression
				AMultExpression mult_exp = (AMultExpression)left_expression;
				
				//Add it's children to the stack.
				non_terminals.push(mult_exp.getRight());
				non_terminals.push(mult_exp.getLeft());
			}
			
			//If the expression is a substraction...
			else if(left_expression instanceof AMinusExpression)
			{
				//Cast Expression to a Substraction Expression
				AMinusExpression minus_exp = (AMinusExpression)left_expression;
				
				//Add it's children to the stack.
				non_terminals.push(minus_exp.getRight());
				non_terminals.push(minus_exp.getLeft());
			}
			
			//If the expression is an expression with parentheses.
			else if(left_expression instanceof AExpExpression)
			{
				//Cast Expression to a Exp Expression
				AExpExpression par_exp = (AExpExpression)left_expression;
				
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
			
			
			//Look all terminal symbols.
			for(int z = 0; z<terminals.size(); z++)
			{
				PExpression p_exp = terminals.get(z);
				
				//If the current symbol is a simple value.
				if(p_exp instanceof AValueExpression)
				{
					//Cast it to a value expression.
					AValueExpression val_exp = (AValueExpression)p_exp;
					
					//If this value is not a number , then print error message.
					if(!(val_exp.getValue() instanceof ANumberValue))
					{
						AStringValue a_number_val = (AStringValue)val_exp.getValue();
						TString t_string = a_number_val.getString();
						System.out.println("\n\nerror "+(++errors)+": Error in line "+t_string.getLine()+" , column "+t_string.getPos()+ ". Arithmetics expression can not contain string values!");						
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
					}
					
					//Else if it has been declared...
					else
					{
						if(table.getVariables().get(t_id.getText().trim()).getLineFound() < t_id.getLine())
						{
							//Get the variable's type.
							String type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the type is STRING , print error message.
							if(type.equals("STRING")) 
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+t_id.getLine()+" , column "+t_id.getPos()+ ". Arithmetics expression can not contain string values!");						
							}
						}
						
						else
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
					}
				}
				
				//If the expression is a function call.
				else if(p_exp instanceof AFuncExpression)
				{
					//Cast the expression to a function call expression.
					AFuncExpression func_exp = (AFuncExpression)p_exp;
					AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
					TId func_id = a_id.getId();
					
					//The number of arguments in the function call.
					int args_number = 0;
					
					//Get the argument list size.
					LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
					
					//The arguments that we use to the function call.
					ArrayList<PExpression> call_args = new ArrayList<PExpression>();
					
					//For all the objects in the argument list.
					for(int i = 0; i<args.size(); i++)
					{
						AArgList a_args = (AArgList)args.get(i);
						
						//Increase the number of arguments.
						args_number++;
						
						//Add the first argument to the function call arguments list.
						call_args.add(a_args.getExpression());
						
						//Get all the rest arguments , if any.
						ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
						
						//Add the number of more_args to the number of total arguments.
						args_number += more_args.size();
						
						//For any of these more_args...
						for(int j = 0; j < more_args.size(); j++)
						{
							//Cast it to a AMoreListArguments object.
							AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
							
							//Add the argument to the function call arguments list.
							call_args.add(a_more_args.getExpression());
						}
					}
					
					//Create the function's key.
					String key = func_id.getText().trim()+"_"+args_number;
					
					//If there in no function matching this key , print error message.
					if(!table.getFunctions().containsKey(key))
					{
						System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
					}
					
					//Else if there is such a function...
					else
					{
						//Get this function.
						Function function = table.getFunctions().get(key);
						
						//This variable shows if the association between the function arguments and the call arguments is correct.
						boolean arguments_correct = true;
						
						//The return type of the function , if the function returns an argument that we don't know it's type.
						String temp_return_type = "";
					
						//For all the call arguments.
						for(int i = 0; i < call_args.size(); i++)
						{
							//The type of the current argument.
							String current_type = "";
							
							//The column that the current call argument was found.
							int column = -1;
							
							//If the current call argument is a constant...
							if(call_args.get(i) instanceof AValueExpression)
							{
								//Cast it to AValueExpression object.
								AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
								
								//If this constant is a number...
								if(a_val_exp.getValue() instanceof ANumberValue)
								{
									ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
									AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
									TInteger t_int = a_number.getInteger();
									column = t_int.getPos();
									current_type = "INTEGER";
								}
								
								//Else if the constant's type is a string...
								else
								{
									AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
									TString t_string = a_string_value.getString();
									column = t_string.getPos();
									current_type = "STRING";
								}
								
								
								//If the call argument and the associated function argument does not match...
								if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
								{
									//If the function argument's type is not "ARGUMENT" , print error message.
									if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
									{
										arguments_correct = false;
										System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
									}
									
									//Else if the function argument's type is "ARGUMENT"
									else
									{
										//If the function's return variable is not null...
										if(!(function.getReturnVariable() == null))
										{
											//If the current call argument , matches the return variable of the function...
											if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
											{
												//...set the return type of the function , to this argument's type.
												temp_return_type = current_type;
											}
										}
									}
								}
							}
							
							//Else if the current call argument is a variable...
							else if(call_args.get(i) instanceof AIdExpression)
							{
								//Find the token that corresponds to this variable.
								AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
								AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
								TId t_id = a_identifier.getId();
								
								//If this variable is not in the symbol table , print error message.
								if(!(table.getVariables().containsKey(t_id.getText().trim())))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if there is such a variable but is declared later in the program , print error message.
								else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if the variable already exists...
								else
								{
									//The current arguments type , will be the variable's type.
									current_type = table.getVariables().get(t_id.getText().trim()).getType();
								
									//If the call argument and the associated function argument does not match...
									if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
									{
										//If the function argument's type is not "ARGUMENT" , print error message.
										if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
										{
											arguments_correct = false;
											System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
										}
										
										//Else if the function argument's type is "ARGUMENT"
										else
										{
											//If the function's return variable is not null...
											if(!(function.getReturnVariable() == null))
											{
												//If the current call argument , matches the return variable of the function...
												if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
												{
													//...set the return type of the function , to this argument's type.
													temp_return_type = current_type;
												}
											}
										}
									}
								}
					
								
							}
							
							
						}
						
						//If the arguments are passed correctly , it is time to look for the function's return type.
						if(arguments_correct)
						{
							//If the function does not return anything , print error message.
							if(function.getReturns().equals("VOID"))
							{
								System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not use VOID in a comparison");
							}
							
							//Else if the function returns an integer...
							else if(function.getReturns().equals("INTEGER"))
							{
								//Do nothing.
							}
							
							//Else if the function returns an string...
							else if(function.getReturns().equals("STRING"))
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". Arithmetics expression can not contain string values!");						
							}
							
							//Else if the return type is an argument.
							else if(function.getReturns().equals("ARGUMENT"))
							{
								if(temp_return_type.equals("STRING"))
								{
									System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". Arithmetics expression can not contain string values!");						
								}
							}
						}
						
					}
				}
			}
			
		}
		
		
		
		
		/*
		 * Now , we will check the expression on the right.
		 */
		 
		//If right_expression is a constant.
		if(right_expression instanceof AValueExpression)
		{
			//Cast it to a value expression.
			AValueExpression val_exp = (AValueExpression)right_expression;
			
			//If this value is not a number , then print error message.
			if(!(val_exp.getValue() instanceof ANumberValue))
			{
				AStringValue a_val = (AStringValue)val_exp.getValue();
				TString t_string = a_val.getString();
				System.out.println("\n\nerror "+(++errors)+": Error in line "+t_string.getLine()+ " , column "+ t_string.getPos() +". IF statement can not contain string values!");						
			}
		}
		
		//else if right_expression is a variable.
		else if(right_expression instanceof AIdExpression)
		{
			//Cast the IdExpression into token.
			AIdExpression a_id_exp = (AIdExpression)right_expression;
			AIdentifier a_id = (AIdentifier)a_id_exp.getIdentifier();
			TId t_id = a_id.getId();
			
			if(table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() < t_id.getLine())
			{
				if(table.getVariables().get(t_id.getText().trim()).getType().equals("STRING"))
				{
					System.out.println("\n\nerror "+(++errors)+": Error in line "+t_id.getLine()+ " , column "+ t_id.getPos() +". IF statement can not contain string values!");						
				}
			}
			
			else
			{
				System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
			}
		}
		
		//else if the right_expression is a function call.
		else if(right_expression instanceof AFuncExpression)
		{
			//Cast the expression to a function call expression.
			AFuncExpression func_exp = (AFuncExpression)right_expression;
			AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
			
			//Get the identifier that the function call is assigned to.
			AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
			TId func_id = a_id.getId();
			
			//The number of arguments in the function call.
			int args_number = 0;
			
			//Get the argument list size.
			LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
			
			//The arguments that we use to the function call.
			ArrayList<PExpression> call_args = new ArrayList<PExpression>();
			
			//For all the objects in the argument list.
			for(int i = 0; i<args.size(); i++)
			{
				AArgList a_args = (AArgList)args.get(i);
				
				//Increase the number of arguments.
				args_number++;
				
				//Add the first argument to the function call arguments list.
				call_args.add(a_args.getExpression());
				
				//Get all the rest arguments , if any.
				ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
				
				//Add the number of more_args to the number of total arguments.
				args_number += more_args.size();
				
				//For any of these more_args...
				for(int j = 0; j < more_args.size(); j++)
				{
					//Cast it to a AMoreListArguments object.
					AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
					
					//Add the argument to the function call arguments list.
					call_args.add(a_more_args.getExpression());
				}
			}
			
			//Create the function's key.
			String key = func_id.getText().trim()+"_"+args_number;
			
			//If there in no function matching this key , print error message.
			if(!table.getFunctions().containsKey(key))
			{
				System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
			}
			
			//Else if there is such a function...
			else
			{
				//Get this function.
				Function function = table.getFunctions().get(key);
				
				//This variable shows if the association between the function arguments and the call arguments is correct.
				boolean arguments_correct = true;
				
				//The return type of the function , if the function returns an argument that we don't know it's type.
				String temp_return_type = "";
			
				//For all the call arguments.
				for(int i = 0; i < call_args.size(); i++)
				{
					//The type of the current argument.
					String current_type = "";
					
					//The column that the current call argument was found.
					int column = -1;
					
					//If the current call argument is a constant...
					if(call_args.get(i) instanceof AValueExpression)
					{
						//Cast it to AValueExpression object.
						AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
						
						//If this constant is a number...
						if(a_val_exp.getValue() instanceof ANumberValue)
						{
							ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
							AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
							TInteger t_int = a_number.getInteger();
							column = t_int.getPos();
							current_type = "INTEGER";
						}
						
						//Else if the constant's type is a string...
						else
						{
							AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
							TString t_string = a_string_value.getString();
							column = t_string.getPos();
							current_type = "STRING";
						}
						
						
						//If the call argument and the associated function argument does not match...
						if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
						{
							//If the function argument's type is not "ARGUMENT" , print error message.
							if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
							{
								arguments_correct = false;
								System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
							}
							
							//Else if the function argument's type is "ARGUMENT"
							else
							{
								//If the function's return variable is not null...
								if(!(function.getReturnVariable() == null))
								{
									//If the current call argument , matches the return variable of the function...
									if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
									{
										//...set the return type of the function , to this argument's type.
										temp_return_type = current_type;
									}
								}
							}
						}
					}
					
					//Else if the current call argument is a variable...
					else if(call_args.get(i) instanceof AIdExpression)
					{
						//Find the token that corresponds to this variable.
						AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
						AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
						TId t_id = a_identifier.getId();
						
						//If this variable is not in the symbol table , print error message.
						if(!(table.getVariables().containsKey(t_id.getText().trim())))
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if there is such a variable but is declared later in the program , print error message.
						else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
						{
							arguments_correct = false;
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
						
						//Else if the variable already exists...
						else
						{
							//The current arguments type , will be the variable's type.
							current_type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the call argument and the associated function argument does not match...
							if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
							{
								//If the function argument's type is not "ARGUMENT" , print error message.
								if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
								}
								
								//Else if the function argument's type is "ARGUMENT"
								else
								{
									//If the function's return variable is not null...
									if(!(function.getReturnVariable() == null))
									{
										//If the current call argument , matches the return variable of the function...
										if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
										{
											//...set the return type of the function , to this argument's type.
											temp_return_type = current_type;
										}
									}
								}
							}
						}
			
						
					}
					
					
				}
				
				//If the arguments are passed correctly , it is time to look for the function's return type.
				if(arguments_correct)
				{
					//If the function does not return anything , print error message.
					if(function.getReturns().equals("VOID"))
					{
						System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not use VOID in a comparison");
					}
					
					//Else if the function returns an integer...
					else if(function.getReturns().equals("INTEGER"))
					{
						//Do nothing.
					}
					
					//Else if the function returns an string...
					else if(function.getReturns().equals("STRING"))
					{
						System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". IF statement can not contain string values!");						
					}
					
					//Else if the return type is an argument.
					else if(function.getReturns().equals("ARGUMENT"))
					{
						if(temp_return_type.equals("STRING"))
						{
							System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". IF statement can not contain string values!");						
						}
					}
				}
				
			}
		}
		
		else if(right_expression instanceof AAddExpression || right_expression instanceof AMultExpression || right_expression instanceof AMinusExpression || right_expression instanceof AExpExpression)
		{
			//This stack will be used for the dfs algorithm , for all the nodes that are expressions.
			Stack<PExpression> non_terminals = new Stack<PExpression>();
			
			//In this list we are going to keep all the "leaf" nodes that are either variables , values , or function calls.
			ArrayList<PExpression> terminals = new ArrayList<PExpression>();
			
			//If the expression is an addition...
			if(right_expression instanceof AAddExpression)
			{
				//Cast Expression to an Addition Expression
				AAddExpression add_exp = (AAddExpression)right_expression;
				
				//Add it's children to the stack.
				non_terminals.push(add_exp.getRight());
				non_terminals.push(add_exp.getLeft());
			}
			
			//If the expression is a multiplication...
			else if(right_expression instanceof AMultExpression)
			{
				//Cast Expression to a Multiplication Expression
				AMultExpression mult_exp = (AMultExpression)right_expression;
				
				//Add it's children to the stack.
				non_terminals.push(mult_exp.getRight());
				non_terminals.push(mult_exp.getLeft());
			}
			
			//If the expression is a substraction...
			else if(right_expression instanceof AMinusExpression)
			{
				//Cast Expression to a Substraction Expression
				AMinusExpression minus_exp = (AMinusExpression)right_expression;
				
				//Add it's children to the stack.
				non_terminals.push(minus_exp.getRight());
				non_terminals.push(minus_exp.getLeft());
			}
			
			//If the expression is an expression with parentheses.
			else if(right_expression instanceof AExpExpression)
			{
				//Cast Expression to a Exp Expression
				AExpExpression par_exp = (AExpExpression)right_expression;
				
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
			
			
			//Look all terminal symbols.
			for(int z = 0; z<terminals.size(); z++)
			{
				PExpression p_exp = terminals.get(z);
				
				//If the current symbol is a simple value.
				if(p_exp instanceof AValueExpression)
				{
					//Cast it to a value expression.
					AValueExpression val_exp = (AValueExpression)p_exp;
					
					//If this value is not a number , then print error message.
					if(!(val_exp.getValue() instanceof ANumberValue))
					{
						AStringValue a_number_val = (AStringValue)val_exp.getValue();
						TString t_string = a_number_val.getString();
						System.out.println("\n\nerror "+(++errors)+": Error in line "+t_string.getLine()+" , column "+t_string.getPos()+ ". Arithmetics expression can not contain string values!");						
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
					}
					
					//Else if it has been declared...
					else
					{
						if(table.getVariables().get(t_id.getText().trim()).getLineFound() < t_id.getLine())
						{
							//Get the variable's type.
							String type = table.getVariables().get(t_id.getText().trim()).getType();
						
							//If the type is STRING , print error message.
							if(type.equals("STRING")) 
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+t_id.getLine()+" , column "+t_id.getPos()+ ". Arithmetics expression can not contain string values!");						
							}
						}
						
						else
						{
							System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
						}
					}
				}
				
				//If the expression is a function call.
				else if(p_exp instanceof AFuncExpression)
				{
					//Cast the expression to a function call expression.
					AFuncExpression func_exp = (AFuncExpression)p_exp;
					AFunctionCall afunc_call = (AFunctionCall)func_exp.getFunctionCall();
					
					//Get the identifier that the function call is assigned to.
					AIdentifier a_id = (AIdentifier)afunc_call.getIdentifier();
					TId func_id = a_id.getId();
					
					//The number of arguments in the function call.
					int args_number = 0;
					
					//Get the argument list size.
					LinkedList<PArgList> args = new LinkedList<PArgList>(afunc_call.getArgList());
					
					//The arguments that we use to the function call.
					ArrayList<PExpression> call_args = new ArrayList<PExpression>();
					
					//For all the objects in the argument list.
					for(int i = 0; i<args.size(); i++)
					{
						AArgList a_args = (AArgList)args.get(i);
						
						//Increase the number of arguments.
						args_number++;
						
						//Add the first argument to the function call arguments list.
						call_args.add(a_args.getExpression());
						
						//Get all the rest arguments , if any.
						ArrayList<PMoreListArguments> more_args = new ArrayList<PMoreListArguments>(a_args.getMoreListArguments());
						
						//Add the number of more_args to the number of total arguments.
						args_number += more_args.size();
						
						//For any of these more_args...
						for(int j = 0; j < more_args.size(); j++)
						{
							//Cast it to a AMoreListArguments object.
							AMoreListArguments a_more_args = (AMoreListArguments)more_args.get(j);
							
							//Add the argument to the function call arguments list.
							call_args.add(a_more_args.getExpression());
						}
					}
					
					//Create the function's key.
					String key = func_id.getText().trim()+"_"+args_number;
					
					//If there in no function matching this key , print error message.
					if(!table.getFunctions().containsKey(key))
					{
						System.out.println("\n\nerror "+(++errors)+": Function '"+func_id.getText()+"' in line:"+func_id.getLine()+" , in column:"+func_id.getPos()+" undefined");
					}
					
					//Else if there is such a function...
					else
					{
						//Get this function.
						Function function = table.getFunctions().get(key);
						
						//This variable shows if the association between the function arguments and the call arguments is correct.
						boolean arguments_correct = true;
						
						//The return type of the function , if the function returns an argument that we don't know it's type.
						String temp_return_type = "";
					
						//For all the call arguments.
						for(int i = 0; i < call_args.size(); i++)
						{
							//The type of the current argument.
							String current_type = "";
							
							//The column that the current call argument was found.
							int column = -1;
							
							//If the current call argument is a constant...
							if(call_args.get(i) instanceof AValueExpression)
							{
								//Cast it to AValueExpression object.
								AValueExpression a_val_exp = (AValueExpression)call_args.get(i);
								
								//If this constant is a number...
								if(a_val_exp.getValue() instanceof ANumberValue)
								{
									ANumberValue a_number_value = (ANumberValue)a_val_exp.getValue();
									AIntNumber a_number = (AIntNumber)a_number_value.getNumber();
									TInteger t_int = a_number.getInteger();
									column = t_int.getPos();
									current_type = "INTEGER";
								}
								
								//Else if the constant's type is a string...
								else
								{
									AStringValue a_string_value = (AStringValue)a_val_exp.getValue();
									TString t_string = a_string_value.getString();
									column = t_string.getPos();
									current_type = "STRING";
								}
								
								
								//If the call argument and the associated function argument does not match...
								if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
								{
									//If the function argument's type is not "ARGUMENT" , print error message.
									if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
									{
										arguments_correct = false;
										System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+column+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
									}
									
									//Else if the function argument's type is "ARGUMENT"
									else
									{
										//If the function's return variable is not null...
										if(!(function.getReturnVariable() == null))
										{
											//If the current call argument , matches the return variable of the function...
											if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
											{
												//...set the return type of the function , to this argument's type.
												temp_return_type = current_type;
											}
										}
									}
								}
							}
							
							//Else if the current call argument is a variable...
							else if(call_args.get(i) instanceof AIdExpression)
							{
								//Find the token that corresponds to this variable.
								AIdExpression a_id_exp = (AIdExpression)call_args.get(i);
								AIdentifier a_identifier = (AIdentifier)a_id_exp.getIdentifier();
								TId t_id = a_identifier.getId();
								
								//If this variable is not in the symbol table , print error message.
								if(!(table.getVariables().containsKey(t_id.getText().trim())))
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if there is such a variable but is declared later in the program , print error message.
								else if( table.getVariables().containsKey(t_id.getText().trim()) && table.getVariables().get(t_id.getText().trim()).getLineFound() > t_id.getLine() )
								{
									arguments_correct = false;
									System.out.println("\n\nerror "+(++errors)+": Variable '"+t_id.getText()+"' in line:"+t_id.getLine()+" , in column:"+t_id.getPos()+" has not been declared.");
								}
								
								//Else if the variable already exists...
								else
								{
									//The current arguments type , will be the variable's type.
									current_type = table.getVariables().get(t_id.getText().trim()).getType();
								
									//If the call argument and the associated function argument does not match...
									if(! ( function.getArguments().get(i).getType().equals(current_type) )) 
									{
										//If the function argument's type is not "ARGUMENT" , print error message.
										if(!(function.getArguments().get(i).getType().equals("ARGUMENT")))
										{
											arguments_correct = false;
											System.out.println("\n\nerror "+(++errors)+": Wrong argument type in line : " + func_id.getLine() + " , column : "+t_id.getPos()+". Expected " + function.getArguments().get(i).getType()+ " but found " + current_type);
										}
										
										//Else if the function argument's type is "ARGUMENT"
										else
										{
											//If the function's return variable is not null...
											if(!(function.getReturnVariable() == null))
											{
												//If the current call argument , matches the return variable of the function...
												if(function.getArguments().get(i).getName().equals(function.getReturnVariable().getName()))
												{
													//...set the return type of the function , to this argument's type.
													temp_return_type = current_type;
												}
											}
										}
									}
								}
					
								
							}
							
							
						}
						
						//If the arguments are passed correctly , it is time to look for the function's return type.
						if(arguments_correct)
						{
							//If the function does not return anything , print error message.
							if(function.getReturns().equals("VOID"))
							{
								System.out.println("\n\nerror "+(++errors)+": Wrong usage of function : '" + func_id.getText() +"' in line "+func_id.getLine() +". Can not use VOID in a comparison");
							}
							
							//Else if the function returns an integer...
							else if(function.getReturns().equals("INTEGER"))
							{
								//Do nothing.
							}
							
							//Else if the function returns an string...
							else if(function.getReturns().equals("STRING"))
							{
								System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". Arithmetics expression can not contain string values!");						
							}
							
							//Else if the return type is an argument.
							else if(function.getReturns().equals("ARGUMENT"))
							{
								if(temp_return_type.equals("STRING"))
								{
									System.out.println("\n\nerror "+(++errors)+": Error in line "+func_id.getLine()+ " , column "+ func_id.getPos() +". Arithmetics expression can not contain string values!");						
								}
							}
						}
						
					}
				}
			}
		}
		
	}
	
	
	
}
