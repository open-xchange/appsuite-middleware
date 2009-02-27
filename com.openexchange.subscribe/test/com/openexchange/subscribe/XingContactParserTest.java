package com.openexchange.subscribe;

import java.io.IOException;

import org.xml.sax.SAXException;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.subscribe.parser.XingContactParser;

import junit.framework.TestCase;

public class XingContactParserTest extends TestCase{
	
	public void testGetXingContacts(){
		XingContactParser parser = new XingContactParser();
		//TODO: Insert valid credentials for www.xing.com here
		String xingUser="";
		String xingPassword ="";
		try {
			ContactObject[] contacts = parser.getXingContactsForUser(xingUser, xingPassword);
			assertTrue("There should be at least one contact.", contacts.length >= 1);
			ContactObject firstContact = contacts[0];
			System.out.println("1st contact retrieved is : " + firstContact.getDisplayName());
			ContactObject lastContact = contacts[contacts.length-1];
			System.out.println("last contact retrieved is : " + lastContact.getDisplayName());
			System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}

}
