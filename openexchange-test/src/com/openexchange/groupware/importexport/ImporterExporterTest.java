package com.openexchange.groupware.importexport;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.sessiond.SessionObject;

import junit.framework.JUnit4TestAdapter;


public class ImporterExporterTest {
	
	//workaround for JUnit 3 runner
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(SizedInputStreamTest.class);
	}
	
	private ImporterExporter impEx = null;
	private SessionObject sessObj = null;
	private Map<String, Integer> folders;
	private Set<ConversionPath> possibleImports;
	
	@BeforeClass
	public void init(){
		//TODO Tierlieb: get a session for testing purposes
		
		String beanPath = SystemConfig.getProperty("IMPORTEREXPORTER");
		XmlBeanFactory beanfactory = new XmlBeanFactory( new FileSystemResource( new File(beanPath) ) );
		impEx = ( ImporterExporter ) beanfactory.getBean("importerexporter");
	}
	
	@Test
	public void canImportData(){
		//writes possibleImports
		fail("Not implemented");
	}
	
	@Test 
	public void importData(){
		fail("Not implemented");
		
	}
}
