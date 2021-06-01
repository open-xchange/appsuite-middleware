/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
