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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link SimSubscribeService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimSubscribeService extends AbstractSubscribeService {

    public SimSubscribeService(com.openexchange.folderstorage.FolderService mockService) {
        super(mockService);
    }

    public static SimSubscribeService createSimSubscribeService(com.openexchange.folderstorage.FolderService mockService) {
        return new SimSubscribeService(mockService);
    }

    private SubscriptionSource source;

    private Subscription subscription;

    private List<Subscription> subscriptionIds = new LinkedList<>();

    private Collection<?> content;

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(final Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    public void setSubscriptionSource(final SubscriptionSource source) {
        this.source = source;

    }

    @Override
    public boolean handles(final int folderModule) {
        return true;
    }

    @Override
    public Collection<Subscription> loadSubscriptions(final Context context, final String folderId, final String secret) {
        return subscriptionIds;
    }

    @SuppressWarnings("unused")
    public Collection<Subscription> loadForUser(final Context context, final int userId) {
        return null;
    }

    @Override
    public void subscribe(final Subscription subscription) {

    }

    @Override
    public void unsubscribe(final Subscription subscription) {

    }

    @Override
    public void update(final Subscription subscription) {

    }

    @Override
    public Collection<?> getContent(final Subscription subscription) {
        return content;
    }

    @Override
    public Subscription loadSubscription(final Context context, final int subscriptionId, final String secret) {
        final Subscription subscriptionIdMemo = new Subscription();
        subscriptionIdMemo.setContext(context);
        subscriptionIdMemo.setId(subscriptionId);
        subscriptionIds.add(subscriptionIdMemo);
        return subscription;
    }

    public List<Subscription> getSubscriptionIDs() {
        return subscriptionIds;
    }

    public void clearSim() {
        subscriptionIds.clear();
    }

    public void setContent(final Collection<?> content) {
        this.content = content == null ? null : content;
    }

    @Override
    public boolean knows(final Context context, final int subscriptionId) {
        return true;
    }

    public void setSubscriptions(final List<Subscription> subscriptions){
        this.subscriptionIds = subscriptions;
    }

    @Override
    public Collection<Subscription> loadSubscriptions(final Context context, final int userId, final String secret) throws OXException {
        // Nothing to do
        return null;
    }

    @Override
    public void migrateSecret(final Session session, final String oldSecret, final String newSecret) throws OXException {
        // Nothing to do
    }

    @Override
    public boolean hasAccounts(final Context context, final User user) throws OXException {
        return false;
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        // Ignore
    }

    @Override
    public void touch(Context ctx, int subscriptionId) throws OXException {

    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        // Ignore
    }
}
