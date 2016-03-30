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

package com.openexchange.admin.mysql;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * Creates the tables required for the calendar.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class CreateCalendarTables extends AbstractCreateTableImpl {

    /**
     * Table name of prg_dates table
     */
    private static final String TABLE_PRG_DATES = "prg_dates";

    /**
     * SQL statement for prg_dates table
     */
    private static final String CREATE_PRG_DATES = "CREATE TABLE " + TABLE_PRG_DATES + " ("
        + "creating_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
        + "created_from INT4 UNSIGNED NOT NULL,"
        + "changing_date INT8 NOT NULL,"
        + "changed_from INT4 UNSIGNED NOT NULL,"
        + "fid INT4 UNSIGNED NOT NULL,"
        + "pflag INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "timestampfield01 DATETIME NOT NULL,"
        + "timestampfield02 DATETIME NOT NULL,"
        + "timezone VARCHAR(64) NOT NULL,"
        + "intfield01 INT4 UNSIGNED NOT NULL,"
        + "intfield02 INT4 UNSIGNED,"
        + "intfield03 INT4 UNSIGNED,"
        + "intfield04 INT4 UNSIGNED,"
        + "intfield05 INT4 UNSIGNED,"
        + "intfield06 INT4 UNSIGNED NOT NULL,"
        + "intfield07 INT4 UNSIGNED,"
        + "intfield08 INT4 UNSIGNED,"
        + "field01 VARCHAR(255),"
        + "field02 VARCHAR(255),"
        + "field04 TEXT,"
        + "field06 VARCHAR(64),"
        + "field07 TEXT,"
        + "field08 TEXT,"
        + "field09 VARCHAR(255),"
        + "uid VARCHAR(1024),"
        + "organizer VARCHAR(255),"
        + "sequence INT4 UNSIGNED,"
        + "organizerId INT4 UNSIGNED,"
        + "principal VARCHAR(255),"
        + "principalId INT4 UNSIGNED,"
        + "filename VARCHAR(255),"
        + "PRIMARY KEY (cid, intfield01, fid),"
        + "INDEX (cid, intfield02),"
        + "INDEX (cid, timestampfield01),"
        + "INDEX (cid, timestampfield02),"
        + "INDEX `uidIndex` (cid, uid(255)),"
        + "INDEX `changingDateIndex` (cid, changing_date)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of prg_date_rights table
     */
    private static final String TABLE_PRG_DATE_RIGHTS = "prg_date_rights";

    /**
     * SQL statement for prg_date_rights table
     */
    private static final String CREATE_PRG_DATE_RIGHTS = "CREATE TABLE " + TABLE_PRG_DATE_RIGHTS + " ("
        + "object_id INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "id INT4 NOT NULL,"
        + "type INT4 UNSIGNED NOT NULL,"
        + "ma VARCHAR(286),"
        + "dn VARCHAR(320),"
        + "PRIMARY KEY (cid, object_id, id, type)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of del_date_rights table
     */
    private static final String TABLE_DEL_DATE_RIGHTS = "del_date_rights";

    /**
     * SQL statement for del_date_rights table
     */
    private static final String CREATE_DEL_DATE_RIGHTS = "CREATE TABLE " + TABLE_DEL_DATE_RIGHTS + " ("
        + "object_id INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "id INT4 NOT NULL,"
        + "type INT4 UNSIGNED NOT NULL,"
        + "ma VARCHAR(286),"
        + "dn VARCHAR(320),"
        + "PRIMARY KEY (cid, object_id, id, type)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of del_dates table
     */
    private static final String TABLE_DEL_DATES = "del_dates";

    /**
     * SQL statement for del_dates table
     */
    private static final String CREATE_DEL_DATES = "CREATE TABLE " + TABLE_DEL_DATES + " ("
        + "creating_date timestamp DEFAULT CURRENT_TIMESTAMP,"
        + "created_from INT4 UNSIGNED NOT NULL,"
        + "changing_date INT8 NOT NULL,"
        + "changed_from INT4 UNSIGNED NOT NULL,"
        + "fid INT4 UNSIGNED NOT NULL,"
        + "pflag INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "timestampfield01 DATETIME,"
        + "timestampfield02 DATETIME,"
        + "timezone VARCHAR(64),"
        + "intfield01 INT4 UNSIGNED NOT NULL,"
        + "intfield02 INT4 UNSIGNED,"
        + "intfield03 INT4 UNSIGNED,"
        + "intfield04 INT4 UNSIGNED,"
        + "intfield05 INT4 UNSIGNED,"
        + "intfield06 INT4 UNSIGNED,"
        + "intfield07 INT4 UNSIGNED,"
        + "intfield08 INT4 UNSIGNED,"
        + "field01 VARCHAR(255),"
        + "field02 VARCHAR(255),"
        + "field04 TEXT,"
        + "field06 VARCHAR(255),"
        + "field07 text,"
        + "field08 TEXT,"
        + "field09 VARCHAR(255),"
        + "uid VARCHAR(1024),"
        + "organizer VARCHAR(255),"
        + "sequence INT4 UNSIGNED,"
        + "organizerId INT4 UNSIGNED,"
        + "principal VARCHAR(255),"
        + "principalId INT4 UNSIGNED,"
        + "filename VARCHAR(255),"
        + "PRIMARY KEY (cid, intfield01, fid),"
        + "INDEX (cid, intfield02),"
        + "INDEX (cid, timestampfield01),"
        + "INDEX (cid, timestampfield02),"
        + "INDEX `uidIndex` (cid, uid(255)),"
        + "INDEX `changingDateIndex` (cid, changing_date)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of del_dates_members table
     */
    private static final String TABLE_DEL_DATES_MEMBERS = "del_dates_members";

    /**
     * SQL statement for del_dates_members table
     */
    private static final String CREATE_DEL_DATES_MEMBERS_PRIMARY_KEY = "CREATE TABLE " + TABLE_DEL_DATES_MEMBERS + " ("
        + "object_id INT4,"
        + "member_uid INT4,"
        + "confirm INT4 UNSIGNED NOT NULL,"
        + "reason TEXT,"
        + "pfid INT4 DEFAULT -2 NOT NULL,"
        + "reminder INT4 UNSIGNED,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "PRIMARY KEY (cid, object_id, member_uid, pfid),"
        + "UNIQUE INDEX member (cid, member_uid, object_id),"
        + "INDEX `givenname` (cid, pfid)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of prg_dates_members table
     */
    private static final String TABLE_PRG_DATES_MEMBERS = "prg_dates_members";

    /**
     * SQL statement for prg_dates_members table
     */
    private static final String CREATE_PRG_DATES_MEMBERS_PRIMARY_KEY = "CREATE TABLE " + TABLE_PRG_DATES_MEMBERS + " ("
        + "object_id INT4,"
        + "member_uid INT4,"
        + "confirm INT4 UNSIGNED NOT NULL,"
        + "reason TEXT,"
        + "pfid INT4 DEFAULT -2 NOT NULL,"
        + "reminder INT4 UNSIGNED,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "PRIMARY KEY (cid, object_id, member_uid, pfid),"
        + "UNIQUE INDEX member (cid, member_uid, object_id),"
        + "INDEX `givenname` (cid, pfid)"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of dateExternal table
     */
    private static final String TABLE_DATE_EXTERNAL = "dateExternal";

    /**
     * SQL statement for dateExternal table
     */
    private static final String CREATE_DATE_EXTERNAL = "CREATE TABLE " + TABLE_DATE_EXTERNAL + " ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "objectId INT4 UNSIGNED NOT NULL,"
        + "mailAddress VARCHAR(255) NOT NULL,"
        + "displayName VARCHAR(255),"
        + "confirm INT4 UNSIGNED NOT NULL,"
        + "reason TEXT,"
        + "PRIMARY KEY (cid,objectId,`mailAddress`(255))"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of delDateExternal table
     */
    private static final String TABLE_DEL_DATE_EXTERNAL = "delDateExternal";

    /**
     * SQL statement for delDateExternal table
     */
    private static final String CREATE_DEL_DATE_EXTERNAL = "CREATE TABLE " + TABLE_DEL_DATE_EXTERNAL + " ("
        + "cid INT4 UNSIGNED NOT NULL,"
        + "objectId INT4 UNSIGNED NOT NULL,"
        + "mailAddress VARCHAR(255) NOT NULL,"
        + "displayName VARCHAR(255),"
        + "confirm INT4 UNSIGNED NOT NULL,"
        + "reason TEXT,"
        + "PRIMARY KEY (cid,objectId,`mailAddress`(255))"
        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateCalendarTables}.
     */
    public CreateCalendarTables() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] tablesToCreate() {
        return new String[] { TABLE_PRG_DATES, TABLE_PRG_DATE_RIGHTS, TABLE_DEL_DATE_RIGHTS, TABLE_DEL_DATES, TABLE_DEL_DATES_MEMBERS, TABLE_PRG_DATES_MEMBERS, TABLE_DATE_EXTERNAL, TABLE_DEL_DATE_EXTERNAL };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getCreateStatements() {
        return new String[] { CREATE_PRG_DATES, CREATE_PRG_DATE_RIGHTS, CREATE_DEL_DATE_RIGHTS, CREATE_DEL_DATES, CREATE_DEL_DATES_MEMBERS_PRIMARY_KEY,
            CREATE_PRG_DATES_MEMBERS_PRIMARY_KEY, CREATE_DATE_EXTERNAL, CREATE_DEL_DATE_EXTERNAL };
    }
}
