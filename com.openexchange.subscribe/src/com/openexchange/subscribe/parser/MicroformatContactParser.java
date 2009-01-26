package com.openexchange.subscribe.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.session.Session;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.SubscriptionSession;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;


public class MicroformatContactParser implements SubscriptionHandler {
    protected Collection<ContactObject> contacts;
    protected SubscribeService service;
    
    public MicroformatContactParser(){
        super();
    }
    
    public MicroformatContactParser(SubscribeService service){
        this.service = service;
    }
 
    /**
     * Estimates whether two contacts are the same, albeit one might contain updated data.
     * @param first
     * @param second
     * @return
     */
    protected boolean isSame(ContactObject first, ContactObject second){
        return first.getGivenName().equals(second.getGivenName()) 
        &&  first.getSurName().equals(second.getSurName());
    }
    
    /**
     * Read the site of a subscription and return its content as a string
     * @param subscription
     * @return
     * @throws IOException
     */
    protected String readSubscription(Subscription subscription) throws IOException{
        URL url = new URL(subscription.getUrl());
        URLConnection connection = url.openConnection();
        BufferedReader buffy = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
        StringBuilder bob = new StringBuilder();
        String line = buffy.readLine();
        while (line != null){
            bob.append (line);
            bob.append ("\n");
            line = buffy.readLine();
        }
        return bob.toString();
    }
    
    /**
     * Update or insert contacts from a subscription
     * @param subscription
     * @throws ContextException
     * @throws OXException
     */
    protected void storeContacts(Subscription subscription, Collection<ContactObject> updatedContacts) throws ContextException, OXException{
        Session session = new SubscriptionSession(subscription);
        RdbContactSQLInterface storage = new RdbContactSQLInterface(session);
        
        for(ContactObject updatedContact: updatedContacts){
            SearchIterator<ContactObject> existingContacts = storage.getContactsInFolder(subscription.getFolderId(), 0, 0, 0, null, ContactObject.ALL_COLUMNS);
            boolean foundMatch = false;
            while( existingContacts.hasNext() && ! foundMatch ){
                ContactObject existingContact = null;
                try {
                    existingContact = existingContacts.next();
                } catch (SearchIteratorException e) {
                    e.printStackTrace();
                }
                if( existingContact == null)
                    continue;
                if( isSame(existingContact, updatedContact)){
                    foundMatch = true;
                    updatedContact.setObjectID( existingContact.getObjectID() );
                    storage.updateContactObject(updatedContact, subscription.getFolderId(), new Date() );
                }
            }
            if(foundMatch)
                continue;
            updatedContact.setParentFolderID( subscription.getFolderId() );
            storage.insertContactObject(updatedContact);
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.subscribe.parser.SubscriptionHandler#handleSubscription(com.openexchange.subscribe.Subscription)
     */
    public void handleSubscription(Subscription subscription){
        try {
            String website = readSubscription(subscription);
            
            parse( website );
            
            storeContacts(subscription, this.getContacts());
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ContextException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public void parse(String text) {
        XMLReader xmlReader = null;
        try {
            MicroformatSAXHandler handler = new MicroformatSAXHandler();
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler( handler );
            xmlReader.setErrorHandler( handler );
            xmlReader.parse( new InputSource( new ByteArrayInputStream(text.getBytes()) ) );
            contacts = handler.getContacts();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Collection<ContactObject> getContacts() {
        return contacts;
    }

}
