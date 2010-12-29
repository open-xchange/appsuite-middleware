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

import java.util.List;
import java.util.Map;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.id.IDGeneratorService;
import com.openexchange.id.SimIDGenerator;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthInteraction;
import com.openexchange.oauth.OAuthInteractionType;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaDataRegistry;
import com.openexchange.oauth.osgi.MetaDataRegistry;


/**
 * An {@link OAuthService} Implementation using the RDB for storage and Scribe OAuth library for the OAuth interaction. 
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OAuthServiceImpl implements OAuthService {

    private final OAuthServiceMetaDataRegistry registry;
    private DBProvider provider;
    private IDGeneratorService idGenerator;
    
    /**
     * Initializes a new {@link OAuthServiceImpl}.
     * @param provider 
     * @param simIDGenerator 
     */
    public OAuthServiceImpl(DBProvider provider, IDGeneratorService idGenerator, OAuthServiceMetaDataRegistry registry) {
        super();
        this.registry = registry;
        this.provider = provider;
        this.idGenerator = idGenerator;
    }

    public OAuthServiceMetaDataRegistry getMetaDataRegistry() {
        return registry;
    }

    public List<OAuthAccount> getAccounts(int user, int contextId) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    public List<OAuthAccount> getAccounts(String serviceMetaData, int user, int contextId) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    public OAuthInteraction initOAuth(String serviceMetaData) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    public OAuthAccount createAccount(String serviceMetaData, OAuthInteractionType type, Map<String, Object> arguments, int user, int contextId) throws OAuthException {
        try {
            DefaultOAuthAccount account = new DefaultOAuthAccount();
            
            account.setDisplayName(arguments.get(OAuthConstants.ARGUMENT_DISPLAY_NAME).toString());
            account.setId(idGenerator.getId(OAuthConstants.TYPE_ACCOUNT, contextId));
            account.setMetaData(registry.getService(serviceMetaData));
            
            obtainToken(type, arguments, account);
            
            return account;
        } catch (AbstractOXException x) {
            throw new OAuthException(x);
        }
    }


    public void deleteAccount(int accountId, int user, int contextId) throws OAuthException {
        // TODO Auto-generated method stub

    }

    public void updateAccount(int accountId, Map<String, Object> arguments, int user, int contextId) throws OAuthException {
        // TODO Auto-generated method stub

    }

    public OAuthAccount getAccount(int accountId, int user, int contextId) throws OAuthException {
        // TODO Auto-generated method stub
        return null;
    }

    
    // OAuth
    
    protected void obtainToken(OAuthInteractionType type, Map<String, Object> arguments, DefaultOAuthAccount account) {
        
    }

    
    // Helper Methods
    
    

}
