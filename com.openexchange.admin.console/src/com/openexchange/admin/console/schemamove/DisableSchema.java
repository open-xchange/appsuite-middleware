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

package com.openexchange.admin.console.schemamove;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.plugin.hosting.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 *
 * {@link DisableSchema}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class DisableSchema extends AbstractSchemaRMIToolkit {

    protected static final char OPT_NAME_SCHEMA_SHORT = 'm';
    protected static final String OPT_NAME_SCHEMA_LONG = "schema";
    protected static final String OPT_NAME_SCHEMA_ARG_NAME = "schema_name";
    protected static final String OPT_NAME_SCHEMA_DESCRIPTION = "The name of the schema to disable";

    protected static final char OPT_NAME_TARGET_RMI_SHORT = 'r';
    protected static final String OPT_NAME_TARGET_RMI_LONG = "rmi-hosts";
    protected static final String OPT_NAME_TARGET_RMI_ARG_NAME = "rmi_hosts";
    protected static final String OPT_NAME_TARGET_RMI_DESCRIPTION = "A list of RMI hosts e.g. 192.168.1.25:1099,192.168.1.26. "
        + "If no port is given the default RMI port 1099 is taken.";

    public static void main(String[] args) {
        DisableSchema es = new DisableSchema();
        es.start(args);
    }

    private void start(final String[] args) {
        final AdminParser parser = new AdminParser("disableschema");

        setDefaultCommandLineOptionsWithoutContextID(parser);

        final CLIOption optSchema = setShortLongOpt(
            parser,
            OPT_NAME_SCHEMA_SHORT,
            OPT_NAME_SCHEMA_LONG,
            OPT_NAME_SCHEMA_ARG_NAME,
            OPT_NAME_SCHEMA_DESCRIPTION,
            true);

        final CLIOption optRMIHosts = setShortLongOptWithDefault(
            parser,
            OPT_NAME_TARGET_RMI_SHORT,
            OPT_NAME_TARGET_RMI_LONG,
            OPT_NAME_TARGET_RMI_ARG_NAME,
            OPT_NAME_TARGET_RMI_DESCRIPTION,
            RMI_HOSTNAME,
            false);

        // parse the command line
        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);
            String schemaName = (String) parser.getOptionValue(optSchema);
            List<String> rmiHosts = getRMIHostsFromOptions(parser, optRMIHosts);

            if (rmiHosts == null) {
                SchemaMoveRemote smr = getSchemaMoveRemoteInterface();
                smr.disableSchema(auth, schemaName);
                smr.invalidateContexts(auth, schemaName, true);
            } else {
                List<SchemaMoveRemote> remotes = new LinkedList<SchemaMoveRemote>();

                // Check first if remote interface is reachable for the given RMI hosts
                for(String rmiHost : rmiHosts) {
                   remotes.add(getSchemaMoveRemoteInterface(rmiHost));
                }

                if (false == remotes.isEmpty()) {
                    boolean shouldDisableSchema = true;
                    for(SchemaMoveRemote smr : remotes) {
                        if (shouldDisableSchema) {
                            smr.disableSchema(auth, schemaName);
                            shouldDisableSchema = false;
                        }
                        smr.invalidateContexts(auth, schemaName, true);
                    }
                }
            }
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    @Override
    protected String getObjectName() {
        return "disableschema";
    }

}
