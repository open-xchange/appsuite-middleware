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

import javax.ws.rs.core.Response.StatusType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.cli.AbstractRestCLI;

/**
 * {@link AbstractMultifactorClt} - Base class for the accessing the multifactor REST API via CLI
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public abstract class AbstractMultifactorClt extends AbstractRestCLI<Void> {

    protected static final String PARAM_ENDPOINT_LONG = "api-root";

    protected static final String PARAM_CONTEXTID_LONG = "contextid";
    protected static final String PARAM_CONTEXTID_SHORT = "c";
    protected static final String PARAM_CONTEXTID_DESC = "A valid context identifier.";

    protected static final String PARAM_USERID_LONG = "userid";
    protected static final String PARAM_USERID_SHORT = "i";
    protected static final String PARAM_USERID_DESC = "A valid user identifier.";

    protected static final String ENDPOINT_DEFAULT = "http://localhost:8009/admin/v1";

    /**
     * Gets the end-point to use for accessing the REST API
     *
     * @param commandLine The {@link CommandLine} to get the endpoint from
     * @return The end-point extracted from the given commandLine, or the default end-point
     */
    protected String getEndpointRoot(CommandLine commandLine) {
        String endPointRoot = commandLine.getOptionValue(PARAM_ENDPOINT_LONG, ENDPOINT_DEFAULT);
        return endPointRoot.endsWith("/") ? endPointRoot.substring(0, endPointRoot.length() - 1) : endPointRoot;
    }

    /**
     * Prints an error message
     *
     * @param msg The basic message
     * @param error The error
     * @param status The status code retunred from the server
     */
    protected void printError(String msg, String error, StatusType status) {
        System.err.println(String.format("%s: \"%s\". HTTP error code %d.", msg, status.getReasonPhrase(), Integer.valueOf(status.getStatusCode())));
        if (error != null && !error.isEmpty()) {
            System.err.println(error);
        }
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(null, PARAM_ENDPOINT_LONG, true, "URL to an alternative HTTP API endpoint. Example: 'https://192.168.0.1:8443/admin/v1/'");
    }

    @Override
    protected void addAdministrativeOptions(Options options, boolean mandatory) {
        options.addOption(createArgumentOption("A", "adminuser", "adminuser", "Admin username", true));
        options.addOption(createArgumentOption("P", "adminpass", "adminpassword", "Admin password", true));
    }

    @Override
    protected String getAuthorizationHeader(CommandLine cmd) {
        return cmd.getOptionValue("A") + ":" + cmd.getOptionValue("P");
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }
    
    @Override
    protected boolean useBasicAuth() {
        return false;
    }
}
