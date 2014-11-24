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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import org.apache.commons.cli.CommandLine;
import com.openexchange.admin.schemamove.mbean.SchemaMoveMBean;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.java.Strings;

/**
 * {@link AbstractSchemaToolkit}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractSchemaToolkit extends AbstractMBeanCLI<Void> {

    @Override
    protected void checkOptions(CommandLine cmd) {
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        authenticator.doAuthentication(login, password);
    }

    /**
     * Fetch the database access information from the MBean
     * 
     * @param schema The schema name
     * @param cmd The command
     * @return A map with all attributes
     * @throws MalformedObjectNameException
     * @throws MBeanException
     */
    protected Map<String, String> fetchDBAccessInfo(String schema, MBeanServerConnection mbsc) throws MalformedObjectNameException, MBeanException {
        SchemaMoveMBean schemaMoveMBean = getMBean(mbsc, SchemaMoveMBean.class, SchemaMoveMBean.DOMAIN);
        List<Attribute> list = schemaMoveMBean.getDbAccessInfoForSchema(schema).asList();
        return convertToMap(list);
    }

    /**
     * Fetch the database access information from the MBean
     * 
     * @param clusterId The cluster identifier
     * @param cmd The command
     * @return A map with all attributes
     * @throws MalformedObjectNameException
     * @throws MBeanException
     */
    protected Map<String, String> fetchDBAccessInfo(int clusterId, MBeanServerConnection mbsc) throws MalformedObjectNameException, MBeanException {
        SchemaMoveMBean schemaMoveMBean = getMBean(mbsc, SchemaMoveMBean.class, SchemaMoveMBean.DOMAIN);
        List<Attribute> list = schemaMoveMBean.getDbAccessInfoForCluster(clusterId).asList();
        return convertToMap(list);
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
     * Convert an {@link AttributeList} to a {@link Map}
     * 
     * @param attrList
     * @return
     */
    protected Map<String, String> convertToMap(List<Attribute> attrList) {
        Map<String, String> map = new HashMap<String, String>(attrList.size());
        for (Attribute attr : attrList) {
            map.put(attr.getName(), (String) attr.getValue());
        }
        return map;
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
}
