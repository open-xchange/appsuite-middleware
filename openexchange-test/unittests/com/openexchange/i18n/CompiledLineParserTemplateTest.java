package com.openexchange.i18n;

import junit.framework.TestCase;

import com.openexchange.i18n.tools.CompiledLineParserTemplate;
import com.openexchange.i18n.tools.StringTemplate;

public class CompiledLineParserTemplateTest extends TestCase {
	
	public void testBasic(){
		final CompiledLineParserTemplate template = new StringTemplate("Hello [title]!\n This is [location]!");
		final String output = template.render("title", "You", "location", "nice");
		assertEquals("Hello You!\n This is nice!",output);
	}


	public void testBeginsWithVar(){
		final String test = "[title] rocks!";
		final String expected  = "Template parsing rocks!";

		final CompiledLineParserTemplate template = new StringTemplate(test);
		final String output = template.render("title","Template parsing");
	
		assertEquals(expected, output);
	}
	
	public void testNull(){
		final String test = "[title] is unset";
		final String expected = " is unset";
		
		final CompiledLineParserTemplate template = new StringTemplate(test);
		final String output = template.render();
	
		assertEquals(expected, output);
	}
}

