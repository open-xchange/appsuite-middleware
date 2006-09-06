package com.openexchange.i18n;

import junit.framework.TestCase;

public class CompiledLineParserTemplateTest extends TestCase {
	
	public void testBasic(){
		CompiledLineParserTemplate template = new StringTemplate("Hello [name]!\n This is [something]!");
		String output = template.render("name", "You", "something", "nice");
		assertEquals("Hello You!\n This is nice!",output);
	}
	
	public void testEscape(){
		String test = "Variables in brackets are replaced. \\[variable\\] is replaced with [variable]." +
				" Backslashes are themselves escaped like this: \\\\\\\\";
		
		String expected  = "Variables in brackets are replaced. [variable] is replaced with value." +
		" Backslashes are themselves escaped like this: \\\\";
		
		CompiledLineParserTemplate template = new StringTemplate(test);
		String output = template.render("variable","value");
	
		assertEquals(expected, output);
	}
	
	public void testCompileError(){
		String test = " This bracket [ is never closed";
		String expected  = "Parser Error: Seems that the bracket opened on line 1 column 15 is never closed.";

		CompiledLineParserTemplate template = new StringTemplate(test);
		String output = template.render();
	
		assertEquals(expected, output);
	}
	
	public void testBeginsWithVar(){
		String test = "[this] rocks!";
		String expected  = "Template parsing rocks!";

		CompiledLineParserTemplate template = new StringTemplate(test);
		String output = template.render("this","Template parsing");
	
		assertEquals(expected, output);
	}
	
	public void testNull(){
		String test = "[this] is unset";
		String expected = " is unset";
		
		CompiledLineParserTemplate template = new StringTemplate(test);
		String output = template.render();
	
		assertEquals(expected, output);
	}
}

