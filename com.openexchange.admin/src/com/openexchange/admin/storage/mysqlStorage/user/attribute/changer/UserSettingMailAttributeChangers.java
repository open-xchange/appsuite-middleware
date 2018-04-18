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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.UserMailAttribute;

/**
 * {@link UserSettingMailAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class UserSettingMailAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(UserSettingMailAttributeChangers.class);

    private final Map<UserMailAttribute, UserAttributeChanger> changers;

    /**
     * Initialises a new {@link UserSettingMailAttributeChangers}.
     */
    public UserSettingMailAttributeChangers() {
        super();
        changers = initialiseChangers();
    }

    /**
     * Initialises the changers
     * 
     * @return a map with the changers
     */
    private Map<UserMailAttribute, UserAttributeChanger> initialiseChangers() {
        Map<UserMailAttribute, UserAttributeChanger> c = new HashMap<>();
        c.put(UserMailAttribute.SEND_ADDRESS, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.SEND_ADDRESS, userData.getDefaultSenderAddress(), connection);
            }
        });
        c.put(UserMailAttribute.STD_DRAFTS, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.STD_DRAFTS, userData.getMail_folder_drafts_name(), connection);
            }
        });
        c.put(UserMailAttribute.STD_SENT, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.STD_SENT, userData.getMail_folder_sent_name(), connection);
            }
        });
        c.put(UserMailAttribute.STD_SPAM, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.STD_SPAM, userData.getMail_folder_spam_name(), connection);
            }
        });
        c.put(UserMailAttribute.STD_TRASH, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.STD_TRASH, userData.getMail_folder_trash_name(), connection);
            }
        });
        c.put(UserMailAttribute.CONFIRMED_HAM, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.CONFIRMED_HAM, userData.getMail_folder_confirmed_ham_name(), connection);
            }
        });
        c.put(UserMailAttribute.CONFIRMED_SPAM, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.CONFIRMED_SPAM, userData.getMail_folder_confirmed_spam_name(), connection);
            }
        });
        c.put(UserMailAttribute.UPLOAD_QUOTA, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.UPLOAD_QUOTA, userData.getUploadFileSizeLimit(), connection);
            }
        });
        c.put(UserMailAttribute.UPLOAD_QUOTA_PER_FILE, new AbstractSingleAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                return setAttribute(userId, contextId, UserMailAttribute.UPLOAD_QUOTA_PER_FILE, userData.getUploadFileSizeLimitPerFile(), connection);
            }
        });
        return Collections.unmodifiableMap(c);
    }

    /**
     * Changes the specified {@link UserMailAttribute}
     * 
     * @param userAttribute The {@link UserMailAttribute} to change
     * @param userData The {@link User} data
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param connection The {@link Connection} to use
     * @return <code>true</code> if the attribute was changed successfully; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    public boolean change(UserMailAttribute userAttribute, User userData, int userId, int contextId, Connection connection) throws SQLException {
        UserAttributeChanger changer = changers.get(userAttribute);
        if (changer == null) {
            LOG.debug("No user attribute changer found for user mail attribute '{}'. The attribute will not be changed.", userAttribute.getSQLFieldName());
            return false;
        }
        return changer.changeAttribute(userId, contextId, userData, connection);
    }
}
