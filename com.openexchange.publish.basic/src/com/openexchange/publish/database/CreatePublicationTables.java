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

package com.openexchange.publish.database;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * Creates tables necessary to run the publish part of PubSub.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CreatePublicationTables extends AbstractCreateTableImpl {

    public static final String CREATE_USER_AND_PASSWORD_CREATE_STATEMENT =
        "CREATE TABLE publication_users (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "id INT4 UNSIGNED NOT NULL," +
            "name VARCHAR(255) NOT NULL," +
        	"password VARCHAR(255) NOT NULL," +
        	"created INT8 NOT NULL DEFAULT 0," +
        	"lastModified INT8 NOT NULL DEFAULT 0," +
        	"PRIMARY KEY (cid,id,name)," +
        	"FOREIGN KEY (cid,id) REFERENCES publications(cid,id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci";

    @Override
    public String[] getCreateStatements() {
        return new String[] {
            "CREATE TABLE publications ("
            + "cid INT4 UNSIGNED NOT NULL,"
            + "id INT4 UNSIGNED NOT NULL,"
            + "user_id INT4 UNSIGNED NOT NULL,"
            + "entity INT4 UNSIGNED NOT NULL,"
            + "module VARCHAR(255) NOT NULL,"
            + "configuration_id INT4 UNSIGNED NOT NULL,"
            + "target_id VARCHAR(255) NOT NULL,"
            + "enabled BOOLEAN DEFAULT true NOT NULL,"
            + "created INT8 NOT NULL DEFAULT 0,"
            + "lastModified INT8 NOT NULL DEFAULT 0,"
            + "PRIMARY KEY (cid,id),"
            + "FOREIGN KEY(cid,user_id) REFERENCES user(cid,id)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",

            "CREATE TABLE sequence_publications ("
            + "cid INT4 UNSIGNED NOT NULL,"
            + "id INT4 UNSIGNED NOT NULL,"
            + "PRIMARY KEY (cid)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",

            CREATE_USER_AND_PASSWORD_CREATE_STATEMENT
        };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "publications", "sequence_publications", "publication_users" };
    }
}
