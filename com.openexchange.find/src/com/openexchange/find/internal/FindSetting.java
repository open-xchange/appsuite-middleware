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

package com.openexchange.find.internal;

import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.java.Strings;
import com.openexchange.jslob.ConfigTreeEquivalent;


/**
 * Abstract class for find settings that automagically provides a {@link ConfigTreeEquivalent}
 * for a {@link PreferencesItemService} implementation.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class FindSetting implements PreferencesItemService, ConfigTreeEquivalent {

    private final String configPath = Strings.join(getPath(), "/");

    @Override
    public String getConfigTreePath() {
        return configPath;
    }

    @Override
    public String getJslobPath() {
        return "io.ox/core//" + getConfigTreePath();
    }

    @Override
    public String toString() {
        return getConfigTreePath() + " > " + getJslobPath();
    }

}
