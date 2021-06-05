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

package com.openexchange.advertisement.impl.services;

import com.openexchange.advertisement.impl.osgi.Services;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.reseller.ResellerService;
import com.openexchange.reseller.data.ResellerAdmin;
import com.openexchange.session.Session;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link AccessCombinationAdvertisementConfigService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class AccessCombinationAdvertisementConfigService extends AbstractAdvertisementConfigService {

    /**
     * Gets the instance of {@code AccessCombinationAdvertisementConfigService}; initializes it if necessary.
     *
     * @return The instance
     */
    public static AccessCombinationAdvertisementConfigService getInstance() {
        return new AccessCombinationAdvertisementConfigService();
    }

    // -------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link AccessCombinationAdvertisementConfigService}.
     */
    private AccessCombinationAdvertisementConfigService() {
        super();
    }

    @Override
    protected String getReseller(int contextId) throws OXException {
        ResellerService resellerService = Services.getService(ResellerService.class);
        ResellerAdmin resellerAdmin = resellerService.getReseller(contextId);
        return resellerAdmin.getName();
    }

    @Override
    protected String getPackage(Session session) throws OXException {
        UserPermissionService permissionService = Services.getService(UserPermissionService.class);
        ContextService contextService = Services.getService(ContextService.class);
        Context ctx = contextService.getContext(session.getContextId());
        return permissionService.getAccessCombinationName(ctx, session.getUserId());
    }

    @Override
    public String getSchemeId() {
        return "AccessCombinations";
    }

}
