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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.handle.osgi;

import java.util.concurrent.Future;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.handle.StanzaStorage;
import com.openexchange.realtime.handle.impl.Services;
import com.openexchange.realtime.handle.impl.StanzaQueueServiceImpl;
import com.openexchange.realtime.handle.impl.iq.IQHandler;
import com.openexchange.realtime.handle.impl.message.MessageHandler;
import com.openexchange.realtime.handle.impl.message.ResourceListener;
import com.openexchange.realtime.handle.impl.presence.PresenceHandler;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

public class StanzaHandlerActivator extends HousekeepingActivator {
    
    private Future<Object> presenceFuture;
    
    private Future<Object> messageFuture;
    
    private Future<Object> iqFuture;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ResourceDirectory.class, PresenceSubscriptionService.class,
            MessageDispatcher.class, LocalMessageDispatcher.class, ThreadPoolService.class, StanzaStorage.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        StanzaQueueServiceImpl queueService = new StanzaQueueServiceImpl();
        ThreadPoolService threadPoolService = getService(ThreadPoolService.class);
        presenceFuture = threadPoolService.submit(ThreadPools.task(new PresenceHandler(queueService.getPresenceQueue())));
        messageFuture = threadPoolService.submit(ThreadPools.task(new MessageHandler(queueService.getMessageQueue())));
        iqFuture = threadPoolService.submit(ThreadPools.task(new IQHandler(queueService.getIqQueue())));
        registerService(StanzaQueueService.class, queueService);
        getService(ResourceDirectory.class).addListener(new ResourceListener());
    }
    
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        if (presenceFuture != null) {
            presenceFuture.cancel(true);
        }
        
        if (messageFuture != null) {
            messageFuture.cancel(true);
        }
        
        if (iqFuture != null) {
            iqFuture.cancel(true);
        }
    }

}
