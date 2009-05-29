package com.openexchange.subscribe.parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.SubscriptionSession;


public class MicroformatContactParser extends ContactHandler implements SubscriptionHandler {
    protected Collection<ContactObject> contacts;
    protected SubscribeService service;
    
    public MicroformatContactParser(){
        super();
    }
    
    public MicroformatContactParser(SubscribeService service){
        this.service = service;
    }
 
    /**
     * Read the site of a subscription and return its content as a string
     * @param subscription
     * @return
     * @throws IOException
     */
    protected String readSubscription(Subscription subscription) throws IOException{
        URL url = null; //new URL(subscription.getUrl());
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
    
    /* (non-Javadoc)
     * @see com.openexchange.subscribe.parser.SubscriptionHandler#handleSubscription(com.openexchange.subscribe.Subscription)
     */
    public void handleSubscription(Subscription subscription) throws AbstractOXException{
        try {
            String website = readSubscription(subscription);
            
            parse( website );
            
            storeContacts(new SubscriptionSession(subscription), subscription.getFolderIdAsInt(), this.getContacts());
            
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
            AbstractMicroformatSAXHandler handler = new MicroformatContactSAXHandler();
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler( handler );
            xmlReader.setErrorHandler( handler );
            xmlReader.parse( new InputSource( new ByteArrayInputStream(text.getBytes()) ) );
            contacts = handler.getObjects();
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
