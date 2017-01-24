
package com.openexchange.tools.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.CommonEvent;

public class TestEventAdmin implements EventAdmin {

    private static TestEventAdmin INSTANCE = new TestEventAdmin();

    private final List<Event> events = new LinkedList<Event>();

    @Override
    public void postEvent(final Event event) {
        events.add(event);
    }

    @Override
    public void sendEvent(final Event event) {
        Thread.dumpStack();
        events.add(event);
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

    public int size() {
        return events.size();
    }

    public CommonEvent getNewest() {
        if (events.isEmpty()) {
            throw new IndexOutOfBoundsException("No newest element, I'm afraid");
        }
        return (CommonEvent) events.get(events.size() - 1).getProperty(CommonEvent.EVENT_KEY);
    }

    public Event getNewestEvent() {
        if (events.isEmpty()) {
            throw new IndexOutOfBoundsException("No newest element, I'm afraid");
        }
        return events.get(events.size() - 1);
    }
}
