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

package com.openexchange.subscribe.yahoo;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.yahoo.YahooService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.oauth.AbstractOAuthSubscribeService;

/**
 * {@link YahooSubscribeService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class YahooSubscribeService extends AbstractOAuthSubscribeService {

    public static final String SOURCE_ID = "com.openexchange.subscribe.socialplugin.yahoo";
    private final ServiceLookup services;

    /**
     * Initializes a new {@link YahooSubscribeService}.
     *
     * @param oAuthServiceMetaData The {@link OAuthServiceMetaData}
     * @param services The {@link ServiceLookup}
     * @throws OXException
     */
    public YahooSubscribeService(OAuthServiceMetaData metadata, ServiceLookup services) throws OXException {
        super(metadata, SOURCE_ID, FolderObject.CONTACT, "Yahoo!", services);
        this.services = services;
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        int oauthAccountId = ((Integer) subscription.getConfiguration().get("account")).intValue();
        YahooService yahooService = services.getService(YahooService.class);
        return yahooService.getContacts(subscription.getSession(), subscription.getUserId(), subscription.getContext().getContextId(), oauthAccountId);
    }

    @Override
    public void modifyOutgoing(final Subscription subscription) throws OXException {
        if (Strings.isNotEmpty(subscription.getSecret())) {
            YahooService yahooService = services.getService(YahooService.class);
            // No extra null or empty check, it will be checked on super
            String displayName = yahooService.getAccountDisplayName(subscription.getSession(), subscription.getUserId(), subscription.getContext().getContextId(), Autoboxing.a2i(subscription.getConfiguration().get("account")));
            subscription.setDisplayName(displayName);
        }
        super.modifyOutgoing(subscription);
    }

    @Override
    protected KnownApi getKnownApi() {
        return KnownApi.YAHOO;
    }
}
