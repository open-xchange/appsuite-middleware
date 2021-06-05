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

import static com.openexchange.java.Autoboxing.i;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.plugin.hosting.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.java.Strings;

/**
 *
 * {@link RestoreReference}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class RestoreReference extends AbstractSchemaRMIToolkit {

    protected static final char OPT_NAME_SOURCE_SCHEMA_SHORT = 'm';
    protected static final String OPT_NAME_SOURCE_SCHEMA_LONG = "source-schema";
    protected static final String OPT_NAME_SOURCE_SCHEMA_ARG_NAME = "source_schema_name";
    protected static final String OPT_NAME_SOURCE_SCHEMA_DESCRIPTION = "The name of the source schema in which the database dump will be replayed.";

    protected static final char OPT_NAME_TARGET_SCHEMA_SHORT = 'n';
    protected static final String OPT_NAME_TARGET_SCHEMA_LONG = "target-schema";
    protected static final String OPT_NAME_TARGET_SCHEMA_ARG_NAME = "target_schema_name";
    protected static final String OPT_NAME_TARGET_SCHEMA_DESCRIPTION = "The name of the target schema in which the database dump will be replayed.";

    protected static final char OPT_NAME_TARGET_CLUSTER_SHORT = 't';
    protected static final String OPT_NAME_TARGET_CLUSTER_LONG = "target-cluster-id";
    protected static final String OPT_NAME_TARGET_CLUSTER_ARG_NAME = "target_cluster_id";
    protected static final String OPT_NAME_TARGET_CLUSTER_DESCRIPTION = "The identifier of the target cluster.";

    protected static final char OPT_NAME_TARGET_RMI_SHORT = 'r';
    protected static final String OPT_NAME_TARGET_RMI_LONG = "rmi-host";
    protected static final String OPT_NAME_TARGET_RMI_ARG_NAME = "rmi_host";
    protected static final String OPT_NAME_TARGET_RMI_DESCRIPTION = "A RMI host address e.g. 192.168.1.25:1099."
        + " If no port is given the default RMI port 1099 is taken.";

    public static void main(String[] args) {
        RestoreReference es = new RestoreReference();
        es.start(args);
    }

    private void start(final String[] args) {
        final AdminParser parser = new AdminParser("restorereference");

        setDefaultCommandLineOptionsWithoutContextID(parser);

        final CLIOption optSourceSchema = setShortLongOpt(
            parser,
            OPT_NAME_SOURCE_SCHEMA_SHORT,
            OPT_NAME_SOURCE_SCHEMA_LONG,
            OPT_NAME_SOURCE_SCHEMA_ARG_NAME,
            OPT_NAME_SOURCE_SCHEMA_DESCRIPTION,
            true);

        final CLIOption optTargetSchema = setShortLongOpt(
            parser,
            OPT_NAME_TARGET_SCHEMA_SHORT,
            OPT_NAME_TARGET_SCHEMA_LONG,
            OPT_NAME_TARGET_SCHEMA_ARG_NAME,
            OPT_NAME_TARGET_SCHEMA_DESCRIPTION,
            true);

        final CLIOption optTargetCluster = setShortLongOpt(
            parser,
            OPT_NAME_TARGET_CLUSTER_SHORT,
            OPT_NAME_TARGET_CLUSTER_LONG,
            OPT_NAME_TARGET_CLUSTER_ARG_NAME,
            OPT_NAME_TARGET_CLUSTER_DESCRIPTION,
            true);

        final CLIOption optRMIHost = setShortLongOptWithDefault(
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
            String sourceSchema = (String) parser.getOptionValue(optSourceSchema);
            String targetSchema = (String) parser.getOptionValue(optTargetSchema);
            int targetClusterId = i(Integer.valueOf((String) parser.getOptionValue(optTargetCluster)));
            String rmiHost = (String) parser.getOptionValue(optRMIHost);

            SchemaMoveRemote smr = null;
            if (Strings.isEmpty(rmiHost)) {
                smr = getSchemaMoveRemoteInterface();
            } else {
                smr = getSchemaMoveRemoteInterface(rmiHost);
            }

            smr.restorePoolReferences(auth, sourceSchema, targetSchema, targetClusterId);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    @Override
    protected String getObjectName() {
        return "restorereference";
    }

}
