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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos;

import java.util.Date;

/**
 * {@link Alarm}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 * @see <a href="https://tools.ietf.org/html/rfc5545#section-3.8.6">RFC 5545, section 3.8.6</a>
 */
public class Alarm {

    int id;
    String uid;
    int eventId;
    String iCalId;
    String relatedTo;

    Trigger trigger;
    Date acknowledged;
    String description;
    AlarmAction action;
    String duration;
    int repeat;

    /**
     * Gets the id
     *
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the uid
     *
     * @return The uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the uid
     *
     * @param uid The uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the eventId
     *
     * @return The eventId
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Sets the eventId
     *
     * @param eventId The eventId to set
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * Gets the iCalId
     *
     * @return The iCalId
     */
    public String getiCalId() {
        return iCalId;
    }

    /**
     * Sets the iCalId
     *
     * @param iCalId The iCalId to set
     */
    public void setiCalId(String iCalId) {
        this.iCalId = iCalId;
    }

    /**
     * Gets the relatedTo
     *
     * @return The relatedTo
     */
    public String getRelatedTo() {
        return relatedTo;
    }

    /**
     * Sets the relatedTo
     *
     * @param relatedTo The relatedTo to set
     */
    public void setRelatedTo(String relatedTo) {
        this.relatedTo = relatedTo;
    }

    /**
     * Gets the trigger
     *
     * @return The trigger
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger
     *
     * @param trigger The trigger to set
     */
    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    /**
     * Gets the description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the action
     *
     * @return The action
     */
    public AlarmAction getAction() {
        return action;
    }

    /**
     * Sets the action
     *
     * @param action The action to set
     */
    public void setAction(AlarmAction action) {
        this.action = action;
    }

    /**
     * Gets the duration
     *
     * @return The duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the duration
     *
     * @param duration The duration to set
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Gets the repeat
     *
     * @return The repeat
     */
    public int getRepeat() {
        return repeat;
    }

    /**
     * Sets the repeat
     *
     * @param repeat The repeat to set
     */
    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    /**
     * Gets the acknowledged
     *
     * @return The acknowledged
     */
    public Date getAcknowledged() {
        return acknowledged;
    }

    /**
     * Sets the acknowledged
     *
     * @param acknowledged The acknowledged to set
     */
    public void setAcknowledged(Date acknowledged) {
        this.acknowledged = acknowledged;
    }

    @Override
    public String toString() {
        return "Alarm [action=" + action + ", trigger=" + trigger + "]";
    }

}
