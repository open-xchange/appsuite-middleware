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

package com.openexchange.websockets.grizzly.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.http.grizzly.service.websocket.WebApplicationService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;
import com.openexchange.websockets.WebSocketListener;
import com.openexchange.websockets.WebSocketService;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketApplication;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketService;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketSessionToucher;
import com.openexchange.websockets.grizzly.remote.HzRemoteWebSocketDistributor;
import com.openexchange.websockets.grizzly.remote.portable.PortableMessageDistributor;
import com.openexchange.websockets.grizzly.remote.portable.PortableMessageDistributorFactory;

/**
 * {@link GrizzlyWebSocketActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GrizzlyWebSocketActivator extends HousekeepingActivator {

    private GrizzlyWebSocketApplication app;
    private ScheduledTimerTask sessionToucherTask;
    private HzRemoteWebSocketDistributor remoteDistributor;

    /**
     * Initializes a new {@link GrizzlyWebSocketActivator}.
     */
    public GrizzlyWebSocketActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, WebApplicationService.class, SessiondService.class, TimerService.class, ThreadPoolService.class,
            ContextService.class, UserService.class};
    }

    @Override
    protected boolean stopOnServiceUnavailability() {
        return true;
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        HzRemoteWebSocketDistributor remoteDistributor = new HzRemoteWebSocketDistributor(getService(TimerService.class), getService(ConfigurationService.class));
        this.remoteDistributor = remoteDistributor;

        WebSocketListenerTracker listenerTracker = new WebSocketListenerTracker(context, remoteDistributor);
        track(WebSocketListener.class, listenerTracker);
        track(HazelcastInstance.class, new HzTracker(remoteDistributor, this, context));
        openTrackers();

        registerService(CustomPortableFactory.class, new PortableMessageDistributorFactory(), null);

        GrizzlyWebSocketApplication app = this.app;
        if (null == app) {
            WebApplicationService webApplicationService = getService(WebApplicationService.class);
            app = new GrizzlyWebSocketApplication(listenerTracker, this);
            listenerTracker.setApplication(app);
            webApplicationService.registerWebSocketApplication("", "/*", app, null);
            registerService(WebSocketService.class, new GrizzlyWebSocketService(app, remoteDistributor));
            this.app = app;

            PortableMessageDistributor.setGrizzlyWebSocketApplication(app);

            long period = GrizzlyWebSocketSessionToucher.getTouchPeriod(getService(ConfigurationService.class));
            sessionToucherTask = getService(TimerService.class).scheduleAtFixedRate(new GrizzlyWebSocketSessionToucher(app), period, period);
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        GrizzlyWebSocketApplication app = this.app;
        if (null != app) {
            this.app = null;

            ScheduledTimerTask sessionToucherTask = this.sessionToucherTask;
            if (null != sessionToucherTask) {
                this.sessionToucherTask = null;
                sessionToucherTask.cancel();
                TimerService timerService = getService(TimerService.class);
                if (null != timerService) {
                    timerService.purge();
                }
            }

            PortableMessageDistributor.setGrizzlyWebSocketApplication(null);

            WebApplicationService webApplicationService = getService(WebApplicationService.class);
            if (null != webApplicationService) {
                webApplicationService.unregisterWebSocketApplication(app);
            }

            app.shutDown();
        }

        HzRemoteWebSocketDistributor remoteDistributor = this.remoteDistributor;
        if (null != remoteDistributor) {
            this.remoteDistributor = null;
            remoteDistributor.shutDown();
        }

        super.stopBundle();
    }

    @Override
    public <S> boolean addService(Class<S> clazz, S service) {
        // TODO Auto-generated method stub
        return super.addService(clazz, service);
    }

    @Override
    public <S> boolean removeService(Class<? extends S> clazz) {
        // TODO Auto-generated method stub
        return super.removeService(clazz);
    }

}
