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

package com.openexchange.subscribe.microsoft.graph.groupware;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.microsoft.graph.MicrosoftContactsSubscribeService;

/**
 * {@link MicrosoftSubscriptionsOAuthAccountDeleteListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.1
 */
public class MicrosoftSubscriptionsOAuthAccountDeleteListener implements OAuthAccountDeleteListener {

    private final MicrosoftContactsSubscribeService contactSubsrcibeService;
    private final ServiceLookup services;

    public MicrosoftSubscriptionsOAuthAccountDeleteListener(MicrosoftContactsSubscribeService contactService, ServiceLookup services) {
        super();
        this.contactSubsrcibeService = contactService;
        this.services = services;
    }

    @Override
    public void onAfterOAuthAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        contactSubsrcibeService.deleteSubscription(getContext(cid), id);
    }

    private Context getContext(int cid) throws OXException {
        ContextService contextService = services.getService(ContextService.class);
        return contextService.getContext(cid);
    }

    @Override
    public void onBeforeOAuthAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        // Nothing to do

    }
}
