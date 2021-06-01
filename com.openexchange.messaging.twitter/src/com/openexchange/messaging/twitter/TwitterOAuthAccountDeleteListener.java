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

package com.openexchange.messaging.twitter;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.twitter.session.TwitterAccessRegistry;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.oauth.OAuthAccountInvalidationListener;

/**
 * {@link TwitterOAuthAccountDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterOAuthAccountDeleteListener implements OAuthAccountDeleteListener, OAuthAccountInvalidationListener {

    /**
     * Initializes a new {@link TwitterOAuthAccountDeleteListener}.
     */
    public TwitterOAuthAccountDeleteListener() {
        super();
    }

    @Override
    public void onBeforeOAuthAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        // Nope
    }

    @Override
    public void onAfterOAuthAccountDeletion(final int id, final Map<String, Object> eventProps, final int user, final int cid, final Connection con) throws OXException {
        TwitterAccessRegistry.getInstance().purgeUserAccess(cid, user);
    }

    @Override
    public void onAfterOAuthAccountInvalidation(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException {
        TwitterAccessRegistry.getInstance().purgeUserAccess(cid, user);
    }

}
