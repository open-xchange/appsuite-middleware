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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.eav.storage.db.balancing;

import com.openexchange.eav.storage.db.sql.Paths;
import com.openexchange.groupware.tx.DBProvider;


/**
 * {@link PathTableManager}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PathTableManager extends BalancedTableManager {

    public static final String CREATE_TABLE = "CREATE TABLE %%tablename%% ("+
    "cid INT4 UNSIGNED NOT NULL,"+
    "module INT1 UNSIGNED NOT NULL,"+
    "objectId INT4 UNSIGNED NOT NULL,"+
    "nodeId INT4 UNSIGNED NOT NULL,"+
    "name VARCHAR(128),"+
    "parent INT4 UNSIGNED NOT NULL,"+
    "eavType VARCHAR(64) NOT NULL,"+
    "PRIMARY KEY (cid, module, objectId),"+
    "FOREIGN KEY (parent) REFERENCES eav_paths1(nodeId)"+
    ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    
    public static final String TABLE_PREFIX = "eav_paths";

    public static final String COLUMN_NAME = "pathTable";

    
    public PathTableManager(DBProvider provider) {
        super();
        PathIndexStrategy strategy = new PathIndexStrategy("eav_pathIndex", provider);
        strategy.setColumnName(COLUMN_NAME);
        strategy.setCreateTable(CREATE_TABLE);
        strategy.setTablePrefix(TABLE_PREFIX);
        
        setStrategy(strategy);
    }
    
    
    
}
