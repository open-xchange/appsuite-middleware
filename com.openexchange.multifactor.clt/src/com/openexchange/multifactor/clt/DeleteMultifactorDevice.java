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

package com.openexchange.multifactor.clt;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.glassfish.jersey.client.ClientConfig;

/**
 * {@link DeleteMultifactorDevice} - A CLI for deleting multifactor authentication devices
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class DeleteMultifactorDevice extends AbstractMultifactorClt {

    private static final String PARAM_PROVIDER_NAME = "provider";
    private static final String PARAM_PROVIDER_NAME_SHORT = "r";
    private static final String PARAM_PROVIDER_NAME_DESC = "The multifactor provider name.";
    private static final String PARAM_DEVICE_ID = "device";
    private static final String PARAM_DEVICE_ID_SHORT = "d";
    private static final String PARAM_DEVICE_ID_DESC = "The multifactor device id.";

    private static final String DELETE_MULTIFACTOR_DEVICE_USAGE = "-c <contextId> -i <userId> -r <providerName> -d <deviceId> -A <masterAdmin | contextAdmin> -P <masterAdminPassword | contextAdminPassword>";

    public static void main(String[] args) {
        new DeleteMultifactorDevice().execute(args);
    }

    @Override
    protected void checkArguments(CommandLine cmd) {
        // Nothing
    }

    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addRequiredOption(PARAM_CONTEXTID_SHORT, PARAM_CONTEXTID_LONG, true, PARAM_CONTEXTID_DESC);
        options.addRequiredOption(PARAM_USERID_SHORT, PARAM_USERID_LONG, true, PARAM_USERID_DESC);
        options.addRequiredOption(PARAM_PROVIDER_NAME_SHORT, PARAM_PROVIDER_NAME, true, PARAM_PROVIDER_NAME_DESC);
        options.addRequiredOption(PARAM_DEVICE_ID_SHORT, PARAM_DEVICE_ID, true, PARAM_DEVICE_ID_DESC);
    }

    @Override
    protected WebTarget getEndpoint(CommandLine cmd) {
        String endpoint = new StringBuilder()
            .append(getEndpointRoot(cmd))
            .append("/contexts/")
            .append(cmd.getOptionValue(PARAM_CONTEXTID_LONG))
            .append("/users/")
            .append(cmd.getOptionValue(PARAM_USERID_LONG))
            .append("/multifactor/devices/")
            .append(cmd.getOptionValue(PARAM_PROVIDER_NAME))
            .append("/")
            .append(cmd.getOptionValue(PARAM_DEVICE_ID))
            .toString();

        try {
            URI uri = new URI(endpoint);

            ClientConfig config = new ClientConfig();
            Client client = ClientBuilder.newClient(config);
            WebTarget baseTarget = client.target(uri);
            return baseTarget;
        } catch (URISyntaxException e) {
            System.err.print("Unable to return endpoint: " + e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, Builder executionContext) throws Exception {
        executionContext.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE);
        Response delete = executionContext.delete();
        if (delete.getStatus() == Response.Status.OK.getStatusCode()) {
            System.out.println("Multifactor authentication device successfully deleted");
        } else {
            printError("Failed to delete the authentication device", delete.readEntity(String.class), delete.getStatusInfo());
            System.exit(-1);
        }
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (!cmd.hasOption(PARAM_CONTEXTID_LONG)) {
            System.out.println("You must provide a context identifier.");
            printHelp();
            System.exit(-1);
        }

        if (!cmd.hasOption(PARAM_USERID_LONG)) {
            System.out.println("You must provide a user identifier.");
            printHelp();
            System.exit(-1);
        }

        if (!cmd.hasOption(PARAM_PROVIDER_NAME)) {
            System.out.println("You must provide a provider name.");
            printHelp();
            System.exit(-1);
        }

        if (!cmd.hasOption(PARAM_DEVICE_ID)) {
            System.out.println("You must provide a device ID.");
            printHelp();
            System.exit(1);
        }
    }

    @Override
    protected String getFooter() {
        return "The command-line tool to delete multifactor authentication devices.";
    }

    @Override
    protected String getName() {
        return "deletemultifactordevice " + DELETE_MULTIFACTOR_DEVICE_USAGE;
    }

}
