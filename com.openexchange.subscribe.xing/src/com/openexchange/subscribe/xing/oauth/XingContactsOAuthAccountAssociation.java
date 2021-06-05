/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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

    @Override
    public String getModule() {
        return Module.CONTACTS.getModuleName();
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(XingOAuthScope.contacts_ro);
    }

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

    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        throw new UnsupportedOperationException("No OAuthAccess for Xing.");
    }
}
