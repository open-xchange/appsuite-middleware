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

package com.openexchange.chat.util;

import com.openexchange.chat.Presence;

/**
 * {@link PresenceImpl} - The basic {@link Presence presence} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PresenceImpl extends PacketImpl implements Presence {

    private Type type;

    private Mode mode;

    private String status;

    /**
     * Initializes a new {@link PresenceImpl}.
     */
    public PresenceImpl() {
        super();
        type = Type.AVAILABLE;
        mode = Mode.AVAILABLE;
    }

    /**
     * Initializes a new {@link PresenceImpl}.
     *
     * @param type The presence type
     */
    public PresenceImpl(final Type type) {
        super();
        this.type = type;
        mode = Type.UNAVAILABLE.equals(type) ? Mode.AWAY : Mode.AVAILABLE;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the type
     *
     * @param type The type to set
     */
    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * Sets the mode
     *
     * @param mode The mode to set
     */
    public void setMode(final Mode mode) {
        this.mode = mode;
    }

    /**
     * Sets the status
     *
     * @param status The status to set
     */
    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("PresenceImpl {");
        if (type != null) {
            builder.append("type=").append(type).append(", ");
        }
        if (mode != null) {
            builder.append("mode=").append(mode).append(", ");
        }
        builder.append("status=").append(status).append(", ");
        if (packetId != null) {
            builder.append("packetId=").append(packetId).append(", ");
        }
        if (from != null) {
            builder.append("from=").append(from).append(", ");
        }
        if (timeStamp != null) {
            builder.append("timeStamp=").append(timeStamp);
        }
        builder.append('}');
        return builder.toString();
    }

}
