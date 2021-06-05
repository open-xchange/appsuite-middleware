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
