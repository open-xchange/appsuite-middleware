package com.openexchange.tools.servlet;

import java.io.File;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.openexchange.test.TestInit;

import junit.framework.TestCase;

public class ServletConfigLoaderTest extends TestCase {
	public void testLoadGeneralConfig(){
		ServletConfigLoader loader = new ServletConfigLoader(new File(TestInit.getTestProperty("testServletConfigs")));
		ServletConfig config = loader.getConfig("com.openexchange.servlets.ImaginaryServlet");
		
		assertEquals("13", config.getInitParameter("cool.temperature"));
		assertEquals("Celsius", config.getInitParameter("cool.scale"));
		
		ServletContext context = loader.getContext("com.openexchange.servlets.ImaginaryServlet");
		
		assertEquals("13", context.getInitParameter("cool.temperature"));
		assertEquals("Celsius", context.getInitParameter("cool.scale"));	
	}
	
	public void testLoadSpecializedConfig() {
		ServletConfigLoader loader = new ServletConfigLoader(new File(TestInit.getTestProperty("testServletConfigs")));
		ServletConfig config = loader.getConfig("com.openexchange.servlets.OtherImaginaryServlet","imaginary/other");
		
		assertEquals("overridden", config.getInitParameter("overrideMe"));
		assertEquals("not overridden", config.getInitParameter("dontOverrideMe"));
		
		ServletContext context = loader.getContext("com.openexchange.servlets.OtherImaginaryServlet","imaginary/other");
		
		assertEquals("overridden", context.getInitParameter("overrideMe"));
		assertEquals("not overridden", context.getInitParameter("dontOverrideMe"));
		
		config = loader.getConfig("com.openexchange.servlets.OtherImaginaryServlet","imaginary/other2");
		
		assertEquals("overridden2", config.getInitParameter("overrideMe"));
		assertEquals("not overridden", config.getInitParameter("dontOverrideMe"));
		
		context = loader.getContext("com.openexchange.servlets.OtherImaginaryServlet","imaginary/other2");
		
		assertEquals("overridden2", context.getInitParameter("overrideMe"));
		assertEquals("not overridden", context.getInitParameter("dontOverrideMe"));
		
	}
	
	public void testLoadSpecializedOnly() {
		ServletConfigLoader loader = new ServletConfigLoader(new File(TestInit.getTestProperty("testServletConfigs")));
		ServletConfig config = loader.getConfig("com.openexchange.servlets.NotConfiguredServlet","imaginary/other");
		
		assertEquals("overridden", config.getInitParameter("overrideMe"));
		assertNull(config.getInitParameter("dontOverrideMe"));
		
	}
	
	public void testLoadSpecializedIgnoreWildcards(){
		ServletConfigLoader loader = new ServletConfigLoader(new File(TestInit.getTestProperty("testServletConfigs")));
		ServletConfig config = loader.getConfig("com.openexchange.servlets.OtherImaginaryServlet","imaginary/other*");
		
		assertEquals("overridden", config.getInitParameter("overrideMe"));
		assertEquals("not overridden", config.getInitParameter("dontOverrideMe"));
	}
	
	public void testOnlySpecialized(){
		ServletConfigLoader loader = new ServletConfigLoader(new File(TestInit.getTestProperty("testServletConfigs")));
		ServletConfig config = loader.getConfig("com.openexchange.servlets.NotConfiguredServlet","imaginary/otherest");
		
		assertEquals("13", config.getInitParameter("cool.temperature"));
		assertEquals("Celsius", config.getInitParameter("cool.scale"));
	}
	
	public void testDefault() {
		ServletConfigLoader loader = new ServletConfigLoader(new File(TestInit.getTestProperty("testServletConfigs")));
		ServletConfigWrapper config = new ServletConfigWrapper();
		ServletContextWrapper context = new ServletContextWrapper(config);
		config.setServletContextWrapper(context);
		
		loader.setDefaultConfig(config);
		loader.setDefaultContext(context);
		
		ServletConfig configLookup = loader.getConfig("com.openexchange.servlets.UnconfiguredServlet");
		ServletContext contextLookup = loader.getContext("com.openexchange.servlets.UnconfiguredServlet");
		
		assertEquals(config, configLookup);
		assertEquals(context, contextLookup);
		
		configLookup = loader.getConfig("com.openexchange.servlets.UnconfiguredServlet", "some/path");
		contextLookup = loader.getContext("com.openexchange.servlets.UnconfiguredServlet", "some/path");
		
		assertEquals(config, configLookup);
		assertEquals(context, contextLookup);
	}
}
