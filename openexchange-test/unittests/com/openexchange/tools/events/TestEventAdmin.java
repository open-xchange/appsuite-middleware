package com.openexchange.tools.events;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

public class TestEventAdmin implements EventAdmin {

    private static TestEventAdmin INSTANCE = new TestEventAdmin();

    private List<Event> events = new LinkedList<Event>();

    public void postEvent(Event event) {
	    events.add( event );
	}

	public void sendEvent(Event event) {
        events.add( event );
	}

    public List<Event> getEvents() {
        return new ArrayList<Event>(events);
    }

    public void clearEvents() {
        this.events.clear();
    }

    public static TestEventAdmin getInstance() {
       return INSTANCE;
    }
}
