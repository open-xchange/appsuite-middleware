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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.heapdump;

import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.mbean.AuthenticatorMBean;
import com.openexchange.cli.AbstractMBeanCLI;

/**
 * {@link HeapDumper} - Command-line tool to obtain a heap dump.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class HeapDumper extends AbstractMBeanCLI<Void> {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new HeapDumper().execute(args);
    }

    /**
     * Initializes a new {@link HeapDumper}.
     */
    public HeapDumper() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        if (!cmd.hasOption('f')) {
            System.out.println("You must provide a file name.");
            printHelp(options);
            System.exit(-1);
            return;
        }
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return false;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, AuthenticatorMBean authenticator) throws MBeanException {
        // Nothing
    }

    @Override
    protected String getFooter() {
        return "The Open-Xchange heap dump tool";
    }

    @Override
    protected String getName() {
        return "heapdump";
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption("f", "file", true, "The name of the file in which to dump the heap snapshot");
    }

    @Override
    protected Void invoke(Options option, CommandLine cmd, MBeanServerConnection mbsc) throws Exception {
        HeapDumpMBean heapDumpMBean = getMBean(mbsc, HeapDumpMBean.class, HeapDumpMBean.DOMAIN);

        String fileName = cmd.getOptionValue('f');
        heapDumpMBean.dumpHeap(fileName, true);

        System.out.println("Heap dump written to " + fileName);
        return null;
    }

}
