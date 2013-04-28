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

package com.openexchange.capabilities.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.logging.Log;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityExceptionCodes;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.StringAllocator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CapabilityServiceImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CapabilityServiceImpl implements CapabilityService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CapabilityServiceImpl.class);

    private static final Object PRESENT = new Object();

    private static final String REGION_NAME_CONTEXT = "CapabilitiesContext";
    private static final String REGION_NAME_USER = "CapabilitiesUser";

    private final ConcurrentMap<String, Capability> capabilities;
    private final ConcurrentMap<String, Object> declaredCapabilities;

    private final ServiceLookup services;
    private volatile Boolean autologin;

    /**
     * Initializes a new {@link CapabilityServiceImpl}.
     */
    public CapabilityServiceImpl(final ServiceLookup services) {
        super();
        this.services = services;
        capabilities = new ConcurrentHashMap<String, Capability>();
        declaredCapabilities = new ConcurrentHashMap<String, Object>();
    }

    private Cache optContextCache() {
        final CacheService service = services.getOptionalService(CacheService.class);
        if (null == service) {
            return null;
        }
        try {
            return service.getCache(REGION_NAME_CONTEXT);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
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
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private boolean autologin() {
        Boolean tmp = autologin;
        if (null == tmp) {
            synchronized (this) {
                tmp = autologin;
                if (null == tmp) {
                    tmp = Boolean.valueOf(services.getService(ConfigurationService.class).getBoolProperty("com.openexchange.sessiond.autologin", false));
                    autologin = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    @Override
    public Set<Capability> getCapabilities(ServerSession session) throws OXException {
        Set<Capability> capabilities = new HashSet<Capability>(64);
        if (!session.isAnonymous()) {
            for (String type : session.getUserConfiguration().getExtendedPermissions()) {
                if (check(type, session)) {
                    capabilities.add(getCapability(type));
                }
            }
        }
        // What about autologin?
        if (autologin()) {
            capabilities.add(new Capability("autologin", true));
        }
        // Now the declared ones
        for (String cap : declaredCapabilities.keySet()) {
            if (check(cap, session)) {
                capabilities.add(getCapability(cap));
            }
        }
        // ------------- Combined capabilities/permissions ------------ //
        if (!session.isAnonymous()) {
            // Portal
            final UserConfiguration userConfiguration = session.getUserConfiguration();
            if (userConfiguration.hasPortal()) {
                capabilities.add(getCapability("portal"));
                capabilities.remove(getCapability("deniedPortal"));
            } else {
                capabilities.remove(getCapability("portal"));
                capabilities.add(getCapability("deniedPortal"));
            }
            // Free-Busy
            if (userConfiguration.hasFreeBusy()) {
                capabilities.add(getCapability("freebusy"));
            } else {
                capabilities.remove(getCapability("freebusy"));
            }
            // Conflict-Handling
            if (userConfiguration.hasConflictHandling()) {
                capabilities.add(getCapability("conflict_handling"));
            } else {
                capabilities.remove(getCapability("conflict_handling"));
            }
            // Participants-Dialog
            if (userConfiguration.hasParticipantsDialog()) {
                capabilities.add(getCapability("participants_dialog"));
            } else {
                capabilities.remove(getCapability("participants_dialog"));
            }
            // Group-ware
            if (userConfiguration.hasGroupware()) {
                capabilities.add(getCapability("groupware"));
            } else {
                capabilities.remove(getCapability("groupware"));
            }
            // PIM
            if (userConfiguration.hasPIM()) {
                capabilities.add(getCapability("pim"));
            } else {
                capabilities.remove(getCapability("pim"));
            }
        }
        // ---------------- Now the ones from database ------------------ //
        {
            final Set<String> set = new HashSet<String>();
            final Set<String> removees = new HashSet<String>();
            // Context-sensitive
            for (final String sCap : getContextCaps(session.getContextId())) {
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
            // User-sensitive
            for (final String sCap : getUserCaps(session.getUserId(), session.getContextId())) {
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
            // Merge them into result set
            for (final String sCap : removees) {
                capabilities.remove(getCapability(sCap));
            }
            for (final String sCap : set) {
                capabilities.add(getCapability(sCap));
            }
        }

        return capabilities;
    }

    private boolean check(String cap, ServerSession session) throws OXException {
        for (CapabilityChecker checker : getCheckers()) {
            if (!checker.isEnabled(cap, session)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all currently known capabilities.
     *
     * @return All capabilities
     * @throws OXException If operation fails
     */
    public Set<Capability> getAllKnownCapabilities() throws OXException {
        return new HashSet<Capability>(capabilities.values());
    }

    /**
     * Gets the singleton capability for given identifier
     *
     * @param id The identifier
     * @return The singleton capability
     */
    public Capability getCapability(String id) {
        Capability capability = capabilities.get(id);
        if (capability != null) {
            return capability;
        }
        final Capability existingCapability = capabilities.putIfAbsent(id, capability = new Capability(id, false));
        return existingCapability == null ? capability : existingCapability;
    }

    @Override
    public void declareCapability(String capability) {
        declaredCapabilities.put(capability, PRESENT);
    }

    /**
     * Gets the available capability checkers.
     *
     * @return The checkers
     */
    protected List<CapabilityChecker> getCheckers() {
        return Collections.emptyList();
    }

    private Set<String> getContextCaps(final int contextId) throws OXException {
        if (contextId <= 0) {
            return Collections.emptySet();
        }
        final Cache cache = optContextCache();
        if (null == cache) {
            return loadContextCaps(contextId);
        }
        final Object object = cache.get(Integer.valueOf(contextId));
        if (object instanceof Set) {
            @SuppressWarnings("unchecked")
            final Set<String> caps = (Set<String>) object;
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

    private Set<String> getUserCaps(final int userId, final int contextId) throws OXException {
        if (contextId <= 0 || userId <= 0) {
            return Collections.emptySet();
        }
        final Cache cache = optUserCache();
        if (null == cache) {
            return loadUserCaps(userId, contextId);
        }
        final Object object = cache.getFromGroup(Integer.valueOf(userId), Integer.toString(contextId));
        if (object instanceof Set) {
            @SuppressWarnings("unchecked")
            final Set<String> caps = (Set<String>) object;
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

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
