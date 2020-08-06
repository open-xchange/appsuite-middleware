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

package com.openexchange.push.dovecot.osgi;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.hazelcast.configuration.HazelcastConfigurationService;
import com.openexchange.mail.MailProviderRegistration;
import com.openexchange.mail.Protocol;
import com.openexchange.osgi.Tools;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.dovecot.AbstractDovecotPushManagerService;
import com.openexchange.push.dovecot.DovecotPushConfiguration;
import com.openexchange.push.dovecot.DovecotPushDeleteListener;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock.Type;
import com.openexchange.push.dovecot.locking.HzDovecotPushClusterLock;
import com.openexchange.push.dovecot.stateful.ClusterLockProvider;
import com.openexchange.push.dovecot.stateful.DovecotPushManagerService;

/**
 * {@link DovecotRegisteringTracker}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class DovecotRegisteringTracker implements ServiceTrackerCustomizer<Object, Object>, DovecotPushManagerLifecycle {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DovecotRegisteringTracker.class);

    private final DovecotPushConfiguration config;
    private final BundleContext context;
    private final DovecotPushActivator activator;
    private final ClusterLockProvider lockProvider;
    private final Lock lock = new ReentrantLock();
    private final boolean hazelcastRequired;
    private ServiceRegistration<PushManagerService> reg;
    private ServiceRegistration<DeleteListener> deLiReg;
    private HazelcastInstance hzInstance;
    private MailProviderRegistration imapRegistration;
    private HazelcastConfigurationService hzConfigService;
    private DovecotPushManagerService pushManager;


    /**
     * Initializes a new {@link DovecotRegisteringTracker}.
     */
    public DovecotRegisteringTracker(DovecotPushConfiguration config,ClusterLockProvider lockProvider, DovecotPushActivator activator) {
        super();
        this.config = config;
        this.context = activator.getBundleContext();
        this.lockProvider = lockProvider;
        this.activator = activator;
        this.hazelcastRequired = lockProvider.getLockType() == Type.HAZELCAST;
    }

    /**
     * Gets the associated filter expression
     *
     * @return The filter
     * @throws InvalidSyntaxException If filter cannot be generated
     */
    public Filter getFilter() throws InvalidSyntaxException {
        if (hazelcastRequired) {
            return Tools.generateServiceFilter(context, MailProviderRegistration.class, HazelcastInstance.class, HazelcastConfigurationService.class);
        }

        return Tools.generateServiceFilter(context, MailProviderRegistration.class);
    }

    private boolean allAvailable() {
        return hazelcastRequired ? (null != imapRegistration && null != hzInstance && null != hzConfigService) : (null != imapRegistration);
    }

    @Override
    public Object addingService(ServiceReference<Object> reference) {
        Object service = context.getService(reference);
        lock.lock();
        try {
            if (HazelcastInstance.class.isInstance(service)) {
                if (false == hazelcastRequired) {
                    context.ungetService(reference);
                    return null;
                }
                this.hzInstance = (HazelcastInstance) service;
            } else if (MailProviderRegistration.class.isInstance(service)) {
                MailProviderRegistration providerRegistration = (MailProviderRegistration) service;
                String protocol = providerRegistration.getRegisteredProvider();
                try {
                    Protocol p = Protocol.parseProtocol(protocol);
                    if (false == p.isSupported("imap")) {
                        context.ungetService(reference);
                        return null;
                    }

                    this.imapRegistration = providerRegistration;
                } catch (OXException e) {
                    LOG.error("Failed to handle registered MailProviderRegistration", e);
                }
            } else if (HazelcastConfigurationService.class.isInstance(service)) {
                if (false == hazelcastRequired) {
                    context.ungetService(reference);
                    return null;
                }
                this.hzConfigService = (HazelcastConfigurationService) service;
            } else {
                // Huh...?
                context.ungetService(reference);
                return null;
            }

            if (allAvailable()) {
                init();
            }
        } finally {
            lock.unlock();
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, Object service) {
        // Nothing
    }

    @Override
    public void removedService(ServiceReference<Object> reference, Object service) {
        boolean someServiceMissing = false;
        lock.lock();
        try {
            if (HazelcastInstance.class.isInstance(service)) {
                if (this.hzInstance != null) {
                    this.hzInstance = null;
                    someServiceMissing = true;
                }
            } else if (MailProviderRegistration.class.isInstance(service)) {
                if (this.imapRegistration != null) {
                    try {
                        MailProviderRegistration providerRegistration = (MailProviderRegistration) service;
                        String protocol = providerRegistration.getRegisteredProvider();
                        if (null != protocol && Protocol.parseProtocol(protocol).isSupported("imap")) {
                            this.imapRegistration = null;
                            someServiceMissing = true;
                        }
                    } catch (Exception e) {
                        LOG.error("Failed to handle unregistered MailProviderRegistration", e);
                    }
                }
            } else if (HazelcastConfigurationService.class.isInstance(service)) {
                if (this.hzConfigService != null) {
                    this.hzConfigService = null;
                    someServiceMissing = true;
                }
            }

            if (null != reg && someServiceMissing) {
                stop();
            }
        } finally {
            lock.unlock();
        }
        context.ungetService(reference);
    }

    private void init() {
        if (null != reg) {
            // Already registered
            return;
        }

        try {
            if (hazelcastRequired) {
                boolean hzEnabled = hzConfigService.isEnabled();
                if (false == hzEnabled) {
                    String msg = "Dovecot Push is configured to use Hazelcast-based locking, but Hazelcast is disabled as per configuration! Start of Dovecot Push aborted!";
                    LOG.error(msg, new Exception(msg));
                    return;
                }

                String mapName = discoverMapName(hzInstance.getConfig());
                ((HzDovecotPushClusterLock) lockProvider.getClusterLock()).setMapName(mapName);
            }

            DovecotPushManagerService pushManager = DovecotPushManagerService.newInstance(config, lockProvider.getClusterLock(), activator);
            this.pushManager = pushManager;
            deLiReg = context.registerService(DeleteListener.class, new DovecotPushDeleteListener(pushManager), null);
            reg = context.registerService(PushManagerService.class, pushManager, null);
        } catch (Exception e) {
            LOG.warn("Failed start-up for {}", context.getBundle().getSymbolicName(), e);
        }

    }

    public void stop() {
        if (deLiReg != null) {
            deLiReg.unregister();
            deLiReg = null;
        }

        if (reg != null) {
            ((DovecotPushManagerService) context.getService(reg.getReference())).stop();
            reg.unregister();
            reg = null;
        }

        if (pushManager != null) {
            pushManager.stop();
            pushManager = null;
        }
    }

    /**
     * Discovers the map name from the supplied Hazelcast configuration.
     *
     * @param config The config object
     * @return The sessions map name
     * @throws IllegalStateException
     */
    private String discoverMapName(Config config) throws IllegalStateException {
        Map<String, MapConfig> mapConfigs = config.getMapConfigs();
        if (null != mapConfigs && 0 < mapConfigs.size()) {
            for (String mapName : mapConfigs.keySet()) {
                if (mapName.startsWith("dovecotnotify-")) {
                    LOG.info("Using distributed Dovecot Push map '{}'.", mapName);
                    return mapName;
                }
            }
        }
        String msg = "No distributed Dovecot Push map found in Hazelcast configuration";
        throw new IllegalStateException(msg, new BundleException(msg, BundleException.ACTIVATOR_ERROR));
    }

    @Override
    public boolean isActive() {
        return pushManager != null;
    }

    @Override
    public AbstractDovecotPushManagerService getActiveInstance() {
        return pushManager;
    }

    @Override
    public void shutDown() {
        lock.lock();
        try {
            stop();
        } finally {
            lock.unlock();
        }
    }

}
