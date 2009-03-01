package com.openexchange.subscribe.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import com.openexchange.groupware.calendar.CalendarDataObject;

/**
 * 
 * {@link MicroformatAppointmentParser}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class MicroformatAppointmentParser {

    private Collection<CalendarDataObject> appointments;

    public void parse(String text) {
        XMLReader xmlReader = null;
        try {
            MicroformatAppointmentSAXHandler handler = new MicroformatAppointmentSAXHandler();
            xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler( handler );
            xmlReader.setErrorHandler( handler );
            xmlReader.parse( new InputSource( new ByteArrayInputStream(text.getBytes()) ) );
            appointments = handler.getObjects();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Collection<CalendarDataObject> getAppointments() {
        return appointments;
    }

}
