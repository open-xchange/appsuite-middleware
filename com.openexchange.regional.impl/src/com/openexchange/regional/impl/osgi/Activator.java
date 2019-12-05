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
