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

package com.openexchange.multifactor.clt;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.client.Invocation.Builder;
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

    public static void main(String[] args) {
        new ListMultifactorDevice().execute(args);
    }

    private void printDevices(JSONObject entity) throws JSONException {
        List<List<String>> dataToPrint = new ArrayList<List<String>>();
        JSONArray devices = entity.getJSONArray("devices");
        for (int i = 0; i < devices.length(); i++) {
            JSONObject device = devices.getJSONObject(i);
            List<String> deviceData = new ArrayList<String>();
            deviceData.add(device.getString("id"));
            deviceData.add(device.getString("providerName"));
            deviceData.add(device.getString("name"));
            deviceData.add(device.getString("enabled"));
            deviceData.add(device.getString("backup"));
            dataToPrint.add(deviceData);
        }
        OutputHelper.doOutput(new String[] { "l", "l", "l", "l", "l" }, new String[] { "ID", "Provider", "Name", "Enabled", "Backup" }, dataToPrint);
    }

    @Override
    protected void checkArguments(CommandLine cmd) {}

    @Override
    protected void addOptions(Options options) {
        super.addOptions(options);
        options.addOption("c", PARAM_CONTEXTID_LONG, true, "A valid context identifier.");
        options.addOption("i", PARAM_USERID_LONG, true, "A valid user identifier.");
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
        }
        return null;
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, Builder executionContext) throws Exception {
        executionContext.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE);
        String response = executionContext.get(String.class);
        JSONObject entity = new JSONObject(response);
        if (entity.has("devices")) {
            printDevices(entity);
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
        return "The command-line tool to list multifactor authentication devices.";
    }

    @Override
    protected String getName() {
        return "listmultifactordevice [OPTIONS]";
    }
}
