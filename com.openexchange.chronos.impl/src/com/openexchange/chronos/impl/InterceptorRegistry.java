/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.impl;

import static com.openexchange.java.Autoboxing.L;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CalendarInterceptor;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * {@link InterceptorRegistry}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.4
 */
public class InterceptorRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(InterceptorRegistry.class);

    private final CalendarSession session;
    private final CalendarFolder folder;

    /**
     * Initializes a new {@link InterceptorRegistry}.
     * 
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public InterceptorRegistry(CalendarSession session, CalendarFolder folder) {
        super();
        this.session = session;
        this.folder = folder;
    }

    /**
     * Gets all currently registered calendar interceptors.
     * 
     * @return The interceptors, or an empty set if there are none
     */
    public Set<CalendarInterceptor> getInterceptors() {
        return session.getCalendarService().getUtilities().getInterceptors();
    }

    /**
     * Triggers the currently registered calendar interceptors prior a new event is created in the storage, within the calendar storage
     * transaction.
     * <p/>
     * Implementations may adjust the passed <code>newEvent</code> reference for their needs.
     *
     * @param newEvent The new event
     * @throws OXException If handling fails and the calendar operation should be aborted
     */
    public void triggerInterceptorsOnBeforeCreate(Event newEvent) throws OXException {
        for (CalendarInterceptor interceptor : getInterceptors()) {
            if (isRelevantFor(newEvent, interceptor)) {
                long start = System.nanoTime();
                try {
                    interceptor.onBeforeCreate(session, folder.getId(), newEvent);
                    LOG.trace("onBeforeCreate for {} handled successfully by {} ({} ms elapsed)", newEvent, interceptor, 
                        L(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS)));
                } catch (Exception e) {
                    LOG.trace("Unexpected error while handling onBeforeCreate for {} by {}: {}", newEvent, interceptor, e.getMessage(), e);
                    throw e;
                }
            }
        }
    }

    /**
     * Triggers the currently registered calendar interceptors prior an existing event is updated in the storage, within the calendar
     * storage transaction.
     * <p/>
     * Implementations may adjust the passed <code>updatedEvent</code> reference for their needs.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @throws OXException If handling fails and the calendar operation should be aborted
     */
    public void triggerInterceptorsOnBeforeUpdate(Event originalEvent, Event updatedEvent) throws OXException {
        for (CalendarInterceptor interceptor : getInterceptors()) {
            if (isRelevantFor(originalEvent, interceptor) || isRelevantFor(updatedEvent, interceptor)) {
                long start = System.nanoTime();
                try {
                    interceptor.onBeforeUpdate(session, folder.getId(), originalEvent, updatedEvent);
                    LOG.trace("onBeforeUpdate for {} / {} handled successfully by {} ({} ms elapsed)", originalEvent, updatedEvent, interceptor, 
                        L(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS)));
                } catch (Exception e) {
                    LOG.trace("Unexpected error while handling onBeforeUpdate for {} / {} by {}: {}", originalEvent, updatedEvent, interceptor, e.getMessage(), e);
                    throw e;
                }
            }
        }
    }

    /**
     * Invoked prior an existing event is deleted in the storage, within the calendar storage transaction.
     * <p/>
     * Note that before triggering the interceptors, it should be ensured that all fields announced via {@link #getRelevantFields()} are
     * present in the passed <code>deletedEvent</code> reference, so that interceptors have the data they require.
     *
     * @param deletedEvent The deleted event
     * @throws OXException If handling fails and the calendar operation should be aborted
     */
    public void triggerInterceptorsOnBeforeDelete(Event deletedEvent) throws OXException {
        for (CalendarInterceptor interceptor : getInterceptors()) {
            if (isRelevantFor(deletedEvent, interceptor)) {
                long start = System.nanoTime();
                try {
                    interceptor.onBeforeDelete(session, folder.getId(), deletedEvent);
                    LOG.trace("onBeforeDelete for {} handled successfully by {} ({} ms elapsed)", deletedEvent, interceptor, 
                        L(TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS)));
                } catch (Exception e) {
                    LOG.trace("Unexpected error while handling onBeforeDelete for {} by {}: {}", deletedEvent, interceptor, e.getMessage(), e);
                    throw e;
                }
            }
        }
    }

    /**
     * Gets the event fields that are of relevance for at least one of the registered interceptors.
     * <p/>
     * If specified, these properties need to be available within the event references when triggering the interceptors.
     *
     * @return The relevant fields, or an empty set if no special metadata is required
     */
    public Set<EventField> getRelevantFields() {
        Set<EventField> relevantFields = new HashSet<EventField>();
        for (CalendarInterceptor interceptor : getInterceptors()) {
            Set<EventField> fields = interceptor.getRelevantFields();
            if (null != fields) {
                relevantFields.addAll(fields);
            }
        }
        return relevantFields;
    }

    private static boolean isRelevantFor(Event event, CalendarInterceptor interceptor) {
        Set<EventField> relevantFields = interceptor.getRelevantFields();
        if (null == relevantFields) {
            return true;
        }
        for (EventField field : relevantFields) {
            if (event.isSet(field)) {
                return true;
            }
        }
        return false;
    }

}
