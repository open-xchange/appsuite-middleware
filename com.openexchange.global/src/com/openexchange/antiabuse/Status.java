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

package com.openexchange.antiabuse;

import java.util.Collections;
import java.util.Map;

/**
 * {@link Status} - Represents the status result as returned by Anti-Abuse service
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v1.0.0
 */
public class Status {

    /** The OK status */
    public static final int OK = 0;

    /** The BLOCKED status */
    public static final int BLOCKED = -1;

    // ------------------------------------------------------------------------------------------------------------------------ //

    private final int status;
    private final Map<String, String> properties;

    /**
     * Initializes a new {@link Status}.
     *
     * @param status The status code returned by Anti-Abuse service
     */
    public Status(int status) {
        this(status, null);
    }

    /**
     * Initializes a new {@link Status}.
     *
     * @param status The status code returned by Anti-Abuse service
     * @param properties The optional properties returned by Anti-Abuse service
     */
    public Status(int status, Map<String, String> properties) {
        super();
        this.status = status;
        this.properties = null == properties ? Collections.<String, String> emptyMap() : properties;
    }

    /**
     * Checks if this status signals that authentication attempt is supposed to be paused for a certain number of seconds.
     *
     * @return A positive integer representing the number of seconds to wait; otherwise <code>0</code> (zero)
     */
    public int getWaitSeconds() {
        return status > 0 ? status : 0;
    }

    /**
     * Checks if this status signals that authentication attempt is all fine.
     *
     * @return <code>true</code> if OK; otherwise <code>false</code>
     */
    public boolean isOk() {
        return status == OK;
    }

    /**
     * Checks if this status signals that authentication attempt is supposed to be blocked.
     *
     * @return <code>true</code> if blocked; otherwise <code>false</code>
     */
    public boolean isBlocked() {
        return status == BLOCKED;
    }

    /**
     * Gets the status.
     *
     * @return The status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Gets the properties.
     *
     * @return The properties or an empty map
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[status=").append(status).append(", ");
        if (properties != null) {
            builder.append("properties=").append(properties);
        }
        builder.append("]");
        return builder.toString();
    }
}
