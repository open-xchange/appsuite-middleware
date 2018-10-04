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

package com.openexchange.admin.diff;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import com.openexchange.admin.diff.result.DiffResult;
import com.openexchange.cli.AbstractCLI;

/**
 * CLT to execute the configuration diff tool.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
// Shouldn't it rather inherit from AbstractAdministrativeCLI?
public class ConfigDiffCLT extends AbstractCLI<Void, Void> {

    private static final String SYNTAX = "listconfigdiff [-f <filename>] | [-h]";
    private static final String FOOTER = "\n\nLists the differences between the default and the installed configuration";

    /**
     * Entry point
     *
     * @param args
     */
    public static void main(String[] args) {
        new ConfigDiffCLT().execute(args);
    }

    /**
     * Initialises a new {@link ConfigDiffCLT}.
     */
    public ConfigDiffCLT() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.Object)
     */
    @Override
    protected Void invoke(Options option, CommandLine cmd, Void context) throws Exception {
        executeDiff(cmd.getOptionValue("f"));
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("f", "file", "filename", "Export diff to file", false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        // no-op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return SYNTAX;
    }

    /**
     * Execute diff
     *
     * @param file optional file to store the diff
     * @throws IOException
     */
    private static void executeDiff(String file) throws IOException {
        ConfigDiff configDiff = new ConfigDiff();
        DiffResult diffResult = configDiff.run();

        if (file == null) {
            System.out.println(diffResult.toString());
        } else {
            File output = new File(file);
            FileUtils.write(output, diffResult.toString());
        }

        if (diffResult.getProcessingErrors().size() > 0) {
            System.exit(1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getContext()
     */
    @Override
    protected Void getContext() {
        return null;
    }
}
