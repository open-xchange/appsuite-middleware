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

package com.openexchange.appstore.internal;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link AppStoreTables}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AppStoreTables extends AbstractCreateTableImpl {

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "userApplications", "applications", "applicationReleases" };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { "CREATE TABLE userApplications (" +
                " cid INT4 unsigned default NULL," +
                " userId INT4 unsigned default NULL," +
                " application varchar(255) default NULL," +
                " status varchar(64) default NULL," +
                " PRIMARY KEY (cid, userId, application)," +
                " FOREIGN KEY (cid, userId) REFERENCES user(cid, id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",

            "CREATE TABLE applications (" +
                " application varchar(255) default NULL," +
                " path varchar(255) default NULL," +
                " description text default NULL," +
                " manifest text default NULL," +
                " PRIMARY KEY (application)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",

            "CREATE TABLE applicationReleases (" +
                " application varchar(255) default NULL," +
                " cid INT4 unsigned default NULL," +
                " userId INT4 unsigned default NULL," +
                " state varchar(64) default NULL," +
                " PRIMARY KEY (application, cid, userId)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci"};
    }

}
