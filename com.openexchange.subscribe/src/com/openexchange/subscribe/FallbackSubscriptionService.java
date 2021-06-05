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

package com.openexchange.subscribe;

import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link FallbackSubscriptionService} is a service which helps to manage subscriptions with missing subscription sources.
 *
 * E.g. in case the required bundles have been uninstalled and there are still active subscriptions in the DB left.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class FallbackSubscriptionService {

    /**
     * The id of the subscription source added to all subscriptions with missing sources
     */
    public static final String ID = "com.openexchange.subscription.fallback";
    private static final FallbackSubscriptionService INSTANCE = new FallbackSubscriptionService();
    private final SubscriptionSource defaultSource;

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static FallbackSubscriptionService getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link FallbackSubscriptionService}.
     */
    private FallbackSubscriptionService() {
        super();
        defaultSource = new SubscriptionSource();
        defaultSource.setDisplayName("Fallback Source");
        defaultSource.setFormDescription(new DynamicFormDescription());
        defaultSource.setId(ID);
        defaultSource.setPriority(0);
    }

    /**
     * Add to the list of given {@link Subscription}s all {@link Subscription}s with a missing {@link SubscribeService}
     *
     * @param ctx The context
     * @param userId The user id
     * @param subscriptions The list of already loaded subscriptions
     * @return The list of subscriptions extended by all subscriptions with missing {@link SubscribeService}
     * @throws OXException
     */
    public List<Subscription> addSubscriptionsFromMissingSource(Context ctx, int userId, List<Subscription> subscriptions) throws OXException {
        List<Subscription> allSubscriptions = AbstractSubscribeService.STORAGE.get().getSubscriptionsOfUser(ctx, userId);
        List<Subscription> result = allSubscriptions.stream().filter((sub) -> subscriptions.stream()
                                                                                           .filter((x) -> x.getId() == sub.getId()).findFirst().isPresent() == false)
                                                                                           .collect(Collectors.toList());
        result.stream().forEach((sub) -> sub.setSource(defaultSource));
        result.addAll(subscriptions);
        return result;
    }

    /**
     * Get the {@link Subscription} with the given id
     *
     * @param ctx The context
     * @param subId The {@link Subscription} id
     * @return The {@link Subscription} or null
     * @throws OXException
     */
    public Subscription getSubscription(Context ctx, int subId) throws OXException {
        Subscription subscription = AbstractSubscribeService.STORAGE.get().getSubscription(ctx, subId);
        if (subscription == null) {
            return null;
        }
        subscription.setSource(defaultSource);
        return subscription;
    }

    /**
     * Deletes the given {@link Subscription} if it exists
     *
     * @param subscription The {@link Subscription} to delete
     * @throws OXException
     */
    public void unsubscribe(Subscription subscription) throws OXException {
        final Subscription loadedSubscription = getSubscription(subscription.getContext(), subscription.getId());
        if (null == loadedSubscription) {
            return;
        }
        if (loadedSubscription.getSession() == null) {
            loadedSubscription.setSession(subscription.getSession());
        }
        checkDelete(loadedSubscription);
        AbstractSubscribeService.STORAGE.get().forgetSubscription(subscription);
    }

    private void checkDelete(final Subscription subscription) throws OXException {
        Session session = subscription.getSession();
        if (null != session && session.getUserId() == subscription.getUserId() || isFolderAdmin(subscription)) {
            return;
        }
        throw SubscriptionErrorMessage.PERMISSION_DENIED.create();
    }

    private boolean isFolderAdmin(final Subscription subscription) throws OXException {
        final OCLPermission permission = loadFolderPermission(subscription);
        return permission.isFolderAdmin() || permission.getFolderPermission() >= OCLPermission.ADMIN_PERMISSION;
    }

    private OCLPermission loadFolderPermission(final Subscription subscription) throws OXException {
        final int folderId = Integer.parseInt(subscription.getFolderId());
        final int userId = subscription.getSession().getUserId();
        final Context ctx = subscription.getContext();
        final UserPermissionBits userPerm = AbstractSubscribeService.USER_PERMISSIONS.get().getUserPermissionBits(userId, ctx);

        return new OXFolderAccess(ctx).getFolderPermission(folderId, userId, userPerm);
    }

}
