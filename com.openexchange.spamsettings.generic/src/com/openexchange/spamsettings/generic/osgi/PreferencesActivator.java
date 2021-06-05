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


package com.openexchange.spamsettings.generic.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.spamsettings.generic.preferences.SpamSettingsModulePreferences;


/**
 * {@link PreferencesActivator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class PreferencesActivator implements BundleActivator {

    private ServiceRegistration<PreferencesItemService> userConfigFlagRegistration;

    @Override
    public void start(final BundleContext context) throws Exception {
        userConfigFlagRegistration = context.registerService(PreferencesItemService.class, new SpamSettingsModulePreferences(), null);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        userConfigFlagRegistration.unregister();
        userConfigFlagRegistration = null;
    }

}
