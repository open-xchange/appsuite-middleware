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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.imap.entity2acl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.config.IMAPConfig;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link Entity2ACL} - Maps numeric entity IDs to corresponding IMAP login name (used in ACLs) and vice versa
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class Entity2ACL {

    private static final AtomicBoolean instancialized = new AtomicBoolean();

    /**
     * The constant reflecting the found group {@link OCLPermission#ALL_GROUPS_AND_USERS}.
     */
    protected static final int[] ALL_GROUPS_AND_USERS = { OCLPermission.ALL_GROUPS_AND_USERS, 1 };

    /**
     * Singleton
     */
    private static Entity2ACL singleton;

    /**
     * Creates a new instance implementing the {@link Entity2ACL} interface.
     * 
     * @param imapConfig The user's IMAP config
     * @return an instance implementing the {@link Entity2ACL} interface.
     * @throws Entity2ACLException if the instance can't be created.
     */
    public static final Entity2ACL getInstance(final IMAPConfig imapConfig) throws Entity2ACLException {
        if (!instancialized.get()) {
            /*
             * Auto-detect dependent on user's IMAP settings
             */
            try {
                return getEntity2ACLImpl(imapConfig.getImapServerAddress(), imapConfig.getPort(), imapConfig.isSecure());
            } catch (final IMAPException e) {
                throw new Entity2ACLException(e);
            }
        }
        return singleton;
    }

    private static final Entity2ACL getEntity2ACLImpl(final InetAddress imapServer, final int port, final boolean isSecure) throws Entity2ACLException {
        try {
            return Entity2ACLAutoDetector.getEntity2ACLImpl(imapServer, port, isSecure);
        } catch (final IOException e) {
            throw new Entity2ACLException(Entity2ACLException.Code.IO_ERROR, e, e.getMessage());
        }
    }

    /**
     * Resets entity2acl
     */
    final static void resetEntity2ACL() {
        singleton = null;
        instancialized.set(false);
        Entity2ACLAutoDetector.resetEntity2ACLMappings();
    }

    /**
     * Only invoked if auto-detection is turned off
     * 
     * @param singleton The singleton instance of {@link Entity2ACL}
     */
    final static void setInstance(final Entity2ACL singleton) {
        Entity2ACL.singleton = singleton;
        instancialized.set(true);
    }

    /**
     * Initializes a new {@link Entity2ACL}
     */
    protected Entity2ACL() {
        super();
    }

    /**
     * Returns a newly created array of <code>int</code> reflecting a found user.
     * 
     * @param userId The user ID
     * @return A newly created array of <code>int</code> reflecting a found user.
     */
    protected final int[] getUserRetval(final int userId) {
        return new int[] { userId, 0 };
    }

    /**
     * Determines the entity name of the user/group whose ID matches given <code>entity</code> that is used in IMAP server's ACL list.
     * 
     * @param entity The user/group ID
     * @param ctx The context
     * @param args The arguments container
     * @return the IMAP login of the user/group whose ID matches given <code>entity</code>
     * @throws AbstractOXException If user/group could not be found
     */
    public abstract String getACLName(int entity, Context ctx, Entity2ACLArgs args) throws AbstractOXException;

    /**
     * Determines the user/group ID whose either ACL entity name or user name matches given <code>pattern</code>.
     * 
     * @param pattern The pattern for either IMAP login or user name
     * @param ctx The context
     * @param args The arguments container
     * @return An array of <code>int</code> with length 2. The first index contains the user/group ID whose IMAP login matches given
     *         <code>pattern</code> or <code>-1</code> if none found. The second index reflects whether matched entity is a group or not:
     *         &gt;= 1 means a group.
     * @throws AbstractOXException If user/group search fails
     */
    public abstract int[] getEntityID(final String pattern, Context ctx, Entity2ACLArgs args) throws AbstractOXException;

}
