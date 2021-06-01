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

package com.openexchange.subscribe.linkedin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.oauth.linkedin.LinkedInService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.linkedin.osgi.Activator;

/**
 * {@link LinkedInSubscribeService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LinkedInSubscribeService extends AbstractSubscribeService {

    /**
     * The source id
     */
    public static final String SOURCE_ID = "com.openexchange.subscribe.socialplugin.linkedin";

    private final Activator activator;

    private final SubscriptionSource source;

    public LinkedInSubscribeService(final Activator activator) {
        super();
        this.activator = activator;

        source = new SubscriptionSource();
        source.setDisplayName("LinkedIn");
        source.setFolderModule(FolderObject.CONTACT);
        source.setId(SOURCE_ID);
        source.setSubscribeService(this);

        final DynamicFormDescription form = new DynamicFormDescription();

        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", activator.getOAuthServiceMetadata().getId());
        form.add(oauthAccount);

        source.setFormDescription(form);
    }

    @Override
    public Collection<?> getContent(final Subscription subscription) throws OXException {
        if (null == subscription) {
            return Collections.emptyList();
        }
        try {
            final LinkedInService linkedInService = activator.getLinkedInService();
            if (null == linkedInService) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(LinkedInService.class.getName());
            }
            final int contextId = subscription.getContext().getContextId();
            Map<String, Object> configuration = subscription.getConfiguration();
            if ((configuration != null) && (configuration.get("account") != null)) {
                final int accountId = ((Integer) configuration.get("account")).intValue();
                return linkedInService.getContacts(subscription.getSession(), subscription.getUserId(), contextId, accountId);
            }
            return Collections.emptyList();
        } catch (final RuntimeException e) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    @Override
    public boolean handles(final int folderModule) {
        return FolderObject.CONTACT == folderModule;
    }

    @Override
    public void modifyIncoming(final Subscription subscription) throws OXException {
        super.modifyIncoming(subscription);
        final Integer accountId = (Integer) subscription.getConfiguration().get("account");
        if (accountId != null) {
            subscription.getConfiguration().put("account", accountId.toString());
        }
    }

    @Override
    public void modifyOutgoing(final Subscription subscription) throws OXException {
        final String accountId = (String) subscription.getConfiguration().get("account");
        if (null != accountId) {
            final Integer accountIdInt = Integer.valueOf(accountId);
            if (null != accountIdInt) {
                subscription.getConfiguration().put("account", accountIdInt);
            }
            String displayName = null;
            if (subscription.getSecret() != null) {
                displayName = activator.getLinkedInService().getAccountDisplayName(subscription.getSession(), subscription.getUserId(), subscription.getContext().getContextId(), (Integer) subscription.getConfiguration().get("account"));
            }
            if (null != displayName && !"".equals(displayName)) {
                subscription.setDisplayName(displayName);
            } else {
                subscription.setDisplayName("LinkedIn");
            }

        }
        super.modifyOutgoing(subscription);
    }

    public void deleteAllUsingOAuthAccount(final Context context, final int id) throws OXException {
        final Map<String, Object> query = new HashMap<String, Object>();
        query.put("account", String.valueOf(id));
        removeWhereConfigMatches(context, query);
    }
}
