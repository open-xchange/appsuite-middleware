package com.openexchange.webdav.client;

import com.openexchange.webdav.WebdavClientTest;

public class SmokeTest extends WebdavClientTest {
	public void testClass1() throws Exception { //FIXME
		mkdir("Sebastian Kauss/test");
		clean.add("Sebastian Kauss/test");
		assertContent("Sebastian Kauss", "test");
		
		mkdir("Sebastian Kauss/test/dir1");
		assertContent("Sebastian Kauss/test","dir1");
		
		save("Sebastian Kauss/test/dir1Sebastian Kauss/test.txt", "Hallo Welt");
		assertContent("Sebastian Kauss/test/dir1","test.txt");
		assertBody("Sebastian Kauss/test/dir1Sebastian Kauss/test.txt","Hallo Welt");
		
		save("Sebastian Kauss/test/dir1Sebastian Kauss/test2.txt", "Abend Welt");
		assertContent("Sebastian Kauss/test/dir1","test.txt","test2.txt");
		assertBody("Sebastian Kauss/test/dir1Sebastian Kauss/test2.txt", "Abend Welt");
		
		mkdir("Sebastian Kauss/test/dir1/dir");
		assertContent("Sebastian Kauss/test/dir1","test.txt", "test2.txt", "dir");
		
		save("Sebastian Kauss/test/dir1/dir/Sebastian Kauss/test.txt", "Tagchen Welt");
		assertContent("Sebastian Kauss/test/dir1/dir","test.txt");
		assertBody("Sebastian Kauss/test/dir1/dir/Sebastian Kauss/test.txt", "Tagchen Welt");
		
		cp("Sebastian Kauss/test/dir1", "Sebastian Kauss/test/dir2");
		assertContent("Sebastian Kauss/test/dir2","test.txt", "test2.txt", "dir");
		assertBody("Sebastian Kauss/test/dir2/dirSebastian Kauss/test.txt", "Tagchen Welt");
		assertBody("Sebastian Kauss/test/dir2Sebastian Kauss/test2.txt", "Abend Welt");
		assertBody("Sebastian Kauss/test/dir2Sebastian Kauss/test.txt","Hallo Welt");
		
		mv("Sebastian Kauss/test/dir2", "Sebastian Kauss/test/dir3");
		assertContent("Sebastian Kauss/test/dir3","test.txt", "test2.txt", "dir");
		assertBody("Sebastian Kauss/test/dir3/dirSebastian Kauss/test.txt", "Tagchen Welt");
		assertBody("Sebastian Kauss/test/dir3Sebastian Kauss/test2.txt", "Abend Welt");
		assertBody("Sebastian Kauss/test/dir3Sebastian Kauss/test.txt","Hallo Welt");
		
		assertContent("Sebastian Kauss/test", "dir1", "dir3");
	}
}
