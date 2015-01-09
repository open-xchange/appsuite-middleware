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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

import com.openexchange.exception.OXException;

/**
 * {@link GuestService}
 *
 * TODO is it necessary to put this interface into c.o.global bundle?
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public interface GuestService {

    /**
     * Adds a new guest or creates a new assignment for the guest if the provided mail address is already registered because the user was added from another context.
     * @param mailAddress - mail address of the guest to add
     * @param contextId - context id the guest should be assigned to
     * @param userId - user id of the guest to add in the given context
     * @throws OXException
     */
    void addGuest(String mailAddress, int contextId, int userId) throws OXException;

    /**
     * Remove an existing guest. If the last assignment of the mail address was removed even the complete guest will be removed.
     *
     * @param mailAddress - mail address of the guest to remove
     * @param contextId - context id the guest is assigned to
     * @param userId - user id in the given context the guest is assigned to
     * @throws OXException
     */
//    void removeGuest(String mailAddress, int contextId, int userId) throws OXException;
    void removeGuest(int contextId, int userId) throws OXException;

    /**
     * Sets the password for all occurrences of the guest which means for all internal users over all contexts the provided mail address is registered.
     * @param mailAddress - mail address to identify the guest and to find associated users
     * @param password - new password to set
     * @throws OXException
     */
    void setPassword(String mailAddress, String password) throws OXException;
}
