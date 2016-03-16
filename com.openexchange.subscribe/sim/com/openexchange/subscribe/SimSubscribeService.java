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

package com.openexchange.subscribe;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;

/**
 * {@link SimSubscribeService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimSubscribeService extends AbstractSubscribeService {

    private SubscriptionSource source;

    private Subscription subscription;

    private List<Subscription> subscriptionIds = new LinkedList<Subscription>();

    private Collection content;

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
    public Collection getContent(final Subscription subscription) {
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

    public void setContent(final Collection content) {
        this.content = content;
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
