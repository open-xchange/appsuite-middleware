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

package com.openexchange.logback.clt;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

/**
 * {@link AbstractLogbackConfigurationAdministrativeCLI}
 * 
 * @param <R> - The return type
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.1
 */
abstract class AbstractLogbackConfigurationAdministrativeCLI<R> extends AbstractRmiCLI<R> {

    private final String syntax;
    private final String footer;

    /**
     * Initialises a new {@link AbstractLogbackConfigurationAdministrativeCLI}.
     */
    public AbstractLogbackConfigurationAdministrativeCLI(String syntax, String footer) {
        super();
        this.syntax = syntax;
        this.footer = footer;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        try {
            authenticator.doAuthentication(login, password);
        } catch (RemoteException e) {
            System.err.print(e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    protected String getFooter() {
        return footer;
    }

    @Override
    protected String getName() {
        return syntax;
    }

    @Override
    protected void printHelp() {
        printHelp(options);
    }

    @Override
    protected void printHelp(Options options) {
        printHelp(options, 120, false);
    }
}
