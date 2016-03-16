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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.admin.console.schemamove;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.schemamove.mbean.SchemaMoveRemote;

/**
 *
 * {@link EnableSchema}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class EnableSchema extends AbstractSchemaRMIToolkit {

    protected static final char OPT_NAME_TARGET_SCHEMA_SHORT = 'm';
    protected static final String OPT_NAME_TARGET_SCHEMA_LONG = "target-schema";
    protected static final String OPT_NAME_TARGET_SCHEMA_ARG_NAME = "schema_name";
    protected static final String OPT_NAME_TARGET_SCHEMA_DESCRIPTION = "The name of the schema to enable.";

    protected static final char OPT_NAME_TARGET_RMI_SHORT = 'r';
    protected static final String OPT_NAME_TARGET_RMI_LONG = "rmi-hosts";
    protected static final String OPT_NAME_TARGET_RMI_ARG_NAME = "rmi_hosts";
    protected static final String OPT_NAME_TARGET_RMI_DESCRIPTION = "A list of RMI hosts e.g. 192.168.1.25:1099,192.168.1.26. "
        + "If no port is given the default RMI port 1099 is taken.";

    public static void main(String[] args) {
        EnableSchema es = new EnableSchema();
        es.start(args);
    }

    private void start(final String[] args) {
        final AdminParser parser = new AdminParser("enableschema");

        setDefaultCommandLineOptionsWithoutContextID(parser);

        final CLIOption optTargetSchema = setShortLongOpt(
            parser,
            OPT_NAME_TARGET_SCHEMA_SHORT,
            OPT_NAME_TARGET_SCHEMA_LONG,
            OPT_NAME_TARGET_SCHEMA_ARG_NAME,
            OPT_NAME_TARGET_SCHEMA_DESCRIPTION,
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

            String schemaName = (String) parser.getOptionValue(optTargetSchema);
            List<String> rmiHosts = getRMIHostsFromOptions(parser, optRMIHosts);

            if(rmiHosts == null) {
                SchemaMoveRemote smr = getSchemaMoveRemoteInterface();
                smr.enableSchema(auth, schemaName);
                smr.invalidateContexts(auth, schemaName, false);
            } else {
                List<SchemaMoveRemote> remotes = new LinkedList<SchemaMoveRemote>();

                // Check first if remote interface is reachable for the given RMI hosts
                for(String rmiHost : rmiHosts) {
                   remotes.add(getSchemaMoveRemoteInterface(rmiHost));
                }

                if(false == remotes.isEmpty()) {
                    boolean shouldEnableSchema = true;
                    for(SchemaMoveRemote smr : remotes) {
                        if(shouldEnableSchema) {
                            smr.enableSchema(auth, schemaName);
                            shouldEnableSchema = false;
                        }
                        smr.invalidateContexts(auth, schemaName, false);
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
        return "enableschema";
    }

}
