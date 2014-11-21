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

import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * {@link EnableSchema}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class EnableSchema extends AbstractSchemaToolkit {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new EnableSchema().execute(args);
    }

    @Override
    protected String getFooter() {
        return "Tool to enable Open-Xchange database schemata.";
    }

    @Override
    protected String getName() {
        return "enableschema";
    }

    @SuppressWarnings("static-access")
    @Override
    protected void addOptions(Options options) {
        options.addOption(OptionBuilder.withLongOpt("target-schema").withArgName("schema_name").withDescription(
            "The name of the schema to enable").hasArg(true).isRequired(true).create("m"));
        options.addOption(OptionBuilder.withLongOpt("delete-source").hasArg(false).withDescription(
            "Flag to indicate that the source schema should be deleted afterwards.").create("d"));
        options.addOption(OptionBuilder.withLongOpt("force-delete-source").hasArg(false).withDescription(
            "Flag to force the deletion of the source schema after migration. Should be used in conjunction with the -d flag").create("f"));
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        // TODO: flesh out
        // - fetch contexts that were disabled during the migration process
        // - iterate over the cids and create a batch statement
        // - execute the batch statement to enable the contexts
        return null;
    }
}
