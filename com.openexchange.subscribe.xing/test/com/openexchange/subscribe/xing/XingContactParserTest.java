package com.openexchange.subscribe.xing;

import junit.framework.TestCase;
import com.openexchange.exceptions.StringComponent;
import com.openexchange.groupware.container.Contact;

public class XingContactParserTest extends TestCase {

    //TODO: Insert valid credentials for www.xing.com here
    protected String xingUser="karsten.will@gmx.de";
    protected String xingPassword ="P1lotXIN";
    
    //TODO: Insert INVALID credentials for www.xing.com here
    protected String invalidXingUser="login";
    protected String invalidXingPassword ="password";
    
    public void setUp() throws Exception {
        super.setUp();

        XingSubscriptionErrorMessage.EXCEPTIONS.setApplicationId("com.openexchange.subscribe.xing");
        XingSubscriptionErrorMessage.EXCEPTIONS.setComponent(new StringComponent("XING"));
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
    }
    
	public void testGetXingContacts() throws Exception {
		XingContactParser parser = new XingContactParser();
		Contact[] contacts = parser.getXingContactsForUser(xingUser, xingPassword);
        assertTrue("There should be at least one contact.", contacts.length >= 1);
        Contact firstContact = contacts[0];
        System.out.println("1st contact retrieved is : " + firstContact.getDisplayName());
        Contact lastContact = contacts[contacts.length-1];
        System.out.println("last contact retrieved is : " + lastContact.getDisplayName());
        System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
	}

	public void testInvalidCredentials() throws Exception {
        XingContactParser parser = new XingContactParser();
        try {
            parser.getXingContactsForUser(invalidXingUser, invalidXingPassword);
            fail("Exception expected");
        } catch (XingSubscriptionException e) {
            assertEquals("Wrong exception", XingSubscriptionErrorMessage.INVALID_LOGIN.getDetailNumber(), e.getDetailNumber());
        }
	}
}
