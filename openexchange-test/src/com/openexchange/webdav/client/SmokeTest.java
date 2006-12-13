package com.openexchange.webdav.client;

import com.openexchange.webdav.WebdavClientTest;

public class SmokeTest extends WebdavClientTest {
	public void testClass1() throws Exception { //FIXME
		mkdir("Sebastian%20Kauss/test");
		clean.add("Sebastian%20Kauss/test");
		assertContent("/Sebastian%20Kauss", "test");
		
		mkdir("Sebastian%20Kauss/test/dir1");
		assertContent("Sebastian%20Kauss/test","dir1");
		
		save("Sebastian%20Kauss/test/dir1Sebastian%20Kauss/test.txt", "Hallo Welt");
		assertContent("Sebastian%20Kauss/test/dir1","test.txt");
		assertBody("Sebastian%20Kauss/test/dir1Sebastian%20Kauss/test.txt","Hallo Welt");
		
		save("Sebastian%20Kauss/test/dir1Sebastian%20Kauss/test2.txt", "Abend Welt");
		assertContent("Sebastian%20Kauss/test/dir1","test.txt","test2.txt");
		assertBody("Sebastian%20Kauss/test/dir1Sebastian%20Kauss/test2.txt", "Abend Welt");
		
		mkdir("Sebastian%20Kauss/test/dir1/dir");
		assertContent("Sebastian%20Kauss/test/dir1","test.txt", "test2.txt", "dir");
		
		save("Sebastian%20Kauss/test/dir1/dirSebastian%20Kauss/test.txt", "Tagchen Welt");
		assertContent("Sebastian%20Kauss/test/dir1/dir","test.txt");
		assertBody("Sebastian%20Kauss/test/dir1/dirSebastian%20Kauss/test.txt", "Tagchen Welt");
		
		cp("Sebastian%20Kauss/test/dir1", "Sebastian%20Kauss/test/dir2");
		assertContent("Sebastian%20Kauss/test/dir2","test.txt", "test2.txt", "dir");
		assertBody("Sebastian%20Kauss/test/dir2/dirSebastian%20Kauss/test.txt", "Tagchen Welt");
		assertBody("Sebastian%20Kauss/test/dir2Sebastian%20Kauss/test2.txt", "Abend Welt");
		assertBody("Sebastian%20Kauss/test/dir2Sebastian%20Kauss/test.txt","Hallo Welt");
		
		mv("Sebastian%20Kauss/test/dir2", "Sebastian%20Kauss/test/dir3");
		assertContent("Sebastian%20Kauss/test/dir3","test.txt", "test2.txt", "dir");
		assertBody("Sebastian%20Kauss/test/dir3/dirSebastian%20Kauss/test.txt", "Tagchen Welt");
		assertBody("Sebastian%20Kauss/test/dir3Sebastian%20Kauss/test2.txt", "Abend Welt");
		assertBody("Sebastian%20Kauss/test/dir3Sebastian%20Kauss/test.txt","Hallo Welt");
		
		assertContent("Sebastian%20Kauss/test", "dir1", "dir3");
	}
}
