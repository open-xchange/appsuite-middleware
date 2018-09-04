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

package com.openexchange.groupware.infostore.autodelete;

import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link InfostoreAutodeleteSettings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class InfostoreAutodeleteSettings {

    /** The property name for the flag whether a user may change/edit the auto-delete settings */
    private static final String PROPERTY_EDITABLE_AUTODELETE_SETTINGS = "com.openexchange.infostore.autodelete.editable";

    /** The property name for the default value of max. number of allowed versions */
    private static final String PROPERTY_DEFAULT_RETENTIONS_DAYS = "com.openexchange.infostore.autodelete.default.retentionDays";

    /** The property name for the default value of versions' retention days */
    private static final String PROPERTY_DEFAULT_MAX_VERSIONS = "com.openexchange.infostore.autodelete.default.maxVersions";


    /** The attribute name for max. number of allowed versions */
    private static final String ATTRIBUTE_MAX_VERSIONS = "com.openexchange.infostore.autodelete.maxVersions";

    /** The attribute name for versions' retention days */
    private static final String ATTRIBUTE_RETENTION_DAYS = "com.openexchange.infostore.autodelete.retentionDays";


    /** The <code>"autodelete_file_versions"</code> capability identifier */
    private static final String CAPABILITY_AUTODELETE_FILE_VERSIONS = "autodelete_file_versions";

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link InfostoreAutodeleteSettings}.
     */
    private InfostoreAutodeleteSettings() {
        super();
    }

    /**
     * Checks if session-associated user holds the capability to perform auto-delete.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: The {@link UserPermissionBits#INFOSTORE "infostore" permission} is also required to finally enable the auto-delete
     * features.
     * </div>
     *
     * @param session The session providing user information
     * @return <code>true</code> if capability is granted; otherwise <code>false</code>
     * @throws OXException If capability cannot be checked
     */
    public static boolean hasAutodeleteCapability(Session session) throws OXException {
        CapabilityService capabilityService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        return (null != capabilityService && capabilityService.getCapabilities(session).contains(CAPABILITY_AUTODELETE_FILE_VERSIONS));
    }

    /**
     * Checks if session-associated user holds the capability to perform auto-delete.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: The {@link UserPermissionBits#INFOSTORE "infostore" permission} is also required to finally enable the auto-delete
     * features.
     * </div>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if capability is granted; otherwise <code>false</code>
     * @throws OXException If capability cannot be checked
     */
    public static boolean hasAutodeleteCapability(int userId, int contextId) throws OXException {
        CapabilityService capabilityService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        return (null != capabilityService && capabilityService.getCapabilities(userId, contextId).contains(CAPABILITY_AUTODELETE_FILE_VERSIONS));
    }

    /**
     * Checks if session-associated user may change/edit the auto-delete settings.
     *
     * @param session The session providing user data
     * @return <code>true</code> if user may change/edit the auto-delete settings; otherwise <code>false</code>
     * @throws OXException If testing the flag fails
     */
    public static boolean mayChangeAutodeleteSettings(Session session) throws OXException {
        return mayChangeAutodeleteSettings(session.getUserId(), session.getContextId());
    }

    /**
     * Checks if given user may change/edit the auto-delete settings.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if user may change/edit the auto-delete settings; otherwise <code>false</code>
     * @throws OXException If testing the flag fails
     */
    public static boolean mayChangeAutodeleteSettings(int userId, int contextId) throws OXException {
        ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == configViewFactory) {
            // Disabled by default
            return false;
        }

        ConfigView configView = configViewFactory.getView(userId, contextId);
        return ConfigViews.getDefinedBoolPropertyFrom(PROPERTY_EDITABLE_AUTODELETE_SETTINGS, true, configView);
    }

    /**
     * Gets the number of retention days to consider for auto-delete.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Does not check for required permissions/capabilities
     * </div>
     *
     * @param session The session to query for
     * @return The number of retention days or <code>0</code> (zero) if disabled
     * @throws OXException If number of retention days cannot be returned
     * @see #hasAutodeleteCapability(Session)
     */
    public static int getNumberOfRetentionDays(Session session) throws OXException {
        return getInt(ATTRIBUTE_RETENTION_DAYS, PROPERTY_DEFAULT_RETENTIONS_DAYS, 0, session.getUserId(), session.getContextId(), session);
    }

    /**
     * Gets the number of retention days to consider for auto-delete.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Does not check for required permissions/capabilities
     * </div>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The number of retention days or <code>0</code> (zero) if disabled
     * @throws OXException If number of retention days cannot be returned
     * @see #hasAutodeleteCapability(Session)
     */
    public static int getNumberOfRetentionDays(int userId, int contextId) throws OXException {
        return getInt(ATTRIBUTE_RETENTION_DAYS, PROPERTY_DEFAULT_RETENTIONS_DAYS, 0, userId, contextId, null);
    }

    /**
     * Sets the number of retention days to consider for auto-delete.
     *
     * @param retentionDays The number of retention days to set
     * @param session The session to set for
     * @throws OXException If number of retention days cannot be set
     */
    public static void setNumberOfRetentionDays(int retentionDays, Session session) throws OXException {
        setInt(ATTRIBUTE_RETENTION_DAYS, retentionDays, session.getUserId(), session.getContextId(), session);
    }

    /**
     * Gets the max. number of file versions to consider for auto-delete.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Does not check for required permissions/capabilities
     * </div>
     *
     * @param session The session to query for
     * @return The max. number of file versions or <code>0</code> (zero) if disabled
     * @throws OXException If max. number of file versions cannot be returned
     * @see #hasAutodeleteCapability(Session)
     */
    public static int getMaxNumberOfFileVersions(Session session) throws OXException {
        return getInt(ATTRIBUTE_MAX_VERSIONS, PROPERTY_DEFAULT_MAX_VERSIONS, 0, session.getUserId(), session.getContextId(), session);
    }

    /**
     * Gets the max. number of file versions to consider for auto-delete.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Does not check for required permissions/capabilities
     * </div>
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The max. number of file versions or <code>0</code> (zero) if disabled
     * @throws OXException If max. number of file versions cannot be returned
     * @see #hasAutodeleteCapability(Session)
     */
    public static int getMaxNumberOfFileVersions(int userId, int contextId) throws OXException {
        return getInt(ATTRIBUTE_MAX_VERSIONS, PROPERTY_DEFAULT_MAX_VERSIONS, 0, userId, contextId, null);
    }

    /**
     * Sets the max. number of file versions to consider for auto-delete.
     *
     * @param maxVersions The max. number of file versions to set
     * @param session The session to set for
     * @throws OXException If max. number of file versions cannot be set
     */
    public static void setMaxNumberOfFileVersions(int maxVersions, Session session) throws OXException {
        setInt(ATTRIBUTE_MAX_VERSIONS, maxVersions, session.getUserId(), session.getContextId(), session);
    }

    private static int getInt(String attrName, String propNameForDefault, int defaultValue, int userId, int contextId, Session optSession) throws OXException {
        User user = getUserBySession(userId, contextId, optSession);
        String attr = user.getAttributes().get(attrName);
        int value;
        if (Strings.isEmpty(attr)) {
            ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
            if (null == configViewFactory) {
                // Disabled by default
                return 0;
            }

            ConfigView configView = configViewFactory.getView(userId, contextId);
            value = ConfigViews.getDefinedIntPropertyFrom(propNameForDefault, defaultValue, configView);
        } else {
            value = Integer.parseUnsignedInt(attr);
        }
        return value;
    }

    private static void setInt(String attrName, int maxVersions, int userId, int contextId, Session optSession) throws OXException {
        UserStorage.getInstance().setAttribute(attrName, Integer.toString(maxVersions), userId, getContextBySession(contextId, optSession));
    }

    // ---------------------------------------------------------------------------------------------------------------------------

    private static User getUserBySession(int userId, int contextId, Session optSession) throws OXException {
        if (optSession instanceof ServerSession) {
            return ((ServerSession) optSession).getUser();
        }

        return UserStorage.getInstance().getUser(userId, contextId);
    }

    private static Context getContextBySession(int contextId, Session optSession) throws OXException {
        if (optSession instanceof ServerSession) {
            return ((ServerSession) optSession).getContext();
        }

        return ContextStorage.getInstance().getContext(contextId);
    }

}
