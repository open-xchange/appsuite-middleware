package com.openexchange.event.impl;

import com.openexchange.event.impl.AppointmentEventInterface;
import com.openexchange.event.impl.TaskEventInterface;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface EventDispatcher {
    void addListener(AppointmentEventInterface listener);

    void addListener(TaskEventInterface listener);
}
