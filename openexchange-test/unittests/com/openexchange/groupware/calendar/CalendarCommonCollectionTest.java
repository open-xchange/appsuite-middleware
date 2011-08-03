package com.openexchange.groupware.calendar;

import com.openexchange.calendar.api.CalendarCollection;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;

import junit.framework.TestCase;

public class CalendarCommonCollectionTest extends TestCase{
    
    public void testCheckParticipants() {
        Participant[] a, b, c, d;
        
        Participant one = new UserParticipant(1);
        Participant two = new UserParticipant(2);
        Participant three = new UserParticipant(3);
        
        a = new Participant[]{one, two};
        b = new Participant[]{two, one};
        c = new Participant[]{one, three};
        d = new Participant[]{one, two, three};
        
        CalendarCollection tools = new CalendarCollection();
        assertFalse(tools.checkParticipants(a, a));
        assertFalse(tools.checkParticipants(a, b));
        assertTrue(tools.checkParticipants(a, c));
        assertTrue(tools.checkParticipants(a, d));
        assertTrue(tools.checkParticipants(null, a));
        assertTrue(tools.checkParticipants(a, null));
        assertFalse(tools.checkParticipants(null, null));
    }
    
}
