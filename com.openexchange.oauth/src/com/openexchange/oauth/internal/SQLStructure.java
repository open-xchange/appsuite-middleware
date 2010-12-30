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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.oauth.internal;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import java.util.List;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.sql.grammar.Column;
import com.openexchange.sql.grammar.INSERT;
import com.openexchange.sql.grammar.Table;

/**
 * {@link SQLStructure}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SQLStructure {
    public static final Table OAUTH_ACCOUNTS = new Table("oauthAccounts");

    public enum OAUTH_COLUMN {
        CID("cid"),
        USER("user"),
        ID("id"),
        DISPLAY_NAME("displayName"),
        ACCESS_TOKEN("accessToken"),
        ACCESS_SECRET("accessSecret"),
        SERVICE_ID("serviceId");
        
        private final Column column;
        
        private OAUTH_COLUMN(final String colName) {
            this.column = new Column(colName);
        }
        
        public Column getColumn() {
            return column;
        }
        
        public Object get(final OAuthAccount account, final int cid, final int userId) {
            switch(this) {
            case CID: return Integer.valueOf(cid);
            case USER: return Integer.valueOf(userId);
            case ID: return Integer.valueOf(account.getId());
            case DISPLAY_NAME: return account.getDisplayName();
            case ACCESS_TOKEN: return account.getToken();
            case ACCESS_SECRET: return account.getSecret();
            case SERVICE_ID: return account.getMetaData().getId();
            }
            return null;
        }
        
    }
    
    public static INSERT INSERT_ACCOUNT(final OAuthAccount account, final int contextId, final int user, final List<Object> values) {
        final INSERT insert = new INSERT().INTO(OAUTH_ACCOUNTS);
        for(final OAUTH_COLUMN column : OAUTH_COLUMN.values()) {
            final Object o = column.get(account, contextId, user);
            if(o != null) {
                insert.SET(column.getColumn(), PLACEHOLDER);
                values.add(o);
            }
        }
        
        return insert;
    }
}
