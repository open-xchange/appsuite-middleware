package com.openexchange.ajax.infostore;

import junit.framework.TestCase;

import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.groupware.infostore.DocumentMetadata;

public class InfostoreParserTest extends TestCase {
	
	public void testParseObject() throws Exception{
		InfostoreParser parser = new InfostoreParser();
		
		DocumentMetadata dm = parser.getDocumentMetadata("{\"title\" : \"The title\", \"url\" : \"http://www.open-xchange.com\" , \"filename\" : \"test.txt\", \"file_mimetype\" :\"text/plain\", \"file_size\" : 12345, \"version\" : 1, \"description\" : \"Description\", \"file_md5sum\" : \"123n12b4askfa2\", \"folder_id\" : 23, \"categories\" : [\"toll\", \"fein\", \"hervorragend\", \"am tollsten\"]}");
		
		assertEquals("The title",dm.getTitle());
		assertEquals("http://www.open-xchange.com",dm.getURL());
		assertEquals("test.txt", dm.getFileName());
		assertEquals("text/plain", dm.getFileMIMEType());
		assertEquals(12345,dm.getFileSize());
		assertEquals(1,dm.getVersion());
		assertEquals("123n12b4askfa2",dm.getFileMD5Sum());
		assertEquals(23,dm.getFolderId());
		
		assertEquals("toll, fein, hervorragend, am tollsten", dm.getCategories());
		
		dm = parser.getDocumentMetadata("{\"title\" : \"The title\", \"url\" : \"http://www.open-xchange.com\" , \"filename\" : \"test.txt\", \"file_mimetype\" :\"text/plain\", \"file_size\" : 12345, \"description\" : \"Description\", \"file_md5sum\" : \"123n12b4askfa2\", \"folder_id\" : 23, \"categories\" : [\"toll\", \"fein\", \"hervorragend\", \"am tollsten\"]}");
		
		assertEquals(0,dm.getVersion());
		
	}
}
