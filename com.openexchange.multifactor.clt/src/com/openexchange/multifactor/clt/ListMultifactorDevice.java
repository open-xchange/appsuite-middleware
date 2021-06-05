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
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.cli.OutputHelper;

/**
 * {@link ListMultifactorDevice} - A CLI for listing multifactor authentication devices of a given user
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class ListMultifactorDevice extends AbstractMultifactorClt {

    private static final String LIST_MULTIFACTOR_DEVICE_USAGE = "-c <contextId> -i <userId> -A <masterAdmin | contextAdmin> -P <masterAdminPassword | contextAdminPassword>";

    public static void main(String[] args) {
        new ListMultifactorDevice().execute(args);
    }

    private void printDevices(JSONArray devices) throws JSONException {
        int length = devices.length();
        if (length > 0) {
            List<List<String>> dataToPrint = new ArrayList<List<String>>(length);
            for (int i = 0; i < length; i++) {
                JSONObject device = devices.getJSONObject(i);
                List<String> deviceData = new ArrayList<String>(6);
                deviceData.add(device.getString("id"));
                deviceData.add(device.getString("providerName"));
                deviceData.add(device.getString("name"));
                deviceData.add(device.getString("enabled"));
                deviceData.add(device.getString("backup"));
                dataToPrint.add(deviceData);
            }
            OutputHelper.doOutput(new String[] { "l", "l", "l", "l", "l" }, new String[] { "ID", "Provider", "Name", "Enabled", "Backup" }, dataToPrint);
        } else {
            System.out.println("No multifactor authentication devices for given user");
        }
    }

    @Override
    protected void checkArguments(CommandLine cmd) {
        // Nothing in here
    }

    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addRequiredOption(PARAM_CONTEXTID_SHORT, PARAM_CONTEXTID_LONG, true, PARAM_CONTEXTID_DESC);
        options.addRequiredOption(PARAM_USERID_SHORT, PARAM_USERID_LONG, true, PARAM_USERID_DESC);
    }

    @Override
    protected WebTarget getEndpoint(CommandLine cmd) {
        String endpoint = new StringBuilder()
            .append(getEndpointRoot(cmd))
            .append("/contexts/")
            .append(cmd.getOptionValue(PARAM_CONTEXTID_LONG))
            .append("/users/")
            .append(cmd.getOptionValue(PARAM_USERID_LONG))
            .append("/multifactor/devices")
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
        Response response = executionContext.get();
        String data = response.readEntity(String.class);
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            JSONArray deviceArrays = new JSONArray(data);
            printDevices(deviceArrays);
        } else {
            printError("Failed to list multifactor authentication devices", data, response.getStatusInfo());
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
    }

    @Override
    protected String getFooter() {
        return "The command-line tool to list multifactor authentication devices for a given user.";
    }

    @Override
    protected String getName() {
        return "listmultifactordevice " + LIST_MULTIFACTOR_DEVICE_USAGE;
    }
}
