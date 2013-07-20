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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mailaccount.internal;

import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.groupware.update.tasks.MailAccountCreateTablesTask;

/**
 * {@link CreateMailAccountTables}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class CreateMailAccountTables extends AbstractCreateTableImpl {

    public CreateMailAccountTables() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return createStatements;
    }

    @Override
    public String[] requiredTables() {
        return requiredTables;
    }

    @Override
    public String[] tablesToCreate() {
        return createdTables;
    }

    private static final String[] requiredTables = { "user" };

    private static final String[] createdTables = { "user_mail_account", "user_mail_account_properties", "user_transport_account", "user_transport_account_properties", "pop3_storage_ids", "pop3_storage_deleted" };

    private static final String[] createStatements = {
        MailAccountCreateTablesTask.getCreateMailAccount(),

        MailAccountCreateTablesTask.getCreateMailAccountProperties(),

        MailAccountCreateTablesTask.getCreateTransportAccount(),

        MailAccountCreateTablesTask.getCreateTransportAccountProperties(),

        "CREATE TABLE pop3_storage_ids ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "id INT4 UNSIGNED NOT NULL,"
        + "uidl VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "fullname VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "uid VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "PRIMARY KEY (cid, user, id, uidl),"
        + "FOREIGN KEY (cid, user) REFERENCES user (cid, id),"
        + "FOREIGN KEY (cid, user, id) REFERENCES user_mail_account (cid, user, id)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",

        "CREATE TABLE pop3_storage_deleted ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "user INT4 UNSIGNED NOT NULL,"
        + "id INT4 UNSIGNED NOT NULL,"
        + "uidl VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,"
        + "PRIMARY KEY (cid, user, id, uidl),"
        + "FOREIGN KEY (cid, user) REFERENCES user (cid, id),"
        + "FOREIGN KEY (cid, user, id) REFERENCES user_mail_account (cid, user, id)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci"
    };
}
