package com.openexchange.subscribe.xing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.openexchange.groupware.container.Contact;
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
    
    private static final ContactSanitizer SANITIZER = new ContactSanitizer();

    private String XING_WEBSITE = "https://www.xing.com";
    
    private String CONTACT_PAGE = "/app/contact?notags_filter=0;card_mode=0;search_filter=;tags_filter=";
    
    private String OFFSET = ";offset=";
    
    private String LOGOUT_PAGE = "/app/user?op=logout";
	
	public Contact[] getXingContactsForUser(String xingUser, String xingPassword) throws XingSubscriptionException {
	    Vector<Contact> contactObjects = new Vector<Contact>();
	    
	    try {
    		// emulate a known client, hopefully keeping our profile low
    		final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_2);
    		// Javascript needs to be disabled as there are errors on the start page
    		webClient.setJavaScriptEnabled(false);
    		
    		// 1st step - login page
    	    final HtmlPage loginPage = webClient.getPage(XING_WEBSITE);
    	    //fill in the credentials and submit the login form
    	    HtmlForm loginForm = loginPage.getFormByName("loginform");
    	    HtmlTextInput userfield = loginForm.getInputByName("login_user_name");
    	    userfield.setValueAttribute(xingUser);
    	    HtmlPasswordInput passwordfield = loginForm.getInputByName("login_password");
    	    passwordfield.setValueAttribute(xingPassword);
    	    
    	    // 2nd step - profile home page
    	    final HtmlPage profileHomePage = (HtmlPage)loginForm.submit(null);
    	    //System.out.println("*****" + profileHomePage.getTitleText());//should be "XING  -  Start"
    	    
    	    // wrong password? 
    	    List<?> errors = profileHomePage.getByXPath("//p[@class='error-message-top']");
    	    if(errors.size() != 0) {
    	        throw XingSubscriptionErrorMessage.INVALID_LOGIN.create();
    	    }
    	    
    	    // Force jump to list standard list view
    	    HtmlPage allContactsPage = webClient.getPage(XING_WEBSITE + CONTACT_PAGE + OFFSET + "0");
    	    HtmlPage currentPage = allContactsPage;
    	    //HtmlAnchor linkToContacts = profileHomePage.getAnchorByHref("/app/contact");
    	    
    	    // 3rd step - first contacts page
    	    //final HtmlPage allContactsPage = linkToContacts.click();
    	    //System.out.println("*****" + allContactsPage.getTitleText()); //should be "XING  -  Contacts"
    	    List<HtmlAnchor> allLinks = allContactsPage.getAnchors();
    	    contactObjects.addAll(getContactsFromVcardLinks(allLinks));
    	    
    	    // 4th step - further contacts pages
    	    int offset = 10;
    	    HtmlAnchor linkToNextContactsPage = getLinkToNextContactsPage(allLinks, offset);
    	    HtmlPage tempNextContactsPage = null;
    	    while (linkToNextContactsPage != null) {
    	    	tempNextContactsPage = linkToNextContactsPage.click();
    	    	currentPage = tempNextContactsPage;
    	    	List<HtmlAnchor> tempAllLinks = tempNextContactsPage.getAnchors();
    	    	Vector<Contact> tempNextContacts = getContactsFromVcardLinks(tempAllLinks);
    	    	contactObjects.addAll(tempNextContacts);
    	    	offset += 10;
    	    	linkToNextContactsPage = getLinkToNextContactsPage(tempAllLinks, offset);
    	    }
    	    
    	    // 5th step - logout
    	    HtmlAnchor logout = currentPage.getAnchorByHref(LOGOUT_PAGE);
    	    logout.click();
    	    webClient.closeAllWindows();
	    } catch (FailingHttpStatusCodeException e) {
            throw XingSubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (IOException e) {
            throw XingSubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        }
	    
	    Contact[] contactObjectsArray = new Contact[contactObjects.size()];
	    for (int i=0; i<contactObjectsArray.length && i< contactObjects.size(); i++){
	    	contactObjectsArray[i] = contactObjects.get(i);
	    }
		return contactObjectsArray;
	}

	private Vector<Contact> getContactsFromVcardLinks (List<HtmlAnchor> allLinks) throws IOException{
		Vector<Contact> contactObjects = new Vector<Contact>();
		final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
	    for (HtmlAnchor tempLink : allLinks){
	    	// there should be some vcard links here. If there are none something is probably wrong
	    	if (tempLink.getHrefAttribute().startsWith("/app/vcard")){
	    		//System.out.println("*****" +tempLink.getHrefAttribute());
	    		Page vcardPage = tempLink.click(); 
	    		byte[] vcard = vcardPage.getWebResponse().getContentAsBytes();
	    		//System.out.println(vcardPage.getContent());
	    		final VersitDefinition def = Versit.getDefinition("text/x-vcard");
	    		final VersitDefinition.Reader versitReader = def.getReader(new ByteArrayInputStream(vcard), "ISO-8859-1");
	    		try {
	    			VersitObject versitObject = def.parse(versitReader);
	    			Contact contactObject = oxContainerConverter.convertContact(versitObject);
	    			SANITIZER.sanitize(contactObject);
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
			if (tempLink.getHrefAttribute().startsWith(CONTACT_PAGE + OFFSET + Integer.toString(offset))){
				return tempLink;
			}
		}
		return null;
	}
	
}
