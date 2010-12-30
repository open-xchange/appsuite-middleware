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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.services.ServiceRegistry;
import com.openexchange.server.ServiceException;


/**
 * An {@link OAuthService} Implementation using the RDB for storage and Scribe OAuth library for the OAuth interaction. 
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthServiceImpl implements OAuthService {

    private final OAuthServiceMetaDataRegistry registry;
    private final DBProvider provider;
    private final IDGeneratorService idGenerator;
    
    /**
     * Initializes a new {@link OAuthServiceImpl}.
     * @param provider 
     * @param simIDGenerator 
     */
    public OAuthServiceImpl(final DBProvider provider, final IDGeneratorService idGenerator, final OAuthServiceMetaDataRegistry registry) {
        super();
        this.registry = registry;
        this.provider = provider;
        this.idGenerator = idGenerator;
    }

    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        return registry;
    }

    public List<OAuthAccount> getAccounts(final int user, final int contextId) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<OAuthAccount> getAccounts(final String serviceMetaData, final int user, final int contextId) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    public OAuthInteraction initOAuth(final String serviceMetaData) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    public OAuthAccount createAccount(final String serviceMetaData, final OAuthInteractionType type, final Map<String, Object> arguments, final int user, final int contextId) throws OAuthException {
        try {
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            
            account.setDisplayName(arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME).toString());
            account.setId(idGenerator.getId(OAuthConstants.TYPE_ACCOUNT, contextId));
            account.setMetaData(registry.getService(serviceMetaData));
            
            obtainToken(type, arguments, account);
            
            return account;
        } catch (final AbstractOXException x) {
            throw new OAuthException(x);
        }
    }


    public void deleteAccount(final int accountId, final int user, final int contextId) throws OAuthException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(false, context);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    public void updateAccount(final int accountId, final Map<String, Object> arguments, final int user, final int contextId) throws OAuthException {
        // TODO Auto-generated method stub

    }

    public OAuthAccount getAccount(final int accountId, final int user, final int contextId) throws OAuthException {
        final Context context = getContext(contextId);
        final Connection con = getConnection(true, context);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT displayName, accessToken, accessSecret, serviceId FROM oauthAccounts WHERE cid = ? AND user = ? and id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, user);
            stmt.setInt(3, accountId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw OAuthExceptionCodes.ACCOUNT_NOT_FOUND.create(Integer.valueOf(accountId), Integer.valueOf(user), Integer.valueOf(contextId));
            }
            final DefaultOAuthAccount account = new DefaultOAuthAccount();
            account.setId(accountId);
            account.setDisplayName(rs.getString(1));
            account.setToken(rs.getString(2));
            account.setSecret(rs.getString(3));
            account.setMetaData(registry.getService(rs.getString(4)));
            return account;
        } catch (final SQLException e) {
            throw OAuthExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            provider.releaseReadConnection(context, con);
        }
    }

    
    // OAuth
    
    protected void obtainToken(final OAuthInteractionType type, final Map<String, Object> arguments, final DefaultOAuthAccount account) {
        
    }

    
    // Helper Methods

    private Connection getConnection(final boolean readOnly, final Context context) throws OAuthException {
        try {
            return readOnly ? provider.getReadConnection(context) : provider.getWriteConnection(context);
        } catch (final DBPoolingException e) {
            throw new OAuthException(e);
        }
    }
    
    private static Context getContext(final int contextId) throws OAuthException {
        try {
            return getService(ContextService.class).getContext(contextId);
        } catch (final ContextException e) {
            throw new OAuthException(e);
        }
    }
    
    private static <S> S getService(final Class<? extends S> clazz) throws OAuthException {
        try {
            return ServiceRegistry.getInstance().getService(clazz, true);
        } catch (final ServiceException e) {
            throw new OAuthException(e);
        }
    }

}
