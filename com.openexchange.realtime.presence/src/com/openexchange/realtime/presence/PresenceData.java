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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.presence;

import java.io.Serializable;
import java.util.Date;
import com.openexchange.realtime.packet.PresenceState;

/**
 * {@link PresenceData} - PresenceState with optional message and timestamp of the creation form the PresenceData that allow us to track a
 * clients status and the last time it was changed.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceData implements Serializable {

    private PresenceState state;

    private String message;

    private final Date timeStamp;

    public static PresenceData OFFLINE = new PresenceData(PresenceState.OFFLINE, "", null);

    /**
     * Initializes a new {@link PresenceData} with the current time as creationTime
     *
     * @param state One of the avilable states to choose from
     * @param message The optional user provided message to associate with the current state. May be null.
     * @throws IllegalArgumentException when the state is missing
     */
    public PresenceData(PresenceState state, String message) {
        if (state == null) {
            throw new IllegalArgumentException("Missing obligatory state parameter");
        }
        this.state = state;
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
        }
        this.timeStamp = new Date();
    }

    /**
     * Initializes a new {@link PresenceData}.
     *
     * @param state         One of the avilable states to choose from
     * @param message       The optional user provided message to associate with the current state. May be null.
     * @param creationTime  The date a user set this PresenceData or null when the user didn't publish any PresenceData yet.
     * @throws IllegalArgumentException when the state is missing
     */
    public PresenceData(PresenceState state, String message, Date creationTime) {
        if (state == null) {
            throw new IllegalArgumentException("Missing obligatory state parameter");
        }
        this.state = state;
        if (message == null) {
            this.message = "";
        } else {
            this.message = message;
        }
        this.timeStamp = creationTime;
    }

    public PresenceState getState() {
        return state;
    }

    public void setState(PresenceState state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the time when a user set this PresenceData or null when the user didn't publish any PresenceData yet (OFFLINE).
     * @return nunll or the creation time
     */
    public Date getCreationTime() {
        return timeStamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PresenceData)) {
            return false;
        }
        PresenceData other = (PresenceData) obj;
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (timeStamp == null) {
            if (other.timeStamp != null) {
                return false;
            }
        } else if (!timeStamp.equals(other.timeStamp)) {
            return false;
        }
        return true;
    }

}
