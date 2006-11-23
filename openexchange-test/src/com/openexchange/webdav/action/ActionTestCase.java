package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import com.openexchange.webdav.protocol.CollectionTest;
import com.openexchange.webdav.protocol.TestWebdavFactoryBuilder;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class ActionTestCase extends TestCase {
	
	protected List<String> clean = new LinkedList<String>();
	
	protected WebdavFactory factory = null;
	
	protected String testCollection = null;
	
	public void setUp() throws Exception {
		TestWebdavFactoryBuilder.setUp();
		factory = TestWebdavFactoryBuilder.buildFactory();
		factory.beginRequest();
		try {
			testCollection = "/testCollection"+System.currentTimeMillis();
			WebdavCollection coll = factory.resolveCollection(testCollection);
			coll.create();
			clean.add(coll.getUrl());
		
			CollectionTest.createStructure(coll, factory);
		} catch (Exception x) {
			tearDown();
			throw x;
		}
	}
	
	public void tearDown() throws Exception {
		try {
			for(String url : clean) {
				factory.resolveResource(url).delete();
			}
		} finally {
			factory.endRequest(200);
			TestWebdavFactoryBuilder.tearDown();
		}
	}
	
	public String getContent(String url) throws WebdavException, IOException {
		WebdavResource res = factory.resolveResource(url);
		byte[] bytes = new byte[(int)(long)res.getLength()];
		InputStream in = res.getBody();
		in.read(bytes);
		return new String(bytes, "UTF-8");
	}
	
}
