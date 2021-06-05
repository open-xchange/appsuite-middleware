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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailsetting;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger;
import com.openexchange.exception.OXException;
import com.openexchange.mail.usersetting.CachingUserSettingMailStorage;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link UserSettingMailAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserSettingMailAttributeChangers extends AbstractUserAttributeChangers {

    static final Logger LOGGER = LoggerFactory.getLogger(UserSettingMailAttributeChangers.class);

    private static final String TABLE = "user_setting_mail";

    /**
     * Initialises a new {@link UserSettingMailAttributeChangers}.
     */
    public UserSettingMailAttributeChangers() {
        super(TABLE, EnumSet.allOf(UserMailSettingAttribute.class));
    }

    @Override
    protected Map<Attribute, UserAttributeChanger> initialiseChangers() {
        Map<UserMailSettingAttribute, UserAttributeChanger> c = new HashMap<>();
        c.put(UserMailSettingAttribute.BITS, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Boolean loadRemoteMailContentByDefault = userData.isLoadRemoteMailContentByDefault();
                if (loadRemoteMailContentByDefault == null) {
                    return false;
                }
                try {
                    UserSettingMail userSettingMail = CachingUserSettingMailStorage.getInstance().getUserSettingMail(userId, contextId);
                    if (loadRemoteMailContentByDefault.booleanValue() != userSettingMail.isAllowHTMLImages()) {
                        userSettingMail.setAllowHTMLImages(loadRemoteMailContentByDefault.booleanValue());
                        return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.BITS, Integer.valueOf(userSettingMail.getBitsValue())), connection);
                    }
                } catch (OXException e) {
                    // Fall through
                    LOGGER.warn("Couldn't change permission bits", e);
                }
                return false;
            }
        });
        c.put(UserMailSettingAttribute.SEND_ADDRESS, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String defaultSenderAddress = userData.getDefaultSenderAddress();
                if (defaultSenderAddress == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.SEND_ADDRESS, defaultSenderAddress), connection);
            }
        });
        c.put(UserMailSettingAttribute.STD_DRAFTS, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String value = userData.getMail_folder_drafts_name();
                if (value == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.STD_DRAFTS, value), connection);
            }
        });
        c.put(UserMailSettingAttribute.STD_SENT, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String value = userData.getMail_folder_sent_name();
                if (value == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.STD_SENT, value), connection);
            }
        });
        c.put(UserMailSettingAttribute.STD_SPAM, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String value = userData.getMail_folder_spam_name();
                if (value == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.STD_SPAM, value), connection);
            }
        });
        c.put(UserMailSettingAttribute.STD_TRASH, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String value = userData.getMail_folder_trash_name();
                if (value == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.STD_TRASH, value), connection);
            }
        });
        c.put(UserMailSettingAttribute.CONFIRMED_HAM, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String value = userData.getMail_folder_confirmed_ham_name();
                if (value == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.CONFIRMED_HAM, value), connection);
            }
        });
        c.put(UserMailSettingAttribute.CONFIRMED_SPAM, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String value = userData.getMail_folder_confirmed_spam_name();
                if (value == null) {
                    return false;
                }
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.CONFIRMED_SPAM, value), connection);
            }
        });
        c.put(UserMailSettingAttribute.UPLOAD_QUOTA, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Integer value = userData.getUploadFileSizeLimit();
                if (value != null) {
                    return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.UPLOAD_QUOTA, value), connection);
                } else if (userData.isUploadFileSizeLimitset()) {
                    return setAttributesDefault(userId, contextId, TABLE, Collections.singleton(UserMailSettingAttribute.UPLOAD_QUOTA), connection);
                }
                return false;
            }
        });
        c.put(UserMailSettingAttribute.UPLOAD_QUOTA_PER_FILE, new AbstractUserSettingMailAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Integer value = userData.getUploadFileSizeLimitPerFile();
                if (value != null) {
                    return setAttributes(userId, contextId, TABLE, Collections.singletonMap(UserMailSettingAttribute.UPLOAD_QUOTA_PER_FILE, value), connection);
                } else if (userData.isUploadFileSizeLimitPerFileset()) {
                    return setAttributesDefault(userId, contextId, TABLE, Collections.singleton(UserMailSettingAttribute.UPLOAD_QUOTA_PER_FILE), connection);
                }
                return false;
            }
        });
        return Collections.unmodifiableMap(c);
    }
}
