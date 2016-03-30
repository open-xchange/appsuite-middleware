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

import java.util.Map;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.java.Strings;

/**
 *
 * {@link CreateSchema}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class CreateSchema extends AbstractSchemaRMIToolkit {
    protected static final char OPT_NAME_TARGET_CLUSTER_SHORT = 't';
    protected static final String OPT_NAME_TARGET_CLUSTER_LONG = "target-cluster-id";
    protected static final String OPT_NAME_TARGET_CLUSTER_ARG_NAME = "target_cluster_id";
    protected static final String OPT_NAME_TARGET_CLUSTER_DESCRIPTION = "The identifier of the target cluster.";

    protected static final char OPT_NAME_TARGET_RMI_SHORT = 'r';
    protected static final String OPT_NAME_TARGET_RMI_LONG = "rmi-host";
    protected static final String OPT_NAME_TARGET_RMI_ARG_NAME = "rmi_host";
    protected static final String OPT_NAME_TARGET_RMI_DESCRIPTION = "A RMI host address e.g. 192.168.1.25:1099."
        + " If no port is given the default RMI port 1099 is taken. ";

    public static void main(String[] args) {
        CreateSchema es = new CreateSchema();
        es.start(args);
    }

    private void start(final String[] args) {
        final AdminParser parser = new AdminParser("createschema");

        setDefaultCommandLineOptionsWithoutContextID(parser);

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

            String rmiHost = (String) parser.getOptionValue(optRMIHost);
            int targetClusterId = Integer.valueOf((String) parser.getOptionValue(optTargetCluster));
            SchemaMoveRemote smr = null;
            if(Strings.isEmpty(rmiHost)) {
                smr = getSchemaMoveRemoteInterface();
            } else {
                smr = getSchemaMoveRemoteInterface(rmiHost);
            }
            String schema = smr.createSchema(auth, targetClusterId);

            final Map<String, String> dbAccessInfo = fetchDBAccessInfo(auth, smr, targetClusterId, parser);
            dbAccessInfo.put("schema", schema);
            printDBAccessInfo(dbAccessInfo);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
        }
    }

    @Override
    protected String getObjectName() {
        return "createschema";
    }
}
