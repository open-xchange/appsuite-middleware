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

package com.openexchange.subscribe.oauth;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link AbstractOAuthSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractOAuthSubscribeService extends AbstractSubscribeService implements SubscribeService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractOAuthSubscribeService.class);

    private final OAuthServiceMetaData metadata;
    private final int module;
    private final String sourceId;
    private final String displayName;
    private final SubscriptionSource source;
    private final ServiceLookup services;

    /**
     * Initialises a new {@link AbstractOAuthSubscribeService}.
     * 
     * @param metadata The OAuth metadata
     * @param sourceId The subscription's source identifier
     * @param module The module
     * @param displayName The display name
     * @param services The {@link ServiceLookup}
     */
    public AbstractOAuthSubscribeService(OAuthServiceMetaData metadata, String sourceId, int module, String displayName, ServiceLookup services) {
        super();
        this.metadata = metadata;
        this.sourceId = sourceId;
        this.module = module;
        this.displayName = displayName;
        this.services = services;
        this.source = initialiseSubscriptionSource();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#getSubscriptionSource()
     */
    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.SubscribeService#handles(int)
     */
    @Override
    public boolean handles(int folderModule) {
        return folderModule == module;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.AbstractSubscribeService#modifyIncoming(com.openexchange.subscribe.Subscription)
     */
    @Override
    public void modifyIncoming(Subscription subscription) throws OXException {
        if (subscription == null) {
            LOG.error("The subscription is null.");
            return;
        }
        super.modifyIncoming(subscription);
        if (subscription.getConfiguration() == null) {
            LOG.error("The subscription's configuration is null");
            return;
        }
        Object accountId = subscription.getConfiguration().get("account");
        if (accountId == null || accountId.toString().equals("nulll")) {
            throw SubscriptionErrorMessage.MISSING_ARGUMENT.create("account");
        }

        subscription.getConfiguration().put("account", Integer.toString(Autoboxing.a2i(subscription.getConfiguration().get("account"))));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.subscribe.AbstractSubscribeService#modifyOutgoing(com.openexchange.subscribe.Subscription)
     */
    @Override
    public void modifyOutgoing(Subscription subscription) throws OXException {
        String accountId = (String) subscription.getConfiguration().get("account");
        if (Strings.isEmpty(accountId)) {
            super.modifyOutgoing(subscription);
            return;
        }
        try {
            Integer accountIdInt = Integer.valueOf(accountId);
            if (null != accountIdInt) {
                subscription.getConfiguration().put("account", accountIdInt);
            }
        } catch (NumberFormatException x) {
            // Invalid account, but at least allow people to delete it.
            LOG.debug("Cannot convert '{}' to integer", accountId);
        }
        String displayName = subscription.getDisplayName();
        if (Strings.isNotEmpty(displayName)) {
            super.modifyOutgoing(subscription);
            return;
        }
        if (Strings.isNotEmpty(subscription.getSecret())) {
            displayName = metadata.getDisplayName();
        }
        subscription.setDisplayName(Strings.isNotEmpty(displayName) ? displayName : this.displayName);
        super.modifyOutgoing(subscription);
    }

    /**
     * Deletes the subscription with the specified identifier in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param id The subscription's identifier
     * @throws OXException if an error is occurred
     */
    public void deleteSubscription(Context context, int id) throws OXException {
        removeWhereConfigMatches(context, Collections.singletonMap("account", String.valueOf(id)));
    }

    /**
     * Returns the {@link OAuthAccount} of the user and the specified {@link Subscription}
     * 
     * @param session The {@link Session}
     * @param subscription The {@link Subscription}
     * @return The {@link OAuthAccount}
     * @throws OXException if the {@link OAuthService} is absent or any other error occurs
     */
    protected OAuthAccount getOAuthAccount(Session session, Subscription subscription) throws OXException {
        OAuthService oAuthService = services.getService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        Object accountId = subscription.getConfiguration().get("account");
        if (null == accountId) {
            return oAuthService.getDefaultAccount(getKnownApi(), session);
        }
        return oAuthService.getAccount(session, Autoboxing.a2i(accountId));
    }

    /**
     * Converts the specified accountId to its String representation
     * 
     * @param accountId The account identifier
     * @return The string representation of the account identifier
     */
    private String toString(Object accountId) {
        return accountId instanceof Integer ? (Integer.toString((Integer) accountId)) : accountId.toString();
    }

    /**
     * Get the {@link KnownApi}
     * 
     * @return the {@link KnownApi}
     */
    protected abstract KnownApi getKnownApi();

    /**
     * Initialises the subscription source
     * 
     * @return the initialised subscription source
     */
    private SubscriptionSource initialiseSubscriptionSource() {
        FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", metadata.getId());

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(oauthAccount);

        SubscriptionSource source = new SubscriptionSource();
        source.setId(sourceId);
        source.setFolderModule(module);
        source.setDisplayName(displayName);
        source.setSubscribeService(this);
        source.setFormDescription(form);

        return source;
    }
}
