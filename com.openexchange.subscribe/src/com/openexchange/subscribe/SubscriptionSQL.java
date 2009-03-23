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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.publish.Transaction;
import com.openexchange.server.impl.DBPoolingException;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SubscriptionSQL {

    private static final String SUBSCRIPTION_TABLE = "subscriptions";

    public static void addSubscription(Subscription subscription) throws DBPoolingException, SQLException {
        removeSubscription(subscription);

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(SUBSCRIPTION_TABLE);
        sb.append(" (");
        sb.append("cid, user, url, folder_id, last_update");
        sb.append(") VALUES (");
        sb.append("?, ?, ?, ?, ?");
        sb.append(")");

        Transaction.commitStatement(
            subscription.getContextId(),
            sb.toString(),
            subscription.getContextId(),
            subscription.getUserId(),
            subscription.getUrl(),
            subscription.getFolderId(),
            subscription.getLastUpdate().getTime());
    }

    public static void removeSubscription(Subscription subscription) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(SUBSCRIPTION_TABLE);
        sb.append(" WHERE cid = ? AND folder_id = ?");

        Transaction.commitStatement(
            subscription.getContextId(),
            sb.toString(),
            subscription.getContextId(),
            subscription.getFolderId());
    }

    public static List<Subscription> getSubscriptions(int contextId, int folderId) throws DBPoolingException, SQLException {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ");
        sb.append(SUBSCRIPTION_TABLE);
        sb.append(" WHERE cid = ? AND folder_id = ?");

        List<Map<String, Object>> subscriptions = Transaction.commitQuery(contextId, sb.toString(), contextId, folderId);

        List<Subscription> retval = new ArrayList<Subscription>();
        for (Map<String, Object> subscription : subscriptions) {
            Subscription subscriptionObject = new Subscription();
            subscriptionObject.setContextId(((Long) subscription.get("cid")).intValue());
            subscriptionObject.setFolderId(((Long) subscription.get("folder_id")).intValue());
            subscriptionObject.setLastUpdate(new Date((Long) subscription.get("last_update")));
            subscriptionObject.setUrl((String) subscription.get("url"));
            subscriptionObject.setUserId(((Long) subscription.get("user")).intValue());
            retval.add(subscriptionObject);
        }

        return retval;
    }

    public static List<Subscription> getSubscriptionsForUser(int contextId, int userId) throws DBPoolingException, SQLException{
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ");
        sb.append(SUBSCRIPTION_TABLE);
        sb.append(" WHERE cid = ? AND user = ?");
        
        List<Map<String, Object>> subscriptions = Transaction.commitQuery(contextId, sb.toString(), contextId, userId);
        
        List<Subscription> retval = new ArrayList<Subscription>();
        for (Map<String, Object> subscription : subscriptions) {
            Subscription subscriptionObject = new Subscription();
            subscriptionObject.setContextId(((Long) subscription.get("cid")).intValue());
            subscriptionObject.setFolderId(((Long) subscription.get("folder_id")).intValue());
            subscriptionObject.setLastUpdate(new Date((Long)subscription.get("last_update")));
            subscriptionObject.setUrl((String) subscription.get("url"));
            subscriptionObject.setUserId(((Long) subscription.get("user")).intValue());
            retval.add(subscriptionObject);
        }

        return retval;
    }
    
    public static void updateSubscription(Subscription subscription) throws DBPoolingException, SQLException {
        if (!subscriptionExists(subscription)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(SUBSCRIPTION_TABLE);
        sb.append(" SET last_update = ? ");
        sb.append("WHERE cid = ? AND user = ? AND url = ? AND folder_id = ?");

        Transaction.commitStatement(
            subscription.getContextId(),
            sb.toString(),
            subscription.getLastUpdate(),
            subscription.getContextId(),
            subscription.getUserId(),
            subscription.getUrl(),
            subscription.getFolderId());
    }

    public static boolean subscriptionExists(Subscription subscription) throws DBPoolingException, SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(SUBSCRIPTION_TABLE);
        sb.append(" WHERE cid = ? AND user = ? AND url = ? AND folder_id = ?");

        List<Map<String, Object>> subscriptions = Transaction.commitQuery(
            subscription.getContextId(),
            sb.toString(),
            subscription.getContextId(),
            subscription.getUserId(),
            subscription.getUrl(),
            subscription.getFolderId());

        return subscriptions.size() > 0;
    }
}
