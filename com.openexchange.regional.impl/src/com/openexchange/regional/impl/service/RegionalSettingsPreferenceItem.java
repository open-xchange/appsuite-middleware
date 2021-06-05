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

package com.openexchange.regional.impl.service;

import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.regional.RegionalSettingsService;

/**
 * {@link RegionalSettingsPreferenceItem}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class RegionalSettingsPreferenceItem implements PreferencesItemService, ConfigTreeEquivalent {

    private static final String JSLOB_PATH = "io.ox/core//localeData";
    private static final String TREE_PATH = "localeData";
    private static final String[] PATH = new String[] { TREE_PATH };

    private final IValueHandler valueHandler;

    /**
     * Initializes a new {@link RegionalSettingsPreferenceItem}.
     * 
     * @param service The {@link RegionalSettingsService}
     */
    public RegionalSettingsPreferenceItem(RegionalSettingsService service) {
        super();
        this.valueHandler = new RegionalSettingsValueHandler(service);
    }

    @Override
    public String[] getPath() {
        return PATH;
    }

    @Override
    public IValueHandler getSharedValue() {
        return valueHandler;
    }

    @Override
    public String getConfigTreePath() {
        return TREE_PATH;
    }

    @Override
    public String getJslobPath() {
        return JSLOB_PATH;
    }

}
