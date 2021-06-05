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

package com.openexchange.group.internal.osgi;

import com.openexchange.config.admin.HideAdminService;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.group.GroupService;
import com.openexchange.group.GroupStorage;
import com.openexchange.group.internal.CachingGroupStorage;
import com.openexchange.group.internal.FilteringGroupService;
import com.openexchange.group.internal.GroupServiceImpl;
import com.openexchange.group.internal.RdbGroupStorage;
import com.openexchange.group.internal.VirtualGroupStorage;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.principalusecount.PrincipalUseCountService;

/**
 * {@link GroupActivator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
public class GroupActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link GroupActivator}.
     */
    public GroupActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { HideAdminService.class, PrincipalUseCountService.class, LeanConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        VirtualGroupStorage storage = new VirtualGroupStorage(new CachingGroupStorage(new RdbGroupStorage()));
        final GroupService groupService = new FilteringGroupService(new GroupServiceImpl(storage, this), this);
        registerService(GroupService.class, groupService);
        registerService(GroupStorage.class, storage);
    }

}
