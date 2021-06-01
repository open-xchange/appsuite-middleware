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

package com.openexchange.passwordchange.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Set;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.guest.GuestService;
import com.openexchange.guest.osgi.GuestServiceServiceTracker;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordchange.BasicPasswordChangeService;
import com.openexchange.passwordchange.DefaultBasicPasswordChangeService;
import com.openexchange.passwordchange.EditPasswordCapabilityChecker;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PasswordChangeActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link PasswordChangeActivator}.
     */
    public PasswordChangeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        Set<Class<?>> classes = new LinkedHashSet<>(8);
        classes.add(UserService.class);
        classes.add(CapabilityService.class);

        Class<?>[] neededServices = EditPasswordCapabilityChecker.getNeededServices();
        if (null != neededServices && neededServices.length > 0) {
            for (Class<?> clazz : neededServices) {
                if (null != clazz) {
                    classes.add(clazz);
                }
            }
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(BasicPasswordChangeService.class, new DefaultBasicPasswordChangeService());

        // Register CapabilityChecker
        {
            final Dictionary<String, Object> properties = new Hashtable<String, Object>(1);
            properties.put(CapabilityChecker.PROPERTY_CAPABILITIES, EditPasswordCapabilityChecker.EDIT_PASSWORD_CAP);
            registerService(CapabilityChecker.class, new EditPasswordCapabilityChecker(this), properties);

            getService(CapabilityService.class).declareCapability(EditPasswordCapabilityChecker.EDIT_PASSWORD_CAP);
        }

        track(GuestService.class, new GuestServiceServiceTracker(this.context));
        openTrackers();
    }
}
