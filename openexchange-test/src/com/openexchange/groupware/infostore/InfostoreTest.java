package com.openexchange.groupware.infostore;
	

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

import junit.framework.TestCase;

public abstract class InfostoreTest extends TestCase {
	
	public static final String BEAN_FILE = "beans";
	public static final String USER = "user1.login";
	public static final String PASSWORD = "user1.password";
	public static final String CTX = "user1.ctx";
	public static final String ID = "user1.id";


	protected Properties fixtures;
	private XmlBeanFactory factory;
	private String bean_file;
	
	protected String userName;
	protected String password;
	protected int userCtx;
	protected int userId;

	public InfostoreTest(){
		fixtures = Init.getInfostoreProperties();
		initProperties();
	}
	
	public InfostoreTest(String filename) {
		fixtures = new Properties();
		FileInputStream fis = null;
		try {
			fixtures.load(fis = new FileInputStream(new File(filename)));
		} catch (IOException x) {
			x.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
				}
		}
		initProperties();
	}
	
	public void initProperties(){
		this.bean_file = fixtures.getProperty(BEAN_FILE);
		this.userName = fixtures.getProperty(USER);
		this.password = fixtures.getProperty(PASSWORD);
	}
	
	public Object getBean(String name){
		if(factory != null){
			factory.getBean(name);
		}
		factory = new XmlBeanFactory(new FileSystemResource(new File(bean_file)));
		return factory.getBean(name);
	}
	
	
	// Utility Methods for subclasses
	
	public static void assertEquals(DocumentMetadata expected, DocumentMetadata value){
		assertEquals(expected,value,0);
	}
	
	public static void assertEquals(DocumentMetadata expected, DocumentMetadata value, int timediff){
		Set<String> propNames1 = expected.getPropertyNames();
		Set<String> propNames2 = value.getPropertyNames();

		assertEquals(propNames1.size(), propNames2.size());

		for (String key1 : propNames1) {
			propNames1.remove(key1);
			assertTrue(propNames2.remove(key1));
			assertEquals(expected.getProperty(key1), value.getProperty(key1));
		}

		assertEquals(0, propNames1.size());
		assertEquals(0, propNames2.size());

		for(Metadata attr : Metadata.VALUES) {
			if(attr.equals(Metadata.SEQUENCE_NUMBER_LITERAL))
				continue;
			Object v1 = attr.getValue(expected);
			Object v2 = attr.getValue(value);
			if(v2 instanceof Date || v2 instanceof Date) {
				assertEquals((Date)v1, (Date)v2, timediff);
			} else {
				assertEquals(v1 , v2);
			}
		}
		
	}
	
	public static void assertEquals(Date d1, Date d2, long timediff) {
		int diff = (int) Math.abs(d1.getTime() - d2.getTime());
		assertTrue(diff <= timediff);
	}
	
	public UserData getUserData(String sessionId){
		return new SimpleUserData(sessionId, userCtx,userId,userName,password);
	}
	
	public InputStream getInputStream(String data){
		return new ByteArrayInputStream(data.getBytes());
	}
	
	public String getString(InputStream is) throws IOException{
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		try {
			String line = null;
			StringBuffer b = new StringBuffer();
			boolean first = true;
			while((line = r.readLine()) != null){
				if(first){
					first = false;
				} else {
					b.append("\n");
				}
				b.append(line);
			}
			return b.toString();
		} finally {
			if(r!=null)
				r.close();
		}
	}
}
