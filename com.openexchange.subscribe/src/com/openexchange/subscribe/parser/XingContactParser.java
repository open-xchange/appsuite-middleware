package com.openexchange.subscribe.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.tools.versit.converter.OXContainerConverter;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;



/**
 * This logs into the Xing business network at www.xing.com and retrieves the contacts for a given username and password as an
 * ox-compatible List of ContactObjects.
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
// TODO: Create a custom exception for changes to the Xing site that make this class unusable (XingWorkflowChangeException?)
// TODO: Make assertions that detect relevant changes to the Xing site and throw this exception
// TODO: Use logging instead of println
public class XingContactParser {
	
	public ContactObject[] getXingContactsForUser(String xingUser, String xingPassword) throws IOException, SAXException{
		
		// emulate a known client, hopefully keeping our profile low
		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
		// Javascript needs to be disabled as there are errors on the start page
		webClient.setJavaScriptEnabled(false);
		
		// 1st step - login page
	    final HtmlPage loginPage = webClient.getPage("https://www.xing.com");
	    //fill in the credentials and submit the login form
	    HtmlForm loginForm = loginPage.getFormByName("loginform");
	    HtmlTextInput userfield = loginForm.getInputByName("login_user_name");
	    userfield.setValueAttribute(xingUser);
	    HtmlPasswordInput passwordfield = loginForm.getInputByName("login_password");
	    passwordfield.setValueAttribute(xingPassword);
	    
	    // 2nd step - profile home page
	    final HtmlPage profileHomePage = (HtmlPage)loginForm.submit(null);
	    //System.out.println("*****" + profileHomePage.getTitleText());//should be "XING  -  Start"
	    HtmlAnchor linkToContacts = profileHomePage.getAnchorByHref("/app/contact");
	    
	    // 3rd step - first contacts page
	    final HtmlPage allContactsPage = linkToContacts.click();
	    //System.out.println("*****" + allContactsPage.getTitleText()); //should be "XING  -  Contacts"
	    List<HtmlAnchor> allLinks = allContactsPage.getAnchors();
	    Vector<ContactObject> contactObjects = getContactsFromVcardLinks(allLinks);
	    
	    // 4th step - further contacts pages
	    int offset = 10;
	    HtmlAnchor linkToNextContactsPage = getLinkToNextContactsPage(allLinks, offset);
	    while (linkToNextContactsPage != null) {
	    	HtmlPage tempNextContactsPage = linkToNextContactsPage.click();
	    	List<HtmlAnchor> tempAllLinks = tempNextContactsPage.getAnchors();
	    	Vector<ContactObject> tempNextContacts = getContactsFromVcardLinks(tempAllLinks);
	    	contactObjects.addAll(tempNextContacts);
	    	offset += 10;
	    	linkToNextContactsPage = getLinkToNextContactsPage(tempAllLinks, offset);
	    }
	    
	    webClient.closeAllWindows();
	    
	    ContactObject[] contactObjectsArray = new ContactObject[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
		return contactObjectsArray;
	}

	private Vector<ContactObject> getContactsFromVcardLinks (List<HtmlAnchor> allLinks) throws IOException{
		Vector<ContactObject> contactObjects = new Vector<ContactObject>();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
	    for (HtmlAnchor tempLink : allLinks){
	    	// there should be some vcard links here. If there are none something is probably wrong
	    	if (tempLink.getHrefAttribute().startsWith("/app/vcard")){
	    		//System.out.println("*****" +tempLink.getHrefAttribute());
	    		TextPage vcardPage = tempLink.click(); 
	    		byte[] vcard = vcardPage.getWebResponse().getContentAsBytes();
	    		//System.out.println(vcardPage.getContent());
	    		final VersitDefinition def = Versit.getDefinition("text/x-vcard");
	    		final VersitDefinition.Reader versitReader = def.getReader(new ByteArrayInputStream(vcard), "ISO-8859-1");
	    		try {
	    			VersitObject versitObject = def.parse(versitReader);
	    			ContactObject contactObject = oxContainerConverter.convertContact(versitObject);
	    			contactObjects.add(contactObject);
	    		} catch (final VersitException e){
	    			e.printStackTrace();
	    		} catch (ConverterException e) {
					e.printStackTrace();
				}
	    		
	    	}
	    }
	    return contactObjects;
	}
	
	private HtmlAnchor getLinkToNextContactsPage(List<HtmlAnchor> allLinks, int offset){
		for (HtmlAnchor tempLink : allLinks){
			if (tempLink.getHrefAttribute().startsWith("/app/contact?notags_filter=0;search_filter=;tags_filter=;offset=" + Integer.toString(offset))){
				return tempLink;
			}
		}
		return null;
	}
	
}
