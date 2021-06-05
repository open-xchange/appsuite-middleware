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

package com.openexchange.regional.impl.osgi;

import com.openexchange.caching.CacheService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.lock.LockService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.regional.impl.db.CreateRegionalSettingsTableService;
import com.openexchange.regional.impl.db.CreateRegionalSettingsTableTask;
import com.openexchange.regional.impl.service.RegionalSettingsDeleteListener;
import com.openexchange.regional.impl.service.RegionalSettingsPreferenceItem;
import com.openexchange.regional.impl.service.RegionalSettingsServiceImpl;
import com.openexchange.regional.impl.storage.CachingRegionalSettingStorage;
import com.openexchange.regional.impl.storage.RegionalSettingStorage;
import com.openexchange.regional.impl.storage.SQLRegionalSettingStorage;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class Activator extends HousekeepingActivator {

    /**
     * Initializes a new {@link Activator}.
     */
    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class, CacheService.class };
    }

    @Override
    protected Class<?>[] getOptionalServices() {
        return new Class[] { LockService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        // Register table tasks
        registerService(CreateTableService.class, new CreateRegionalSettingsTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new CreateRegionalSettingsTableTask()));

        // Register service
        RegionalSettingStorage storage = new CachingRegionalSettingStorage(this, new SQLRegionalSettingStorage(this));
        RegionalSettingsServiceImpl settingsService = new RegionalSettingsServiceImpl(storage);
        registerService(RegionalSettingsService.class, settingsService);

        // Register delete listener
        registerService(DeleteListener.class, new RegionalSettingsDeleteListener(storage));

        // Register PreferenceItem
        RegionalSettingsPreferenceItem item = new RegionalSettingsPreferenceItem(settingsService);
        registerService(PreferencesItemService.class, item);
        registerService(ConfigTreeEquivalent.class, item);
    }

}
