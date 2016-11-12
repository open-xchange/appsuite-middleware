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
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.FailureAwareCapabilityChecker;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.capabilities.ConfigurationProperty;
import com.openexchange.capabilities.DependentCapabilityChecker;
import com.openexchange.capabilities.osgi.PermissionAvailabilityServiceRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.service.PermissionAvailabilityService;
import com.openexchange.java.BoolReference;
import com.openexchange.java.ConcurrentEnumMap;
import com.openexchange.java.Strings;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
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
    private CapabilitySet getCapabilities(int userId, int contextId, Session session, boolean alignPermissions, boolean allowCache) throws OXException {
        final CapabilitySet cachedCapabilitySet = allowCache ? optCachedCapabilitySet(userId, contextId) : null;
        if (null != cachedCapabilitySet) {
            if (alignPermissions) {
                alignPermissions(cachedCapabilitySet);
            }
            return cachedCapabilitySet;
        }

        User user = null;
        Context context = null;
        if (contextId > 0) {
            context = requireService(ContextService.class, services).getContext(contextId);
            if (userId > 0) {
                user = requireService(UserService.class, services).getUser(userId, context);
            }
        }

        /*
         * Never ever re-order the apply methods!
         */
        CapabilitySet capabilities = new CapabilitySet(64);
        applyAutoLogin(capabilities);
        applyUserPermissions(capabilities, user, context);
        applyConfiguredCapabilities(capabilities, user, context, allowCache);
        BoolReference putIntoCache = new BoolReference(true);
        if (session == null) {
            applyDeclaredCapabilities(capabilities, ServerSessionAdapter.valueOf(userId, contextId), putIntoCache); // Code smell level: Ninja...
        } else {
            applyDeclaredCapabilities(capabilities, session, putIntoCache);
        }
        applyGuestFilter(capabilities, user);

        if (putIntoCache.getValue() && userId > 0 && contextId > 0 && (session == null || !session.isTransient())) {
            // Put in cache
            final Cache cache = optCache();
            if (null != cache) {
                cache.putInGroup(Integer.valueOf(userId), Integer.toString(contextId), capabilities.clone(), false);
            }
        }

        if (alignPermissions) {
            alignPermissions(capabilities);
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
     * Checks if autologin is enabled and adds the according capability to the passed set, if so.
     *
     * @param capabilities The capability set
     */
    private void applyAutoLogin(CapabilitySet capabilities) {
        if (autologin()) {
            capabilities.add(CAP_AUTO_LOGIN);
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
    private void applyUserPermissions(CapabilitySet capabilities, User user, Context context) throws OXException {
        if (user == null || context == null) {
            return;
        }

        final UserPermissionBits userPermissionBits = services.getService(UserPermissionService.class).getUserPermissionBits(user.getId(), context.getContextId());
        userPermissionBits.setGroups(user.getGroups());

        // Capabilities by user permission bits
        for (final Permission p : Permission.byBits(userPermissionBits.getPermissionBits())) {
            capabilities.add(getCapability(p));
        }

        // Webmail
        if (user.getId() == context.getMailadmin()) {
            final boolean adminMailLoginEnabled = services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.mail.adminMailLoginEnabled", false);
            if (!adminMailLoginEnabled) {
                capabilities.remove(getCapability(Permission.WEBMAIL));
            }
        }

        // Portal - stick to positive "portal" capability only
        capabilities.remove("denied_portal");
        if (userPermissionBits.hasPortal()) {
            capabilities.add(getCapability("portal"));
        } else {
            capabilities.remove("portal");
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
        if (userPermissionBits.hasWebMail()) {
            UserSettingMail mailSettings = UserSettingMailStorage.getInstance().getUserSettingMail(user.getId(), context);
            if (null != mailSettings && mailSettings.isSpamEnabled()) {
                capabilities.add(getCapability("spam"));
            } else {
                capabilities.remove("spam");
            }
        }
        // Global Address Book
        if (userPermissionBits.isGlobalAddressBookEnabled()) {
            capabilities.add(getCapability("gab"));
        } else {
            capabilities.remove("gab");
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
     * @param user The user; if <code>null</code>, all config cascade lookups are performed with user ID <code>-1</code>;
     *             if the user is a guest, the configure guest capability mode is considered.
     * @param context The context; if <code>null</code>, all config cascade lookups are performed with context ID <code>-1</code>
     * @param allowCache Whether caching of loaded capabilities is allowed
     * @throws OXException
     */
    private void applyConfiguredCapabilities(CapabilitySet capabilities, User user, Context context, boolean allowCache) throws OXException {
        int userId = -1;
        int contextId = -1;
        if (context != null) {
            contextId = context.getContextId();
        }

        if (user != null) {
            userId = user.getId();
            if (user.isGuest()) {
                GuestCapabilityMode capMode = getGuestCapabilityMode(user, context);
                if (capMode == GuestCapabilityMode.INHERIT) {
                    applyConfigCascade(capabilities, user.getCreatedBy(), contextId);
                    applyContextCapabilities(capabilities, contextId, allowCache);
                    applyUserCapabilities(capabilities, user.getCreatedBy(), contextId, allowCache);
                    applyUserCapabilities(capabilities, userId, contextId, allowCache);
                } else if (capMode == GuestCapabilityMode.STATIC) {
                    applyStaticGuestCapabilities(capabilities, user, context);
                    applyUserCapabilities(capabilities, userId, contextId, allowCache);
                } else if (capMode == GuestCapabilityMode.DENY_ALL) {
                    applyUserCapabilities(capabilities, userId, contextId, allowCache);
                }
            } else {
                applyConfigCascade(capabilities, userId, contextId);
                applyContextCapabilities(capabilities, contextId, allowCache);
                applyUserCapabilities(capabilities, userId, contextId, allowCache);
            }
        } else {
            applyConfigCascade(capabilities, userId, contextId);
            applyContextCapabilities(capabilities, contextId, allowCache);
        }
    }

    /**
     * Adds all capabilities that have been specified via config cascade properties to the passed set.
     *
     * <ul>
     *  <li>capabilities specified via the <code>permissions</code> property - looked up via config cascade</li>
     *  <li>capabilities specified as properties with a <code>com.openexchange.capability.</code> prefix - looked up via config cascade</li>
     *  <li>capabilities that depend on whether a feature is enabled via a configuration property (e.g. <code>com.openexchange.caldav.enabled => caldav</code>)</li>
     * </ul>
     *
     * @param capabilities The capability set
     * @param userId The user ID for config cascade lookups
     * @param contextId The context ID for config cascade lookups
     * @throws OXException
     */
    private void applyConfigCascade(CapabilitySet capabilities, int userId, int contextId) throws OXException {
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
                if (propName.startsWith("com.openexchange.capability.", 0)) {
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

    /**
     * Adds all capabilities that are specified as <code>context_capabilities</code> DB entries to the passed set.
     *
     * @param capabilities The capability set
     * @param contextId The context ID; if negative calling this method is a no-op
     * @param allowCache Whether caching of loaded capabilities is allowed
     * @throws OXException
     */
    private void applyContextCapabilities(CapabilitySet capabilities, int contextId, boolean allowCache) throws OXException {
        if (contextId > 0) {
            applySet(capabilities, getContextCaps(contextId, allowCache));
        }
    }

    /**
     * Adds all capabilities that are specified as <code>user_capabilities</code> DB entries to the passed set.
     *
     * @param capabilities The capability set
     * @param userId The user ID; if negative calling this method is a no-op
     * @param contextId The context ID; if negative calling this method is a no-op
     * @param allowCache Whether caching of loaded capabilities is allowed
     * @throws OXException
     */
    private void applyUserCapabilities(CapabilitySet capabilities, int userId, int contextId, boolean allowCache) throws OXException {
        if (contextId > 0 && userId > 0) {
            applySet(capabilities, getUserCaps(userId, contextId, allowCache));
        }
    }

    private void applySet(CapabilitySet capabilities, Set<String> capabilitiesToApply) {
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
            capabilities.remove(sCap);
        }
        for (String sCap : set) {
            capabilities.add(getCapability(sCap));
        }
    }

    /**
     * Adds all capabilities to the passed set, which are specified via {@link CapabilityService#declareCapability(String)}.
     * Every declared capability is checked against the registered {@link CapabilityChecker}s.
     *
     * @param capabilities The capability set
     * @param session The session; either a real one or a synthetic one, but never <code>null</code>
     * @param putIntoCache Tracks whether a put into cache is recommended
     * @throws OXException
     */
    private void applyDeclaredCapabilities(CapabilitySet capabilities, Session session, BoolReference putIntoCache) throws OXException {
        for (String cap : declaredCapabilities.keySet()) {
            if (check(cap, session, capabilities, putIntoCache)) {
                capabilities.add(getCapability(cap));
            } else {
                capabilities.remove(cap);
            }
        }
    }

    /**
     * If the passed user is a guest user, the <code>guest</code> capability is set. Additionally
     * the capabilities <code>share_links</code> and <code>invite_guests</code> are removed.
     *
     * @param capabilities The capability set
     * @param user The user; if <code>null</code>, calling this method is a no-op
     * @throws OXException
     */
    private void applyGuestFilter(CapabilitySet capabilities, User user) throws OXException {
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
            if (!Strings.isEmpty(user.getMail())) {
                capabilities.add(getCapability("edit_password"));
            }
            capabilities.remove("guard");
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
    private void applyStaticGuestCapabilities(CapabilitySet capabilities, User user, Context context) throws OXException {
        ConfigViewFactory configViews = services.getService(ConfigViewFactory.class);
        if (configViews != null) {
            ConfigView view = configViews.getView(user.getId(), context.getContextId());
            String value = view.opt("com.openexchange.share.staticGuestCapabilities", String.class, "");
            if (Strings.isNotEmpty(value)) {
                List<String> staticCapabilities = Strings.splitAndTrim(value, ",");
                for (String cap : staticCapabilities) {
                    capabilities.add(getCapability(cap));
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

    private boolean check(String cap, Session session, CapabilitySet allCapabilities, BoolReference putIntoCache) throws OXException {
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
        } catch (final Exception e) {
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
                } catch (final Exception e) {
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
                } catch (final Exception e) {
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
        Map<String, Map<String, Set<String>>> sets = new LinkedHashMap<String, Map<String, Set<String>>>(6);

        {
            Set<String> capabilities = new TreeSet<String>();
            UserPermissionBits userPermissionBits = services.getService(UserPermissionService.class).getUserPermissionBits(userId, contextId);
            // Capabilities by user permission bits
            for (final Permission p : Permission.byBits(userPermissionBits.getPermissionBits())) {
                capabilities.add(p.getCapabilityName());
            }

            Map<String, Set<String>> arr = new LinkedHashMap<String, Set<String>>(3);
            arr.put("granted", capabilities);
            arr.put("denied", new HashSet<String>(0));
            sets.put("permissions", arr);
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
                Map<String, Set<String>> arr = new LinkedHashMap<String, Set<String>>(3);
                arr.put("granted", set);
                arr.put("denied", removees);
                sets.put("provisioning", arr);
            }
        }

        {
            // Now the declared ones
            CapabilitySet grantedCapabilities = new CapabilitySet(16);
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
        public Set<String> getParameterNames() {
            return parameters.keySet();
        }
    }
}
