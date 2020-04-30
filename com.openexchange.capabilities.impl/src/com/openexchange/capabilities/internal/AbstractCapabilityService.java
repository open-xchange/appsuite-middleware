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

package com.openexchange.capabilities.internal;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Strings.isEmpty;
import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.osgi.Tools.requireService;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import com.google.common.collect.ImmutableMap;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.ConfigurationProperty;
import com.openexchange.capabilities.DependentCapabilityChecker;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.capabilities.osgi.PermissionAvailabilityServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.PermissionConfigurationChecker;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.java.BoolReference;
import com.openexchange.java.ConcurrentEnumMap;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Origin;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AbstractCapabilityService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractCapabilityService implements CapabilityService, Reloadable {

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
        ImmutableMap.Builder<String, PropertyHandler> map = ImmutableMap.builder();

        map.put("com.openexchange.caldav.enabled", new PropertyHandler() {

            @Override
            public void handleProperty(final String propValue, final CapabilitySet capabilities) throws OXException {
                doHandleProperty(propValue, (CapabilitySetImpl) capabilities);
            }

            private void doHandleProperty(final String propValue, final CapabilitySetImpl capabilities) {
                if (Boolean.parseBoolean(propValue)) {
                    capabilities.add(getCapability(Permission.CALDAV), CapabilitySource.CONFIGURATION, "Through property \"com.openexchange.caldav.enabled\"");
                } else {
                    capabilities.remove(Permission.CALDAV.getCapabilityName(), CapabilitySource.CONFIGURATION, "Through property \"com.openexchange.caldav.enabled\"");
                }
            }
        });

        map.put("com.openexchange.carddav.enabled", new PropertyHandler() {

            @Override
            public void handleProperty(final String propValue, final CapabilitySet capabilities) throws OXException {
                doHandleProperty(propValue, (CapabilitySetImpl) capabilities);
            }

            private void doHandleProperty(final String propValue, final CapabilitySetImpl capabilities) {
                if (Boolean.parseBoolean(propValue)) {
                    capabilities.add(getCapability(Permission.CARDDAV), CapabilitySource.CONFIGURATION, "Through property \"com.openexchange.carddav.enabled\"");
                } else {
                    capabilities.remove(Permission.CARDDAV.getCapabilityName(), CapabilitySource.CONFIGURATION, "Through property \"com.openexchange.carddav.enabled\"");
                }
            }
        });

        PROPERTY_HANDLERS = map.build();
    }

    // ------------------------------------------------------------------------------------------------ //

    private static final ConcurrentEnumMap<Permission, Capability> P2CAPABILITIES = new ConcurrentEnumMap<Permission, Capability>(Permission.class);
    private static final ConcurrentMap<String, Capability> CAPABILITIES = new ConcurrentHashMap<String, Capability>(96, 0.9f, 1);

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

    /**
     * Registry that provides the registered JSON bundles to check for permissions
     */
    private final PermissionAvailabilityServiceRegistry registry;

    /**
     * Initializes a new {@link AbstractCapabilityService}.
     *
     * @param registry that provides the services for the registered JSON bundles
     */
    protected AbstractCapabilityService(final ServiceLookup services, PermissionAvailabilityServiceRegistry registry) {
        super();
        this.services = services;
        declaredCapabilities = new ConcurrentHashMap<String, Object>(32, 0.9f, 1);
        this.registry = registry;
    }

    private Cache optContextCache() {
        final CacheService service = services.getOptionalService(CacheService.class);
        if (null == service) {
            return null;
        }
        try {
            return service.getCache(REGION_NAME_CONTEXT);
        } catch (OXException e) {
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
        } catch (OXException e) {
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
        } catch (OXException e) {
            LOG.error("", e);
            return null;
        }
    }

    private CapabilitySetImpl optCachedCapabilitySet(final int userId, final int contextId) {
        final Cache cache = optCache();
        if (null == cache) {
            return null;
        }
        final Object object = cache.getFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        return (object instanceof CapabilitySetImpl) ? ((CapabilitySetImpl) object).clone() : null;
    }

    @Override
    public CapabilitySet getCapabilities(int userId, int contextId) throws OXException {
        return getCapabilities(userId, contextId, false, true);
    }

    @Override
    public CapabilitySet getCapabilities(int userId, int contextId, boolean alignPermissions, boolean allowCache) throws OXException {
        return getCapabilities(userId, contextId, null, alignPermissions, allowCache);
    }

    @Override
    public CapabilitySet getCapabilities(Session session) throws OXException {
        return getCapabilities(session.getUserId(), session.getContextId(), session, false, true);
    }

    @Override
    public CapabilitySet getCapabilities(Session session, boolean alignPermissions) throws OXException {
        return getCapabilities(session.getUserId(), session.getContextId(), session, alignPermissions, true);
    }

    /**
     * Gets the capabilities.
     *
     * @param userId The user ID or <code>-1</code>
     * @param contextId The context ID or <code>-1</code>
     * @param session The session; must either belong to the user and context ID or be <code>null</code>
     * @param alignPermissions Whether permission-bound capabilities shall be removed from the resulting set if the services
     *                         which define/require those are unavailable (e.g. a user has the <code>editpassword</code> permission
     *                         set, but no PasswordChangeService is available).
     * @param allowCache Whether the capabilities may be looked up from cache
     * @return
     * @throws OXException
     */
    private CapabilitySetImpl getCapabilities(int userId, int contextId, Session session, boolean alignPermissions, boolean allowCache) throws OXException {
        return getCapabilities(userId, contextId, session, alignPermissions, getCacheOptionsFor(allowCache, true));
    }


    /**
     * Gets the capabilities.
     *
     * @param userId The user ID or <code>-1</code>
     * @param contextId The context ID or <code>-1</code>
     * @param session The session; must either belong to the user and context ID or be <code>null</code>
     * @param alignPermissions Whether permission-bound capabilities shall be removed from the resulting set if the services
     *                         which define/require those are unavailable (e.g. a user has the <code>editpassword</code> permission
     *                         set, but no PasswordChangeService is available).
     * @param cacheOptions The cache options
     * @return
     * @throws OXException
     */
    private CapabilitySetImpl getCapabilities(int userId, int contextId, Session session, boolean alignPermissions, CacheOptions cacheOptions) throws OXException {
        // Try fetch from cache is allowed
        if (cacheOptions.allowFetchFromCache) {
            CapabilitySetImpl cachedCapabilitySet = optCachedCapabilitySet(userId, contextId);
            if (null != cachedCapabilitySet) {
                if (alignPermissions) {
                    alignPermissions(cachedCapabilitySet);
                }
                return cachedCapabilitySet;
            }
        }

        // Load context
        Context context = null;
        if (contextId > 0) {
            context = requireService(ContextService.class, services).getContext(contextId);
        }

        // Load user
        User user = null;
        if (userId > 0) {
            user = requireService(UserService.class, services).getUser(userId, context);
        }

        /*-
         * Compile set of effective capabilities
         *
         * NOTE: Never ever re-order the apply methods!
         */
        CapabilitySetImpl capabilities = new CapabilitySetImpl(64);
        Map<Capability, ValueAndScope> forcedCapabilities = new HashMap<Capability, ValueAndScope>(4);
        applyUserPermissions(capabilities, user, context);
        applyConfiguredCapabilities(capabilities, forcedCapabilities, user, context, cacheOptions.allowFetchFromCache);
        BoolReference putIntoCache = new BoolReference(cacheOptions.allowPutIntoCache);
        if (session == null) {
            applyDeclaredCapabilities(capabilities, ServerSessionAdapter.valueOf(userId, contextId), putIntoCache); // Code smell level: Ninja...
        } else {
            applyDeclaredCapabilities(capabilities, session, putIntoCache);
        }
        applyGuestFilter(capabilities, user);
        for (Map.Entry<Capability, ValueAndScope> forcedCapability : forcedCapabilities.entrySet()) {
            Capability capability = forcedCapability.getKey();
            ValueAndScope vas = forcedCapability.getValue();
            if (vas.value.booleanValue()) {
                capabilities.add(capability, CapabilitySource.CONFIGURATION, "Forced through \"com.openexchange.capability.forced." + capability.getId() + "\" with final scope " + vas.scope);
            } else {
                capabilities.remove(capability, CapabilitySource.CONFIGURATION, "Forced through \"com.openexchange.capability.forced." + capability.getId() + "\" with final scope " + vas.scope);
            }
        }

        if (putIntoCache.getValue() && userId > 0 && contextId > 0 && (session == null || !session.isTransient())) {
            // Put in cache
            Cache cache = optCache();
            if (null != cache) {
                cache.putInGroup(Integer.valueOf(userId), Integer.toString(contextId), capabilities.clone(), false);
            }
        }

        if (alignPermissions) {
            alignPermissions(capabilities);
        }

        if (LOG.isDebugEnabled()) {
            capabilities.printHistoryFor(userId, contextId, LOG);
        }

        return capabilities;
    }

    /**
     * Reacts to a configuration reload by invalidating cached capabilities.
     */
    public void onReloadConfiguration() {
        Cache cache = optCache();
        if (null != cache) {
            try {
                cache.clear();
            } catch (OXException e) {
                LOG.warn("Failed to invalidate '{}' cache", REGION_NAME, e);
            }
        }
    }

    /**
     * Adds the capabilities that represent user permissions to the passed set, if a valid user and context are given.
     *
     * @param capabilities The capability set
     * @param user The user; if <code>null</code>, calling this method is a no-op
     * @param context The context; if <code>null</code>, calling this method is a no-op
     * @throws OXException
     */
    private void applyUserPermissions(CapabilitySetImpl capabilities, User user, Context context) throws OXException {
        if (user == null || context == null) {
            return;
        }

        final UserPermissionBits userPermissionBits = services.getService(UserPermissionService.class).getUserPermissionBits(user.getId(), context.getContextId());
        userPermissionBits.setGroups(user.getGroups());

        // Capabilities by user permission bits
        for (final Permission p : Permission.byBits(userPermissionBits.getPermissionBits())) {
            capabilities.add(getCapability(p), CapabilitySource.PERMISSIONS);
        }

        // Webmail
        if (user.getId() == context.getMailadmin()) {
            final boolean adminMailLoginEnabled = services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.mail.adminMailLoginEnabled", false);
            if (!adminMailLoginEnabled) {
                capabilities.remove(getCapability(Permission.WEBMAIL), CapabilitySource.PERMISSIONS, "Through property \"com.openexchange.mail.adminMailLoginEnabled\"");
            }
        }

        // Portal - stick to positive "portal" capability only
        capabilities.remove("denied_portal");
        if (userPermissionBits.hasPortal()) {
            capabilities.add(getCapability("portal"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("portal", CapabilitySource.PERMISSIONS);
        }
        // Free-Busy
        if (userPermissionBits.hasFreeBusy()) {
            capabilities.add(getCapability("freebusy"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("freebusy", CapabilitySource.PERMISSIONS);
        }
        // Conflict-Handling
        if (userPermissionBits.hasConflictHandling()) {
            capabilities.add(getCapability("conflict_handling"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("conflict_handling", CapabilitySource.PERMISSIONS);
        }
        // Participants-Dialog
        if (userPermissionBits.hasParticipantsDialog()) {
            capabilities.add(getCapability("participants_dialog"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("participants_dialog", CapabilitySource.PERMISSIONS);
        }
        // Group-ware
        if (userPermissionBits.hasGroupware()) {
            capabilities.add(getCapability("groupware"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("groupware", CapabilitySource.PERMISSIONS);
        }
        // PIM
        if (userPermissionBits.hasPIM()) {
            capabilities.add(getCapability("pim"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("pim", CapabilitySource.PERMISSIONS);
        }
        // Spam
        if (userPermissionBits.hasWebMail()) {
            UserSettingMail mailSettings = UserSettingMailStorage.getInstance().getUserSettingMail(user.getId(), context);
            if (null != mailSettings && mailSettings.isSpamEnabled()) {
                capabilities.add(getCapability("spam"), CapabilitySource.PERMISSIONS);
            } else {
                capabilities.remove("spam", CapabilitySource.PERMISSIONS);
            }
        }
        // Global Address Book
        if (userPermissionBits.isGlobalAddressBookEnabled()) {
            capabilities.add(getCapability("gab"), CapabilitySource.PERMISSIONS);
        } else {
            capabilities.remove("gab", CapabilitySource.PERMISSIONS);
        }
    }

    /**
     * Adds all capabilities that have been specified via any mechanism of configuration to the passed set. Such capabilities are:
     *
     * <ul>
     *  <li>capabilities specified via the <code>permissions</code> property - looked up via config cascade</li>
     *  <li>capabilities specified as properties with a <code>com.openexchange.capability.</code> prefix - looked up via config cascade</li>
     *  <li>capabilities that depend on whether a feature is enabled via a configuration property (e.g. <code>com.openexchange.caldav.enabled => caldav</code>)</li>
     *  <li>context-specific capabilities contained in the context_capabilities database table</li>
     *  <li>user-specific capabilities contained in the user_capabilities database table</li>
     * </ul>
     *
     * @param capabilities The capability set
     * @param forcedCapabilities The forcibly configured capabilities
     * @param user The user; if <code>null</code>, all config cascade lookups are performed with user ID <code>-1</code>;
     *             if the user is a guest, the configure guest capability mode is considered.
     * @param context The context; if <code>null</code>, all config cascade lookups are performed with context ID <code>-1</code>
     * @param allowCache Whether caching of loaded capabilities is allowed
     * @throws OXException
     */
    private void applyConfiguredCapabilities(CapabilitySetImpl capabilities, Map<Capability, ValueAndScope> forcedCapabilities, User user, Context context, boolean allowCache) throws OXException {
        final int contextId = context == null ? -1 : context.getContextId();
        final int userId = user == null ? -1 : user.getId();
        FilteringCapabilities filteredCaps = new FilteringCapabilities(forcedCapabilities, capabilities, (name) -> B(isLegal(name, userId, contextId)));
        if (user != null) {
            if (user.isGuest()) {
                GuestCapabilityMode capMode = getGuestCapabilityMode(user, context);
                if (capMode == GuestCapabilityMode.INHERIT) {
                    applyConfigCascade(filteredCaps, user.getCreatedBy(), contextId);
                    applyContextCapabilities(filteredCaps, contextId, allowCache);
                    applyUserCapabilities(filteredCaps, user.getCreatedBy(), contextId, allowCache);
                    applyUserCapabilities(filteredCaps, userId, contextId, allowCache);
                } else if (capMode == GuestCapabilityMode.STATIC) {
                    applyStaticGuestCapabilities(capabilities, user, context);
                    applyUserCapabilities(filteredCaps, userId, contextId, allowCache);
                } else if (capMode == GuestCapabilityMode.DENY_ALL) {
                    applyUserCapabilities(filteredCaps, userId, contextId, allowCache);
                }
            } else {
                applyConfigCascade(filteredCaps, userId, contextId);
                applyContextCapabilities(filteredCaps, contextId, allowCache);
                applyUserCapabilities(filteredCaps, userId, contextId, allowCache);
            }
        } else {
            applyConfigCascade(filteredCaps, userId, contextId);
            applyContextCapabilities(filteredCaps, contextId, allowCache);
        }
    }

    /**
     * Checks whether the capability should be applied or not
     *
     * @param capName The name of the capability to check
     * @param userId The user identifier or <code>-1</code>
     * @param ctxId The context identifier or <code>-1</code>
     * @return <code>true</code> if the capability should be applied, <code>false</code> otherwise
     */
    private final boolean isLegal(String capName, int userId, int ctxId) {
        try {
            return services.getServiceSafe(PermissionConfigurationChecker.class).isLegal(capName, userId, ctxId);
        } catch (OXException e) {
            LOG.error("", e);
            return true;
        }
    }

    /**
     * Adds all capabilities that have been specified via config-cascade properties to the passed set.
     *
     * <ul>
     *  <li>capabilities specified via the <code>permissions</code> property - looked up via config-cascade</li>
     *  <li>capabilities specified as properties with a <code>com.openexchange.capability.</code> prefix - looked up via config-cascade</li>
     *  <li>capabilities that depend on whether a feature is enabled via a configuration property (e.g. <code>com.openexchange.caldav.enabled => caldav</code>)</li>
     * </ul>
     *
     * @param capabilities The {@link FilteringCapabilities}
     * @param userId The user ID for config-cascade lookups
     * @param contextId The context ID for config-cascade lookups
     * @throws OXException
     */
    private void applyConfigCascade(FilteringCapabilities capabilities, int userId, int contextId) throws OXException {
        // Permission properties
        final ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        if (configViews != null) {
            final ConfigView view = configViews.getView(userId, contextId);
            final String property = PERMISSION_PROPERTY;
            for (final String scope : configViews.getSearchPath()) {
                final String permissions = view.property(property, String.class).precedence(scope).get();
                if (permissions != null) {
                    for (String permissionModifier : Strings.splitByComma(permissions)) {
                        if (!isEmpty(permissionModifier)) {
                            char firstChar = permissionModifier.charAt(0);
                            if ('-' == firstChar) {
                                capabilities.remove(permissionModifier.substring(1), CapabilitySource.CONFIGURATION, "Through \"permissions\" property in file 'permissions.properties'");
                            } else {
                                if ('+' == firstChar) {
                                    String name = permissionModifier.substring(1);
                                    capabilities.add(name, () -> getCapability(name), CapabilitySource.CONFIGURATION, "Through \"permissions\" property in file 'permissions.properties'");
                                } else {
                                    String name = permissionModifier;
                                    capabilities.add(name, () -> getCapability(name), CapabilitySource.CONFIGURATION, "Through \"permissions\" property in file 'permissions.properties'");
                                }
                            }
                        }
                    }
                }
            }

            final Map<String, ComposedConfigProperty<String>> all = view.all();
            for (Map.Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
                final String propName = entry.getKey();
                if (propName.startsWith("com.openexchange.capability.", 0)) {
                    ComposedConfigProperty<String> configProperty = entry.getValue();
                    /*
                     * don't apply undefined capabilities
                     */
                    if (false == configProperty.isDefined()) {
                        LOG.debug("Ignoring undefined capability property for user {} in context {}: {}",
                            Integer.valueOf(userId), Integer.valueOf(contextId), propName);
                        continue;
                    }
                    /*
                     * apply capability
                     */
                    String name = toLowerCase(propName.substring(28));
                    String scope = configProperty.getScope();
                    String value = configProperty.get();
                    if (name.startsWith("forced.", 0)) {
                        name = name.substring(7);
                        final String capName = name;
                        capabilities.addForced(name, () -> getCapability(capName), new ValueAndScope(Boolean.valueOf(value), scope));
                    } else {
                        final String capName = name;
                        if (Boolean.parseBoolean(value)) {
                            capabilities.add(name, () -> getCapability(capName), CapabilitySource.CONFIGURATION, "Final scope: " + scope);
                        } else {
                            capabilities.remove(name, CapabilitySource.CONFIGURATION, "Final scope: " + scope);
                        }
                    }
                    /*
                     * additionally check for discouraged use of module permissions
                     */
                    Permission matchingModulePermission = Permission.get(name);
                    if (null != matchingModulePermission) {
                        LOG.debug("Overriding module permission {} with 'capability' property {}={} for user {} in context {}.",
                            matchingModulePermission, propName, value, Integer.valueOf(userId), Integer.valueOf(contextId));
                    }
                }
            }

            // Check for a property handler
            for (final Map.Entry<String, PropertyHandler> entry : PROPERTY_HANDLERS.entrySet()) {
                final ComposedConfigProperty<String> composedConfigProperty = all.get(entry.getKey());
                if (null != composedConfigProperty) {
                    entry.getValue().handleProperty(composedConfigProperty.get(), capabilities.getCapabilitySet());
                }
            }
        }
    }

    /**
     * Adds all capabilities that are specified as <code>context_capabilities</code> DB entries to the passed set.
     *
     * @param capabilities The {@link FilteringCapabilities}
     * @param contextId The context ID; if negative calling this method is a no-op
     * @param allowCache Whether caching of loaded capabilities is allowed
     * @throws OXException
     */
    private void applyContextCapabilities(FilteringCapabilities capabilities, int contextId, boolean allowCache) throws OXException {
        if (contextId > 0) {
            applySet(capabilities, getContextCaps(contextId, allowCache), CapabilitySource.CONTEXT);
        }
    }

    /**
     * Adds all capabilities that are specified as <code>user_capabilities</code> DB entries to the passed set.
     *
     * @param capabilities The {@link FilteringCapabilities}
     * @param userId The user ID; if negative calling this method is a no-op
     * @param contextId The context ID; if negative calling this method is a no-op
     * @param allowCache Whether caching of loaded capabilities is allowed
     * @throws OXException
     */
    private void applyUserCapabilities(FilteringCapabilities capabilities, int userId, int contextId, boolean allowCache) throws OXException {
        if (contextId > 0 && userId > 0) {
            applySet(capabilities, getUserCaps(userId, contextId, allowCache), CapabilitySource.USER);
        }
    }

    /**
     * Applies the given capabilities to the {@link FilteringCapabilities} set
     *
     *
     * @param capabilities The {@link FilteringCapabilities}
     * @param capabilitiesToApply The capabilities to apply
     */
    private void applySet(FilteringCapabilities capabilities, Set<String> capabilitiesToApply, CapabilitySource source) {
        Set<String> set = new HashSet<String>();
        Set<String> removees = new HashSet<String>();
        for (String sCap : capabilitiesToApply) {
            if (!isEmpty(sCap)) {
                char firstChar = sCap.charAt(0);
                if ('-' == firstChar) {
                    String val = toLowerCase(sCap.substring(1));
                    set.remove(val);
                    removees.add(val);
                } else {
                    if ('+' == firstChar) {
                        String cap = toLowerCase(sCap.substring(1));
                        set.add(cap);
                        removees.remove(cap);
                    } else {
                        String cap = toLowerCase(sCap);
                        set.add(cap);
                        removees.remove(cap);
                    }
                }
            }
        }

        // Merge them into result set
        for (String sCap : removees) {
            capabilities.remove(sCap, source, null);
        }
        for (String sCap : set) {
            capabilities.add(sCap, () -> getCapability(sCap), source, null);
        }
    }

    /**
     * Adds all capabilities to the passed set, which are specified via {@link CapabilityService#declareCapability(String)}.
     * Every declared capability is checked against the registered {@link CapabilityChecker}s.
     *
     * @param capabilities The capability set
     * @param session The session; either a real one or a synthetic one, but never <code>null</code>
     * @param putIntoCache Tracks whether a put into cache is recommended
     */
    private void applyDeclaredCapabilities(CapabilitySetImpl capabilities, Session session, BoolReference putIntoCache) {
        for (String cap : declaredCapabilities.keySet()) {
            if (check(cap, session, capabilities, putIntoCache)) {
                capabilities.add(getCapability(cap), CapabilitySource.DECLARED);
            } else {
                capabilities.remove(cap, CapabilitySource.DECLARED);
            }
        }
    }

    /**
     * If the passed user is a guest user, the <code>guest</code> capability is set. Additionally
     * the capabilities <code>share_links</code> and <code>invite_guests</code> are removed.
     *
     * @param capabilities The capability set
     * @param user The user; if <code>null</code>, calling this method is a no-op
     */
    private void applyGuestFilter(CapabilitySetImpl capabilities, User user) {
        if (user == null) {
            return;
        }

        if (user.isGuest()) {
            capabilities.add(getCapability("guest"));
            if (ShareTool.isAnonymousGuest(user)) {
                capabilities.add(getCapability("anonymous"));
            }
            capabilities.remove("share_links");
            capabilities.remove("invite_guests");
            if (Strings.isNotEmpty(user.getMail())) {
                capabilities.add(getCapability("edit_password"));
            }
            capabilities.remove("mailfilter_v2");
            capabilities.remove("mailfilter");
        }
    }

    /**
     * A user might have some permission bits set that belong to services which are (currently or generally) unavailable.
     * This method removes those permissions from the passed capability set.
     *
     * @param capabilities The capability set
     */
    protected void alignPermissions(CapabilitySet capabilities) {
        final PermissionAvailabilityServiceRegistry registry = this.registry;
        if (registry != null) {
            final Map<Permission, PermissionAvailabilityService> serviceList = registry.getServiceMap();
            for (final Permission p : PermissionAvailabilityService.CONTROLLED_PERMISSIONS) {
                if (!serviceList.containsKey(p)) {
                    capabilities.remove(p.getCapabilityName());
                }
            }
        } else {
            LOG.warn("Registry not initialized. Cannot check permissions for JSON requests");
        }
    }

    /**
     * Applies the statically configured capabilities to the passed capability set.
     *
     * @param capabilities The capability set
     * @param user The guest user
     * @param context The context
     * @throws OXException
     */
    private void applyStaticGuestCapabilities(CapabilitySetImpl capabilities, User user, Context context) throws OXException {
        ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        if (configViews != null) {
            ConfigView view = configViews.getView(user.getId(), context.getContextId());
            String value = view.opt("com.openexchange.share.staticGuestCapabilities", String.class, "");
            if (Strings.isNotEmpty(value)) {
                List<String> staticCapabilities = Strings.splitAndTrim(value, ",");
                for (String cap : staticCapabilities) {
                    capabilities.add(getCapability(cap), CapabilitySource.CONFIGURATION, "Through property \"com.openexchange.share.staticGuestCapabilities\"");
                }
            }
        }
    }

    /**
     * Gets the capability mode for the given guest user.
     *
     * @param user The guest user
     * @param context The context
     * @return The mode
     * @throws OXException
     */
    private GuestCapabilityMode getGuestCapabilityMode(User user, Context context) throws OXException {
        ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        if (configViews != null) {
            ConfigView view = configViews.getView(user.getId(), context.getContextId());
            String value = view.opt("com.openexchange.share.guestCapabilityMode", String.class, "static");
            for (GuestCapabilityMode mode : GuestCapabilityMode.values()) {
                if (mode.name().toLowerCase().equals(value)) {
                    return mode;
                }
            }
        }

        return GuestCapabilityMode.STATIC;
    }

    private static enum GuestCapabilityMode {
        DENY_ALL, STATIC, INHERIT;
    }

    private boolean check(String cap, Session session, CapabilitySet allCapabilities, BoolReference putIntoCache) {
        final Map<String, List<CapabilityChecker>> checkers = getCheckers();

        List<CapabilityChecker> list = checkers.get(cap.toLowerCase());
        if (null != list && !list.isEmpty()) {
            for (CapabilityChecker checker : list) {
                if (!performCapabilityCheck(checker, cap, session, allCapabilities, putIntoCache)) {
                    return false;
                }
            }
        }

        list = checkers.get("*");
        if (null != list && !list.isEmpty()) {
            for (CapabilityChecker checker : list) {
                if (!performCapabilityCheck(checker, cap, session, allCapabilities, putIntoCache)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean performCapabilityCheck(CapabilityChecker checker, String cap, Session session, CapabilitySet allCapabilities, BoolReference putIntoCache) {
        try {
            if (checker instanceof DependentCapabilityChecker) {
                if (!((DependentCapabilityChecker) checker).isEnabled(cap, session, allCapabilities)) {
                    return false;
                }
            } else if (checker instanceof FailureAwareCapabilityChecker) {
                switch (((FailureAwareCapabilityChecker) checker).checkEnabled(cap, session)) {
                    case DISABLED:
                        return false;
                    case FAILURE:
                        putIntoCache.setValue(false);
                        return false;
                    default:
                        break;
                }
            } else if (!checker.isEnabled(cap, session)) {
                return false;
            }
        } catch (Exception e) {
            LOG.warn("Could not check availability for capability '{}'. Assuming as absent this time.", cap, e);
            putIntoCache.setValue(false);
            return false;
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
        boolean added = null == declaredCapabilities.putIfAbsent(capability, PRESENT);

        if (added) {
            final Cache optCache = optCache();
            if (null != optCache) {
                try {
                    optCache.localClear();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return added;
    }

    @Override
    public boolean undeclareCapability(String capability) {
        boolean removed = null != declaredCapabilities.remove(capability);

        if (removed) {
            final Cache optCache = optCache();
            if (null != optCache) {
                try {
                    optCache.localClear();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        return removed;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<ConfigurationProperty> getConfigurationSource(int userId, int contextId, String searchPattern) throws OXException {
        List<ConfigurationProperty> properties = new ArrayList<ConfigurationProperty>();

        final ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        if (configViews != null) {
            final ConfigView view = configViews.getView(userId, contextId);

            if (view != null) {
                Map<String, ComposedConfigProperty<String>> all = view.all();

                for (Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
                    String key = entry.getKey();

                    if (!StringUtils.containsIgnoreCase(key, searchPattern)) {
                        continue;
                    }
                    if ((entry.getValue().getScope() == null) && (entry.getValue().get() == null)) {
                        LOG.info("Scope and value for property {} null. Going to ignore it", key);
                        continue;
                    }
                    properties.add(new ConfigurationProperty(entry.getValue().getScope(), key, entry.getValue().get()));
                }
            }
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Map<String, Set<String>>> getCapabilitiesSource(int userId, int contextId) throws OXException {
        if (userId <= 0 || contextId <= 0) {
            return doGetCapabilitiesSource(userId, contextId);
        }

        // User and context identifier available
        LogProperties.putUserProperties(userId, contextId);
        try {
            return doGetCapabilitiesSource(userId, contextId);
        } finally {
            LogProperties.removeUserProperties();
        }
    }

    private Map<String, Map<String, Set<String>>> doGetCapabilitiesSource(int userId, int contextId) throws OXException {
        if (userId > 0 && contextId > 0) {
            if (LOG.isDebugEnabled()) {
                CapabilitySetImpl capabilitySet = optCachedCapabilitySet(userId, contextId);
                if (capabilitySet == null) {
                    capabilitySet = getCapabilities(userId, contextId, null, true, getCacheOptionsFor(false, false));
                } else {
                    alignPermissions(capabilitySet);
                }

                capabilitySet.printHistoryFor(userId, contextId, LOG);
            }
        }

        Map<String, Map<String, Set<String>>> sets = new LinkedHashMap<String, Map<String, Set<String>>>(6);

        /*
         * Add capabilities based on user permissions
         */
        {
            User user = null;
            Context context = null;
            if (contextId > 0) {
                context = requireService(ContextService.class, services).getContext(contextId);
                if (userId > 0) {
                    user = requireService(UserService.class, services).getUser(userId, context);
                }
            }

            Set<String> capabilities = new TreeSet<String>();
            CapabilitySetImpl capabilitySet = new CapabilitySetImpl(64);
            applyUserPermissions(capabilitySet, user, context);
            for (Iterator<Capability> iterator = capabilitySet.iterator(); iterator.hasNext();) {
                Capability capability = iterator.next();
                if (null != capability) {
                    capabilities.add(capability.getId());
                }
            }
            Map<String, Set<String>> arr = new LinkedHashMap<String, Set<String>>(3);
            arr.put("granted", capabilities);
            arr.put("denied", new HashSet<String>(0));
            sets.put("permissions", arr);
        }

        {
            CapabilitySetImpl grantedCapabilities = new CapabilitySetImpl(16);
            CapabilitySetImpl deniedCapabilities = new CapabilitySetImpl(16);
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
                                    String name = permissionModifier.substring(1);
                                    if (isLegal(name, userId, contextId) == false) {
                                        continue;
                                    }
                                    deniedCapabilities.add(getCapability(name));
                                } else {
                                    String name = ('+' == firstChar) ? permissionModifier.substring(1) : permissionModifier;
                                    if (isLegal(name, userId, contextId) == false) {
                                        continue;
                                    }
                                    grantedCapabilities.add(getCapability(name));
                                }
                            }
                        }
                    }
                }

                final Map<String, ComposedConfigProperty<String>> all = view.all();
                for (Map.Entry<String, ComposedConfigProperty<String>> entry : all.entrySet()) {
                    final String propName = entry.getKey();
                    if (propName.startsWith("com.openexchange.capability.")) {
                        ComposedConfigProperty<String> configProperty = entry.getValue();
                        if (false == configProperty.isDefined()) {
                            LOG.debug("Ignoring undefined capability property for user {} in context {}: {}",
                                Integer.valueOf(userId), Integer.valueOf(contextId), propName);
                            continue;
                        }
                        boolean value = Boolean.parseBoolean(configProperty.get());
                        String name = toLowerCase(propName.substring(28));
                        if (isLegal(name, userId, contextId) == false) {
                            continue;
                        }
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

                Map<String, Set<String>> arr = new LinkedHashMap<String, Set<String>>(3);
                {
                    Set<String> set = new TreeSet<String>();
                    for (Capability cap : grantedCapabilities) {
                        set.add(cap.getId());
                    }
                    arr.put("granted", set);
                }
                {
                    Set<String> set = new TreeSet<String>();
                    for (Capability cap : deniedCapabilities) {
                        set.add(cap.getId());
                    }
                    arr.put("denied", set);
                }
                sets.put("configuration", arr);
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
                            final String name = toLowerCase(sCap.substring(1));
                            if (isLegal(name, userId, contextId) == false) {
                                continue;
                            }
                            set.remove(name);
                            removees.add(name);
                        } else {
                            String name = ('+' == firstChar) ? sCap.substring(1) : sCap;
                            if (isLegal(name, userId, contextId) == false) {
                                continue;
                            }
                            set.add(name);
                        }
                    }
                }
                // User-sensitive
                if (userId > 0) {
                    for (final String sCap : getUserCaps(userId, contextId, false)) {
                        if (!isEmpty(sCap)) {
                            final char firstChar = sCap.charAt(0);
                            if ('-' == firstChar) {
                                final String name = toLowerCase(sCap.substring(1));
                                if (isLegal(name, userId, contextId) == false) {
                                    continue;
                                }
                                set.remove(name);
                                removees.add(name);
                            } else {
                                String name = ('+' == firstChar) ? sCap.substring(1) : sCap;
                                if (isLegal(name, userId, contextId) == false) {
                                    continue;
                                }
                                set.add(name);
                                removees.remove(name);
                            }
                        }
                    }
                }

                // Merge them into result set
                Map<String, Set<String>> arr = new LinkedHashMap<String, Set<String>>(3);
                arr.put("granted", set);
                arr.put("denied", removees);
                sets.put("provisioning", arr);
            }
        }

        {
            // Now the declared ones
            CapabilitySetImpl grantedCapabilities = new CapabilitySetImpl(16);
            FakeSession fakeSession = new FakeSession(userId, contextId);
            BoolReference dontCare = new BoolReference();
            for (String cap : declaredCapabilities.keySet()) {
                if (check(cap, fakeSession, grantedCapabilities, dontCare)) {
                    grantedCapabilities.add(getCapability(cap));
                }
            }

            Map<String, Set<String>> arr = new LinkedHashMap<String, Set<String>>(3);
            {
                Set<String> set = new TreeSet<String>();
                for (Capability cap : grantedCapabilities) {
                    set.add(cap.getId());
                }
                arr.put("granted", set);
            }
            arr.put("denied", new HashSet<String>(0));
            sets.put("programmatic", arr);
        }

        return sets;
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
        } catch (SQLException e) {
            throw CapabilityExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
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
        } catch (SQLException e) {
            throw CapabilityExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw CapabilityExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(PermissionConfigurationChecker.PROP_APPLY_ILLEGAL_PERMISSIONS).build();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        try {
            optCache().clear();
        } catch (OXException e) {
            LOG.error("Unable to clear capability cache: {}",e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------- //

    private static final CacheOptions READ_OPTIONS = new CacheOptions(true, true);

    private static final CacheOptions NON_READ_OPTIONS = new CacheOptions(false, true);

    private static CacheOptions getCacheOptionsFor(boolean allowFetchFromCache, boolean allowPutInotCache) {
        if (allowPutInotCache) {
            return allowFetchFromCache ? READ_OPTIONS : NON_READ_OPTIONS;
        }
        return new CacheOptions(allowFetchFromCache, allowPutInotCache);
    }

    private static final class CacheOptions {

        final boolean allowFetchFromCache;
        final boolean allowPutIntoCache;

        CacheOptions(boolean allowFetchFromCache, boolean allowPutIntoCache) {
            super();
            this.allowFetchFromCache = allowFetchFromCache;
            this.allowPutIntoCache = allowPutIntoCache;
        }
    }

    private static final class FakeSession implements Session, Serializable {

        private static final long serialVersionUID = -7827564586038651789L;

        private final int userId;
        private final int contextId;
        private final ConcurrentMap<String, Object> parameters;

        FakeSession(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            parameters = new ConcurrentHashMap<String, Object>(8, 0.9f, 1);
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
        public boolean isStaySignedIn() {
            return false;
        }

        @Override
        public Set<String> getParameterNames() {
            return parameters.keySet();
        }

        @Override
        public Origin getOrigin() {
            return Origin.SYNTHETIC;
        }
    }
}
