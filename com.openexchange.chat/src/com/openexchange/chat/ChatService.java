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

package com.openexchange.chat;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ChatService} - The chat service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ChatService {

    /**
     * The identifier for default service.
     */
    public static final String DEFAULT_SERVICE = "db";

    /**
     * The identifier for default account.
     */
    public static final String DEFAULT_ACCOUNT = "0";

    /**
     * Gets the access to specified chat account.
     *
     * @param accountId The account identifier; e.g. "0" for default account
     * @return The access to specified chat account
     * @throws OXException If access cannot be provided; e.g. because no such account exists
     * @see #DEFAULT_ACCOUNT
     */
    ChatAccess access(String accountId, Session session) throws OXException;

    /**
     * Gets the account manager for this chat service.
     *
     * @return The account manager
     */
    ChatAccountManager getAccountManager();

    /**
     * Gets the service's identifier.
     *
     * @return The identifier
     * @see #DEFAULT_SERVICE
     */
    String getId();

    /**
     * Gets the service's display name.
     *
     * @return The display name
     */
    String getDisplayName();

}
