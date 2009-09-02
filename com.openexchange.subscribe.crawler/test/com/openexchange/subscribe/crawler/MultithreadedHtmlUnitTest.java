package com.openexchange.subscribe.crawler;

import java.util.ArrayList;
import java.util.Date;

import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.SubscriptionException;

import junit.framework.TestCase;

public class MultithreadedHtmlUnitTest extends TestCase {
	
	public static void testMultithreadedHtmlUnit () {
		Date dateBefore = new Date();
		// insert valid poweruser-credentials (preferrably 100+ connections) of your choice here
		String username ="";
		String password ="";
		
		ArrayList<Step> listOfSteps = new ArrayList<Step>();
        listOfSteps.add(new LoginPageStep(
            "Login to www.linkedin.com",
            "https://www.linkedin.com/secure/login",
            "",
            "",
            "login",
            "session_key",
            "session_password",
            "LinkedIn | Home", "https://www.linkedin.com"));
        listOfSteps.add(new PageByUrlStep(
            "Get to the contacts list", 
            "http://www.linkedin.com/connections?trk=hb_side_cnts"));
        listOfSteps.add(new PageByUrlStep(
            "Get to the no-javascript contacts list",
            "http://www.linkedin.com/connectionsnojs?trk=cnx_nojslink"));
        listOfSteps.add(new AnchorsByLinkRegexStep(
            "Get all pages that link to a connections profile",
            "(/connectionsnojs\\?split_page=).*",
            "(/profile\\?viewProfile=).*(goback).*"));
        listOfSteps.add(new ContactObjectsByHTMLAnchorsMultithreadedStep(
            "Extract the contact information from these pages",
            "/addressBookExport?exportMemberVCard",
            "http://media.linkedin.com/mpr/mpr/shrink_80_80"));
        
        Workflow linkedInWorkflow = new Workflow(listOfSteps);
        
        Contact[] contacts = new Contact[0];
		try {
			contacts = linkedInWorkflow.execute(username, password);
		} catch (SubscriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Date dateAfter = new Date();
		
		assertTrue("There should be some contacts", contacts.length != 0);
		for (Contact contact: contacts) {
			System.out.println("contact retrieved is : " + contact.getDisplayName());
			System.out.println("contacts first name : " + contact.getGivenName());
			System.out.println("contacts last name : " + contact.getSurName());
			System.out.println("contacts email address : " + contact.getEmail1());
			System.out.println("contacts note is : " + contact.getNote());
			
	        System.out.println("----------");
		}
        System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
        System.out.println("Started import at : " + dateBefore.toString());
        System.out.println("Finished import at : " + dateAfter.toString());
	}

}
