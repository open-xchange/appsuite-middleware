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

package com.openexchange.eav.storage.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import com.openexchange.eav.EAVType;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.SELECT;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.eav.storage.db.sql.AbstractColumns.cid;
import static com.openexchange.eav.storage.db.sql.PathIndex.module;
import static com.openexchange.eav.storage.db.sql.PathIndex.objectId;
import static com.openexchange.eav.storage.db.sql.Paths.*;
import static com.openexchange.sql.tools.SQLTools.createLIST;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Paths {

    private String pathTable;

    private int id;

    private int mod;

    private Context ctx;

    public Paths(String pathTable, Context ctx, int module, int objectId) {
        this.pathTable = pathTable;
        this.ctx = ctx;
        this.mod = module;
        this.id = objectId;
    }
    
    public EnumMap<EAVType, String> getTables(Connection con, EAVType...types) {
        EnumMap<EAVType, String> retval = new EnumMap<EAVType, String>(EAVType.class);
        
        if (types.length == 0) {
            return retval;
        }
        
        SELECT select = new SELECT(ASTERISK).FROM(pathsPrefix + pathTable)
            .WHERE(new EQUALS(cid, PLACEHOLDER)
                .AND(new EQUALS(module, PLACEHOLDER))
                .AND(new EQUALS(objectId, PLACEHOLDER))
                .AND(new IN(eavType, createLIST(types.length, PLACEHOLDER))));
        
        List<Object> values = new ArrayList<Object>();
        values.add(ctx.getContextId());
        values.add(mod);
        values.add(id);
        for (EAVType type : types) {
            values.add(type.getKeyword());
        }
        
        StatementBuilder sb = new StatementBuilder();
        ResultSet rs = null;
        try {
            rs = sb.executeQuery(con, select, values);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return retval;
    }
}
