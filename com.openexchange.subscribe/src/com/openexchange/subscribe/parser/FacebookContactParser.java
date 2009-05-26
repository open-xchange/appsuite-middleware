package com.openexchange.subscribe.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * This logs into the Facebook network at www.facebook.com and retrieves the contacts for a given username and password as an
 * ox-compatible List of ContactObjects. Please note that there is not that much data in each contact: 
 * Displayname, a picture and a mobile phone number are all that is currently available
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
//TODO: Create a custom exception for changes to the Facebook site that make this class unusable (WorkflowChangeException?)
//TODO: Make assertions that detect relevant changes to the Facebook site and throw this exception
//TODO: Use logging instead of println
public class FacebookContactParser {

	/*public ContactObject[] getFacebookContactsForUser(String facebookUser, String facebookPassword) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		Vector<ContactObject> contactObjects = new Vector<ContactObject>();
		
		// emulate a known client, hopefully keeping our profile low
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		// Javascript needs to be disabled as there are errors on the start page
		webClient.setJavaScriptEnabled(false);
		
		// 1st step - login page
	    final HtmlPage loginPage = webClient.getPage("https://www.facebook.com/");
	    //System.out.println("***** " + loginPage.getTitleText()); // Should be "Willkommen bei Facebook! | Facebook"
	    //fill in the credentials and submit the login form
	    HtmlForm loginForm = loginPage.getFormByName("menubar_login");
	    HtmlTextInput userfield = loginForm.getInputByName("email");
	    userfield.setValueAttribute(facebookUser);
	    HtmlPasswordInput passwordfield = loginForm.getInputByName("pass");
	    passwordfield.setValueAttribute(facebookPassword);
	    
	    // 2nd step - profile home page
	    final HtmlPage profileHomePage = (HtmlPage) loginForm.submit(null);
	    //System.out.println("***** " + profileHomePage.getTitleText()); // Should be "Facebook | Home"
	    HtmlAnchor linkToContacts = profileHomePage.getAnchorByHref("http://www.facebook.com/friends/?ref=tn");
	    
	    // 3rd step - all contact page
	    final HtmlPage allContactsPage = linkToContacts.click();
	    //System.out.println("***** " + allContactsPage.getTitleText()); // Should be "Facebook | All Friends"
	    
	    String string = allContactsPage.getWebResponse().getContentAsString();
	    String profileId="";
	    Pattern pattern = Pattern.compile("Friends.friendClick\\(this, event, [0-9]*\\)");
        Matcher matcher = pattern.matcher(string);
    
        // Find all matches
        while (matcher.find()) {
            // Get the matching string
            String match = matcher.group();
            //System.out.println("***** Match : " + match);
            match = match.split("Friends.friendClick\\(this, event, ")[1];
            profileId = match.split("\\)")[0];
            //System.out.println("***** ProfileId : " + profileId);
            
            //4th step - a single contact page
		    HtmlPage contactPage = webClient.getPage("http://www.facebook.com/profile.php?id=" + profileId);
		    //System.out.println("***** " + contactPage.getTitleText()); // Should be "Facebook | NAME_OF_CONTACT"
		    
		    contactObjects.add(getContactObjectFromPage(contactPage.getWebResponse().getContentAsString()));
        }	    
	    
	    
		
	    webClient.closeAllWindows();
	    
		ContactObject[] contactObjectsArray = new ContactObject[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
		return contactObjectsArray;
	}
	
	private ContactObject getContactObjectFromPage(String page) throws IOException {
		String pictureUrl = getUniqueAttribute(page, "<a href=\"/album.php\\?profile\\&amp;id=[0-9]*\"><img src=\"", "\"");
		//System.out.println("***** picture URL " + pictureUrl);
		ContactObject contact = getContactObjectWithPicture(pictureUrl);
		
		contact.setDisplayName(getUniqueAttribute(page, "<h1 id=\"profile_name\">", "</h1>"));
		contact.setCellularTelephone1(getUniqueAttribute(page, "<dt>Mobile:</dt><dd>", "</dd>"));
		
		return contact;
	}
	
	private String getUniqueAttribute(String page, String start, String end){
		String returnString = "";
		
		Pattern pattern = Pattern.compile(start + "[0-9a-zA-Z+ŠšŸ\\s/:\\._]*" + end);
        Matcher matcher = pattern.matcher(page);
    
        if (matcher.find()) {
            // Get the matching string
            String match = matcher.group();
            //System.out.println("***** Match : " + match);
            match = match.split(start)[1];
            returnString = match.split(end)[0];
        }
		
		return returnString;
	}
	
	private ContactObject getContactObjectWithPicture(String pictureUrl) throws IOException{
		ContactObject contactObject = new ContactObject();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
		
		//create the vcard string
		String vcardString = "BEGIN:VCARD\nVERSION:2.1\nPHOTO;VALUE=URI:" + pictureUrl + "\nEND:VCARD\n";
		
		//System.out.println(vcardString);
		byte[] vcard = vcardString.getBytes();
		//System.out.println(vcardPage.getContent());
		final VersitDefinition def = Versit.getDefinition("text/x-vcard");
		final VersitDefinition.Reader versitReader = def.getReader(new ByteArrayInputStream(vcard), "ISO-8859-1");
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
	}*/
	
}
