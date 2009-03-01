package com.openexchange.subscribe.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import com.openexchange.groupware.container.ContactObject;


public class MicroformatContactSAXHandler extends 
AbstractMicroformatSAXHandler<ContactObject> {
    
    public MicroformatContactSAXHandler(){
        super();
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        String className = atts.getValue("class");
        if(null == className ){
            return;
        }
        if( className.equals("ox-contact")){
            newObjectEncountered();
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
            newObjectEncountered();
        } 
        else if( currentlyReading.equals("surname")){
            currentObject.setSurName( stringArray(ch, start, start+length) );
        } 
        else if( currentlyReading.equals("givenName")){
            currentObject.setGivenName( stringArray(ch, start, start+length) );
        } 
        else if( currentlyReading.equals("email1")){
            currentObject.setEmail1( stringArray(ch, start, start+length) );
        }
    }

    @Override
    public ContactObject instantiate() {
        return new ContactObject();
    }
}
