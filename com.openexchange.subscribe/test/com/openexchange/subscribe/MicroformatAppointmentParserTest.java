package com.openexchange.subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.junit.Test;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.subscribe.parser.MicroformatAppointmentParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class MicroformatAppointmentParserTest {    public Date defaultStartDate;
    public Date defaultEndDate;
    public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss z");

    public String wellBehavedHtml;


    public MicroformatAppointmentParserTest(){
        super();
        try {
            defaultStartDate = dateFormat.parse("1935-01-08, 12:00:00 GMT");
            defaultEndDate = dateFormat.parse("1977-08-16, 13:00:00 GMT");
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        wellBehavedHtml =
            "<div class=\"ox-appointment\">\n" +
            "   <span class=\"title\">My appointment</span>\n"+
            "   <span class=\"note\">There are many appointments but this one is mine</span>\n"+
            "   <span class=\"location\">Your place or mine?</span>\n"+
            "   <span class=\"startDate\">" + dateFormat.format(defaultStartDate) + "</span>\n" +
            "   <span class=\"endDate\">" + dateFormat.format(defaultEndDate) + "</span>\n" +
            "</div>";

    }

         @Test
     public void testShouldWorkUnderBestPossibleCircumstances(){
        final MicroformatAppointmentParser parser = new MicroformatAppointmentParser();
        parser.parse(wellBehavedHtml);
        final Collection<CalendarDataObject> appointments = parser.getAppointments();
        assertEquals("Should contain one element", 1, appointments.size());
        final CalendarDataObject appointment = appointments.iterator().next();
        assertEquals("Should parse title", "My appointment", appointment.getTitle());
        assertEquals("Should parse note", "There are many appointments but this one is mine", appointment.getNote() );
        assertEquals("Should parse location", "Your place or mine?", appointment.getLocation() );
        assertEquals("Should parse start date", defaultStartDate, appointment.getStartDate() );
        assertEquals("Should parse end date", defaultEndDate, appointment.getEndDate() );
    }

}
