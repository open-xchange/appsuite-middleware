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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.subscribe.xing.oauth;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.xing.XingOAuthScope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.oauth.AbstractSubscribeOAuthAccountAssociation;
import com.openexchange.subscribe.xing.Services;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.access.XingOAuthAccessProvider;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingUnlinkedException;

/**
 * {@link XingContactsOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class XingContactsOAuthAccountAssociation extends AbstractSubscribeOAuthAccountAssociation {

    /**
     * Initialises a new {@link XingContactsOAuthAccountAssociation}.
     * 
     * @param accountId The subscription's identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param displayName The association's display name
     * @param subscription The subscription
     */
    public XingContactsOAuthAccountAssociation(int accountId, int userId, int contextId, String displayName, Subscription subscription) {
        super(accountId, userId, contextId, displayName, subscription);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getModule()
     */
    @Override
    public String getModule() {
        return Module.CONTACTS.getModuleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getScopes()
     */
    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(XingOAuthScope.contacts_ro);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#getStatus(com.openexchange.session.Session)
     */
    @Override
    public Status getStatus(Session session) throws OXException {
        XingOAuthAccessProvider provider = Services.getService(XingOAuthAccessProvider.class);
        if (null == provider) {
            throw ServiceExceptionCode.absentService(XingOAuthAccessProvider.class);
        }
        try {
            XingOAuthAccess access = provider.accessFor(OAuthUtil.getAccountId(getSubscription().getConfiguration()), session);
            access.getXingAPI().userInfo();
            return Status.OK;
        } catch (XingUnlinkedException e) {
            return Status.INVALID_GRANT;
        } catch (OXException | XingException e) {
            return Status.RECREATION_NEEDED;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#newAccess(com.openexchange.session.Session)
     */
    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        throw new UnsupportedOperationException("No OAuthAccess for Xing.");
    }
}
