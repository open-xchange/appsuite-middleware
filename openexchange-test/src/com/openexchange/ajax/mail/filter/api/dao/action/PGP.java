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

package com.openexchange.ajax.mail.filter.api.dao.action;

import java.util.List;
import com.openexchange.ajax.mail.filter.api.dao.ActionCommand;
import com.openexchange.ajax.mail.filter.api.dao.action.argument.PGPActionArgument;

/**
 * {@link PGP}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class PGP extends AbstractAction implements Action<PGPActionArgument> {

    /**
     * Initialises a new {@link PGP}.
     */
    public PGP() {
        super(ActionCommand.pgp);
    }

    /**
     * Initialises a new {@link PGP}.
     * 
     * @param keys A list with PGP keys
     */
    public PGP(List<String> keys) {
        this();
        addArgument(PGPActionArgument.keys, keys);
    }

    @Override
    public ActionCommand getActionCommand() {
        return ActionCommand.pgp;
    }

    @Override
    public void setArgument(PGPActionArgument argument, Object value) {
        addArgument(argument, value);
    }

    @Override
    public Object getArgument(PGPActionArgument argument) {
        return getArguments().get(argument);
    }
}
