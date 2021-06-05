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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.DefaultMessagingAccountManager;
import com.openexchange.messaging.twitter.osgi.Services;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;


/**
 * {@link TwitterMessagingAccountManager}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingAccountManager extends DefaultMessagingAccountManager {

    /**
     * Initializes a new {@link TwitterMessagingAccountManager}.
     * @param service
     */
    public TwitterMessagingAccountManager(final MessagingService service) {
        super(service);
    }

    @Override
    public List<MessagingAccount> getAccounts(Session session) throws OXException {
        SecretService secretService = Services.optService(SecretService.class);
        if (null != secretService && Strings.isEmpty(secretService.getSecret(session))) {
            // The OAuth-based file storage needs a valid secret string for operation
            return Collections.emptyList();
        }

        return super.getAccounts(session);
    }

    @Override
    protected MessagingAccount modifyIncoming(final MessagingAccount account) throws OXException {
        final Map<String, Object> configuration = account.getConfiguration();
        if (null != configuration) {
            final Object value = configuration.get(TwitterConstants.TWITTER_OAUTH_ACCOUNT);
            if (value instanceof Integer) {
                final Integer id = (Integer) value;
                configuration.put(TwitterConstants.TWITTER_OAUTH_ACCOUNT, id.toString());
            } else {
                configuration.remove(TwitterConstants.TWITTER_OAUTH_ACCOUNT);
            }
        }
        return account;
    }

    @Override
    protected MessagingAccount modifyOutgoing(final MessagingAccount account) throws OXException {
        final Map<String, Object> configuration = account.getConfiguration();
        if (null != configuration) {
            final String id = (String) configuration.get(TwitterConstants.TWITTER_OAUTH_ACCOUNT);
            if (null != id) {
                configuration.put(TwitterConstants.TWITTER_OAUTH_ACCOUNT, Integer.valueOf(id.trim()));
            }
        }
        return account;
    }

}
