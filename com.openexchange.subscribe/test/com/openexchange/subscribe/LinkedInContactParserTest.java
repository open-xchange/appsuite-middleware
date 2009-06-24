package com.openexchange.subscribe;

import java.io.IOException;
import java.net.MalformedURLException;

import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.parser.LinkedInContactParser;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class LinkedInContactParserTest extends TestCase {
	
	/*public void testGetLinkedInContacts() {
		LinkedInContactParser parser = new LinkedInContactParser();
		// Enter valid credentials for linkedin.com here
		String linkedInUser = "";
		String linkedInPassword = "";
		ContactObject[] contacts;
		try {
			contacts = parser.getLinkedInContactsForUser(linkedInUser, linkedInPassword);
			assertTrue("There should be at least one contact.", contacts.length >= 1);
			ContactObject firstContact = contacts[0];
			System.out.println("1st contact retrieved is : " + firstContact.getDisplayName());
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
