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

package com.openexchange.realtime.client;

import org.apache.commons.lang.Validate;

/**
 * Keep track of the user state like session and cookies
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTUserState {

    /**
     * Session that is assigned to the client/user
     */
    private final String session;

    /**
     * Session that is assigned to the client/user
     */
    private final String random;

    /**
     * Secret session key that is required for requests
     */
    private final String secretSessionKey;

    /**
     * Secret session value that is required for requests
     */

    private final String secretSessionValue;

    /**
     * Session cookie that is required for requests
     */
    private final String jSessionID;

    /**
     * Initializes a new {@link RTUserState}.
     * 
     * @param name - name of the user
     * @param session - current session
     * @param secretSessionKey - currently assigned secret session key
     * @param secretSessionValue- currently assigned secret session value
     * @param cookie- currently assigned cookie
     */
    public RTUserState(final String session, final String random, final String secretSessionKey, final String secretSessionValue, final String jSessionID) {
        Validate.notNull(session, "ERROR: Current session mustn't be null!");
        Validate.notNull(random, "ERROR: Current random mustn't be null!");
        Validate.notNull(secretSessionKey, "ERROR: Key for secret session mustn't be null!");
        Validate.notNull(secretSessionValue, "ERROR: Value for secret session mustn't be null!");
        Validate.notNull(jSessionID, "ERROR: jSessionID mustn't be null!");

        this.random = random;
        this.session = session;
        this.secretSessionKey = secretSessionKey;
        this.secretSessionValue = secretSessionValue;
        this.jSessionID = jSessionID;
    }

    /**
     * Gets the session
     * 
     * @return The session
     */
    public String getSession() {
        return this.session;
    }

    /**
     * Gets the random
     * 
     * @return The random
     */
    public String getRandom() {
        return this.random;
    }

    /**
     * Gets the secretSessionKey
     * 
     * @return The secretSessionKey
     */
    public String getSecretSessionKey() {
        return this.secretSessionKey;
    }

    /**
     * Gets the secretSessionValue
     * 
     * @return The secretSessionValue
     */
    public String getSecretSessionValue() {
        return this.secretSessionValue;
    }

    /**
     * Gets the cookie
     * 
     * @return The cookie
     */
    public String getCookie() {
        return this.jSessionID;
    }

}
