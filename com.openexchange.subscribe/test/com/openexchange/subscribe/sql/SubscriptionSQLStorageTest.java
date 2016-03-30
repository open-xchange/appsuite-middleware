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

package com.openexchange.subscribe.sql;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.sql.grammar.Constant.ASTERISK;
import static com.openexchange.sql.schema.Tables.subscriptions;
import java.sql.SQLException;
import java.util.List;
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
            AND(new EQUALS("enabled", false)));

        assertResult(new StatementBuilder().buildCommand(select));
    }

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

    public void testListGet() throws Exception {
        clearFolder(folderId);
        storage.rememberSubscription(subscription);
        assertTrue("Id should be greater 0", subscription.getId() > 0);
        subscriptionsToDelete.add(I(subscription.getId()));

        storage.rememberSubscription(subscription2);
        assertTrue("Id should be greater 0", subscription2.getId() > 0);
        subscriptionsToDelete.add(I(subscription2.getId()));

        final List<Subscription> list = storage.getSubscriptions(ctx, folderId);

        assertEquals("Number of Subscriptions does not match", 2, list.size());

        for (final Subscription loadedSubscription : list) {
            if (loadedSubscription.getId() == subscription.getId()) {
                assertEquals(subscription, loadedSubscription);
            } else if (loadedSubscription.getId() == subscription2.getId()) {
                assertEquals(subscription2, loadedSubscription);
            } else {
                fail("Unexpected subscription loaded");
            }
        }
    }

    public void testUpdate() throws Exception {
        storage.rememberSubscription(subscription);
        assertTrue("Id should be greater 0", subscription.getId() > 0);
        subscriptionsToDelete.add(I(subscription.getId()));
        subscription2.setId(subscription.getId());
        storage.updateSubscription(subscription2);
        assertEquals("Id should not changed", subscription.getId(), subscription2.getId());

        final SELECT select = new SELECT(ASTERISK).
        FROM(subscriptions).
        WHERE(new EQUALS("id", I(subscription.getId())).
            AND(new EQUALS("cid", I(ctx.getContextId()))).
            AND(new EQUALS("user_id", I(userId))).
            AND(new EQUALS("source_id", "com.openexchange.subscribe.test.basic2")).
            AND(new EQUALS("folder_id", subscription2.getFolderId())).
            AND(new EQUALS("last_update", L(subscription2.getLastUpdate()))).
            AND(new EQUALS("enabled", false)));

        assertResult(new StatementBuilder().buildCommand(select));
    }

    public void testIDCheckDuringRemember() throws Exception {
        subscription.setId(123);
        try {
            storage.rememberSubscription(subscription);
            subscriptionsToDelete.add(I(subscription.getId()));
            fail("Exception expected");
        } catch (final OXException e) {
            assertTrue("Wrong error code", SubscriptionErrorMessage.IDGiven.equals(e));
        }
    }

    public void testGet() throws Exception {
        storage.rememberSubscription(subscription2);
        assertTrue("Id should be greater 0", subscription2.getId() > 0);
        subscriptionsToDelete.add(I(subscription2.getId()));

        final Subscription loadedSubscription = storage.getSubscription(ctx, subscription2.getId());

        assertEquals(subscription2, loadedSubscription);
    }

    public void testDeleteAllSubscriptionsOfAUser() throws OXException, SQLException{
        storage.rememberSubscription(subscription);
        storage.deleteAllSubscriptionsForUser(userId, ctx);
        final SELECT select =
            new SELECT(ASTERISK).
            FROM(subscriptions).
            WHERE(new EQUALS("cid", I(ctx.getContextId())).AND(new EQUALS("user_id", I(userId))));
        assertNoResult(new StatementBuilder().buildCommand(select));
    }

    public void testDeleteAllSubscriptionsOfAContext() throws OXException, SQLException{
        storage.rememberSubscription(subscription);
        storage.deleteAllSubscriptionsInContext(ctx.getContextId(), ctx);
        final SELECT select =
            new SELECT(ASTERISK).
            FROM(subscriptions).
            WHERE(new EQUALS("cid", I(ctx.getContextId())));
        assertNoResult(new StatementBuilder().buildCommand(select));
    }

    public void testGetAllSubscriptionsOfAUser() throws OXException{
        storage.rememberSubscription(subscription);
        List<Subscription> subscriptionsOfUser = storage.getSubscriptionsOfUser(ctx, userId);
        assertEquals("should find one subscription", 1, subscriptionsOfUser.size());

        storage.rememberSubscription(subscription2);
        subscriptionsOfUser = storage.getSubscriptionsOfUser(ctx, userId);
        assertEquals("should find two subscriptions", 2, subscriptionsOfUser.size());
    }
}
