/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
