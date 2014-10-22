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

package com.openexchange.capabilities.internal;

import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.DependentCapabilityChecker;
import com.openexchange.capabilities.osgi.PermissionAvailabilityServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.java.ConcurrentEnumMap;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractCapabilityService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractCapabilityService implements CapabilityService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCapabilityService.class);

    private static final Object PRESENT = new Object();

    private static final String REGION_NAME_CONTEXT = "CapabilitiesContext";
    private static final String REGION_NAME_USER = "CapabilitiesUser";
    private static final String REGION_NAME = "Capabilities";

    private static final String PERMISSION_PROPERTY = "permissions".intern();

    private static final Pattern P_SPLIT = Pattern.compile("\\s*[, ]\\s*");

    // ------------------------------------------------------------------------------------------------ //

    private static interface PropertyHandler {

        void handleProperty(String propValue, CapabilitySet capabilities) throws OXException;
    }

    /** The property handlers for special config-cascade properties */
    private static final Map<String, PropertyHandler> PROPERTY_HANDLERS;

    static {
        final Map<String, PropertyHandler> map = new HashMap<String, PropertyHandler>(4);

        map.put("com.openexchange.caldav.enabled", new PropertyHandler() {

            @Override
            public void handleProperty(final String propValue, final CapabilitySet capabilities) throws OXException {
                if (Boolean.parseBoolean(propValue)) {
                    capabilities.add(getCapability(Permission.CALDAV));
                } else {
                    capabilities.remove(Permission.CALDAV.getCapabilityName());
                }
            }
        });

        map.put("com.openexchange.carddav.enabled", new PropertyHandler() {

            @Override
            public void handleProperty(final String propValue, final CapabilitySet capabilities) throws OXException {
                if (Boolean.parseBoolean(propValue)) {
                    capabilities.add(getCapability(Permission.CARDDAV));
                } else {
                    capabilities.remove(Permission.CARDDAV.getCapabilityName());
                }
            }
        });

        PROPERTY_HANDLERS = Collections.unmodifiableMap(map);
    }

    // ------------------------------------------------------------------------------------------------ //

    private static final ConcurrentEnumMap<Permission, Capability> P2CAPABILITIES = new ConcurrentEnumMap<Permission, Capability>(Permission.class);
    private static final ConcurrentMap<String, Capability> CAPABILITIES = new ConcurrentHashMap<String, Capability>(96);

    /**
     * Gets the singleton capability for given identifier
     *
     * @param permission The permission
     * @return The singleton capability
     */
    public static Capability getCapability(Permission permission) {
        if (null == permission) {
            return null;
        }
        ConcurrentEnumMap<Permission, Capability> p2capabilities = P2CAPABILITIES;
        Capability capability = p2capabilities.get(permission);
        if (null == capability) {
            Capability newcapability = getCapability(permission.getCapabilityName());
            capability = p2capabilities.putIfAbsent(permission, newcapability);
            if (null == capability) {
                capability = newcapability;
            }
        }
        return capability;
    }

    /**
     * Gets the singleton capability for given identifier
     *
     * @param id The identifier
     * @return The singleton capability
     */
    public static Capability getCapability(String id) {
        if (null == id) {
            return null;
        }
        ConcurrentMap<String, Capability> capabilities = CAPABILITIES;
        Capability capability = capabilities.get(id);
        if (capability != null) {
            return capability;
        }
        Capability existingCapability = capabilities.putIfAbsent(id, capability = new Capability(id));
        return existingCapability == null ? capability : existingCapability;
    }

    // ------------------------------------------------------------------------------------------------ //

    private final ConcurrentMap<String, Object> declaredCapabilities;

    private final ServiceLookup services;

    private volatile Boolean autologin;

    /**
     * Registry that provides the registered JSON bundles to check for permissions
     */
    private final PermissionAvailabilityServiceRegistry registry;

    /**
     * Initializes a new {@link AbstractCapabilityService}.
     *
     * @param registry that provides the services for the registered JSON bundles
     */
    public AbstractCapabilityService(final ServiceLookup services, PermissionAvailabilityServiceRegistry registry) {
        super();
        this.services = services;
        declaredCapabilities = new ConcurrentHashMap<String, Object>(32);
        this.registry = registry;
    }

    private Cache optContextCache() {
        final CacheService service = services.getOptionalService(CacheService.class);
        if (null == service) {
            return null;
        }
        try {
            return service.getCache(REGION_NAME_CONTEXT);
        } catch (final OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    private Cache optUserCache() {
        final CacheService service = services.getOptionalService(CacheService.class);
        if (null == service) {
            return null;
        }
        try {
            return service.getCache(REGION_NAME_USER);
        } catch (final OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    private Cache optCache() {
        final CacheService service = services.getOptionalService(CacheService.class);
        if (null == service) {
            return null;
        }
        try {
            return service.getCache(REGION_NAME);
        } catch (final OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    private CapabilitySet optCachedCapabilitySet(final int userId, final int contextId) {
        final Cache cache = optCache();
        if (null == cache) {
            return null;
        }
        final Object object = cache.getFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        return (object instanceof CapabilitySet) ? ((CapabilitySet) object).clone() : null;
    }

    private boolean autologin() {
        Boolean tmp = autologin;
        if (null == tmp) {
            synchronized (this) {
                tmp = autologin;
                if (null == tmp) {
                    final ConfigurationService configurationService = services.getService(ConfigurationService.class);
                    if (null == configurationService) {
                        // Return default value
                        return false;
                    }
                    tmp = Boolean.valueOf(configurationService.getBoolProperty("com.openexchange.sessiond.autologin", false));
                    autologin = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private static final Capability CAP_AUTO_LOGIN = new Capability("autologin");

    /**
     * Gets the capabilities tree showing which capability comes from which source
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The capabilities tree
     * @throws OXException If capabilities tree cannot be returned
     */
    public List<List<Set<String>>> getCapabilitiesTree(int userId, int contextId) throws OXException {
        List<List<Set<String>>> sets = new LinkedList<List<Set<String>>>();

        {
            Set<String> capabilities = new TreeSet<String>();
            UserPermissionBits userPermissionBits = services.getService(UserPermissionService.class).getUserPermissionBits(userId, contextId);
            // Capabilities by user permission bits
            for (final Permission p : Permission.byBits(userPermissionBits.getPermissionBits())) {
                capabilities.add(p.getCapabilityName());
            }

            List<Set<String>> arr = new ArrayList<Set<String>>(2);
            arr.add(capabilities);
            arr.add(new HashSet<String>(0));
            sets.add(arr);
        }

        {
            CapabilitySet grantedCapabilities = new CapabilitySet(16);
            CapabilitySet deniedCapabilities = new CapabilitySet(16);
            final ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
            if (configViews != null) {
                final ConfigView view = configViews.getView(userId, contextId);
                final String property = PERMISSION_PROPERTY;
                for (final String scope : configViews.getSearchPath()) {
                    final String permissions = view.property(property, String.class).precedence(scope).get();
                    if (permissions != null) {
                        for (String permissionModifier : P_SPLIT.split(permissions)) {
                            if (!isEmpty(permissionModifier)) {
                                permissionModifier = permissionModifier.trim();
                                final char firstChar = permissionModifier.charAt(0);
                                if ('-' == firstChar) {
                                    deniedCapabilities.add(getCapability(permissionModifier.substring(1)));
                                } else {
                                    if ('+' == firstChar) {
                                        grantedCapabilities.add(getCapability(permissionModifier.substring(1)));
                                    } else {
                                        grantedCapabilities.add(getCapability(permissionModifier));
                                    }
                                }
                            }
                        }
                    }
                }

                final Map<String, ComposedConfigProperty<String>> all = view.all();
                for (Map.Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
                    final String propName = entry.getKey();
                    if (propName.startsWith("com.openexchange.capability.")) {
                        boolean value = Boolean.parseBoolean(entry.getValue().get());
                        String name = toLowerCase(propName.substring(28));
                        if (value) {
                            grantedCapabilities.add(getCapability(name));
                        } else {
                            deniedCapabilities.add(getCapability(name));
                        }
                    }
                }

                // Check for a property handler
                for (final Map.Entry<String, PropertyHandler> entry : PROPERTY_HANDLERS.entrySet()) {
                    final ComposedConfigProperty<String> composedConfigProperty = all.get(entry.getKey());
                    if (null != composedConfigProperty) {
                        entry.getValue().handleProperty(composedConfigProperty.get(), grantedCapabilities);
                    }
                }

                List<Set<String>> arr = new ArrayList<Set<String>>(2);

                Set<String> set = new TreeSet<String>();
                for (Capability cap : grantedCapabilities) {
                    set.add(cap.getId());
                }
                arr.add(set);

                set = new TreeSet<String>();
                for (Capability cap : deniedCapabilities) {
                    set.add(cap.getId());
                }
                arr.add(set);

                sets.add(arr);
            }
        }

        {
            if (contextId > 0) {
                final Set<String> set = new HashSet<String>();
                final Set<String> removees = new HashSet<String>();
                // Context-sensitive
                for (final String sCap : getContextCaps(contextId, false)) {
                    if (!isEmpty(sCap)) {
                        final char firstChar = sCap.charAt(0);
                        if ('-' == firstChar) {
                            final String val = toLowerCase(sCap.substring(1));
                            set.remove(val);
                            removees.add(val);
                        } else {
                            if ('+' == firstChar) {
                                set.add(toLowerCase(sCap.substring(1)));
                            } else {
                                set.add(toLowerCase(sCap));
                            }
                        }
                    }
                }
                // User-sensitive
                if (userId > 0) {
                    for (final String sCap : getUserCaps(userId, contextId, false)) {
                        if (!isEmpty(sCap)) {
                            final char firstChar = sCap.charAt(0);
                            if ('-' == firstChar) {
                                final String val = toLowerCase(sCap.substring(1));
                                set.remove(val);
                                removees.add(val);
                            } else {
                                if ('+' == firstChar) {
                                    final String cap = toLowerCase(sCap.substring(1));
                                    set.add(cap);
                                    removees.remove(cap);
                                } else {
                                    final String cap = toLowerCase(sCap);
                                    set.add(cap);
                                    removees.remove(cap);
                                }
                            }
                        }
                    }
                }

                // Merge them into result set
                List<Set<String>> arr = new ArrayList<Set<String>>(2);
                arr.add(set);
                arr.add(removees);
                sets.add(arr);
            }
        }

        {
            // Now the declared ones
            CapabilitySet grantedCapabilities = new CapabilitySet(16);
            FakeSession fakeSession = new FakeSession(userId, contextId);
            for (String cap : declaredCapabilities.keySet()) {
                if (check(cap, fakeSession, grantedCapabilities)) {
                    grantedCapabilities.add(getCapability(cap));
                }
            }

            List<Set<String>> arr = new ArrayList<Set<String>>(2);

            Set<String> set = new TreeSet<String>();
            for (Capability cap : grantedCapabilities) {
                set.add(cap.getId());
            }
            arr.add(set);

            arr.add(new HashSet<String>(0));

            sets.add(arr);
        }

        return sets;
    }

    @Override
    public CapabilitySet getCapabilities(final int userId, final int contextId, final boolean computeCapabilityFilters, final boolean allowCache) throws OXException {
        // Initialize server session
        ServerSession serverSession = ServerSessionAdapter.valueOf(userId, contextId);

        // Create capability set
        CapabilitySet capabilities = new CapabilitySet(64);

        // What about autologin?
        if (autologin()) {
            capabilities.add(CAP_AUTO_LOGIN);
        }

        // ------------- Combined capabilities/permissions ------------ //
        if (!serverSession.isAnonymous()) {
            // Check cache
            final CapabilitySet cachedCapabilitySet = allowCache ? optCachedCapabilitySet(userId, contextId) : null;
            if (null != cachedCapabilitySet) {
                capabilities = cachedCapabilitySet;
                if (computeCapabilityFilters) {
                    applyUIFilter(capabilities);
                }
                return capabilities;
            }
            // Obtain user permissions
            final Context context = serverSession.getContext();
            final UserPermissionBits userPermissionBits = services.getService(UserPermissionService.class).getUserPermissionBits(serverSession.getUserId(), serverSession.getContext());
            // Capabilities by user permission bits
            for (final Permission p : Permission.byBits(userPermissionBits.getPermissionBits())) {
                capabilities.add(getCapability(p));
            }
            // Apply capabilities for non-transient sessions
            if (!serverSession.isTransient()) {
                userPermissionBits.setGroups(serverSession.getUser().getGroups());
                // Webmail
                if (serverSession.getUserId() == context.getMailadmin()) {
                    final boolean adminMailLoginEnabled = services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.mail.adminMailLoginEnabled", false);
                    if (!adminMailLoginEnabled) {
                        capabilities.remove(getCapability(Permission.WEBMAIL));
                    }
                }
                // Portal
                if (userPermissionBits.hasPortal()) {
                    capabilities.add(getCapability("portal"));
                    capabilities.remove("deniedPortal");
                } else {
                    capabilities.remove("portal");
                    capabilities.add(getCapability("deniedPortal"));
                }
                // Free-Busy
                if (userPermissionBits.hasFreeBusy()) {
                    capabilities.add(getCapability("freebusy"));
                } else {
                    capabilities.remove("freebusy");
                }
                // Conflict-Handling
                if (userPermissionBits.hasConflictHandling()) {
                    capabilities.add(getCapability("conflict_handling"));
                } else {
                    capabilities.remove("conflict_handling");
                }
                // Participants-Dialog
                if (userPermissionBits.hasParticipantsDialog()) {
                    capabilities.add(getCapability("participants_dialog"));
                } else {
                    capabilities.remove("participants_dialog");
                }
                // Group-ware
                if (userPermissionBits.hasGroupware()) {
                    capabilities.add(getCapability("groupware"));
                } else {
                    capabilities.remove("groupware");
                }
                // PIM
                if (userPermissionBits.hasPIM()) {
                    capabilities.add(getCapability("pim"));
                } else {
                    capabilities.remove("pim");
                }
                // Spam
                {
                    UserSettingMail mailSettings = serverSession.getUserSettingMail();
                    if (null != mailSettings && mailSettings.isSpamEnabled()) {
                        capabilities.add(getCapability("spam"));
                    } else {
                        capabilities.remove("spam");
                    }
                }
                // Global Address Book
                if (userPermissionBits.isGlobalAddressBookEnabled(serverSession)) {
                    capabilities.add(getCapability("gab"));
                } else {
                    capabilities.remove("gab");
                }
                // Permission properties
                final ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
                if (configViews != null) {
                    final ConfigView view = configViews.getView(userId, contextId);
                    final String property = PERMISSION_PROPERTY;
                    for (final String scope : configViews.getSearchPath()) {
                        final String permissions = view.property(property, String.class).precedence(scope).get();
                        if (permissions != null) {
                            for (String permissionModifier : P_SPLIT.split(permissions)) {
                                if (!isEmpty(permissionModifier)) {
                                    permissionModifier = permissionModifier.trim();
                                    final char firstChar = permissionModifier.charAt(0);
                                    if ('-' == firstChar) {
                                        capabilities.remove(permissionModifier.substring(1));
                                    } else {
                                        if ('+' == firstChar) {
                                            capabilities.add(getCapability(permissionModifier.substring(1)));
                                        } else {
                                            capabilities.add(getCapability(permissionModifier));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    final Map<String, ComposedConfigProperty<String>> all = view.all();
                    for (Map.Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
                        final String propName = entry.getKey();
                        if (propName.startsWith("com.openexchange.capability.")) {
                            boolean value = Boolean.parseBoolean(entry.getValue().get());
                            String name = toLowerCase(propName.substring(28));
                            if (value) {
                                capabilities.add(getCapability(name));
                            } else {
                                capabilities.remove(name);
                            }
                        }
                    }

                    // Check for a property handler
                    for (final Map.Entry<String, PropertyHandler> entry : PROPERTY_HANDLERS.entrySet()) {
                        final ComposedConfigProperty<String> composedConfigProperty = all.get(entry.getKey());
                        if (null != composedConfigProperty) {
                            entry.getValue().handleProperty(composedConfigProperty.get(), capabilities);
                        }
                    }

                }
            }
        }

        // ---------------- Now the ones from database ------------------ //
        {
            if (contextId > 0) {
                final Set<String> set = new HashSet<String>();
                final Set<String> removees = new HashSet<String>();
                // Context-sensitive
                for (final String sCap : getContextCaps(contextId, allowCache)) {
                    if (!isEmpty(sCap)) {
                        final char firstChar = sCap.charAt(0);
                        if ('-' == firstChar) {
                            final String val = toLowerCase(sCap.substring(1));
                            set.remove(val);
                            removees.add(val);
                        } else {
                            if ('+' == firstChar) {
                                set.add(toLowerCase(sCap.substring(1)));
                            } else {
                                set.add(toLowerCase(sCap));
                            }
                        }
                    }
                }
                // User-sensitive
                if (userId > 0) {
                    for (final String sCap : getUserCaps(userId, contextId, allowCache)) {
                        if (!isEmpty(sCap)) {
                            final char firstChar = sCap.charAt(0);
                            if ('-' == firstChar) {
                                final String val = toLowerCase(sCap.substring(1));
                                set.remove(val);
                                removees.add(val);
                            } else {
                                if ('+' == firstChar) {
                                    final String cap = toLowerCase(sCap.substring(1));
                                    set.add(cap);
                                    removees.remove(cap);
                                } else {
                                    final String cap = toLowerCase(sCap);
                                    set.add(cap);
                                    removees.remove(cap);
                                }
                            }
                        }
                    }
                }
                // Merge them into result set
                for (final String sCap : removees) {
                    capabilities.remove(sCap);
                }
                for (final String sCap : set) {
                    capabilities.add(getCapability(sCap));
                }
            }
        }

        // Now the declared ones
        for (String cap : declaredCapabilities.keySet()) {
            if (check(cap, serverSession, capabilities)) {
                capabilities.add(getCapability(cap));
            }
        }

        // Put in cache
        if (!serverSession.isAnonymous() && !serverSession.isTransient()) {
            final Cache cache = optCache();
            if (null != cache) {
                cache.putInGroup(Integer.valueOf(userId), Integer.toString(contextId), capabilities.clone(), false);
            }
        }

        if (computeCapabilityFilters) {
            applyUIFilter(capabilities);
        }

//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        System.out.println("AbstractCapabilityService.getCapabilities()");
//        new Throwable().printStackTrace(System.out);
//        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        return capabilities;
    }

    /**
     * Applies the filter on capabilities for JSON requests and if services (e. g. PasswordChangeService) are not available.
     *
     * @param capabilitiesToFilter - the capabilities the filter should be applied on
     */
    protected void applyUIFilter(CapabilitySet capabilitiesToFilter) {
        final PermissionAvailabilityServiceRegistry registry = this.registry;
        if (registry != null) {
            final Map<Permission, PermissionAvailabilityService> serviceList = registry.getServiceMap();
            for (final Permission p : PermissionAvailabilityService.CONTROLLED_PERMISSIONS) {
                if (!serviceList.containsKey(p)) {
                    capabilitiesToFilter.remove(p.getCapabilityName());
                }
            }
        } else {
            LOG.warn("Registry not initialized. Cannot check permissions for JSON requests");
        }
    }

    @Override
    public CapabilitySet getCapabilities(final int userId, final int contextId) throws OXException {
        return getCapabilities(userId, contextId, false, true);
    }

    @Override
    public CapabilitySet getCapabilities(final Session session) throws OXException {
        return getCapabilities(session.getUserId(), session.getContextId());
    }

    @Override
    public CapabilitySet getCapabilities(final Session session, final boolean computeCapabilityFilters) throws OXException {
        return getCapabilities(session.getUserId(), session.getContextId(), computeCapabilityFilters, true);
    }

    private boolean check(String cap, Session session, CapabilitySet allCapabilities) throws OXException {
        final Map<String, List<CapabilityChecker>> checkers = getCheckers();

        List<CapabilityChecker> list = checkers.get(cap.toLowerCase());
        if (null != list && !list.isEmpty()) {
            for (CapabilityChecker checker : list) {
                try {
                    if (checker instanceof DependentCapabilityChecker) {
                        DependentCapabilityChecker dependentChecker = (DependentCapabilityChecker) checker;
                        if (!dependentChecker.isEnabled(cap, session, allCapabilities)) {
                            return false;
                        }
                    } else if (!checker.isEnabled(cap, session)) {
                        return false;
                    }
                } catch (final Exception e) {
                    LOG.warn("Could not check availability for capability '{}'. Assuming as absent this time.", cap, e);
                }
            }
        }

        list = checkers.get("*");
        if (null != list && !list.isEmpty()) {
            for (CapabilityChecker checker : list) {
                if (checker instanceof DependentCapabilityChecker) {
                    DependentCapabilityChecker dependentChecker = (DependentCapabilityChecker) checker;
                    if (!dependentChecker.isEnabled(cap, session, allCapabilities)) {
                        return false;
                    }
                } else if (!checker.isEnabled(cap, session)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Gets all currently known capabilities.
     *
     * @return All capabilities
     */
    public Set<Capability> getAllKnownCapabilities() {
        return new HashSet<Capability>(CAPABILITIES.values());
    }

    @Override
    public boolean declareCapability(String capability) {
        final boolean added = null == declaredCapabilities.putIfAbsent(capability, PRESENT);

        if (added) {
            final Cache optCache = optCache();
            if (null != optCache) {
                try {
                    optCache.clear();
                } catch (final Exception e) {
                    // ignore
                }
            }
        }

        return added;
    }

    /**
     * Gets the available capability checkers.
     *
     * @return The checkers
     */
    protected abstract Map<String, List<CapabilityChecker>> getCheckers();

    private Set<String> getContextCaps(final int contextId, final boolean allowCache) throws OXException {
        if (contextId <= 0) {
            return Collections.emptySet();
        }
        final Cache cache = allowCache ? optContextCache() : null;
        if (null == cache) {
            return loadContextCaps(contextId);
        }
        final Object object = cache.get(Integer.valueOf(contextId));
        if (object instanceof Set) {
            @SuppressWarnings("unchecked") final Set<String> caps = (Set<String>) object;
            return caps;
        }
        // Load from database
        final Set<String> caps = loadContextCaps(contextId);
        cache.put(Integer.valueOf(contextId), new HashSet<String>(caps), false);
        return caps;
    }

    private Set<String> loadContextCaps(final int contextId) throws OXException {
        final DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            return Collections.emptySet();
        }
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cap FROM capability_context WHERE cid=?");
            stmt.setLong(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }
            final Set<String> set = new HashSet<String>();
            do {
                set.add(rs.getString(1));
            } while (rs.next());
            return set;
        } catch (final SQLException e) {
            throw CapabilityExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw CapabilityExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    private Set<String> getUserCaps(final int userId, final int contextId, final boolean allowCache) throws OXException {
        if (contextId <= 0 || userId <= 0) {
            return Collections.emptySet();
        }
        final Cache cache = allowCache ? optUserCache() : null;
        if (null == cache) {
            return loadUserCaps(userId, contextId);
        }
        final Object object = cache.getFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        if (object instanceof Set) {
            @SuppressWarnings("unchecked") final Set<String> caps = (Set<String>) object;
            return caps;
        }
        // Load from database
        final Set<String> caps = loadUserCaps(userId, contextId);
        cache.putInGroup(Integer.valueOf(userId), Integer.toString(contextId), new HashSet<String>(caps), false);
        return caps;
    }

    private Set<String> loadUserCaps(final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = services.getOptionalService(DatabaseService.class);
        if (null == databaseService) {
            return Collections.emptySet();
        }
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cap FROM capability_user WHERE cid=? AND user=?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }
            final Set<String> set = new HashSet<String>();
            do {
                set.add(rs.getString(1));
            } while (rs.next());
            return set;
        } catch (final SQLException e) {
            throw CapabilityExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw CapabilityExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    private static final class FakeSession implements Session, Serializable {

        private static final long serialVersionUID = -7827564586038651789L;

        private final int userId;
        private final int contextId;
        private final ConcurrentMap<String, Object> parameters;

        FakeSession(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            parameters = new ConcurrentHashMap<String, Object>(8);
        }

        @Override
        public int getContextId() {
            return contextId;
        }

        @Override
        public String getLocalIp() {
            return null;
        }

        @Override
        public void setLocalIp(final String ip) {
            // Nothing to do
        }

        @Override
        public String getLoginName() {
            return null;
        }

        @Override
        public boolean containsParameter(final String name) {
            return parameters.containsKey(name);
        }

        @Override
        public Object getParameter(final String name) {
            return parameters.get(name);
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getRandomToken() {
            return null;
        }

        @Override
        public String getSecret() {
            return null;
        }

        @Override
        public String getSessionID() {
            return null;
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public String getUserlogin() {
            return null;
        }

        @Override
        public String getLogin() {
            return null;
        }

        @Override
        public void setParameter(final String name, final Object value) {
            if (null == value) {
                parameters.remove(name);
            } else {
                parameters.put(name, value);
            }
        }

        @Override
        public String getAuthId() {
            return null;
        }

        @Override
        public String getHash() {
            return null;
        }

        @Override
        public void setHash(final String hash) {
            // Nope
        }

        @Override
        public String getClient() {
            return null;
        }

        @Override
        public void setClient(final String client) {
            // Nothing to do
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        @Override
        public Set<String> getParameterNames() {
            return parameters.keySet();
        }
    }

}
