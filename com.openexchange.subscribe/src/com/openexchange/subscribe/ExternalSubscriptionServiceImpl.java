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
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.publish.Transaction;
import com.openexchange.server.impl.DBPoolingException;

import static com.openexchange.publish.Transaction.INT;

/**
 * {@link ExternalSubscriptionServiceImpl}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ExternalSubscriptionServiceImpl implements ExternalSubscriptionService {
    
    private static final Log LOG = LogFactory.getLog(ExternalSubscriptionServiceImpl.class);
    
    public ExternalSubscription getSubscriptionForUser(int contextId, int userId, String externalService) {
        try {
            List<Map<String,Object>> results = Transaction.commitQuery(contextId, "SELECT userName, password, targetFolder FROM external_subscriptions WHERE user = ? and cid = ? and externalService = ?", userId, contextId, externalService);
            if(results.isEmpty()) {
                return null;
            }
            ExternalSubscription subscription = transform(results.get(0));
            subscription.setContextId(contextId);
            subscription.setUserId(userId);
            subscription.setExternalService(externalService);
            return subscription;
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private ExternalSubscription transform(Map<String, Object> map) {
        String xingUser = (String) map.get("userName");
        String xingPassword = (String) map.get("password");
        int targetFolder = INT(map.get("targetFolder"));
        
        ExternalSubscription subscription = new ExternalSubscription();
        subscription.setUserName(xingUser);
        subscription.setPassword(xingPassword);
        subscription.setTargetFolder(targetFolder);
        
        return subscription;
    }

    public void removeSubscription(ExternalSubscription subscription) {
        executeUpdate(subscription.getContextId(), "DELETE FROM external_subscriptions WHERE user = ? and cid = ? and externalService = ?", subscription.getUserId(), subscription.getContextId(), subscription.getExternalService());

    }

    public void saveSubscription(ExternalSubscription subscription) {
        subscription.getPassword();
        ExternalSubscription oldSubscription = getSubscriptionForUser(subscription.getContextId(), subscription.getUserId(), subscription.getExternalService());
        if (oldSubscription != null) {
            executeUpdate(
                subscription.getContextId(),
                "UPDATE external_subscriptions SET userName = ?, password = ?, targetFolder = ? WHERE user = ? AND cid = ? and externalService = ?",
                subscription.getUserName(),
                subscription.getPassword(),
                subscription.getTargetFolder(),
                subscription.getUserId(),
                subscription.getContextId(),
                subscription.getExternalService());
        } else {
            executeUpdate(
                subscription.getContextId(),
                "INSERT INTO external_subscriptions (userName, password, targetFolder, user, cid, externalService) VALUES (?,?,?,?,?,?) ",
                subscription.getUserName(),
                subscription.getPassword(),
                subscription.getTargetFolder(),
                subscription.getUserId(),
                subscription.getContextId(),
                subscription.getExternalService());
        }

    }

    protected void executeUpdate(int contextId, String sql, Object... values) {
        try {
            Transaction.commitStatement(contextId, sql, values);
        } catch (DBPoolingException e) {
            LOG.error(e.getMessage(), e);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
