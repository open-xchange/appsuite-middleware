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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.oauth.internal;

import java.util.Iterator;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.oauth.MailOAuthProvider;
import com.openexchange.mail.oauth.MailOAuthService;
import com.openexchange.mail.oauth.TokenInfo;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link MailOAuthServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailOAuthServiceImpl implements MailOAuthService {

    private final MailOAuthProviderRegistry registry;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link MailOAuthServiceImpl}.
     */
    public MailOAuthServiceImpl(MailOAuthProviderRegistry registry, ServiceLookup services) {
        super();
        this.registry = registry;
        this.services = services;
    }

    private void checkOAuthAccount(OAuthAccount oAuthAccount, Session session) throws OXException {
        OAuthServiceMetaData oAuthServiceMetaData = oAuthAccount.getMetaData();

        // Check if mail is supported
        {
            Set<OAuthScope> availableScopes = oAuthServiceMetaData.getAvailableScopes(session.getUserId(), session.getContextId());
            OAuthScope mailScope = null;
            for (Iterator<OAuthScope> it = availableScopes.iterator(); null == mailScope && it.hasNext();) {
                OAuthScope scope = it.next();
                if (OXScope.mail == scope.getOXScope()) {
                    mailScope = scope;
                }
            }
            if (null == mailScope) {
                throw OAuthExceptionCodes.NO_SUCH_SCOPE_AVAILABLE.create(OXScope.mail.getDisplayName());
            }

            boolean supportsMail = false;
            for (Iterator<OAuthScope> it = oAuthAccount.getEnabledScopes().iterator(); !supportsMail && it.hasNext();) {
                if (OXScope.mail == it.next().getOXScope()) {
                    supportsMail = true;
                }
            }
            if (false == supportsMail) {
                throw OAuthExceptionCodes.NO_SCOPE_PERMISSION.create(oAuthServiceMetaData.getDisplayName(), OXScope.mail.getDisplayName());
            }
        }
    }

    @Override
    public Autoconfig getAutoconfigFor(int oauthAccountId, Session session) throws OXException {
        OAuthService oAuthService = services.getOptionalService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Get the OAuth account
        OAuthAccount oAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
        checkOAuthAccount(oAuthAccount, session);

        // Try to compose the auto-configuration
        MailOAuthProvider provider = registry.getProviderFor(oAuthAccount.getMetaData().getId());
        return null == provider ? null : provider.getAutoconfigFor(oAuthAccount, session);
    }

    @Override
    public TokenInfo getTokenFor(int oauthAccountId, Session session) throws OXException {
        OAuthService oAuthService = services.getOptionalService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Get the OAuth account
        OAuthAccount oAuthAccount = oAuthService.getAccount(oauthAccountId, session, session.getUserId(), session.getContextId());
        //checkOAuthAccount(oAuthAccount, session);

        MailOAuthProvider provider = registry.getProviderFor(oAuthAccount.getMetaData().getId());
        return null == provider ? null : provider.getTokenFor(oAuthAccount, session);
    }

}
