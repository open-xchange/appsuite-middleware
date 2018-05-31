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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailaccount;

import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link UserMailAccountAttribute}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum UserMailAccountAttribute implements Attribute {

    DRAFTS("standard-drafts-folder-name", "drafts", String.class),
    DRAFTS_FULLNAME("standard-drafts-folder-fullname", "drafts_fullname", String.class),

    SENT("standard-sent-folder-name", "sent", String.class),
    SENT_FULLNAME("standard-sent-folder-fullname", "sent_fullname", String.class),

    SPAM("standard-spam-folder-name", "spam", String.class),
    SPAM_FULLNAME("standard-spam-folder-name", "spam_fullname", String.class),

    TRASH("standard-trash-folder-name", "trash", String.class),
    TRASH_FULLNAME("standard-trash-folder-fullname", "trash_fullname", String.class),

    ARCHIVE("standard-archive-folder-name", "archive", String.class),
    ARCHIVE_FULLNAME("standard-archive-folder-fullname", "archive_fullname", String.class),

    CONFIRMED_HAM("standard-confirmed-ham-folder-name", "confirmed_ham", String.class),
    CONFIRMED_HAM_FULLNAME("standard-confirmed-ham-folder-fullname", "confirmed_ham_fullname", String.class),

    CONFIRMED_SPAM("standard-confirmed-spam-folder-name", "confirmed_spam", String.class),
    CONFIRMED_SPAM_FULLNAME("standard-confirmed-spam-folder-fullname", "confirmed_spam_fullname", String.class),
    ;

    private final String sqlFieldName;
    private final Class<?> originalType;
    private static final String TABLE = "user_mail_account";
    private final String attributeName;

    /**
     * 
     * Initialises a new {@link UserMailAccountAttribute}.
     * 
     * @param sqlFieldName the names of the attribute
     */
    private UserMailAccountAttribute(String attributeName, String sqlFieldName, Class<?> originalType) {
        this.attributeName = attributeName;
        this.sqlFieldName = sqlFieldName;
        this.originalType = originalType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.Attribute#getSQLFieldName()
     */
    @Override
    public String getSQLFieldName() {
        return sqlFieldName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.Attribute#getOriginalType()
     */
    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.Attribute#getSQLTableName()
     */
    @Override
    public String getSQLTableName() {
        return TABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute#getName()
     */
    @Override
    public String getName() {
        return attributeName;
    }
}
