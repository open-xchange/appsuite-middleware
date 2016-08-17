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
 * Creates the tables required for contacts.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class CreateContactsTables extends AbstractCreateTableImpl {

    /**
     * Table name of prg_dlist table
     */
    private static final String TABLE_PRG_DLIST = "prg_dlist";

    /**
     * SQL statement for prg_dlist table
     */
    private static final String CREATE_PRG_DLIST_PRIMARY_KEY = "CREATE TABLE " + TABLE_PRG_DLIST + " ("
        + "intfield01 INT4 NOT NULL,"
        + "intfield02 INT4,"
        + "intfield03 INT4,"
        + "intfield04 INT4,"
        + "field01 VARCHAR(320),"
        + "field02 VARCHAR(128),"
        + "field03 VARCHAR(128),"
        + "field04 VARCHAR(128),"
        + "cid INT4 NOT NULL,"
        + "uuid binary(16) NOT NULL,"
        + "PRIMARY KEY(uuid, cid, intfield01),"
        + "INDEX (intfield01, cid),"
        + "INDEX (intfield01, intfield02, intfield03, cid)"
        + ") ENGINE  = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of del_dlist table
     */
    private static final String TABLE_DEL_DLIST = "del_dlist";

    /**
     * SQL statement for del_dlist table
     */
    private static final String CREATE_DEL_DLIST_PRIMARY_KEY = "CREATE TABLE " + TABLE_DEL_DLIST + " ("
        + "intfield01 INT4 NOT NULL,"
        + "intfield02 INT4,"
        + "intfield03 INT4,"
        + "intfield04 INT4,"
        + "field01 VARCHAR(320),"
        + "field02 VARCHAR(128),"
        + "field03 VARCHAR(128),"
        + "field04 VARCHAR(128),"
        + "cid INT4 NOT NULL,"
        + "uuid binary(16) NOT NULL,"
        + "PRIMARY KEY (uuid, cid, intfield01),"
        + "INDEX (intfield01, cid),"
        + "INDEX (intfield01, intfield02, intfield03, cid)"
        + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of prg_contacts_linkage table
     */
    private static final String TABLE_PRG_CONTACTS_LINKAGE = "prg_contacts_linkage";

    /**
     * SQL statement for prg_contacts_linkage table
     */
    private static final String CREATE_PRG_CONTACTS_LINKAGE_PRIMARY_KEY = "CREATE TABLE " + TABLE_PRG_CONTACTS_LINKAGE + " ("
        + "intfield01 INT4 NOT NULL,"
        + "intfield02 INT4 NOT NULL,"
        + "field01 VARCHAR(320),"
        + "field02 VARCHAR(320),"
        + "cid INT4 NOT NULL,"
        + "uuid binary(16) NOT NULL,"
        + "PRIMARY KEY (cid, uuid),"
        + "INDEX (intfield01, intfield02, cid),"
        + "INDEX (cid)"
        + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of prg_contacts_image table
     */
    private static final String TABLE_PRG_CONTACTS_IMAGE = "prg_contacts_image";

    /**
     * SQL statement for prg_contacts_image table
     */
    private static final String CREATE_PRG_CONTACTS_IMAGE = "CREATE TABLE " + TABLE_PRG_CONTACTS_IMAGE + " ("
        + "intfield01 INT4 NOT NULL,"
        + "image1 MEDIUMBLOB,"
        + "changing_date INT8 NOT NULL,"
        + "mime_type VARCHAR(32) NOT NULL,"
        + "cid INT4 NOT NULL,"
        + "PRIMARY KEY (cid, intfield01)"
        + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of del_contacts_image table
     */
    private static final String TABLE_DEL_CONTACTS_IMAGE = "del_contacts_image";

    /**
     * SQL statement for del_contacts_image table
     */
    private static final String CREATE_DEL_CONTACTS_IMAGE = "CREATE TABLE " + TABLE_DEL_CONTACTS_IMAGE + " ("
        + "intfield01 INT4 NOT NULL,"
        + "image1 MEDIUMBLOB,"
        + "changing_date INT8 NOT NULL,"
        + "mime_type VARCHAR(32) NOT NULL,"
        + "cid INT4 NOT NULL,"
        + "PRIMARY KEY (cid, intfield01)"
        + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of del_contacts table
     */
    private static final String TABLE_DEL_CONTACTS = "del_contacts";

    /**
     * SQL statement for del_contacts table
     */
    private static final String CREATE_DEL_CONTACTS = "CREATE TABLE " + TABLE_DEL_CONTACTS + " ("
        + "creating_date INT8 NOT NULL,"
        + "created_from INT4 NOT NULL,"
        + "changing_date INT8 NOT NULL,"
        + "changed_from INT4,"
        + "fid INT4 NOT NULL,"
        + "cid INT4 NOT NULL,"
        + "userid INT4,"
        + "pflag INT4,"
        + "timestampfield01 DATE,"
        + "timestampfield02 DATE,"
        + "intfield01 INT4 NOT NULL,"
        + "intfield02 INT4,"
        + "intfield03 INT4,"
        + "intfield04 INT4,"
        + "intfield05 INT4,"
        + "intfield06 INT4,"
        + "intfield07 INT4,"
        + "intfield08 INT4,"
        + "field01 VARCHAR(320),"
        + "field02 VARCHAR(128),"
        + "field03 VARCHAR(128),"
        + "field04 VARCHAR(128),"
        + "field05 VARCHAR(64),"
        + "field06 VARCHAR(64),"
        + "field07 VARCHAR(256),"
        + "field08 VARCHAR(64),"
        + "field09 VARCHAR(64),"
        + "field10 VARCHAR(64),"
        + "field11 VARCHAR(64),"
        + "field12 VARCHAR(64),"
        + "field13 VARCHAR(64),"
        + "field14 VARCHAR(64),"
        + "field15 VARCHAR(64),"
        + "field16 VARCHAR(64),"
        + "field17 VARCHAR(5680),"
        + "field18 VARCHAR(512),"
        + "field19 VARCHAR(128),"
        + "field20 VARCHAR(128),"
        + "field21 VARCHAR(64),"
        + "field22 VARCHAR(64),"
        + "field23 VARCHAR(256),"
        + "field24 VARCHAR(64),"
        + "field25 VARCHAR(128),"
        + "field26 VARCHAR(64),"
        + "field27 VARCHAR(64),"
        + "field28 VARCHAR(64),"
        + "field29 VARCHAR(64),"
        + "field30 VARCHAR(128),"
        + "field31 VARCHAR(64),"
        + "field32 VARCHAR(64),"
        + "field33 VARCHAR(64),"
        + "field34 TEXT,"
        + "field35 VARCHAR(64),"
        + "field36 VARCHAR(64),"
        + "field37 VARCHAR(256),"
        + "field38 VARCHAR(64),"
        + "field39 VARCHAR(64),"
        + "field40 VARCHAR(64),"
        + "field41 VARCHAR(64),"
        + "field42 VARCHAR(64),"
        + "field43 VARCHAR(64),"
        + "field44 VARCHAR(128),"
        + "field45 VARCHAR(64),"
        + "field46 VARCHAR(64),"
        + "field47 VARCHAR(64),"
        + "field48 VARCHAR(64),"
        + "field49 VARCHAR(64),"
        + "field50 VARCHAR(64),"
        + "field51 VARCHAR(64),"
        + "field52 VARCHAR(64),"
        + "field53 VARCHAR(64),"
        + "field54 VARCHAR(64),"
        + "field55 VARCHAR(64),"
        + "field56 VARCHAR(64),"
        + "field57 VARCHAR(64),"
        + "field58 VARCHAR(64),"
        + "field59 VARCHAR(64),"
        + "field60 VARCHAR(64),"
        + "field61 VARCHAR(64),"
        + "field62 VARCHAR(64),"
        + "field63 VARCHAR(64),"
        + "field64 VARCHAR(64),"
        + "field65 VARCHAR(256),"
        + "field66 VARCHAR(256),"
        + "field67 VARCHAR(256),"
        + "field68 VARCHAR(128),"
        + "field69 VARCHAR(1024),"
        + "field70 VARCHAR(64),"
        + "field71 VARCHAR(64),"
        + "field72 VARCHAR(64),"
        + "field73 VARCHAR(64),"
        + "field74 VARCHAR(64),"
        + "field75 VARCHAR(64),"
        + "field76 VARCHAR(64),"
        + "field77 VARCHAR(64),"
        + "field78 VARCHAR(64),"
        + "field79 VARCHAR(64),"
        + "field80 VARCHAR(64),"
        + "field81 VARCHAR(64),"
        + "field82 VARCHAR(64),"
        + "field83 VARCHAR(64),"
        + "field84 VARCHAR(64),"
        + "field85 VARCHAR(64),"
        + "field86 VARCHAR(64),"
        + "field87 VARCHAR(64),"
        + "field88 VARCHAR(64),"
        + "field89 VARCHAR(64),"
        + "field90 VARCHAR(320),"
        + "useCount INT4 UNSIGNED,"
        + "yomiFirstName VARCHAR(128),"
        + "yomiLastName VARCHAR(128),"
        + "yomiCompany VARCHAR(512),"
        + "homeAddress VARCHAR(512) collate utf8_unicode_ci default NULL,"
        + "businessAddress VARCHAR(512) collate utf8_unicode_ci default NULL,"
        + "otherAddress VARCHAR(512) collate utf8_unicode_ci default NULL,"
        + "uid VARCHAR(255) collate utf8_unicode_ci default NULL,"
        + "filename VARCHAR(255) collate utf8_unicode_ci default NULL,"
        + "vCardId VARCHAR(256) collate utf8_unicode_ci default NULL,"
        + "INDEX (created_from),"
        + "INDEX (changing_date),"
        + "INDEX (userid),"
        + "INDEX (cid, fid),"
        + "INDEX `givenname` (`cid`,`field03`),"
        + "INDEX `surname` (`cid`,`field02`),"
        + "INDEX `displayname` (`cid`,`field01`(255)),"
        + "INDEX `email1` (`cid`,`field65`(255)),"
        + "INDEX `email2` (`cid`,`field66`(255)),"
        + "INDEX `email3` (`cid`,`field67`(255)),"
        + "PRIMARY KEY (cid, intfield01, fid)"
        + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Table name of prg_contacts table
     */
    private static final String TABLE_PRG_CONTACTS = "prg_contacts";

    /**
     * SQL statement for prg_contacts table
     */
    private static final String CREATE_PRG_CONTACTS = "CREATE TABLE " + TABLE_PRG_CONTACTS + " ("
        + "creating_date INT8 NOT NULL,"
        + "created_from INT4 NOT NULL,"
        + "changing_date INT8 NOT NULL,"
        + "changed_from INT4,"
        + "fid INT4 NOT NULL,"
        + "cid INT4 NOT NULL,"
        + "userid INT4,"
        + "pflag INT4,"
        + "timestampfield01 DATE,"
        + "timestampfield02 DATE,"
        + "intfield01 INT4 NOT NULL,"
        + "intfield02 INT4,"
        + "intfield03 INT4,"
        + "intfield04 INT4,"
        + "intfield05 INT4,"
        + "intfield06 INT4,"
        + "intfield07 INT4,"
        + "intfield08 INT4,"
        + "field01 VARCHAR(320),"
        + "field02 VARCHAR(128),"
        + "field03 VARCHAR(128),"
        + "field04 VARCHAR(128),"
        + "field05 VARCHAR(64),"
        + "field06 VARCHAR(64),"
        + "field07 VARCHAR(256),"
        + "field08 VARCHAR(64),"
        + "field09 VARCHAR(64),"
        + "field10 VARCHAR(64),"
        + "field11 VARCHAR(64),"
        + "field12 VARCHAR(64),"
        + "field13 VARCHAR(64),"
        + "field14 VARCHAR(64),"
        + "field15 VARCHAR(64),"
        + "field16 VARCHAR(64),"
        + "field17 VARCHAR(5680),"
        + "field18 VARCHAR(512),"
        + "field19 VARCHAR(128),"
        + "field20 VARCHAR(128),"
        + "field21 VARCHAR(64),"
        + "field22 VARCHAR(64),"
        + "field23 VARCHAR(256),"
        + "field24 VARCHAR(64),"
        + "field25 VARCHAR(128),"
        + "field26 VARCHAR(64),"
        + "field27 VARCHAR(64),"
        + "field28 VARCHAR(64),"
        + "field29 VARCHAR(64),"
        + "field30 VARCHAR(128),"
        + "field31 VARCHAR(64),"
        + "field32 VARCHAR(64),"
        + "field33 VARCHAR(64),"
        + "field34 TEXT,"
        + "field35 VARCHAR(64),"
        + "field36 VARCHAR(64),"
        + "field37 VARCHAR(256),"
        + "field38 VARCHAR(64),"
        + "field39 VARCHAR(64),"
        + "field40 VARCHAR(64),"
        + "field41 VARCHAR(64),"
        + "field42 VARCHAR(64),"
        + "field43 VARCHAR(64),"
        + "field44 VARCHAR(128),"
        + "field45 VARCHAR(64),"
        + "field46 VARCHAR(64),"
        + "field47 VARCHAR(64),"
        + "field48 VARCHAR(64),"
        + "field49 VARCHAR(64),"
        + "field50 VARCHAR(64),"
        + "field51 VARCHAR(64),"
        + "field52 VARCHAR(64),"
        + "field53 VARCHAR(64),"
        + "field54 VARCHAR(64),"
        + "field55 VARCHAR(64),"
        + "field56 VARCHAR(64),"
        + "field57 VARCHAR(64),"
        + "field58 VARCHAR(64),"
        + "field59 VARCHAR(64),"
        + "field60 VARCHAR(64),"
        + "field61 VARCHAR(64),"
        + "field62 VARCHAR(64),"
        + "field63 VARCHAR(64),"
        + "field64 VARCHAR(64),"
        + "field65 VARCHAR(256),"
        + "field66 VARCHAR(256),"
        + "field67 VARCHAR(256),"
        + "field68 VARCHAR(128),"
        + "field69 VARCHAR(1024),"
        + "field70 VARCHAR(64),"
        + "field71 VARCHAR(64),"
        + "field72 VARCHAR(64),"
        + "field73 VARCHAR(64),"
        + "field74 VARCHAR(64),"
        + "field75 VARCHAR(64),"
        + "field76 VARCHAR(64),"
        + "field77 VARCHAR(64),"
        + "field78 VARCHAR(64),"
        + "field79 VARCHAR(64),"
        + "field80 VARCHAR(64),"
        + "field81 VARCHAR(64),"
        + "field82 VARCHAR(64),"
        + "field83 VARCHAR(64),"
        + "field84 VARCHAR(64),"
        + "field85 VARCHAR(64),"
        + "field86 VARCHAR(64),"
        + "field87 VARCHAR(64),"
        + "field88 VARCHAR(64),"
        + "field89 VARCHAR(64),"
        + "field90 VARCHAR(320),"
        + "useCount INT4 UNSIGNED DEFAULT 0,"
        + "yomiFirstName VARCHAR(128),"
        + "yomiLastName VARCHAR(128),"
        + "yomiCompany VARCHAR(512),"
        + "homeAddress VARCHAR(512) collate utf8_unicode_ci default NULL,"
        + "businessAddress VARCHAR(512) collate utf8_unicode_ci default NULL,"
        + "otherAddress VARCHAR(512) collate utf8_unicode_ci default NULL,"
        + "uid VARCHAR(255) collate utf8_unicode_ci default NULL,"
        + "filename VARCHAR(255) collate utf8_unicode_ci default NULL,"
        + "vCardId VARCHAR(256) collate utf8_unicode_ci default NULL,"
        + "INDEX (created_from),"
        + "INDEX (changing_date),"
        + "INDEX (userid),"
        + "INDEX (cid, fid),"
        + "INDEX `givenname` (`cid`,`field03`),"
        + "INDEX `surname` (`cid`,`field02`),"
        + "INDEX `displayname` (`cid`,`field01`(255)),"
        + "INDEX `email1` (`cid`,`field65`(255)),"
        + "INDEX `email2` (`cid`,`field66`(255)),"
        + "INDEX `email3` (`cid`,`field67`(255)),"
        + "INDEX `department` (`cid`,`field19`(128)),"
        + "PRIMARY KEY (cid, intfield01, fid)"
        + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    /**
     * Initializes a new {@link CreateContactsTables}.
     */
    public CreateContactsTables() {
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
        return new String[] {
            TABLE_PRG_DLIST, TABLE_DEL_DLIST, TABLE_PRG_CONTACTS_LINKAGE, TABLE_PRG_CONTACTS_IMAGE, TABLE_DEL_CONTACTS_IMAGE,
            TABLE_DEL_CONTACTS, TABLE_PRG_CONTACTS };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getCreateStatements() {
        return new String[] { CREATE_PRG_DLIST_PRIMARY_KEY, CREATE_DEL_DLIST_PRIMARY_KEY, CREATE_PRG_CONTACTS_LINKAGE_PRIMARY_KEY, CREATE_PRG_CONTACTS_IMAGE,
            CREATE_DEL_CONTACTS_IMAGE, CREATE_DEL_CONTACTS, CREATE_PRG_CONTACTS };
    }

}
