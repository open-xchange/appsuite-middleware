package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.*;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public abstract class ActionTestCase extends TestCase {
	
	protected List<WebdavPath> clean = new LinkedList<WebdavPath>();
	
	protected WebdavFactory factory = null;
	
	protected WebdavPath testCollection = null;
	
	public void setUp() throws Exception {
		TestWebdavFactoryBuilder.setUp();
		factory = TestWebdavFactoryBuilder.buildFactory();
		factory.beginRequest();
		try {
			testCollection = new WebdavPath("testCollection"+System.currentTimeMillis());
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
			for(WebdavPath url : clean) {
				factory.resolveResource(url).delete();
			}
		} finally {
			factory.endRequest(200);
			TestWebdavFactoryBuilder.tearDown();
		}
	}
	
	public String getContent(WebdavPath url) throws WebdavException, IOException {
		WebdavResource res = factory.resolveResource(url);
		byte[] bytes = new byte[(int)(long)res.getLength()];
		InputStream in = res.getBody();
		in.read(bytes);
		return new String(bytes, "UTF-8");
	}
	
}
