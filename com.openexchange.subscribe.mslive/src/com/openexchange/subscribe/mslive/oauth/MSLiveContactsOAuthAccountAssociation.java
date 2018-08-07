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

package com.openexchange.subscribe.mslive.oauth;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.association.AbstractOAuthAccountAssociation;
import com.openexchange.oauth.association.Module;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.msliveconnect.MSLiveConnectOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.mslive.MSLiveApiClient;

/**
 * {@link MSLiveContactsOAuthAccountAssociation}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MSLiveContactsOAuthAccountAssociation extends AbstractOAuthAccountAssociation {

    private final Subscription subscription;
    private final String displayName;

    /**
     * Initialises a new {@link MSLiveContactsOAuthAccountAssociation}.
     */
    public MSLiveContactsOAuthAccountAssociation(int accountId, int userId, int contextId, String displayName, Subscription subscription) {
        super(accountId, userId, contextId);
        this.displayName = displayName;
        this.subscription = subscription;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getServiceId()
     */
    @Override
    public String getServiceId() {
        return subscription.getSource().getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getId()
     */
    @Override
    public String getId() {
        return Integer.toString(subscription.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return displayName;
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
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#optFolder()
     */
    @Override
    public String optFolder() {
        return subscription.getFolderId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.OAuthAccountAssociation#getScopes()
     */
    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(MSLiveConnectOAuthScope.contacts_ro);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#getStatus(com.openexchange.session.Session)
     */
    @Override
    public Status getStatus(Session session) throws OXException {
        //FIXME: get the oauth account...
        String accessToken;
        try {
            accessToken = MSLiveApiClient.getAccessToken(null, session);
        } catch (OXException e) {
            return Status.RECREATION_NEEDED;
        }
        //FIXME: ... and ping
        if (Strings.isNotEmpty(accessToken)) {
            return Status.OK;
        }
        return Status.INVALID_GRANT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.association.AbstractOAuthAccountAssociation#newAccess(com.openexchange.session.Session)
     */
    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        // nope
        return null;
    }
}
