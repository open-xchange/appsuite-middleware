package com.openexchange.groupware.infostore.validation;

import com.openexchange.exception.OXException;
import junit.framework.TestCase;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;

public class ValidationChainTest extends TestCase{



	public void testValidate(){
		final ValidationChain validators = new ValidationChain();

		validators.add(new TestValidation1());
		validators.add(new TestValidation2());
		validators.add(new TestValidation3());

		try {
			validators.validate(null);
			fail("No Exception thrown");
		} catch (final OXException x) {
			assertEquals("TestValidation2: (title) sucks\nTestValidation3: (title, description) stinks\n", (String)x.getDisplayArgs()[0]);
		}

	}




	private static class TestValidation1 implements InfostoreValidator{

		@Override
        public DocumentMetadataValidation validate(final DocumentMetadata metadata) {
			return new DocumentMetadataValidation();
		}

		@Override
        public String getName(){
			return "TestValidation1";
		}

	}

	private static class TestValidation2 implements InfostoreValidator{

		@Override
        public DocumentMetadataValidation validate(final DocumentMetadata metadata) {
			final DocumentMetadataValidation validation = new DocumentMetadataValidation();
			validation.setError(Metadata.TITLE_LITERAL, "sucks");
			return validation;
		}

		@Override
        public String getName(){
			return "TestValidation2";
		}

	}

	private static class TestValidation3 implements InfostoreValidator{

		@Override
        public DocumentMetadataValidation validate(final DocumentMetadata metadata) {
			final DocumentMetadataValidation validation = new DocumentMetadataValidation();
			validation.setError(Metadata.TITLE_LITERAL, "stinks");
			validation.setError(Metadata.DESCRIPTION_LITERAL, "stinks");
			return validation;
		}

		@Override
        public String getName(){
			return "TestValidation3";
		}

	}
}
