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

package com.openexchange.realtime.osgi;

import java.util.concurrent.TimeUnit;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.Component;
import com.openexchange.realtime.RealtimeConfig;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanupImpl;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.management.ManagementHouseKeeper;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDManager;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.payload.converter.PayloadTreeConverter;
import com.openexchange.realtime.payload.converter.impl.DefaultPayloadTreeConverter;
import com.openexchange.realtime.payload.converter.impl.DurationToJSONConverter;
import com.openexchange.realtime.payload.converter.impl.JSONToDurationConverter;
import com.openexchange.realtime.synthetic.DevNullChannel;
import com.openexchange.realtime.synthetic.SyntheticChannel;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;


/**
 * {@link RealtimeActivator} - Publish needed services and set the lookup in the RealtimeServiceRegistry.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeActivator extends HousekeepingActivator {

    private static final Logger LOG = LoggerFactory.getLogger(RealtimeActivator.class);
    private SyntheticChannel synth;
    private RealtimeConfig realtimeConfig;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ ConfigurationService.class, ContextService.class, UserService.class, TimerService.class, SimpleConverter.class, ThreadPoolService.class, ManagementService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        RealtimeServiceRegistry.SERVICES.set(this);
        ManagementHouseKeeper managementHouseKeeper = ManagementHouseKeeper.getInstance();
        managementHouseKeeper.initialize(this);

        realtimeConfig = RealtimeConfig.getInstance();
        realtimeConfig.start();

        managementHouseKeeper.addManagementObject(realtimeConfig.getManagementObject());
        
        IDManager idManager = new IDManager();
        ID.ID_MANAGER_REF.set(idManager);
        RealtimeJanitors.getInstance().addJanitor(idManager);

        //Add the node-wide cleanup service and start tracking Janitors 
        LocalRealtimeCleanupImpl localRealtimeCleanup = new LocalRealtimeCleanupImpl(context);
        rememberTracker(localRealtimeCleanup);
        registerService(LocalRealtimeCleanup.class, localRealtimeCleanup);
        //And track the cluster wide cleanup
        track(GlobalRealtimeCleanup.class, new SimpleRegistryListener<GlobalRealtimeCleanup>() {

            @Override
            public void added(ServiceReference<GlobalRealtimeCleanup> ref, GlobalRealtimeCleanup service) {
                SyntheticChannel.GLOBAL_CLEANUP_REF.set(service);
            }

            @Override
            public void removed(ServiceReference<GlobalRealtimeCleanup> ref, GlobalRealtimeCleanup service) {
                SyntheticChannel.GLOBAL_CLEANUP_REF.set(null);
            }
        });
        
        synth = new SyntheticChannel(this, localRealtimeCleanup);

        TimerService timerService = getService(TimerService.class);
        timerService.scheduleAtFixedRate(synth, 0, 1, TimeUnit.MINUTES);

        registerService(Channel.class, synth);

        track(Component.class, new SimpleRegistryListener<Component>() {

            @Override
            public void added(ServiceReference<Component> ref, Component service) {
                synth.addComponent(service);
            }

            @Override
            public void removed(ServiceReference<Component> ref, Component service) {
                synth.removeComponent(service);
            }
        });

        DefaultPayloadTreeConverter converter = new DefaultPayloadTreeConverter(this);
        PayloadTree.CONVERTER = converter;
        PayloadTreeNode.CONVERTER = converter;

        registerService(PayloadTreeConverter.class, converter);

        registerService(Channel.class, new DevNullChannel());
        registerService(SimplePayloadConverter.class, new DurationToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToDurationConverter());

        //Register all RealtimeJanitors
        for(RealtimeJanitor realtimeJanitor : RealtimeJanitors.getInstance().getJanitors()) {
            registerService(RealtimeJanitor.class, realtimeJanitor);
        }

        //Expose all ManagementObjects for this bundle
        try {
            managementHouseKeeper.exposeManagementObjects();
        } catch (OXException oxe) {
            LOG.error("Failed to expose ManagementObjects", oxe);
        }
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        synth.shutdown();
        //Conceal all ManagementObjects for this bundle and remove them from the housekeeper
        ManagementHouseKeeper.getInstance().cleanup();
        ID.ID_MANAGER_REF.set(null);
        RealtimeJanitors.getInstance().cleanup();
        realtimeConfig.stop();
        super.cleanUp();
    }

}
