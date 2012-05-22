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

package com.openexchange.indexedSearch.json.action;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.indexedSearch.json.IndexAJAXRequest;
import com.openexchange.indexedSearch.json.ResultConverters;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;


/**
 * {@link IsIndexedAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IsIndexedAction extends AbstractIndexAction {
        
    private static final String FOLDER = "folder";
    
    private static final String ACCOUNT = "account";
    

    /**
     * Initializes a new {@link IsIndexedAction}.
     * @param services
     * @param registry
     */
    public IsIndexedAction(ServiceLookup services, ResultConverters registry) {
        super(services, registry);
    }

    @Override
    protected AJAXRequestResult perform(IndexAJAXRequest req) throws OXException, JSONException {
        ServerSession session = req.getSession();
        String accountStr = req.getParameter(ACCOUNT);
        String folder = req.getParameter(FOLDER);
        
        int accountId = -1;
        try {
            accountId = Integer.parseInt(accountStr);
        } catch(NumberFormatException e) {
            // May be null, we ignore the account
        }
        
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("isIndexed", isIndexed(session.getContextId(), session.getUserId(), accountId, folder));
        return new AJAXRequestResult(jsonResult, "json");
    }
    
    private boolean isIndexed(int contextId, int userId, int accountId, String fullName) throws OXException {        
        DatabaseService dbService = getService(DatabaseService.class);
        if (null == dbService) {
            return false;
        }
        
        StringBuilder sb = new StringBuilder("SELECT sync FROM mailSync WHERE cid = ? AND user = ?");
        if (fullName != null) {
            sb.append(" AND fullName = ?");
        }
        if (accountId != -1) {
            sb.append(" AND accountId = ?");
        }
        
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(sb.toString());
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            if (fullName != null) {
                stmt.setString(pos++, fullName);
            }
            if (accountId != -1) {
                stmt.setInt(pos, accountId);
            }
            
            rs = stmt.executeQuery();
            
            boolean indexed = false;
            while(rs.next()) {
                if (rs.getBoolean(1)) {
                    indexed = false;
                    break;
                }
                indexed = true;
            }
            
            return indexed;
        } catch (SQLException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public String getAction() {        
        return "isIndexed";
    }

}
