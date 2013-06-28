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
 * Keeps track of the session information like session id and cookies.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTSession {

    /**
     * ConextID of the current client/user
     */
    private final long contextID;

    /**
     * SessionID that is assigned to the client/user
     */
    private final String sessionID;

    /**
     * Locale that is configured for the client/user
     */
    private final String locale;

    /**
     * Session that is assigned to the client/user
     */
    private final String random;

    /**
     * User id of the current client/user
     */
    private final long userID;

    /**
     * Secret session key that is required for requests
     */
    private final String secretSessionKey;

    /**
     * Secret session value that is required for requests
     */

    private final String secretSessionValue;

    /**
     * JSessionID that is required for request routing
     */
    private final String jSessionID;

    /**
     * User name
     */
    private final String user;

    /**
     * Initializes a new {@link RTSession}.
     * 
     * @param contextID - contextID of the user
     * @param sessionID - sessionID of the user
     * @param locale - locale of the user
     * @param random - current random
     * @param userID - numeric userID
     * @param secretSessionKey - currently assigned secret session key
     * @param secretSessionValue- currently assigned secret session value
     * @param jSessionID - currently assigned jSessionID
     * @param user - name of the user
     */
    public RTSession(final long contextID, final String sessionID, final String locale, final String random, final long userID, final String user, final String secretSessionKey, final String secretSessionValue, final String jSessionID) {
        Validate.isTrue(contextID != 0, "ERROR: contextID is missing!");
        Validate.notEmpty(sessionID, "ERROR: SessionID is missing!");
        Validate.notEmpty(locale, "ERROR: Locale is missing!");
        Validate.notEmpty(random, "ERROR: Random is missing!");
        Validate.isTrue(userID != 0, "ERROR: userID is missing!");
        Validate.notEmpty(user, "ERROR: user is missing!");
        Validate.notEmpty(secretSessionKey, "ERROR: Key for secret is missing!");
        Validate.notEmpty(secretSessionValue, "ERROR: Value for secret is missing!");
        Validate.notEmpty(jSessionID, "ERROR: jSessionID is missing!");

        this.contextID = contextID;
        this.sessionID = sessionID;
        this.locale=locale;
        this.random = random;
        this.userID = userID;
        this.secretSessionKey = secretSessionKey;
        this.secretSessionValue = secretSessionValue;
        this.jSessionID = jSessionID;
        this.user = user;
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
     * Gets the contextID
     *
     * @return The contextID
     */
    public long getContextID() {
        return contextID;
    }


    /**
     * Gets the sessionID
     *
     * @return The sessionID
     */
    public String getSessionID() {
        return sessionID;
    }


    /**
     * Gets the locale
     *
     * @return The locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Gets the userID
     *
     * @return The userID
     */
    public long getUserID() {
        return userID;
    }

    /**
     * Gets the jSessionID
     *
     * @return The jSessionID
     */
    public String getjSessionID() {
        return jSessionID;
    }


    /**
     * Gets the user login
     *
     * @return The user login
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns the current status of the object in string representation
     * 
     * @return String with the status of the object
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLine);
        result.append(" User: " + this.user + newLine);
        result.append(" userID: " + this.userID + newLine);
        result.append(" ContextID " + this.contextID + newLine);
        result.append(" jSessionID: " + this.jSessionID + newLine);
        result.append(" locale: " + this.locale + newLine);
        result.append(" random: " + this.random + newLine);
        result.append(" secretSessionKey: " + this.secretSessionKey + newLine);
        result.append(" secretSessionValue: " + this.secretSessionValue + newLine);
        result.append(" sessionID: " + this.sessionID + newLine);
        result.append("}");

        return result.toString();
    }
}
