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

package com.openexchange.configread.clt;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.config.rmi.RemoteConfigurationService;
import com.openexchange.java.Strings;


/**
 * {@link ListReloadablesCLT}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class ListReloadablesCLT extends AbstractRmiCLI<Void> {

    /**
     * Initializes a new {@link ListReloadablesCLT}.
     */
    public ListReloadablesCLT() {
        super();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ListReloadablesCLT().execute(args);
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.FALSE;
    }

    @Override
    protected String getFooter() {
        return "";
    }

    @Override
    protected String getName() {
        return "listreloadableoptions " + BASIC_USAGE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // nothing to do
    }

    @Override
    protected void addOptions(Options options) {
        // nothing to do
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        // nothing to do
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteConfigurationService remoteConfigService = getRmiStub(optRmiHostName, RemoteConfigurationService.RMI_NAME);
        Map<String, List<String>> reloadables = remoteConfigService.listReloadables();
        StringBuilder sb = new StringBuilder();
        for (Entry<String, List<String>> entry : reloadables.entrySet()) {
            String fileName = entry.getKey();
            if (null != fileName && Strings.isNotEmpty(fileName)) {
                sb.append(fileName).append(":").append("\n");
                for (String property : entry.getValue()) {
                    sb.append(property).append("\n");
                }
                sb.append("\n");
            }
        }
        System.out.println(sb.toString());
        return null;
    }

}
