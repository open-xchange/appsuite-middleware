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

package com.openexchange.chronos.itip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;

/**
 * 
 * {@link ITipMessage}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class ITipMessage {

    private ITipMethod method;

    private String comment;

    private final Set<Object> features;

    private int owner;

    private Event event;

    private List<Event> exceptions;

    public ITipMessage() {
        this.features = new HashSet<>();
        this.exceptions = new ArrayList<>();
    }

    /**
     * Gets the method.
     * 
     * @return the method
     */
    public ITipMethod getMethod() {
        return method;
    }

    /**
     * Sets the method.
     * 
     * @param method the method
     */
    public void setMethod(ITipMethod method) {
        this.method = method;
    }

    /**
     * Gets the comment
     *
     * @return The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment
     *
     * @param comment The comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Adds a feature.
     * 
     * @param feature to add
     */
    public void addFeature(Object feature) {
        features.add(feature);
    }

    /**
     * Checks for a feature.
     * 
     * @param feature to check for
     * @return true if this message has the given feature, false otherwise
     */
    public boolean hasFeature(Object feature) {
        return features.contains(feature);
    }

    /**
     * Gets the owner
     *
     * @return The owner
     */
    public int getOwner() {
        return owner;
    }

    /**
     * Sets the owner
     *
     * @param owner The owner to set
     */
    public void setOwner(int owner) {
        this.owner = owner;
    }

    /**
     * Sets the event.
     * 
     * @param event the event
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * Gets the event.
     * 
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Adds an exception.
     * 
     * @param exception the exception to add
     */
    public void addException(Event exception) {
        this.exceptions.add(exception);
    }

    /**
     * Gets an exception Iterable.
     * 
     * @return the Iterable
     */
    public Iterable<Event> exceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    /**
     * Gets the number of exceptions.
     * 
     * @return the number of exceptions
     */
    public int numberOfExceptions() {
        return exceptions.size();
    }

}
