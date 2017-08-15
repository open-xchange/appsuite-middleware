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

package com.openexchange.passwordchange.history.impl.events;

import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.passwordchange.history.PasswordChangeHandlerRegistryService;
import com.openexchange.passwordchange.history.PasswordChangeHistoryProperties;
import com.openexchange.passwordchange.history.PasswordChangeInfo;
import com.openexchange.passwordchange.history.PasswordHistoryHandler;
import com.openexchange.passwordchange.history.impl.PasswordChangeInfoImpl;
import com.openexchange.passwordchange.history.impl.exception.PasswordChangeHistoryException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;

/**
 * {@link PasswordChangeHelper} - Utility class to save or clear password changes history
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeHelper.class);

    private final ServiceLookup                 service;
    private final PasswordChangeHandlerRegistryService registry;

    /**
     * 
     * Initializes a new {@link PasswordChangeHelper}.
     * 
     * @param service The {@link ServiceLookup} to get services from
     * @param registry The {@link PasswordChangeHandlerRegistryService} to get the {@link PasswordHistoryHandler} from
     */
    public PasswordChangeHelper(ServiceLookup service, PasswordChangeHandlerRegistryService registry) {
        super();
        this.service = service;
        this.registry = registry;

    }

    /**
     * Calls the according tracker for a user if the feature is available and saves the data
     * 
     * @param service To lookup needed services
     * @param registry The registry to get the handler from
     * @param contextID The context of the user
     * @param userID The ID representing the user. For this user the password change will be recorded
     * @param ipAddress The IP address if available
     * @param client The calling resource. E.g. {@link PasswordChangeInfo#PROVISIONING}
     */
    public void recordChange(int contextID, int userID, final String ipAddress, final String client) {
        try {
            PasswordHistoryHandler handler = loadHandler(contextID, userID);
            PasswordChangeInfo info = new PasswordChangeInfoImpl(System.currentTimeMillis(), client, ipAddress);
            handler.trackPasswordChange(userID, contextID, info);
        } catch (Exception e) {
            LOG.debug("Error while tracking password change for user {} in context {}. Reason: ", userID, contextID, e.getMessage(), e);
        }
    }

    /**
     * Clears the password change history for a specific user
     * 
     * @param service To lookup needed services
     * @param registry The registry to get the handler from
     * @param contextID The context of the user
     * @param userID The ID of the user
     * @param limit see {@link PasswordHistoryHandler#clear(int, int, int)}
     */
    public void clearFor(int contextID, int userID, int limit) {
        try {
            PasswordHistoryHandler tracker = loadHandler(contextID, userID);
            tracker.clear(userID, contextID, limit);
        } catch (Exception e) {
            LOG.debug("Error while deleting password change history for user {} in context {}. Reason: {}", userID, contextID, e.getMessage(), e);
        }
    }

    /**
     * Get the handler for the specified user in a specific context
     * 
     * @param contextID The ID of the context
     * @param userID The ID of the user
     * @return A {@link PasswordHistoryHandler} for the user
     * @throws OXException In case a service is missing or some properties aren't configured (well)
     */
    private PasswordHistoryHandler loadHandler(int contextID, int userID) throws OXException {
        // Load config and get according tracker
        ConfigViewFactory casscade = service.getService(ConfigViewFactory.class);
        if (null == casscade) {
            LOG.warn("Could not get config.");
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }
        ConfigView view = casscade.getView(userID, contextID);
        Boolean enabled = PasswordChangeHistoryProperties.ENABLE.getDefaultValue(Boolean.class);
        String handlerName = null;

        try {
            enabled = view.get(PasswordChangeHistoryProperties.ENABLE.getFQPropertyName(), Boolean.class);
            handlerName = view.get(PasswordChangeHistoryProperties.HANDLER.getFQPropertyName(), String.class);
        } catch (Exception e) {
            // Could not load, no configuration available
            throw PasswordChangeHistoryException.DISABLED.create(userID, contextID);
        }

        if (null == enabled || Boolean.FALSE == enabled) {
            throw PasswordChangeHistoryException.DISABLED.create(userID, contextID);
        }

        if (Strings.isEmpty(handlerName)) {
            handlerName = PasswordChangeHistoryProperties.HANDLER.getDefaultValue(String.class);
            LOG.debug("No PasswordChangeTracker found. Falling back to default value");
        }

        if (null == registry) {
            LOG.debug("Could not get PasswordChangeTrackerRegistry");
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(PasswordChangeHandlerRegistryService.class.getName());
        }

        PasswordHistoryHandler handler = registry.getHandler(handlerName);
        if (null == handler) {
            // If no tracker available, there should be no tracking
            LOG.debug("Could not load {} for user {} in context {}", handlerName, userID, contextID);
            throw PasswordChangeHistoryException.MISSING_HANDLER.create(handlerName);
        }
        return handler;
    }
}
