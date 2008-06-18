package com.openexchange.i18n;

import junit.framework.TestCase;

import com.openexchange.i18n.tools.CompiledLineParserTemplate;
import com.openexchange.i18n.tools.StringTemplate;

public class CompiledLineParserTemplateTest extends TestCase {
	
	public void testBasic(){
		final CompiledLineParserTemplate template = new StringTemplate("Hello [name]!\n This is [something]!");
		final String output = template.render("name", "You", "something", "nice");
		assertEquals("Hello You!\n This is nice!",output);
	}
	
	public void testEscape(){
		final String test = "Variables in brackets are replaced. \\[variable\\] is replaced with [variable]." +
				" Backslashes are themselves escaped like this: \\\\\\\\";
		
		final String expected  = "Variables in brackets are replaced. [variable] is replaced with value." +
		" Backslashes are themselves escaped like this: \\\\";
		
		final CompiledLineParserTemplate template = new StringTemplate(test);
		final String output = template.render("variable","value");
	
		assertEquals(expected, output);
	}
	
	public void testCompileError(){
		final String test = " This bracket [ is never closed";
		final String expected  = "Parser Error: Seems that the bracket opened on line 1 column 15 is never closed.";

		final CompiledLineParserTemplate template = new StringTemplate(test);
		final String output = template.render();
	
		assertEquals(expected, output);
	}
	
	public void testBeginsWithVar(){
		final String test = "[this] rocks!";
		final String expected  = "Template parsing rocks!";

		final CompiledLineParserTemplate template = new StringTemplate(test);
		final String output = template.render("this","Template parsing");
	
		assertEquals(expected, output);
	}
	
	public void testNull(){
		final String test = "[this] is unset";
		final String expected = " is unset";
		
		final CompiledLineParserTemplate template = new StringTemplate(test);
		final String output = template.render();
	
		assertEquals(expected, output);
	}
}

