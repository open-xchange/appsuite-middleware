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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.subscribe.SubscriptionErrorMessage.INACTIVE_SOURCE;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.generic.FolderUpdaterRegistry;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SubscriptionExecutionServiceImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionExecutionServiceImpl implements SubscriptionExecutionService, FolderUpdaterRegistry {

	private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SubscriptionExecutionServiceImpl.class));

    private final SubscriptionSourceDiscoveryService discoverer;

    private final Collection<FolderUpdaterService<?>> folderUpdaters;

    private final ContextService contextService;

    public SubscriptionExecutionServiceImpl(final SubscriptionSourceDiscoveryService discoverer, final Collection<FolderUpdaterService<?>> folderUpdaters, final ContextService contexts) {
        this.discoverer = discoverer;
        this.folderUpdaters = folderUpdaters;
        this.contextService = contexts;
    }

    @Override
    public int executeSubscription(final String sourceId, final ServerSession session, final int subscriptionId) throws OXException {
        final SubscribeService subscribeService = discoverer.getSource(sourceId).getSubscribeService();
        final Subscription subscription = subscribeService.loadSubscription(session.getContext(), subscriptionId, null);
        subscription.setSession(session);
        final boolean knowsSource = discoverer.filter(subscription.getUserId(), session.getContextId()).knowsSource(subscribeService.getSubscriptionSource().getId());
        if(!knowsSource) {
            throw INACTIVE_SOURCE.create();
        }
        final Collection<?> data = subscribeService.getContent(subscription);

        storeData(data, subscription);
        return data.size();
    }

    /**
     * @param data
     * @param subscription
     * @throws OXException
     */
    protected void storeData(final Collection<?> data, final Subscription subscription) throws OXException {
        getFolderUpdater(subscription).save(data, subscription);
    }

    /**
     * @param subscription
     */
    @Override
    public FolderUpdaterService getFolderUpdater(final TargetFolderDefinition target) throws OXException {
        final FolderObject folder = getFolder(new TargetFolderSession(target), target.getContext().getContextId(), target.getFolderIdAsInt());
//
        final boolean moreThanOneSubscriptionOnThisFolder = isThereMoreThanOneSubscriptionOnThisFolder(target.getContext(), target.getFolderId(), null);
        for (final FolderUpdaterService updater : folderUpdaters) {
            if (updater.handles(folder)) {
                // if there are 2 or more subscriptions on the folder: use the multiple-variant of the strategy if it exists
                if (moreThanOneSubscriptionOnThisFolder && updater.usesMultipleStrategy()) {return updater;}
                if (!moreThanOneSubscriptionOnThisFolder && !updater.usesMultipleStrategy()) {return updater;}

            }
        }
        // if there are 2 or more subscriptions on a folder but no multiple-variant Strategy is available use a single one
        for (final FolderUpdaterService updater : folderUpdaters) {
            if (updater.handles(folder)) {
                return updater;
            }
        }
        return null;
    }

    protected FolderObject getFolder(final TargetFolderSession subscriptionSession, final int contextId, final int folderId) throws OXException {
        final OXFolderAccess ofa = new OXFolderAccess(contextService.getContext(contextId));
        return ofa.getFolderObject(folderId);
    }

    @Override
    public int executeSubscription(final ServerSession session, final int subscriptionId) throws OXException {
        final Context context = session.getContext();
        final SubscriptionSource source = discoverer.getSource(context, subscriptionId);
        if(source == null) {
            throw INACTIVE_SOURCE.create();
        }
        final SubscribeService subscribeService = source.getSubscribeService();
        final Subscription subscription = subscribeService.loadSubscription(context, subscriptionId, null);
        subscription.setSession(session);
        final boolean knowsSource = discoverer.filter(subscription.getUserId(), context.getContextId()).knowsSource(subscribeService.getSubscriptionSource().getId());
        if(!knowsSource) {
            throw INACTIVE_SOURCE.create();
        }        final Collection data = subscribeService.getContent(subscription);
        storeData(data, subscription);
        return data.size();
    }

    @Override
    public int executeSubscriptions(final List<Subscription> subscriptionsToRefresh, final ServerSession session) throws OXException {
        int sum = 0;
        for (final Subscription subscription : subscriptionsToRefresh) {
            subscription.setSession(session);
            if(!subscription.isEnabled()) {
                LOG.debug("Skipping subscription "+subscription.getDisplayName()+" because it is disabled");

            } else {
            	final SubscriptionSource source = subscription.getSource();
            	if(source == null) {
                	throw INACTIVE_SOURCE.create();
            	}
            	final SubscribeService subscribeService = source.getSubscribeService();
            	final Collection data = subscribeService.getContent(subscription);
            	storeData(data, subscription);
            	sum += data.size();
            }
        }
        return sum;
    }
    private boolean isThereMoreThanOneSubscriptionOnThisFolder(final Context context, final String folder, final String secret) throws OXException {
        final List<SubscriptionSource> sources = discoverer.getSources();
        final List<Subscription> allSubscriptionsOnThisFolder = new ArrayList<Subscription>(10);
        for (final SubscriptionSource subscriptionSource : sources) {
            final Collection<Subscription> subscriptions = subscriptionSource.getSubscribeService().loadSubscriptions(context, folder, secret);
            if (subscriptions != null) {
                allSubscriptionsOnThisFolder.addAll(subscriptions);
            }
        }
        return allSubscriptionsOnThisFolder.size() >= 2;
    }




}
