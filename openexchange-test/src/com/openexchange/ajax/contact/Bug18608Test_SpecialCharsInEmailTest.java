package com.openexchange.ajax.contact;

import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.groupware.container.Contact;

public class Bug18608Test_SpecialCharsInEmailTest extends AbstractManagedContactTest {

	public Bug18608Test_SpecialCharsInEmailTest(String name) {
		super(name);
	}
	
	public void testUmlaut(){
		testEMail("california\u00fcberalles@host.invalid");
	}
	
	public void testHanCharacter(){
		testEMail("\u6279@somewhere.invalid");
	}
	
	private void testEMail(String email1){
		manager.setFailOnError(false);
		Contact c = generateContact();
		c.setEmail1(email1);
		c = manager.newAction(c);
		AbstractAJAXResponse lastResponse = manager.getLastResponse();
		assertTrue("We do bit allow special characters in e-mail addresses", lastResponse.hasError());
	}

}
