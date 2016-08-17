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

package com.openexchange.guest;

import java.io.Serializable;

/**
 * This class handles an assignment of a guest (identified by the mail address) to a context and user.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GuestAssignment implements Serializable {

    private static final long serialVersionUID = 622650365568736720L;

    /**
     * The context the guest is assigned to.
     */
    private final int contextId;

    /**
     * The user id within the given context;
     */
    private final int userId;

    /**
     * The mail address the user is registered with
     */
    private final long guestId;

    /**
     * The password of the user
     */
    private final String password;

    /**
     * The mechanism the password is encrypted with
     */
    private final String passwordMech;

    /**
     * Initializes a new {@link GuestAssignment}.
     *
     * @param guestId - internal guest id of the user
     * @param contextId - context id the user is in
     * @param userId - user id in the context
     */
    public GuestAssignment(long guestId, int contextId, int userId, String password, String passwordMech) {
        this.guestId = guestId;
        this.contextId = contextId;
        this.userId = userId;
        this.password = password;
        this.passwordMech = passwordMech;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the guestId
     *
     * @return The guestId
     */
    public long getGuestId() {
        return guestId;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the passwordMech
     *
     * @return The passwordMech
     */
    public String getPasswordMech() {
        return passwordMech;
    }
}
