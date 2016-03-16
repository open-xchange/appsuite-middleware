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
 * {@link CreateIcalVcardTables}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class CreateIcalVcardTables extends AbstractCreateTableImpl {
    
    private static final String icalPrincipalTableName = "ical_principal";
    private static final String icalIdsTableName = "ical_ids";
    private static final String vcardPrincipalTableName = "vcard_principal";
    private static final String vcardIdsTableName = "vcard_ids";

    private static final String createIcalPrincipalTable = "CREATE TABLE ical_principal (" 
        + "object_id INT4 UNSIGNED NOT NULL," 
        + "cid INT4 UNSIGNED NOT NULL," 
        + "principal text NOT NULL," 
        + "calendarfolder INT4 UNSIGNED NOT NULL," 
        + "taskfolder INT4 UNSIGNED NOT NULL,"
        + "PRIMARY KEY (cid, object_id)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String createIcalIdsTable = "CREATE TABLE ical_ids ("
        + "object_id INT4 UNSIGNED NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "principal_id INT4 UNSIGNED NOT NULL,"
        + "client_id text,"
        + "target_object_id int,"
        + "module int,"
        + "PRIMARY KEY (cid, object_id)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String createVcardPrincipalTable = "CREATE TABLE vcard_principal ("
        + "object_id int NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "principal text,"
        + "contactfolder int,"
        + "PRIMARY KEY (cid, object_id)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    private static final String createVcardIdsTable = "CREATE TABLE vcard_ids ("
        + "object_id int NOT NULL,"
        + "cid INT4 UNSIGNED NOT NULL,"
        + "principal_id int,"
        + "client_id text,"
        + "target_object_id int,"
        + "PRIMARY KEY (cid, object_id)"
      + ") ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    /**
     * Initializes a new {@link CreateIcalVcardTables}.
     */
    public CreateIcalVcardTables() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#requiredTables()
     */
    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.database.CreateTableService#tablesToCreate()
     */
    @Override
    public String[] tablesToCreate() {
        return new String[] { icalIdsTableName, icalPrincipalTableName, vcardIdsTableName, vcardPrincipalTableName };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { createIcalIdsTable, createIcalPrincipalTable, createVcardIdsTable, createVcardPrincipalTable };
    }

}
