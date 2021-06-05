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

package com.openexchange.admin.diff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class ConfigDiffCLT extends AbstractCLI<Void, Void> {

    private static final String SYNTAX = "listconfigdiff [-f <filename>] | [-h]";
    private static final String FOOTER = "Lists the differences between the default and the installed configuration";

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

    @Override
    protected Void invoke(Options option, CommandLine cmd, Void context) throws Exception {
        executeDiff(cmd.getOptionValue("f"));
        return null;
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("f", "file", "filename", "Export diff to file", false));
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // no-op
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

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
            FileUtils.write(output, diffResult.toString(), StandardCharsets.UTF_8);
        }

        if (diffResult.getProcessingErrors().size() > 0) {
            System.exit(1);
        }
    }

    @Override
    protected Void getContext() {
        return null;
    }
}
