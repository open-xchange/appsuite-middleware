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

package com.openexchange.subscribe.internal;

import java.util.Collection;
import java.util.List;
import com.openexchange.api2.OXException;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.FolderUpdaterService;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSession;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link SubscriptionExecutionServiceImpl}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionExecutionServiceImpl implements SubscriptionExecutionService {

    private SubscriptionSourceDiscoveryService discoverer;

    private Collection<FolderUpdaterService> folderUpdaters;

    private ContextService contextService;

    public SubscriptionExecutionServiceImpl(SubscriptionSourceDiscoveryService discoverer, Collection<FolderUpdaterService> folderUpdaters, ContextService contexts) {
        this.discoverer = discoverer;
        this.folderUpdaters = folderUpdaters;
        this.contextService = contexts;
    }

    public void executeSubscription(String sourceId, int contextId, int subscriptionId) throws AbstractOXException {
        SubscribeService subscribeService = discoverer.getSource(sourceId).getSubscribeService();
        Subscription subscription = subscribeService.loadSubscription(contextId, subscriptionId);
        Collection data = subscribeService.getContent(subscription);

        storeData(data, subscription);
    }

    /**
     * @param data
     * @param subscription
     * @throws OXException
     */
    protected void storeData(Collection data, Subscription subscription) throws AbstractOXException {
        getFolderUpdater(subscription).save(data, subscription);
    }

    /**
     * @param subscription
     */
    protected FolderUpdaterService getFolderUpdater(Subscription subscription) throws AbstractOXException {
        FolderObject folder = getFolder(new SubscriptionSession(subscription), subscription.getContextId(), subscription.getFolderId());
        for (FolderUpdaterService updater : folderUpdaters) {
            if (updater.handles(folder)) {
                return updater;
            }
        }
        return null;
    }

    protected FolderObject getFolder(SubscriptionSession subscriptionSession, int contextId, int folderId) throws AbstractOXException {
        OXFolderAccess ofa = new OXFolderAccess(contextService.getContext(contextId));
        return ofa.getFolderObject(folderId);
    }

    public void executeSubscription(int contextId, int subscriptionId) throws AbstractOXException {
        SubscriptionSource source = discoverer.getSource(contextId, subscriptionId);
        SubscribeService subscribeService = source.getSubscribeService();
        Subscription subscription = subscribeService.loadSubscription(contextId, subscriptionId);
        Collection data = subscribeService.getContent(subscription);
        storeData(data, subscription);

    }

    public void executeSubscriptions(List<Subscription> subscriptionsToRefresh) throws AbstractOXException {
        for (Subscription subscription : subscriptionsToRefresh) {
            SubscribeService subscribeService = subscription.getSource().getSubscribeService();
            Collection data = subscribeService.getContent(subscription);
            storeData(data, subscription);
        }
    }
}
