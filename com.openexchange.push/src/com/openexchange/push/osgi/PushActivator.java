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

package com.openexchange.push.osgi;

import org.osgi.service.event.EventAdmin;
import com.openexchange.event.EventFactoryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushUtility;

/**
 * {@link PushActivator} - The activator for push bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PushActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link PushActivator}.
     */
    public PushActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { EventAdmin.class, EventFactoryService.class };
    }

    @Override
    public void startBundle() throws Exception {
        Services.setServiceLookup(this);

        PushClientCheckerTracker checkerListing = new PushClientCheckerTracker(context);
        rememberTracker(checkerListing);
        openTrackers();

        PushUtility.setPushClientCheckerListing(checkerListing);
    }

    @Override
    public void stopBundle() throws Exception {
        PushUtility.setPushClientCheckerListing(null);
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
