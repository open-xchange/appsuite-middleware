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

package com.openexchange.admin.db;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;

/**
 * {@link ReplaySchema}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ReplaySchema extends AbstractMBeanCLI<Void> {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ReplaySchema().execute(args);
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
        return "Tool to replay Open-Xchange database schemata.";
    }

    @Override
    protected String getName() {
        return "replayschema";
    }

    @SuppressWarnings("static-access")
    @Override
    protected void addOptions(Options options) {
        options.addOption(OptionBuilder.withLongOpt("source-schema").withArgName("schema_name").withDescription(
            "The name of the source schema in which the database dump will be replayed").hasArg(true).isRequired(true).create("m"));
        options.addOption(OptionBuilder.withLongOpt("in").withArgName("dump_file").withDescription("The name of the dump file to replay.").hasArg(
            true).isRequired(true).create("i"));
        options.addOption(OptionBuilder.withLongOpt("write-db-pool").withArgName("write_db_pool_id").withDescription(
            "The identifier of the write db pool.").hasArg(true).isRequired(true).create("w"));
        options.addOption(OptionBuilder.withLongOpt("read-db-pool").withArgName("read_db_pool_id").withDescription(
            "The identifier of the read db pool.").hasArg(true).isRequired(true).create("r"));
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        // TODO: flesh out
        // - create new schema
        // - replay the dump file in that schema
        // - update the context_server2db_pool entries
        return null;
    }

}
