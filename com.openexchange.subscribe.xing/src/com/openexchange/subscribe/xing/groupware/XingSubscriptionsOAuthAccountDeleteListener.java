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

package com.openexchange.subscribe.xing.groupware;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.subscribe.xing.XingSubscribeService;

/**
 * {@link XingSubscriptionsOAuthAccountDeleteListener}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class XingSubscriptionsOAuthAccountDeleteListener implements OAuthAccountDeleteListener {

    private XingSubscribeService xingService;
    private ContextService contextService;

    /**
     * Initializes a new {@link XingSubscriptionsOAuthAccountDeleteListener}.
     */
    public XingSubscriptionsOAuthAccountDeleteListener(final XingSubscribeService xingService, final ContextService contextService) {
        super();
        this.xingService = xingService;
        this.contextService = contextService;
    }

    @Override
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        xingService.deleteSubscription(contextService.getContext(cid), id);
    }

    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        // no op
    }
}
