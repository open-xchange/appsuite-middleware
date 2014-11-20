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
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import com.openexchange.admin.schemamove.mbean.SchemaMoveMBean;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;
import com.openexchange.java.Strings;

/**
 * {@link DumpSchema}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DumpSchema extends AbstractMBeanCLI<Void> {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new DumpSchema().execute(args);
    }

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

    @Override
    protected String getFooter() {
        return "Tool to dump an Open-Xchange database schema to a file.";
    }

    @Override
    protected String getName() {
        return "dumpschema";
    }

    @SuppressWarnings("static-access")
    @Override
    protected void addOptions(Options options) {
        options.addOption(OptionBuilder.withLongOpt("schema").withArgName("schema_name").withDescription("The name of the schema to dump.").hasArg(
            true).isRequired(true).create("m"));
        options.addOption(OptionBuilder.withLongOpt("out").withArgName("dump_file").withDescription(
            "The name of the dump file. If not specified, the dump will be written to standard output").hasArg(true).isRequired(true).create(
            "o"));
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        SchemaMoveMBean schemaMoveMBean = getMBean(mbsc, SchemaMoveMBean.class, SchemaMoveMBean.DOMAIN);
        List<Attribute> list = schemaMoveMBean.getDbAccessInfoForSchema(cmd.getOptionValue('m')).asList();
        final Map<String, String> dbAccessInfo = SchemaTools.convertToMap(list);

        String url = getAttribute("url", dbAccessInfo);
        int pos = url.indexOf("jdbc:");
        if (pos >= 0) {
            url = url.substring(pos + 5);
        }
        URI uri = new URI(url);

        String login = getAttribute("login", dbAccessInfo);
        String password = getAttribute("password", dbAccessInfo);
        String schema = getAttribute("schema", dbAccessInfo);
        String output = cmd.getOptionValue('o');
        
        print(uri, login, password, schema, output);

        return null;
    }

    /**
     * Print
     * 
     * @param host
     * @param login
     * @param password
     * @param out
     * @throws IOException
     */
    private void print(URI uri, String login, String password, String schema, String output) {
        StringBuilder builder = new StringBuilder();
        builder.append(schema).append(" -h ").append(uri.getHost());
        if (uri.getPort() > 0) {
            builder.append(" -P ").append(uri.getPort());
        }
        builder.append(" -u ").append(login).append(" -p").append(password).append(" --single-transaction > ").append(output);
        System.out.println(builder.toString());
    }

    /**
     * Helper method to get the attribute from the map
     * 
     * @param name
     * @param map
     * @return
     */
    private String getAttribute(final String name, final Map<String, String> map) {
        final String attribute = map.get(name);
        if (Strings.isEmpty(attribute)) {
            System.err.println("Missing the following attribute in MBean response: " + name);
            System.exit(1);
        }
        return attribute;
    }
}
