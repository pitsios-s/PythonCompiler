PythonCompiler
==============

A compiler for the language "Mini Python", created using sablecc and Java.

This project was developed for the purposes of the course 'Compilers' of cs department, Athens University of Economics and Business, during the Spring semester 2012 - 2013.

Our job was given the language's grammar in a BNF form, to build a grammar in a form that the sablecc programm can uderstand and then build some programms for Lexer, Syntax and Semantics check.

## Execution
* Step 1 : Open a command prompt and execute the grammar by typing ./sablecc MiniPythonGrammar.grammar(on Linux) or sablecc.bat MiniPythonGrammar.grammar(on Windows).

* Step 2 : Compile all Java programms by typing : javac *.java

* For Lexer test run : java LexerTest1 ./examples/lexer_syntax/c1.py. There are many programs to execute there beside c1.

* For simple syntax test type : java ParserTest1 ./examples/lexer_syntax/c1.py. The files that start with 'c' are correct, while the ones that start with 'r' are wrong.

* For syntax test that also generates the syntax tree, type : java ParserTest2 ./examples/lexer_syntax/c1.py.

* Finally, for semanctics test, type: java SemanticsTest ./examples/semantics/c1.py. Again, files that start with 'c' are correct, while the ones that start with 'r' should produce errors.


#### Note : The project was created using an older version of sablecc. If you try to execute it with the latest version, it won't run.

