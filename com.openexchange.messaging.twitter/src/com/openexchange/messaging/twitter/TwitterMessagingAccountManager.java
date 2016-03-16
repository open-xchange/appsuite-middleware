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
