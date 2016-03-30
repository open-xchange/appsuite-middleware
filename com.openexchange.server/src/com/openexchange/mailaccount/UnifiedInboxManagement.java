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

package com.openexchange.mailaccount;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link UnifiedInboxManagement} - Management for Unified Mail accounts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface UnifiedInboxManagement {

    /**
     * Full name of INBOX.
     */
    public static final String INBOX = "INBOX".intern();

    /**
     * Full name of Trash.
     */
    public static final String TRASH = "Trash".intern();

    /**
     * Full name of Sent.
     */
    public static final String SENT = "Sent".intern();

    /**
     * Full name of Spam.
     */
    public static final String SPAM = "Spam".intern();

    /**
     * Full name of Drafts.
     */
    public static final String DRAFTS = "Drafts".intern();

    /**
     * A set containing all known default folders for an Unified Mail account.
     */
    public static final Set<String> KNOWN_FOLDERS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] { INBOX, DRAFTS, SENT, SPAM, TRASH })));

    /**
     * The Unified Mail protocol name.
     */
    public static final String PROTOCOL_UNIFIED_INBOX = "unifiedinbox".intern();

    /**
     * The Unified Mail name.
     */
    public static final String NAME_UNIFIED_INBOX = "Unified Mail".intern();

    /**
     * Creates the Unified Mail account for given user in specified context.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @throws OXException If creating the Unified Mail account fails for given user in specified context
     */
    void createUnifiedINBOX(int userId, int contextId) throws OXException;

    /**
     * Creates the Unified Mail account for given user in specified context.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @throws OXException If creating the Unified Mail account fails for given user in specified context
     */
    void createUnifiedINBOX(int userId, int contextId, Connection con) throws OXException;

    /**
     * Deletes the Unified Mail account for given user in specified context.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @throws OXException If deleting the Unified Mail account fails for given user in specified context
     */
    void deleteUnifiedINBOX(int userId, int contextId) throws OXException;

    /**
     * Deletes the Unified Mail account for given user in specified context.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @throws OXException If deleting the Unified Mail account fails for given user in specified context
     */
    void deleteUnifiedINBOX(int userId, int contextId, Connection con) throws OXException;

    /**
     * Checks if the Unified Mail account exists for given user in specified context.
     * <p>
     * The Unified Mail account is considered to be enabled if at least one account indicates its subscription to Unified Mail.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return <code>true</code> if the Unified Mail account exists for given user in specified context; otherwise <code>false</code>
     * @throws OXException If checking Unified Mail account's enabled status fails
     */
    boolean exists(int userId, int contextId) throws OXException;

    /**
     * Checks if the Unified Mail account exists for given user in specified context.
     * <p>
     * The Unified Mail account is considered to be enabled if at least one account indicates its subscription to Unified Mail.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @return <code>true</code> if the Unified Mail account exists for given user in specified context; otherwise <code>false</code>
     * @throws OXException If checking Unified Mail account's enabled status fails
     */
    boolean exists(int userId, int contextId, Connection con) throws OXException;

    /**
     * Checks if the Unified Mail account is enabled for given user in specified context.
     * <p>
     * The Unified Mail account is considered to be enabled if at least one account indicates its subscription to Unified Mail.
     *
     * @param session The associated session
     * @return <code>true</code> if the Unified Mail account is enabled for given user in specified context; otherwise <code>false</code>
     * @throws OXException If checking Unified Mail account's enabled status fails
     */
    boolean isEnabled(Session session) throws OXException;

    /**
     * Checks if the Unified Mail account is enabled for given user in specified context.
     * <p>
     * The Unified Mail account is considered to be enabled if at least one account indicates its subscription to Unified Mail.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return <code>true</code> if the Unified Mail account is enabled for given user in specified context; otherwise <code>false</code>
     * @throws OXException If checking Unified Mail account's enabled status fails
     */
    boolean isEnabled(int userId, int contextId) throws OXException;

    /**
     * Checks if the Unified Mail account is enabled for given user in specified context.
     * <p>
     * The Unified Mail account is considered to be enabled if at least one account indicates its subscription to Unified Mail.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @return <code>true</code> if the Unified Mail account is enabled for given user in specified context; otherwise <code>false</code>
     * @throws OXException If checking Unified Mail account's enabled status fails
     */
    boolean isEnabled(int userId, int contextId, Connection con) throws OXException;

    /**
     * Gets the ID of the mail account denoting the Unified Mail account.
     *
     * @param session The session
     * @return The ID of the Unified Mail account or <code>-1</code> if none found
     * @throws OXException If detecting ID of the Unified Mail account fails
     */
    int getUnifiedINBOXAccountID(Session session) throws OXException;

    /**
     * Gets the ID of the mail account denoting the Unified Mail account.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The ID of the Unified Mail account or <code>-1</code> if none found
     * @throws OXException If detecting ID of the Unified Mail account fails
     */
    int getUnifiedINBOXAccountID(int userId, int contextId) throws OXException;

    /**
     * Gets the ID of the mail account denoting the Unified Mail account.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @return The ID of the Unified Mail account or <code>-1</code> if none found
     * @throws OXException If detecting ID of the Unified Mail account fails
     */
    int getUnifiedINBOXAccountID(int userId, int contextId, Connection con) throws OXException;

    /**
     * Gets the ID of the mail account denoting the Unified Mail account if enabled.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The ID of the Unified Mail account or <code>-1</code> if none found or not enabled
     * @throws OXException If detecting ID of the Unified Mail account fails
     */
    int getUnifiedINBOXAccountIDIfEnabled(int userId, int contextId) throws OXException;

    /**
     * Gets the ID of the mail account denoting the Unified Mail account if enabled.
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @param con The connection to use
     * @return The ID of the Unified Mail account or <code>-1</code> if none found or not enabled
     * @throws OXException If detecting ID of the Unified Mail account fails
     */
    int getUnifiedINBOXAccountIDIfEnabled(int userId, int contextId, Connection con) throws OXException;

}
