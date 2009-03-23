package com.openexchange.subscribe.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.TimeZone;
import java.util.Vector;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This logs into the LinkedIn business network at www.linkedin.com and retrieves the contacts for a given username and password as an
 * ox-compatible List of ContactObjects. Please note that there is not that much information available about every contact: 
 * First name, last name, a picture, an email address and current job are all that is available
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
//TODO: Create a custom exception for changes to the LinkedIn site that make this class unusable (WorkflowChangeException?)
//TODO: Make assertions that detect relevant changes to the LinkedIn site and throw this exception
//TODO: Use logging instead of println
public class LinkedInContactParser {

	public ContactObject[] getLinkedInContactsForUser (String linkedInUser, String linkedInPassword) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Vector<ContactObject> contactObjects = new Vector<ContactObject>();
		
		// emulate a known client, hopefully keeping our profile low
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		// Javascript needs to be disabled as there are errors on the start page
		webClient.setJavaScriptEnabled(false);
		
		// 1st step - login page
	    final HtmlPage loginPage = webClient.getPage("https://www.linkedin.com/secure/login");
	    //System.out.println("***** " + loginPage.getTitleText()); // Should be "LinkedIn: Sign In"
	    //fill in the credentials and submit the login form
	    HtmlForm loginForm = loginPage.getFormByName("login");
	    HtmlTextInput userfield = loginForm.getInputByName("session_key");
	    userfield.setValueAttribute(linkedInUser);
	    HtmlPasswordInput passwordfield = loginForm.getInputByName("session_password");
	    passwordfield.setValueAttribute(linkedInPassword);
	    
	    // 2nd step - profile home page
	    final HtmlPage profileHomePage = (HtmlPage) loginForm.submit(null);
	    //System.out.println("***** " + profileHomePage.getTitleText()); // Should be "LinkedIn: Home"
	    HtmlAnchor linkToContacts = profileHomePage.getAnchorByHref("/connections?trk=hb_side_cnts");
	    
	    // 3rd step - first contacts page
	    final HtmlPage allContactsPage = linkToContacts.click();
	    //System.out.println("***** " + allContactsPage.getTitleText()); // Should be "LinkedIn: My Contacts: Connections"
	    //System.out.println("***** " + allContactsPage.getWebResponse().getContentAsString());
	    HtmlAnchor linkToNoJsContacts = null;
	    for (HtmlAnchor link : allContactsPage.getAnchors()){
	    	if (link.getHrefAttribute().startsWith("/connectionsnojs"))
	    		linkToNoJsContacts = link;
	    }
	    
	    
	    // 4th step - no javascript contacts list
	    final HtmlPage noJsContactsPage = linkToNoJsContacts.click();
	    //System.out.println("***** " + noJsContactsPage.getTitleText()); // Should still be "LinkedIn: My Contacts: Connections"
	    
	    // 5th step - follow all links to profile pages
	    for (HtmlAnchor link : noJsContactsPage.getAnchors()){
	    	// only follow links to profiles but not my own one
	    	if (link.getHrefAttribute().startsWith("/profile?viewProfile") && !link.getHrefAttribute().endsWith("trk=tab_pro")){
	    		HtmlPage profilePage = link.click();
	    		// get the vcard of this contact
	    		HtmlAnchor vcardLink = null;
	    		String pictureUrl = "";
	    		for (HtmlAnchor link2 : profilePage.getAnchors()){
	    			if (link2.getHrefAttribute().startsWith("/addressBookExport?exportMemberVCard")){
	    				vcardLink = link2;
	    			}
	    		}
	    		if (profilePage.getWebResponse().getContentAsString().contains("http://media.linkedin.com/mpr/mpr/shrink_80_80")){
	    			int startIndex = profilePage.getWebResponse().getContentAsString().indexOf("http://media.linkedin.com/mpr/mpr/shrink_80_80");
	    			String substring = profilePage.getWebResponse().getContentAsString().substring(startIndex);
	    			pictureUrl = substring.substring(0, substring.indexOf("\""));
	    			//System.out.println("***** Picture URL : " + pictureUrl);
	    		}	
	    		//System.out.println(vcardLink.getHrefAttribute());
	    		ContactObject contact = getContactObjectForThisContact(profilePage, vcardLink, pictureUrl);
	    		contactObjects.add(contact);
	    		
	    	}
	    }
		
	    webClient.closeAllWindows();
		
	    ContactObject[] contactObjectsArray = new ContactObject[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
		return contactObjectsArray;
	}
	
	private ContactObject getContactObjectForThisContact(HtmlPage profilePage, HtmlAnchor vcardLink, String pictureUrl) throws IOException{
		ContactObject contactObject = new ContactObject();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
		// get the vcard object
		TextPage vcardPage = vcardLink.click(); 
		String encoding = "ISO-8859-1";
		String vcardString = vcardPage.getWebResponse().getContentAsString(encoding);
		// if there is a picture for this contact enter its url into the vcard
		if (!pictureUrl.equals("")){
			int indexEnd = vcardString.indexOf("END:VCARD");
			String textUntilEnd = vcardString.substring(0, indexEnd);
			vcardString = textUntilEnd + "PHOTO;VALUE=URI:" + pictureUrl + "\n" + "END:VCARD" + "\n";
		}
		byte[] vcard = vcardString.getBytes(encoding);
		//System.out.println(vcardPage.getContent());
		final VersitDefinition def = Versit.getDefinition("text/x-vcard");
		final VersitDefinition.Reader versitReader = def.getReader(new ByteArrayInputStream(vcard), encoding);
		try {
			VersitObject versitObject = def.parse(versitReader);
			// parse it into a contact object
			contactObject = oxContainerConverter.convertContact(versitObject);
		} catch (final VersitException e){
			e.printStackTrace();
		} catch (ConverterException e) {
			e.printStackTrace();
		}
		
		
		return contactObject;
	}
}
