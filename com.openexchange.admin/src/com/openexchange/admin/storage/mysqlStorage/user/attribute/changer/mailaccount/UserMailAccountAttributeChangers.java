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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailaccount;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger;

/**
 * {@link UserMailAccountAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserMailAccountAttributeChangers extends AbstractUserAttributeChangers {

    private static final String TABLE = "user_mail_account";

    /**
     * Initialises a new {@link UserMailAccountAttributeChangers}.
     */
    public UserMailAccountAttributeChangers() {
        super(TABLE, EnumSet.allOf(UserMailAccountAttribute.class));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers#initialiseChangers()
     */
    @Override
    protected Map<Attribute, UserAttributeChanger> initialiseChangers() {
        Map<UserMailAccountAttribute, UserAttributeChanger> changers = new HashMap<>();
        changers.put(UserMailAccountAttribute.DRAFTS, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_drafts_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.DRAFTS, userData.getMail_folder_drafts_name());
                attributes.put(UserMailAccountAttribute.DRAFTS_FULLNAME, "");
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });

        changers.put(UserMailAccountAttribute.SENT, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_sent_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.SENT, userData.getMail_folder_sent_name());
                attributes.put(UserMailAccountAttribute.SENT_FULLNAME, "");
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });

        changers.put(UserMailAccountAttribute.SPAM, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_spam_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.SPAM, userData.getMail_folder_spam_name());
                attributes.put(UserMailAccountAttribute.SPAM_FULLNAME, "");
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });

        changers.put(UserMailAccountAttribute.TRASH, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_trash_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.TRASH, userData.getMail_folder_trash_name());
                attributes.put(UserMailAccountAttribute.TRASH_FULLNAME, "");
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });

        changers.put(UserMailAccountAttribute.ARCHIVE, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_archive_full_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.ARCHIVE, "");
                attributes.put(UserMailAccountAttribute.ARCHIVE_FULLNAME, userData.getMail_folder_archive_full_name());
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });

        changers.put(UserMailAccountAttribute.SPAM, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_confirmed_spam_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.SPAM, userData.getMail_folder_confirmed_spam_name());
                attributes.put(UserMailAccountAttribute.SPAM_FULLNAME, "");
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });

        changers.put(UserMailAccountAttribute.CONFIRMED_HAM, new AbstractUserMailAccountAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                if (userData.getMail_folder_confirmed_ham_name() == null) {
                    return false;
                }
                Map<Attribute, Object> attributes = new HashMap<>(2);
                attributes.put(UserMailAccountAttribute.CONFIRMED_HAM, userData.getMail_folder_confirmed_ham_name());
                attributes.put(UserMailAccountAttribute.CONFIRMED_HAM_FULLNAME, "");
                return setAttributes(userId, contextId, TABLE, attributes, connection);
            }
        });
        return Collections.unmodifiableMap(changers);
    }
}
