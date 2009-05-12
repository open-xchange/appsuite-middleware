package com.openexchange.subscribe;

import java.util.Collection;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.subscribe.parser.MicroformatContactParser;
import junit.framework.TestCase;


public class MicroformatContactParserTest extends TestCase {
    public String wellBehavedHtml = 
        "<div class=\"ox-contact\">\n" +
        "   <img class=\"image1\" src=\"bla.png\"></img>\n" + 
        "   <span class=\"surname\">Hans</span>\n"+
        "   <span class=\"givenName\">Meier</span>\n" +
        "   <span class=\"email1\">Hans.Meier@tralala.invalid</span>\n" + 
        "</div>";
    
    public void testShouldWorkUnderIdealCircumstances(){
        MicroformatContactParser parser = new MicroformatContactParser();
        parser.parse(wellBehavedHtml);
        Collection<ContactObject> contacts = parser.getContacts();
        assertEquals("Should find one contact", 1, contacts.size() );
        ContactObject contact = contacts.iterator().next();
        assertEquals("First name", "Hans", contact.getSurName());
        assertEquals("Family name", "Meier", contact.getGivenName() );
        assertEquals("E-Mail", "Hans.Meier@tralala.invalid", contact.getEmail1() );
    }
    
    public void testShouldWorkViaURL(){
        MicroformatContactParser parser = new MicroformatContactParser();
        Subscription subscription = new Subscription();
        subscription.setContext(new SimContext(1));
        subscription.setUserId(8);
        //subscription.setUrl("http://localhost/~development/test/micro.html");
        
        parser.handleSubscription(subscription);
        Collection<ContactObject> contacts = parser.getContacts();
        assertEquals("Should find one contact", 1, contacts.size() );
        ContactObject contact = contacts.iterator().next();
        assertEquals("First name", "Hans", contact.getSurName());
        assertEquals("Family name", "Meier", contact.getGivenName() );
        assertEquals("E-Mail", "Hans.Meier@tralala.invalid", contact.getEmail1() );
    }

}
