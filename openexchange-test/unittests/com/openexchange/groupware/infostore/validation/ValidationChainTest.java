package com.openexchange.groupware.infostore.validation;

import junit.framework.TestCase;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.utils.Metadata;

public class ValidationChainTest extends TestCase{

	
	
	public void testValidate(){
		ValidationChain validators = new ValidationChain();
		
		validators.add(new TestValidation1());
		validators.add(new TestValidation2());
		validators.add(new TestValidation3());
		
		try {
			validators.validate(null);
			fail("No Exception thrown");
		} catch (InfostoreException x) {
			assertEquals("TestValidation2: (title) sucks\nTestValidation3: (title, description) stinks\n", (String)x.getMessageArgs()[0]);
		}
		
	}
	
	
	
	
	private static class TestValidation1 implements InfostoreValidator{

		public DocumentMetadataValidation validate(DocumentMetadata metadata) {
			return new DocumentMetadataValidation();
		}
		
		public String getName(){
			return "TestValidation1";
		}
		
	}
	
	private static class TestValidation2 implements InfostoreValidator{

		public DocumentMetadataValidation validate(DocumentMetadata metadata) {
			DocumentMetadataValidation validation = new DocumentMetadataValidation();
			validation.setError(Metadata.TITLE_LITERAL, "sucks");
			return validation;
		}
		
		public String getName(){
			return "TestValidation2";
		}
		
	}

	private static class TestValidation3 implements InfostoreValidator{

		public DocumentMetadataValidation validate(DocumentMetadata metadata) {
			DocumentMetadataValidation validation = new DocumentMetadataValidation();
			validation.setError(Metadata.TITLE_LITERAL, "stinks");
			validation.setError(Metadata.DESCRIPTION_LITERAL, "stinks");
			return validation;
		}
		
		public String getName(){
			return "TestValidation3";
		}
		
	}
}
