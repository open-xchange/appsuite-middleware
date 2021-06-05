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

package com.openexchange.subscribe.sql;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.subscribe.sql.SubscriptionSQLStorage.subscriptions;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.SQLException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.SELECT;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;


/**
 * {@link SubscriptionSQLStorageTest}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SubscriptionSQLStorageTest extends AbstractSubscriptionSQLStorageTest {
         @Test
     public void testRemember() throws Exception {
        storage.rememberSubscription(subscription2);
        assertTrue("Id should be greater 0", subscription2.getId() > 0);
        subscriptionsToDelete.add(I(subscription2.getId()));

        final SELECT select = new SELECT(ASTERISK).
        FROM(subscriptions).
        WHERE(new EQUALS("id", I(subscription2.getId())).
            AND(new EQUALS("cid", I(ctx.getContextId()))).
            AND(new EQUALS("user_id", I(userId))).
            AND(new EQUALS("source_id", "com.openexchange.subscribe.test.basic2")).
            AND(new EQUALS("folder_id", folderId)).
            AND(new EQUALS("last_update", L(lastUpdate))).
            AND(new EQUALS("enabled", Boolean.FALSE)));

        assertResult(new StatementBuilder().buildCommand(select));
    }

         @Test
     public void testForget() throws Exception {
        storage.rememberSubscription(subscription2);
        assertTrue("Id should be greater 0", subscription2.getId() > 0);
        subscriptionsToDelete.add(I(subscription2.getId()));

        storage.forgetSubscription(subscription2);

        final SELECT select = new SELECT(ASTERISK).
        FROM(subscriptions).
        WHERE(new EQUALS("id", I(subscription2.getId())).
            AND(new EQUALS("cid", I(ctx.getContextId()))).
            AND(new EQUALS("user_id", I(userId))).
            AND(new EQUALS("source_id", "com.openexchange.subscribe.test.basic2")).
            AND(new EQUALS("folder_id", folderId)).
            AND(new EQUALS("last_update", L(lastUpdate))));

        assertNoResult(new StatementBuilder().buildCommand(select));
    }

         @Test
     public void testListGet() throws Exception {
        clearFolder(folderId);
        storage.rememberSubscription(subscription);
        assertTrue("Id should be greater 0", subscription.getId() > 0);
        subscriptionsToDelete.add(I(subscription.getId()));

        storage.rememberSubscription(subscription2);
        assertTrue("Id should be greater 0", subscription2.getId() > 0);
        subscriptionsToDelete.add(I(subscription2.getId()));

        final List<Subscription> list = storage.getSubscriptions(ctx, folderId);

        Assert.assertEquals("Number of Subscriptions does not match", 2, list.size());

        for (final Subscription loadedSubscription : list) {
            if (loadedSubscription.getId() == subscription.getId()) {
                Assert.assertEquals(subscription, loadedSubscription);
            } else if (loadedSubscription.getId() == subscription2.getId()) {
                Assert.assertEquals(subscription2, loadedSubscription);
            } else {
                fail("Unexpected subscription loaded");
            }
        }
    }

         @Test
     public void testUpdate() throws Exception {
        storage.rememberSubscription(subscription);
        assertTrue("Id should be greater 0", subscription.getId() > 0);
        subscriptionsToDelete.add(I(subscription.getId()));
        subscription2.setId(subscription.getId());
        storage.updateSubscription(subscription2);
        Assert.assertEquals("Id should not changed", subscription.getId(), subscription2.getId());

        final SELECT select = new SELECT(ASTERISK).
        FROM(subscriptions).
        WHERE(new EQUALS("id", I(subscription.getId())).
            AND(new EQUALS("cid", I(ctx.getContextId()))).
            AND(new EQUALS("user_id", I(userId))).
            AND(new EQUALS("source_id", "com.openexchange.subscribe.test.basic2")).
            AND(new EQUALS("folder_id", subscription2.getFolderId())).
            AND(new EQUALS("last_update", L(subscription2.getLastUpdate()))).
            AND(new EQUALS("enabled", Boolean.FALSE)));

        assertResult(new StatementBuilder().buildCommand(select));
    }

    @Test
    public void testIDCheckDuringRemember() {
        subscription.setId(123);
        try {
            storage.rememberSubscription(subscription);
            subscriptionsToDelete.add(I(subscription.getId()));
            fail("Exception expected");
        } catch (OXException e) {
            assertTrue("Wrong error code", SubscriptionErrorMessage.IDGiven.equals(e));
        }
    }

         @Test
     public void testGet() throws Exception {
        storage.rememberSubscription(subscription2);
        assertTrue("Id should be greater 0", subscription2.getId() > 0);
        subscriptionsToDelete.add(I(subscription2.getId()));

        final Subscription loadedSubscription = storage.getSubscription(ctx, subscription2.getId());

        assertEquals(subscription2, loadedSubscription);
    }

         @Test
     public void testDeleteAllSubscriptionsOfAUser() throws OXException, SQLException{
        storage.rememberSubscription(subscription);
        storage.deleteAllSubscriptionsForUser(userId, ctx);
        final SELECT select =
            new SELECT(ASTERISK).
            FROM(subscriptions).
            WHERE(new EQUALS("cid", I(ctx.getContextId())).AND(new EQUALS("user_id", I(userId))));
        assertNoResult(new StatementBuilder().buildCommand(select));
    }

         @Test
     public void testDeleteAllSubscriptionsOfAContext() throws OXException, SQLException{
        storage.rememberSubscription(subscription);
        storage.deleteAllSubscriptionsInContext(ctx.getContextId(), ctx);
        final SELECT select =
            new SELECT(ASTERISK).
            FROM(subscriptions).
            WHERE(new EQUALS("cid", I(ctx.getContextId())));
        assertNoResult(new StatementBuilder().buildCommand(select));
    }

         @Test
     public void testGetAllSubscriptionsOfAUser() throws OXException{
        storage.rememberSubscription(subscription);
        List<Subscription> subscriptionsOfUser = storage.getSubscriptionsOfUser(ctx, userId);
        Assert.assertEquals("should find one subscription", 1, subscriptionsOfUser.size());

        storage.rememberSubscription(subscription2);
        subscriptionsOfUser = storage.getSubscriptionsOfUser(ctx, userId);
        Assert.assertEquals("should find two subscriptions", 2, subscriptionsOfUser.size());
    }
}
