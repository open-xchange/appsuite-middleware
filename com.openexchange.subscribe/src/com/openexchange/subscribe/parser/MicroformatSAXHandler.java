package com.openexchange.subscribe.parser;

import java.util.Collection;
import java.util.LinkedList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import com.openexchange.groupware.container.ContactObject;


public class MicroformatSAXHandler extends DefaultHandler{
    protected Collection<ContactObject> contacts;
    protected ContactObject currentContact;
    public String currentlyReading;
    
    public MicroformatSAXHandler(){
        super();
        contacts = new LinkedList<ContactObject>();
    }
    
    protected String stringArray(char[] arr, int start, int end){
        StringBuilder bob = new StringBuilder( new String(arr));
        return bob.substring(start, end);
    }
    
    protected void newContactEncountered(){
        if( null != currentContact ){
            contacts.add( currentContact ); //clone?
        }
        currentContact = new ContactObject();
    }
    
    public void startElement (String uri, String name,
        String qName, Attributes atts){
        String className = atts.getValue("class");
        if(null == className ){
            return;
        }
        if( className.equals("ox-contact")){
            newContactEncountered();
        } 
        else if( className.equals("surname")){
            currentlyReading = "surname";
        } 
        else if( className.equals("givenName")){
            currentlyReading = "givenName";
        } 
        else if( className.equals("email1")){
            currentlyReading = "email1";
        }
        else {
            currentlyReading = null;
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if( currentlyReading == null){
            return;
        }
        if( currentlyReading.equals("ox-contact")){
            newContactEncountered();
        } 
        else if( currentlyReading.equals("surname")){
            currentContact.setSurName( stringArray(ch, start, start+length) );
        } 
        else if( currentlyReading.equals("givenName")){
            currentContact.setGivenName( stringArray(ch, start, start+length) );
        } 
        else if( currentlyReading.equals("email1")){
            currentContact.setEmail1( stringArray(ch, start, start+length) );
        }
    }

    public void endElement (String uri, String name, String qName){
        currentlyReading = null;
    }
    
    public Collection<ContactObject> getContacts(){
        if(currentContact != null){
            contacts.add(currentContact);
        }
        return contacts;
    }

}
