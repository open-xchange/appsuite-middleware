package com.openexchange.groupware.infostore.validation;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.validation.DocumentMetadataValidation;
import com.openexchange.groupware.infostore.validation.InvalidCharactersValidator;

import junit.framework.TestCase;

public class InfostoreInvalidCharactersCheckTest extends TestCase {
	
	public static class TestValidator extends InvalidCharactersValidator {
	
		List<String> strings = new ArrayList<String>();
		
		@Override
		public String check(String s) {
			strings.add(s);
			return s;
		}

	
	}
	
	public void testSwitcher(){
		DocumentMetadata metadata = new DocumentMetadataImpl();
		metadata.setCategories("categories");
		metadata.setDescription("description");
		metadata.setFileMD5Sum("sum");
		metadata.setFileMIMEType("mimetype");
		metadata.setFileName("name");
		metadata.setTitle("title");
		metadata.setURL("url");
		metadata.setVersionComment("comment");
		
		DocumentMetadataValidation validation = new TestValidator().validate(metadata);
		
		assertFalse(validation.isValid());
		
		Metadata[] fields = new Metadata[]{Metadata.CATEGORIES_LITERAL, Metadata.DESCRIPTION_LITERAL, Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_MIMETYPE_LITERAL, Metadata.FILENAME_LITERAL, Metadata.TITLE_LITERAL, Metadata.URL_LITERAL, Metadata.VERSION_COMMENT_LITERAL};

		GetSwitch get = new GetSwitch(metadata);
		for(Metadata m : fields) {
			assertTrue(validation.hasErrors(m));
			assertEquals(m.doSwitch(get), validation.getError(m));
		}
		
		
	}
}
