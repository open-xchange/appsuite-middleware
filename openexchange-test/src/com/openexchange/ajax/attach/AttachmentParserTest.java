package com.openexchange.ajax.attach;

import org.json.JSONException;

import com.openexchange.ajax.parser.AttachmentParser;
import com.openexchange.ajax.parser.AttachmentParser.UnknownColumnException;
import com.openexchange.groupware.attach.AttachmentMetadata;

import junit.framework.TestCase;

public class AttachmentParserTest extends TestCase {

	public void testParse() throws JSONException{
		AttachmentParser parser = new AttachmentParser();
		
		AttachmentMetadata attachment = parser.getAttachmentMetadata("{ \"filename\" : \"test.txt\", \"file_mimetype\" :\"text/plain\", \"file_size\" : 12345 , \"folder\" : 23, \"id\" : 24, \"module\" : 25, \"attached\" : 26, \"created_by\" : 27, \"creation_date\":230023 }");
		
		assertEquals("test.txt",attachment.getFilename());
		assertEquals("text/plain", attachment.getFileMIMEType());
		assertEquals(12345,attachment.getFilesize());
		assertEquals(23, attachment.getFolderId());
		assertEquals(24, attachment.getId());
		assertEquals(25, attachment.getModuleId());
		assertEquals(26, attachment.getAttachedId());
		assertEquals(27, attachment.getCreatedBy());
		assertEquals(230023, attachment.getCreationDate().getTime());
	}
	
	public void testNullColumns() throws UnknownColumnException {
		AttachmentParser parser = new AttachmentParser();
		assertEquals(null, parser.getColumns(null));
	}
}
