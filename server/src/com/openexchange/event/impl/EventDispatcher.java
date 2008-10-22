package com.openexchange.event.impl;


/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface EventDispatcher {
    void addListener(AppointmentEventInterface listener);

    void addListener(TaskEventInterface listener);
}
