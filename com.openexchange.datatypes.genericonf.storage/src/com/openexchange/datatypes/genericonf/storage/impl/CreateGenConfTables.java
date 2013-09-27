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

package com.openexchange.datatypes.genericonf.storage.impl;

import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.groupware.update.FullPrimaryKeySupportService;

/**
 * Creates the tables for the generic conf storage if a new schema is created.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class CreateGenConfTables extends AbstractCreateTableImpl {

    private final FullPrimaryKeySupportService fullPrimaryKeySupportService;

    /**
     * Default constructor.
     */
    public CreateGenConfTables(FullPrimaryKeySupportService fullPrimaryKeySupportService) {
        super();
        this.fullPrimaryKeySupportService = fullPrimaryKeySupportService;
    }

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        return createdTables;
    }

    @Override
    public String[] getCreateStatements() {
        if (fullPrimaryKeySupportService.isFullPrimaryKeySupported()) {
            return createsPrimaryKey;
        }
        return creates;
    }

    private static final String[] createdTables = { "genconf_attributes_strings", "genconf_attributes_bools", "sequence_genconf" };

    private static final String[] creates = {
        "CREATE TABLE genconf_attributes_strings (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,name VARCHAR(100) DEFAULT NULL,value VARCHAR(256) DEFAULT NULL,uuid BINARY(16) DEFAULT NULL,KEY (cid,id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",
        "CREATE TABLE genconf_attributes_bools (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,name VARCHAR(100) DEFAULT NULL,value BOOL DEFAULT NULL,uuid BINARY(16) DEFAULT NULL,KEY (cid,id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",
        "CREATE TABLE sequence_genconf (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,PRIMARY KEY (cid)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci" };

    private static final String[] createsPrimaryKey = {
        "CREATE TABLE genconf_attributes_strings (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,name VARCHAR(100) DEFAULT NULL,value VARCHAR(256) DEFAULT NULL,uuid BINARY(16) NOT NULL,PRIMARY KEY (cid, id, uuid),KEY (cid,id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",
        "CREATE TABLE genconf_attributes_bools (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,name VARCHAR(100) DEFAULT NULL,value BOOL DEFAULT NULL,uuid BINARY(16) NOT NULL,PRIMARY KEY (cid, id, uuid),KEY (cid,id,name)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci",
        "CREATE TABLE sequence_genconf (cid INT4 UNSIGNED NOT NULL,id INT4 UNSIGNED NOT NULL,PRIMARY KEY (cid)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci" };

}
