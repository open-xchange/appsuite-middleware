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

import java.util.function.Function;
import com.openexchange.admin.rmi.dataobjects.ExtendableDataObject;
import com.openexchange.admin.rmi.dataobjects.User;

/**
 * {@link UserMailAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public enum UserMailAttribute implements Attribute {

    SEND_ADDRESS("send_addr", (user) -> user.getDefaultSenderAddress(), String.class),
    STD_DRAFTS("std_drafts", (user) -> user.getMail_folder_drafts_name(), String.class),
    STD_SENT("std_sent", (user) -> user.getMail_folder_sent_name(), String.class),
    STD_SPAM("std_spam", (user) -> user.getMail_folder_spam_name(), String.class),
    STD_TRASH("std_trash", (user) -> user.getMail_folder_trash_name(), String.class),
    CONFIRMED_SPAM("confirmed_spam", (user) -> user.getMail_folder_confirmed_spam_name(), String.class),
    CONFIRMED_HAM("confirmed_ham", (user) -> user.getMail_folder_confirmed_ham_name(), String.class),
    UPLOAD_QUOTA("upload_quota", (user) -> Integer.toString(user.getUploadFileSizeLimit()), Integer.class),
    UPLOAD_QUOTA_PER_FILE("upload_quota_per_file", (user) -> Integer.toString(user.getUploadFileSizeLimitPerFile()), Integer.class),
    ;

    private final String sqlFieldName;
    private final Function<User, String> getter;
    private final Class<?> originalType;
    private static final String TABLE_NAME = "user_setting_mail";

    /**
     * 
     * Initialises a new {@link UserMailAttribute}.
     * 
     * @param sqlFieldNames the names of the attribute
     */
    private UserMailAttribute(String sqlFieldName, Function<User, String> getter, Class<?> originalType) {
        this.sqlFieldName = sqlFieldName;
        this.getter = getter;
        this.originalType = originalType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getSQLFieldName()
     */
    @Override
    public String getSQLFieldName() {
        return sqlFieldName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getSQLTableName()
     */
    @Override
    public String getSQLTableName() {
        return TABLE_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getValue(com.openexchange.admin.rmi.dataobjects.ExtendableDataObject)
     */
    @Override
    public <T extends ExtendableDataObject> String getValue(T object) {
        return getter.apply((User) object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getOriginalType()
     */
    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }
}
