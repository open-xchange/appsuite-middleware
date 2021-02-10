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

package com.openexchange.quota.json.actions;

import java.util.Iterator;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.unified.UnifiedQuotaService;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.java.Strings;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link AbstractUnifiedQuotaAction} - The abstract quota action for unified quota.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public abstract class AbstractUnifiedQuotaAction extends AbstractQuotaAction {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractUnifiedQuotaAction.class);

    private final ServiceListing<UnifiedQuotaService> unifiedQuotaServices;

    /**
     * Initializes a new {@link AbstractUnifiedQuotaAction}.
     *
     * @param unifiedQuotaServices The tracked unified quota services
     * @param services The service look-up
     */
    protected AbstractUnifiedQuotaAction(ServiceListing<UnifiedQuotaService> unifiedQuotaServices, ServiceLookup services) {
        super(services);
        this.unifiedQuotaServices = unifiedQuotaServices;
    }

    /**
     * Gets the highest-ranked unified quota service for given user
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The quota service with highest ranking or <code>null</code>
     * @throws OXException If quota service cannot be returned
     */
    protected UnifiedQuotaService getHighestRankedBackendService(int userId, int contextId) throws OXException  {
        if (userId <= 0) {
            return null;
        }

        Iterator<UnifiedQuotaService> iter = unifiedQuotaServices.iterator();
        if (false == iter.hasNext()) {
            // No one available...
            LOGGER.debug("No Unified Quota service available for user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
            return null;
        }

        if (false == checkIfUnifiedQuotaIsEnabledFor(userId, contextId)) {
            LOGGER.debug("Unified Quota is not enabled for user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
            return null;
        }

        do {
            UnifiedQuotaService unifiedQuotaService = iter.next();
            if (unifiedQuotaService.isApplicableFor(userId, contextId)) {
                LOGGER.debug("Using Unified Quota service '{}' for user {} in context {}.", unifiedQuotaService.getMode(), Integer.valueOf(userId), Integer.valueOf(contextId));
                return unifiedQuotaService;
            }
            LOGGER.debug("Unified Quota service '{}' is not applicable for user {} in context {}.", unifiedQuotaService.getMode(), Integer.valueOf(userId), Integer.valueOf(contextId));
        } while (iter.hasNext());

        LOGGER.debug("No Unified Quota service applicable for user {} in context {}.", Integer.valueOf(userId), Integer.valueOf(contextId));
        return null;
    }

    private boolean checkIfUnifiedQuotaIsEnabledFor(int userId, int contextId) throws OXException {
        try {
            return isUnifiedQuotaEnabledFor(userId, contextId);
        } catch (OXException e) {
            if (UserConfigurationCodes.NOT_FOUND.equals(e)) {
                // Such a user does not (yet) exist. Thus Unified Quota cannot be enabled.
                return false;
            }
            throw e;
        }
    }

    private boolean isUnifiedQuotaEnabledFor(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<String> property = view.property("com.openexchange.unifiedquota.enabled", String.class);
        if (null == property || !property.isDefined()) {
            // Not enabled by default
            return false;
        }

        String value = property.get();
        boolean defaultValue = false;
        return Strings.isEmpty(value) ? defaultValue : ("true".equalsIgnoreCase(value.trim()) ? true : ("false".equalsIgnoreCase(value.trim()) ? false : defaultValue));
    }

}
