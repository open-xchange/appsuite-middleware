package com.openexchange.groupware.calendar;

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
        
        assertFalse(CalendarCommonCollection.checkParticipants(a, a));
        assertFalse(CalendarCommonCollection.checkParticipants(a, b));
        assertTrue(CalendarCommonCollection.checkParticipants(a, c));
        assertTrue(CalendarCommonCollection.checkParticipants(a, d));
        assertTrue(CalendarCommonCollection.checkParticipants(null, a));
        assertTrue(CalendarCommonCollection.checkParticipants(a, null));
        assertFalse(CalendarCommonCollection.checkParticipants(null, null));
    }
    
}
