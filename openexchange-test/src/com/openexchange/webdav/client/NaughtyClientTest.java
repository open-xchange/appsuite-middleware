package com.openexchange.webdav.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.xml.sax.SAXException;

import com.openexchange.webdav.WebdavClientTest;

public class NaughtyClientTest extends WebdavClientTest {


    public void testDummy() {
        
    }
    // Bug 7642
    // Defunct with HTTP Client 3+
    public void _testContentLengthTooLarge() throws Exception{
		contentLengthTest(20, 30);
	}

	// Bug 7642
	// This doesn't work, as the webserver faithfully closes the stream after receiving content-length bytes. 
	// In this case the file would be truncated to the claimed length.
	//public void testContentLengthTooSmall() throws Exception {
	//	contentLengthTest(20,10);
	//}
	
	public void contentLengthTest(int size, int pretendSize) throws MalformedURLException, IOException, SAXException, InterruptedException {
		byte[] data = new byte[size];
		Random r = new Random();
		for(int i = 0; i < data.length; i++) { data[i] = (byte) r.nextInt(); }
		
		String url = "http://"+hostname+"/servlet/webdav.infostore/Sebastian%20Kauss/testFile.bin";
		
		PutMethod put = new PutMethod(url);
		put.setRequestContentLength(pretendSize);
		put.setRequestBody(new ByteArrayInputStream(data));
		setAuth(put);
		
		HttpClient client = new HttpClient();
		
		try {
			client.executeMethod(put);
		} catch (IOException x) {
			// This exception is expected, because we don't provide all the data (or more) than we claim.
		}
		clean.add("/Sebastian Kauss/testFile.bin");
		
		// The invalid request mucks up synchronization between client and server, so the file is not
		// necessarily saved at this point (The stream is closed but server processing continues anyway.
		// We'll try to load it a few times, and see, if we can succeed.
		
		GetMethod get = new GetMethod(url);
		int i = 0;
		do {
			Thread.sleep(100);
			get =  new GetMethod(url);
			setAuth(get);
		
			client.executeMethod(get);
			i++;
		} while(i < 10 && get.getStatusCode() != 200);
		
		assertEquals(200, get.getStatusCode());
		assertEquals(String.valueOf(size), get.getResponseHeader("content-length").getValue());
		assertEqualContent(get.getResponseBodyAsStream(), new ByteArrayInputStream(data));

	}
}
