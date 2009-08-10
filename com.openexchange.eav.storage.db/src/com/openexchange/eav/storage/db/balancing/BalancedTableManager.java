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

import java.util.List;
import com.openexchange.eav.storage.db.exception.EAVStorageException;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link BalancedTableManager}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class BalancedTableManager {

    private TableManagerStrategy strategy;
    private int capacity = 1000000;

    /**
     * Initializes a new {@link BalancedTableManager}.
     * @param sim
     */
    public BalancedTableManager(TableManagerStrategy strategy) {
        super();
        this.strategy = strategy;
    }

    public String getTable(Context ctx, int module, int oid) throws EAVStorageException {
        String predefinedTable = strategy.getPredefinedTable(ctx, module, oid);
        if(predefinedTable != null) {
            return predefinedTable;
        }
        return strategy.register(ctx, module, oid, getLeastUsedTable(ctx));
    }
    private String getLeastUsedTable(Context ctx) throws EAVStorageException {
        List<TableMetadata> tables = strategy.getTableMetadataForAllTables(ctx);
        if(tables.size() == 0) {
            return strategy.createNewTable(ctx);
        }
        
        TableMetadata leastUsed = null;
        
        for (TableMetadata tableMetadata : tables) {
            if(leastUsed == null || leastUsed.getObjectCount() > tableMetadata.getObjectCount()) {
                leastUsed = tableMetadata;
            }
        }
        int threshold = calculateThreshold(capacity, tables.size());
        if(leastUsed.getObjectCount() >= threshold) {
            return strategy.createNewTable(ctx);
        }
        return leastUsed.getName();
    }

    public static int calculateThreshold(int capacity, int tableCount) {
        int threshold = 0;
        for(int i = 0; i < tableCount; i++) {
            int toAdd = capacity;
            for(int j = 0; j <= i; j++) {
                toAdd = toAdd / 2;
            }
            threshold += toAdd;
        }
        return threshold;
    }

    public void setCapacity(int i) {
        capacity = i;
    }

}
