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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.java.Strings;

/**
 * {@link AbstractSchemaRMIToolkit}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public abstract class AbstractSchemaRMIToolkit extends ObjectNamingAbstraction {

    /**
     * Gets the remote interface from <code>rmi://localhost:1099</code>
     *
     * @return The remote interface
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws NotBoundException
     */
    protected SchemaMoveRemote getSchemaMoveRemoteInterface() throws MalformedURLException, RemoteException, NotBoundException {
        return (SchemaMoveRemote) Naming.lookup(RMI_HOSTNAME + SchemaMoveRemote.RMI_NAME);
    }

    /**
     * Gets the remote interface from the specified RMI host
     *
     * @param rmiHost The RMI host
     * @return The remote interface
     * @throws MalformedURLException
     * @throws RemoteException
     * @throws NotBoundException
     * @throws InvalidDataException - If RMI host is null
     */
    protected SchemaMoveRemote getSchemaMoveRemoteInterface(String rmiHost) throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException {
        if(false == Strings.isEmpty(rmiHost)) {
            StringBuffer sb = new StringBuffer(64);
            if(false == rmiHost.startsWith("rmi://")) {
               sb.append("rmi://");
               sb.append(rmiHost);
            } else {
                sb.append(rmiHost);
            }
            if(false == rmiHost.contains(":")) {
                sb.append(":1099");
            }
            if(false == rmiHost.endsWith("/")) {
                sb.append("/");
            }
            return (SchemaMoveRemote) Naming.lookup(sb.toString() + SchemaMoveRemote.RMI_NAME);
        }
        throw new InvalidDataException("Could not parse the RMI host address");
    }

    /**
     * Fetch the database access information from RMI call
     *
     * @param schema The schema name
     * @param cmd The command
     * @return A map with all attributes
     */
    protected Map<String, String> fetchDBAccessInfo(Credentials auth, SchemaMoveRemote smr, String schema, AdminParser parser) {
        try {
            return smr.getDbAccessInfoForSchema(auth, schema);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
            return null;
        }
    }

    private final static Pattern COMMA_SEPERATION_PATTERN = Pattern.compile("\\s*,\\s*");

    protected List<String> getRMIHostsFromOptions(AdminParser parser, CLIOption rmiOption) {
        String list = (String) parser.getOptionValue(rmiOption);
        if(list == null) {
            return null;
        }
        return new LinkedList<String>(Arrays.asList(COMMA_SEPERATION_PATTERN.split(list)));
    }

    /**
     * Fetch the database access information from RMI call
     *
     * @param clusterId The cluster identifier
     * @param cmd The command
     * @return A map with all attributes
     */
    protected Map<String, String> fetchDBAccessInfo(Credentials auth, SchemaMoveRemote smr, int clusterId, AdminParser parser) {
        try {
            return smr.getDbAccessInfoForCluster(auth, clusterId);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
            sysexit(1);
            return null;
        }
    }

    /**
     * Process and print the database access information along with some optional parameters
     *
     * @param dbAccessInfo The database info map
     * @param optionalParams The optional command line parameters
     * @throws URISyntaxException
     */
    protected void printDBAccessInfo(Map<String, String> dbAccessInfo, String... optionalParams) throws URISyntaxException {
        String url = getAttribute("url", dbAccessInfo);
        int pos = url.indexOf("jdbc:");
        if (pos >= 0) {
            url = url.substring(pos + 5);
        }
        URI uri = new URI(url);

        String login = getAttribute("login", dbAccessInfo);
        String password = getAttribute("password", dbAccessInfo);
        String schema = getAttribute("schema", dbAccessInfo);

        print(uri, login, password, schema, optionalParams);
    }

    /**
     * Helper method to get the attribute from the map
     *
     * @param name
     * @param map
     * @return
     */
    protected String getAttribute(final String name, final Map<String, String> map) {
        final String attribute = map.get(name);
        if (Strings.isEmpty(attribute)) {
            System.err.println("Missing the following attribute in MBean response: " + name);
            System.exit(1);
        }
        return attribute;
    }

    /**
     * Print schema information
     *
     * @param host
     * @param login
     * @param password
     * @param out
     * @throws IOException
     */
    protected void print(URI uri, String login, String password, String schema, String... optional) {
        StringBuilder builder = new StringBuilder();
        builder.append(schema).append(" -h ").append(uri.getHost());
        if (uri.getPort() > 0) {
            builder.append(" -P ").append(uri.getPort());
        }
        builder.append(" -u ").append(login).append(" -p").append(password).append(" ");
        for (String s : optional) {
            builder.append(s).append(" ");
        }
        System.out.println(builder.toString());
    }

    protected static final String OPT_NAME_MASTERADMINPASS_DESCRIPTION="master Admin password";
    protected static final String OPT_NAME_MASTERADMINUSER_DESCRIPTION="master Admin user name";

    @Override
    protected void setAdminPassOption(final AdminParser admp) {
        this.adminPassOption = setShortLongOpt(admp,OPT_NAME_ADMINPASS_SHORT, OPT_NAME_ADMINPASS_LONG, OPT_NAME_MASTERADMINPASS_DESCRIPTION, true, NeededQuadState.possibly);
    }

    @Override
    protected void setAdminUserOption(final AdminParser admp) {
        this.adminUserOption= setShortLongOpt(admp,OPT_NAME_ADMINUSER_SHORT, OPT_NAME_ADMINUSER_LONG, OPT_NAME_MASTERADMINUSER_DESCRIPTION, true, NeededQuadState.possibly);
    }
}
