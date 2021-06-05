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

package com.openexchange.imap.osgi.console;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import com.openexchange.imap.cache.ListLsubCache;

/**
 * {@link ClearListLsubCommandProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ClearListLsubCommandProvider implements CommandProvider {

    /**
     * Initializes a new {@link ClearListLsubCommandProvider}.
     */
    public ClearListLsubCommandProvider() {
        super();
    }

    @Override
    public String getHelp() {
        return "clearimaplistlsub <user-id> <context-id> - Clears the content of the IMAP LIST/LSUB cache for the primary account\n";
    }

    public void _clearimaplistlsub(CommandInterpreter ci) {
        String sUserId = ci.nextArgument();
        String sContextId = ci.nextArgument();

        ListLsubCache.dropFor(Integer.parseInt(sUserId.trim()), Integer.parseInt(sContextId.trim()), false, true);
        ci.println("Cleared LIST/LSUB cache for user " + sUserId + " in context " + sContextId);
    }

}
