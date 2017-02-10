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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.ipcheck.osgi;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.ajax.SessionServlet;
import com.openexchange.ajax.ipcheck.IPCheckConfiguration;
import com.openexchange.ajax.ipcheck.IPCheckService;
import com.openexchange.ajax.ipcheck.internal.BuiltInChecker;
import com.openexchange.ajax.ipcheck.internal.DefaultIPCheckConfiguration;
import com.openexchange.ajax.ipcheck.internal.NoneIPChecker;
import com.openexchange.ajax.ipcheck.internal.StrictIPChecker;
import com.openexchange.ajax.ipcheck.spi.IPChecker;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.sessiond.impl.SubnetMask;


/**
 * {@link IPCheckServiceImpl} - The IP check service implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class IPCheckServiceImpl extends ServiceTracker<IPChecker, IPChecker> implements IPCheckService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IPCheckServiceImpl.class);

    private static final String SESSION_WHITELIST_FILE = SessionServlet.SESSION_WHITELIST_FILE;
    private static final Queue<IPRange> RANGES = new ConcurrentLinkedQueue<IPRange>();

    /**
     * Initializes static stuff.
     *
     * @param configService The service to use
     */
    public static void initialize(ConfigurationService configService) {
        String text = configService.getProperty(SESSION_WHITELIST_FILE);
        if (text == null) {
            // Fall back to configuration service
            text = configService.getText(SESSION_WHITELIST_FILE);
        }
        if (text != null) {
            LOG.info("Exceptions from IP Check have been defined.");
            // Serialize range parsing. This might happen more than once, but shouldn't matter, since the list
            // is accessed exclusively, so it winds up correct.
            RANGES.clear();
            final String[] lines = Strings.splitByCRLF(text);
            final List<IPRange> ranges = new LinkedList<IPRange>();
            for (String line : lines) {
                line = line.replaceAll("\\s", "");
                if (!line.equals("") && (line.length() == 0 || line.trim().charAt(0) != '#')) {
                    ranges.add(IPRange.parseRange(line));
                }
            }
            RANGES.addAll(ranges);
        }
    }

    private static final Cache<UserAndContext, IPCheckConfiguration> CONFIGURATIONS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    private static final Cache<UserAndContext, IPChecker> CHECKERS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * Invalidates cached configurations.
     */
    public static void invalidateCaches() {
        CHECKERS.invalidateAll();
        CONFIGURATIONS.invalidateAll();
    }

    // ---------------------------------------------------------------------------------

    private final ConcurrentMap<String, IPChecker> trackedCheckers;
    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link IPCheckServiceImpl}.
     */
    public IPCheckServiceImpl(BundleContext context, ServiceLookup serviceLookup) {
        super(context, IPChecker.class, null);
        trackedCheckers = new ConcurrentHashMap<String, IPChecker>(4, 0.9F, 1);
        this.serviceLookup = serviceLookup;
    }

    @Override
    public void handleChangedIp(String current, String previous, Session session) throws OXException {
        getCheckerFor(session).handleChangedIp(current, previous, session, getConfigurationFor(session));
    }

    /**
     * Gets the appropriate IP checker for specified session
     *
     * @param session The session
     * @return The IP checker
     * @throws OXException If checker cannot be obtained
     */
    private IPChecker getCheckerFor(final Session session) throws OXException {
        UserAndContext key = UserAndContext.newInstance(session);
        IPChecker checker = CHECKERS.getIfPresent(key);
        if (null != checker) {
            return checker;
        }

        Callable<IPChecker> loader = new Callable<IPChecker>() {

            @Override
            public IPChecker call() throws OXException {
                return initCheckerFor(session);
            }
        };
        try {
            return CHECKERS.get(key, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw new OXException(cause);
        }
    }

    IPChecker initCheckerFor(Session session) throws OXException {
        ConfigViewFactory factory = serviceLookup.getOptionalService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        // Check if old-style IP check is configured
        {
            ComposedConfigProperty<Boolean> property = view.property(ServerConfig.Property.IP_CHECK.getPropertyName(), boolean.class);
            if (property.isDefined()) {
                Boolean enabled = property.get();
                if (null != enabled && enabled.booleanValue()) {
                    // Old-style IP checker is enabled
                    return StrictIPChecker.getInstance();
                }
            }
        }

        String checkerId;
        {
            ComposedConfigProperty<String> property = view.property("com.openexchange.ipcheck.mode", String.class);
            if (false == property.isDefined()) {
                // Fall-back
                return NoneIPChecker.getInstance();
            }

            checkerId = property.get();
            if (Strings.isEmpty(checkerId)) {
                // Fall-back
                return NoneIPChecker.getInstance();
            }
        }

        IPChecker checker = trackedCheckers.get(checkerId);
        if (null != checker) {
            // Matches a tracked checker
            return checker;
        }

        // Check if identifier matches a built-in one
        BuiltInChecker builtInChecker = BuiltInChecker.builtInCheckerFor(checkerId);
        if (null == builtInChecker) {
            // No such checker
            LOG.warn("Specified IP check mode \"{}\" for user {} in context {} does not match any known IP check mechanism", checkerId, session.getUserId(), session.getContextId());
            return NoneIPChecker.getInstance();
        }

        switch (builtInChecker) {
            case NONE:
                return NoneIPChecker.getInstance();
            case STRICT:
                return StrictIPChecker.getInstance();
            default:
                break;
        }

        // Cannot occur
        LOG.warn("Specified IP check mode \"{}\" for user {} in context {} does not match any known IP check mechanism", checkerId, session.getUserId(), session.getContextId());
        return NoneIPChecker.getInstance();
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public IPCheckConfiguration getConfigurationFor(final Session session) throws OXException {
        UserAndContext key = UserAndContext.newInstance(session);
        IPCheckConfiguration configuration = CONFIGURATIONS.getIfPresent(key);
        if (null != configuration) {
            return configuration;
        }

        Callable<IPCheckConfiguration> loader = new Callable<IPCheckConfiguration>() {

            @Override
            public IPCheckConfiguration call() throws Exception {
                return newConfigurationFor(session);
            }
        };

        try {
            return CONFIGURATIONS.get(key, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }

            throw new OXException(cause);
        }
    }

    IPCheckConfiguration newConfigurationFor(Session session) throws OXException {
        ConfigViewFactory factory = serviceLookup.getOptionalService(ConfigViewFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = factory.getView(session.getUserId(), session.getContextId());
        DefaultIPCheckConfiguration.Builder configurationBuilder = DefaultIPCheckConfiguration.builder();

        // Client white-list
        {
            ComposedConfigProperty<String> property = view.property(ServerConfig.Property.IP_CHECK_WHITELIST.getPropertyName(), String.class);
            String whitelist = "\"open-xchange-mailapp\", \"open-xchange-mobile-api-facade\"";
            if (property.isDefined()) {
                String str = property.get();
                if (Strings.isNotEmpty(str)) {
                    whitelist = str;
                }
            }
            configurationBuilder.clientWhitelist(new ClientWhitelist().add(whitelist));
        }

        // IP white-list
        {
            Queue<IPRange> ranges = RANGES;
            configurationBuilder.ranges(ranges);
        }

        // Subnet white-list
        {
            String ipMaskV4 = null;
            {
                ComposedConfigProperty<String> property = view.property(ServerConfig.Property.IP_MASK_V4.getPropertyName(), String.class);
                if (property.isDefined()) {
                    String str = property.get();
                    if (Strings.isNotEmpty(str)) {
                        ipMaskV4 = str;
                    }
                }
            }

            String ipMaskV6 = null;
            {
                ComposedConfigProperty<String> property = view.property(ServerConfig.Property.IP_MASK_V6.getPropertyName(), String.class);
                if (property.isDefined()) {
                    String str = property.get();
                    if (Strings.isNotEmpty(str)) {
                        ipMaskV6 = str;
                    }
                }
            }

            configurationBuilder.allowedSubnet(new SubnetMask(ipMaskV4, ipMaskV6));
        }

        return configurationBuilder.build();
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public IPChecker addingService(ServiceReference<IPChecker> reference) {
        IPChecker checker = context.getService(reference);

        String id = checker.getId();
        if (null != BuiltInChecker.builtInCheckerFor(id)) {
            // Reserved identifier
            LOG.error("IP checker '{}' uses reserved identifier \"{}\" and thus cannot be registered", checker.getClass().getName(), id);
            context.ungetService(reference);
            return null;
        }

        if (null == trackedCheckers.putIfAbsent(id, checker)) {
            CHECKERS.invalidateAll();
            return checker;
        }

        LOG.error("IP checker '{}' uses duplicate identifier \"{}\" and thus cannot be registered", checker.getClass().getName(), id);
        context.ungetService(reference);
        return null;
    }

    @Override
    public void removedService(ServiceReference<IPChecker> reference, IPChecker service) {
        trackedCheckers.remove(service.getId());
        context.ungetService(reference);
    }

}
