package com.openexchange.webdav.client;

import com.openexchange.webdav.WebdavClientTest;

public class SmokeTest extends WebdavClientTest {
	public void testClass1() throws Exception { //FIXME
		mkdir("test");
		clean.add("test");
		assertContent("", "test");
		
		mkdir("test/dir1");
		assertContent("test","dir1");
		
		save("test/dir1/test.txt", "Hallo Welt");
		assertContent("test/dir1","test.txt");
		assertBody("test/dir1/test.txt","Hallo Welt");
		
		save("test/dir1/test2.txt", "Abend Welt");
		assertContent("test/dir1","test.txt","test2.txt");
		assertBody("test/dir1/test2.txt", "Abend Welt");
		
		mkdir("test/dir1/dir");
		assertContent("test/dir1","test.txt", "test2.txt", "dir");
		
		save("test/dir1/dir/test.txt", "Tagchen Welt");
		assertContent("test/dir1/dir","test.txt");
		assertBody("test/dir1/dir/test.txt", "Tagchen Welt");
		
		cp("test/dir1", "test/dir2");
		assertContent("test/dir2","test.txt", "test2.txt", "dir");
		assertBody("test/dir2/dir/test.txt", "Tagchen Welt");
		assertBody("test/dir2/test2.txt", "Abend Welt");
		assertBody("test/dir2/test.txt","Hallo Welt");
		
		mv("test/dir2", "test/dir3");
		assertContent("test/dir3","test.txt", "test2.txt", "dir");
		assertBody("test/dir3/dir/test.txt", "Tagchen Welt");
		assertBody("test/dir3/test2.txt", "Abend Welt");
		assertBody("test/dir3/test.txt","Hallo Welt");
		
		assertContent("test", "dir1", "dir3");
	}
}
