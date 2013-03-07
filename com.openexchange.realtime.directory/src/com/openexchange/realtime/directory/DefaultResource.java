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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.directory;

import java.io.Serializable;
import java.util.Date;
import com.openexchange.realtime.packet.PresenceState;

/**
 * {@link DefaultResource} Abstract {@link Resource} implementation.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
// TODO: add Presence field instead of timestamp, state, message, priority as Stanzas aren't restricted to the base set of fields.
public class DefaultResource implements Resource {

    private static final long serialVersionUID = -1140736920132224444L;

    protected Date timestamp;

    protected PresenceState state;

    protected String message;

    protected byte priority;

    protected Serializable routingInfo;

    /**
     * Initializes a new {@link DefaultResource} with {@link PresenceState#ONLINE}.
     */
    public DefaultResource() {
        // don't set a PresenceState for DefaultResources so we can distinguish DefaultResources from the ones with a Presence associated
        // see TODO
        this(null);
    }

    /**
     * Initializes a new {@link DefaultResource} with the supplied presence state, assuming the current time as timestamp.
     * 
     * @param state The presence state
     */
    public DefaultResource(PresenceState state) {
        this(state, new Date());
    }

    /**
     * Initializes a new {@link DefaultResource}.
     * 
     * @param state The presence state
     * @param timestamp The timestamp
     */
    public DefaultResource(PresenceState state, Date timestamp) {
        super();
        this.timestamp = timestamp;
        this.state = state;
    }

    @Override
    public Date getTimestamp() {
        return this.timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public byte getPriority() {
        return this.priority;
    }

    /**
     * Sets the priority
     * 
     * @param priority The priority to set
     */
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    @Override
    public Serializable getRoutingInfo() {
        return this.routingInfo;
    }

    @Override
    public void setRoutingInfo(Serializable routingInfo) {
        this.routingInfo = routingInfo;
    }

    @Override
    public PresenceState getPresenceState() {
        return this.state;
    }

    /**
     * Sets the presence state
     * 
     * @param state the presence state to set
     */
    public void setPresenceState(PresenceState state) {
        this.state = state;
    }

    /**
     * Sets the message
     * 
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + priority;
        result = prime * result + ((routingInfo == null) ? 0 : routingInfo.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultResource other = (DefaultResource) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (priority != other.priority)
            return false;
        if (routingInfo == null) {
            if (other.routingInfo != null)
                return false;
        } else if (!routingInfo.equals(other.routingInfo))
            return false;
        if (state != other.state)
            return false;
        if (timestamp == null) {
            if (other.timestamp != null)
                return false;
        } else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

}
