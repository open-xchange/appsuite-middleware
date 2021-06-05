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

package com.openexchange.groupware.settings.tree.modules.mailaccount;

import com.openexchange.groupware.settings.tree.AbstractModules;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link Module} - Contains initialization for the modules configuration tree setting mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Module extends AbstractModules {

    /**
     * Default constructor.
     */
    public Module() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mailaccount", "module" };
    }

    @Override
    protected boolean getModule(final UserConfiguration userConfig) {
        return userConfig.isMultipleMailAccounts() && ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class) != null;
    }

}
