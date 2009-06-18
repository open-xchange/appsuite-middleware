package com.openexchange.subscribe;

import java.io.IOException;
import java.net.MalformedURLException;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.subscribe.parser.FacebookContactParser;
import com.openexchange.subscribe.parser.LinkedInContactParser;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */

public class FacebookContactParserTest extends TestCase{
  //TODO: Comment in and get it working, please
    
	/*public void testGetFacebookContacts() {
		FacebookContactParser parser = new FacebookContactParser();
		// Enter valid credentials for facebook.com here
		String facebookUser = "";
		String facebookPassword = "";
		try {
			ContactObject[] contacts = parser.getFacebookContactsForUser(facebookUser, facebookPassword);
			assertTrue("There should be at least one contact.", contacts.length >= 1);
			ContactObject firstContact = contacts[0];
			System.out.println("1st contact retrieved is : " + firstContact.getDisplayName());
			System.out.println("Mobile Phone Number : " + firstContact.getCellularTelephone1());
			ContactObject lastContact = contacts[contacts.length-1];
			System.out.println("last contact retrieved is : " + lastContact.getDisplayName());
			System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
}
