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

package com.openexchange.imap.entity2acl;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.mail.MessagingException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link Entity2ACL} - Maps numeric entity IDs to corresponding IMAP login name (used in ACLs) and vice versa
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class Entity2ACL {

    private static final ConcurrentMap<String, String> GREETING_CACHE = new ConcurrentHashMap<String, String>(16, 0.9f, 1);

    private static String getGreeting(IMAPStore imapStore, IMAPConfig imapConfig) throws MessagingException {
        String greeting = GREETING_CACHE.get(imapConfig.getServer());
        if (null == greeting) {
            final String grt = imapStore.getGreeting();
            greeting = GREETING_CACHE.putIfAbsent(imapConfig.getServer(), grt);
            if (null == greeting) {
                greeting = grt;
            }
        }
        return greeting;
    }

    private static volatile boolean instantiated;

    /**
     * The constant reflecting the found group {@link OCLPermission#ALL_GROUPS_AND_USERS}.
     */
    protected static final UserGroupID ALL_GROUPS_AND_USERS = new UserGroupID(OCLPermission.ALL_GROUPS_AND_USERS, true);

    /**
     * Singleton
     */
    private static volatile Entity2ACL singleton;

    /**
     * Creates a new instance implementing the {@link Entity2ACL} interface.
     *
     * @param imapConfig The user's IMAP config
     * @return an instance implementing the {@link Entity2ACL} interface.
     * @throws OXException if the instance can't be created.
     */
    public static final Entity2ACL getInstance(IMAPConfig imapConfig) throws OXException {
        if (instantiated && MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            /*
             * Auto-detection is turned off, return configured implementation
             */
            return singleton;
        }
        /*
         * Auto-detect dependent on user's IMAP settings
         */
        try {
            return Entity2ACLAutoDetector.getEntity2ACLImpl(imapConfig);
        } catch (IOException e) {
            throw Entity2ACLExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static final String PARAM_NAME = "Entity2ACL";

    /**
     * Creates a new instance implementing the {@link Entity2ACL} interface.
     *
     * @param imapStore The IMAP store
     * @param imapConfig The user's IMAP configuration
     * @return an instance implementing the {@link Entity2ACL} interface.
     * @throws OXException If a mail error occurs
     */
    public static final Entity2ACL getInstance(IMAPStore imapStore, IMAPConfig imapConfig) throws OXException {
        if (instantiated && MailAccount.DEFAULT_ID == imapConfig.getAccountId()) {
            /*
             * Auto-detection is turned off, return configured implementation
             */
            return singleton;
        }
        try {
            Entity2ACL cached = imapConfig.getParameter(PARAM_NAME, Entity2ACL.class);
            if (null == cached) {
                cached = Entity2ACLAutoDetector.implFor(getGreeting(imapStore, imapConfig), imapConfig);
                imapConfig.setParameter(PARAM_NAME, cached);
            }
            return cached;
        } catch (MessagingException e) {
            throw MimeMailException.handleMessagingException(e, imapConfig);
        }
    }

    /**
     * Resets entity2acl
     */
    protected final static void resetEntity2ACL() {
        singleton = null;
        instantiated = false;
    }

    /**
     * Only invoked if auto-detection is turned off
     *
     * @param singleton The singleton instance of {@link Entity2ACL}
     */
    protected final static void setInstance(Entity2ACL singleton) {
        Entity2ACL.singleton = singleton;
        instantiated = true;
    }

    /*-
     * Member section
     */

    /**
     * Initializes a new {@link Entity2ACL}
     */
    protected Entity2ACL() {
        super();
    }

    /**
     * Returns a newly created {@link UserGroupID} instance reflecting a found user.
     *
     * @param userId The user ID
     * @return A newly created {@link UserGroupID} instance reflecting a found user.
     */
    protected final UserGroupID getUserRetval(int userId) {
        if (userId < 0) {
            return UserGroupID.NULL;
        }
        return new UserGroupID(userId, false);
    }

    /**
     * Determines the entity name of the user/group whose ID matches given <code>entity</code> that is used in IMAP server's ACL list.
     *
     * @param entity The user/group ID
     * @param ctx The context
     * @param args The arguments container
     * @return the IMAP login of the user/group whose ID matches given <code>entity</code>
     * @throws OXException IF user/group could not be found
     */
    public abstract String getACLName(int entity, Context ctx, Entity2ACLArgs args) throws OXException;

    /**
     * Determines the user/group ID whose either ACL entity name or user name matches given <code>pattern</code>.
     *
     * @param pattern The pattern for either IMAP login or user name
     * @param ctx The context
     * @param args The arguments container
     * @return An instance of {@link UserGroupID} providing the user/group identifier whose IMAP login matches given <code>pattern</code> or
     *         {@link UserGroupID#NULL} if none found.
     * @throws OXException If user/group search fails
     */
    public abstract UserGroupID getEntityID(String pattern, Context ctx, Entity2ACLArgs args) throws OXException;

}
