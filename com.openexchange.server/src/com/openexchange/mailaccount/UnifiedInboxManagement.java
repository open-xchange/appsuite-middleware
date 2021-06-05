/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailaccount;

import java.sql.Connection;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
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
    public static final Set<String> KNOWN_FOLDERS = ImmutableSet.of(INBOX, DRAFTS, SENT, SPAM, TRASH);

    /**
     * The Unified Mail protocol name.
     */
    public static final String PROTOCOL_UNIFIED_INBOX = "unifiedinbox".intern();

    /**
     * The Unified Mail name.
     */
    public static final String NAME_UNIFIED_INBOX = "Unified Mail".intern();

    /**
     * The domain part of the virtual E-Mail address for a Unified Mail account.
     */
    public static final String MAIL_ADDRESS_DOMAIN_PART = "@unifiedinbox.com";

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
