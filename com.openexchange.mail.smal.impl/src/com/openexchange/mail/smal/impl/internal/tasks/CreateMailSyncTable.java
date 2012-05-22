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

package com.openexchange.mail.smal.impl.internal.tasks;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CreateMailSyncTable}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CreateMailSyncTable extends AbstractCreateTableImpl {

    public static final String MAIL_SYNC_TABLE = "mailSync";

    public static final String CREATE_MAIL_SYNC_TABLE_STATEMENT =
        "CREATE TABLE "+ MAIL_SYNC_TABLE + " (" +
        "cid INT4 unsigned NOT NULL," +
        "user INT4 unsigned NOT NULL," +
        "accountId INT4 unsigned NOT NULL," +
        "fullName varchar(128) collate utf8_unicode_ci NOT NULL," +
        "timestamp bigint(64) NOT NULL," +
        "sync tinyint(3) unsigned NOT NULL," +
        "PRIMARY KEY (cid, user, accountId, fullName)," +
        "INDEX accountIndex (cid, user, accountId)," +
        "INDEX timestampIndex (cid, user, accountId, timestamp)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";


    public CreateMailSyncTable() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { CREATE_MAIL_SYNC_TABLE_STATEMENT };
    }

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { MAIL_SYNC_TABLE };
    }

}
