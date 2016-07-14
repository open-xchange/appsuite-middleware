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

package com.openexchange.guard.api.authentication;

import java.util.Date;

/**
 * {@link GuardAuthenticationToken} represents a token used for authenticating a user against OX Guard.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.3
 */
public class GuardAuthenticationToken {

    private final String tokenValue;
    private final String guardSessionId;
    private final Date createdOn;
    private final int minutesValid;
    private volatile boolean used;

    /**
     * Initializes a new {@link GuardAuthenticationToken}.
     *
     * @param guardSessionId A secret random value which represents a guard session.
     * @param tokenValue The value of the authentication token.
     */
    public GuardAuthenticationToken(String guardSessionId, String tokenValue) {
        this.guardSessionId = guardSessionId;
        this.tokenValue = tokenValue;
        this.minutesValid = 0;
        this.createdOn = new Date();
        this.used = false;
    }

    /**
     * Initializes a new {@link GuardAuthenticationToken}.
     *
     * @param guardSessionId A secret random value which represents a Guard session.
     * @param tokenValue The value of the authentication token.
     * @paran minutesValid The amount of time (in minutes) the authentication token should be valid.
     */
    public GuardAuthenticationToken(String guardSessionId, String tokenValue, int minutesValid) {
        this.guardSessionId = guardSessionId;
        this.tokenValue = tokenValue;
        this.minutesValid = minutesValid;
        this.createdOn = new Date();
        this.used = false;
    }

    /**
     * @return A secret random value which represents a Guard session
     */
    public String getGuardSessionId() {
        return this.guardSessionId;
    }

    /**
     * @return The token value
     */
    public String getValue() {
        return this.tokenValue;
    }

    /**
     * @return The amount of minutes the authentication token should be valid.
     */
    public int getMinutesValid() {
        return this.minutesValid;
    }

    /**
     * @return The creation time stamp of the token.
     */
    public Date getCreatedOn() {
        return this.createdOn;
    }

    /**
     * @return True, if the token has been used at least once for authentication, false otherwise.
     */
    public boolean isUsed() {
        return this.used;
    }

    /**
     * Marks the token as used
     */
    public void setUsed() {
        this.used = true;
    }
}
