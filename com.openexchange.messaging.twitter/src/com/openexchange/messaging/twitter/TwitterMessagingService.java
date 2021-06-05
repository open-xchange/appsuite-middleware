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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.MessagingService;
import com.openexchange.session.Session;

/**
 * {@link TwitterMessagingService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingService implements MessagingService {

    private static final List<MessagingAction> ACTIONS = Collections.unmodifiableList(Arrays.asList(
        new MessagingAction(TwitterConstants.TYPE_RETWEET, MessagingAction.Type.STORAGE, TwitterConstants.TYPE_TWEET),
        new MessagingAction(TwitterConstants.TYPE_RETWEET_NEW, MessagingAction.Type.STORAGE),
        new MessagingAction(TwitterConstants.TYPE_DIRECT_MESSAGE, MessagingAction.Type.STORAGE, TwitterConstants.TYPE_TWEET),
        new MessagingAction(TwitterConstants.TYPE_TWEET, MessagingAction.Type.MESSAGE)));

    private static final String ID = "com.openexchange.messaging.twitter";

    private static final String DISPLAY_NAME = "Twitter";

    /**
     * Gets the service identifier for twitter messaging service.
     *
     * @return The service identifier
     */
    public static String getServiceId() {
        return ID;
    }

    /*-
     * -------------------------------------- Member section --------------------------------------
     */

    private final MessagingAccountManager accountManager;

    private final DynamicFormDescription formDescription;

    /**
     * Initializes a new {@link TwitterMessagingService}.
     */
    public TwitterMessagingService() {
        super();
        accountManager = new TwitterMessagingAccountManager(this);
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        /*
         * API & secret key
         */
        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", "com.openexchange.oauth.twitter");
        tmpDescription.add(oauthAccount);
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws OXException {
        return new TwitterMessagingAccountAccess(accountManager.getAccount(accountId, session), session);
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws OXException {
        return new TwitterMessagingAccountTransport(accountManager.getAccount(accountId, session), session);
    }

    @Override
    public List<MessagingAction> getMessageActions() {
        return ACTIONS;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    public static int[] getStaticRootPerms() {
        return new int[] {MessagingPermission.READ_FOLDER,
            MessagingPermission.READ_ALL_OBJECTS,
            MessagingPermission.NO_PERMISSIONS,
            MessagingPermission.DELETE_OWN_OBJECTS};
    }

    @Override
    public int[] getStaticRootPermissions() {
        return getStaticRootPerms();
    }

}
