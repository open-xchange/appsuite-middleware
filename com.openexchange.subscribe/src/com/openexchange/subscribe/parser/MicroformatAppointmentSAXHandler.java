package com.openexchange.subscribe.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import com.openexchange.groupware.calendar.CalendarDataObject;

/**
 * 
 * {@link MicroformatAppointmentSAXHandler}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class MicroformatAppointmentSAXHandler extends AbstractMicroformatSAXHandler<CalendarDataObject>{
    
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss z"); //iso 8601, this one should be locale-independend    
    private List<Exception> exceptions = new LinkedList<Exception>();
    
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String className = atts.getValue("class");
        if(null == className ){
            return;
        }
        if( className.equals("ox-appointment")){
            newObjectEncountered();
        } 
        else if( className.equals("title")){
            currentlyReading = "title";
        } 
        else if( className.equals("note")){
            currentlyReading = "note";
        } 
        else if( className.equals("location")){
            currentlyReading = "location";
        }
        else if( className.equals("startDate")){
            currentlyReading = "startDate";
        }
        else if( className.equals("endDate")){
            currentlyReading = "endDate";
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
        if( currentlyReading.equals("ox-appointment")){
            newObjectEncountered();
        } 
        else if( currentlyReading.equals("title")){
            currentObject.setTitle( stringArray(ch, start, start+length) );
        } 
        else if( currentlyReading.equals("note")){
            currentObject.setNote( stringArray(ch, start, start+length) );
        } 
        else if( currentlyReading.equals("location")){
            currentObject.setLocation( stringArray(ch, start, start+length) );
        }
        else if( currentlyReading.equals("startDate")){
            try {
                currentObject.setStartDate( dateFormat.parse( stringArray(ch, start, start+length) ) );
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
        else if( currentlyReading.equals("endDate")){
            try {
                currentObject.setEndDate( dateFormat.parse( stringArray(ch, start, start+length) ) );
            } catch (ParseException e) {
                exceptions.add(e);
            }
        }
    }

    @Override
    public CalendarDataObject instantiate() {
        return new CalendarDataObject();
    }
}
