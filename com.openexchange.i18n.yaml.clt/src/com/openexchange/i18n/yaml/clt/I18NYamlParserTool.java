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

package com.openexchange.i18n.yaml.clt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.HeaderUtil;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoWriter;
import com.openexchange.cli.AbstractCLI;
import com.openexchange.i18n.yaml.internal.I18NYamlParserImpl;
import com.openexchange.i18n.yaml.internal.I18nYamlParseException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link I18NYamlParserTool}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class I18NYamlParserTool extends AbstractCLI<Void, Void> {

    public static void main(String[] args) {
        new I18NYamlParserTool().execute(args);
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link I18NYamlParserTool}.
     */
    private I18NYamlParserTool() {
        super();
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(new Option("y", "yamlfile", true, "The name of the YAML file to parse; either a fully qualified path name or only the name to look it up in default directory \"/opt/open-xchange/etc\""));
        options.addOption(new Option("o", "output", true, "The path name for the .pot output file; e.g. \"/tmp/yamlstrings.pot\""));
        options.addOption(new Option("f", "force", false, "Whether to force .pot creation. That is to overwrite the denoted path name in case it does already exist."));
        options.addOption(new Option("d", "date", false, "Whether to set the \"POT-Creation-Date\" in the headers section of the .pot file; e.g. \"POT-Creation-Date: 2015-10-18 06:46+0100\\n\""));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, Void voit) throws Exception {
        String yamlFile = cmd.getOptionValue('y');

        List<String> literals;
        try {
            I18NYamlParserImpl yamlParser = new I18NYamlParserImpl(null);
            literals = yamlParser.parseFile(yamlFile);
        } catch (I18nYamlParseException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
            return null;
        }

        if (null == literals) {
            System.out.println("Unable to look-up such a file \"" + yamlFile + "\".");
            System.exit(-1);
            return null;
        }

        if (literals.isEmpty()) {
            System.out.println("Denoted YAML file \"" + yamlFile + "\" appears to have no translatable string literals.");
            System.exit(-1);
            return null;
        }

        // Generate .pot catalog & add literals to it
        Catalog catalog = new Catalog(true);
        for (String literal : literals) {
            Message message = new Message();
            message.addComment(literal);
            message.setMsgid(literal);
            message.setMsgstr("");
            catalog.addMessage(message);
        }

        // Generate & add header
        Message header = HeaderUtil.generateDefaultHeader();
        catalog.addMessage(header);

        // Check whether to keep or drop the "POT-Creation-Date" header line
        boolean keepCreationDate = cmd.hasOption('d');
        if (false == keepCreationDate) {
            /*-
             * "msgstr" is something like:
             *
             * Project-Id-Version: PACKAGE VERSION
             * Report-Msgid-Bugs-To:
             * POT-Creation-Date: 2016-01-18 06:46+0100  <------ This line is supposed to be dropped
             * PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE
             * Last-Translator: FULL NAME <EMAIL@ADDRESS>
             * Language-Team: LANGUAGE <LL@li.org>
             * MIME-Version: 1.0
             * Content-Type: text/plain; charset=UTF-8
             * Content-Transfer-Encoding: 8bit
             */
            String msgstr = header.getMsgstr();
            int start = msgstr.indexOf("POT-Creation-Date:");
            if (start >= 0) {
                int end = msgstr.indexOf("PO-Revision-Date", start + 1);
                if (end > 0) {
                    StringBuilder newMsgStr = new StringBuilder(msgstr.length());
                    newMsgStr.append(msgstr.substring(0, start));
                    newMsgStr.append(msgstr.substring(end));
                    header.setMsgstr(newMsgStr.toString());
                }
            }
        }

        // Write .pot catalog to denoted output
        File outputFile = new File(cmd.getOptionValue('o'));
        saveCat(catalog, outputFile);
        System.out.println(outputFile + " successfully generated");

        return null;
    }

    private void saveCat(Catalog cat, File poFile) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(poFile));
        try {
            PoWriter writer = new PoWriter();
            writer.setGenerateHeader(false);
            writer.write(cat, out);
        } finally {
            Streams.close(out);
        }
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        {
            String yamlFile = cmd.getOptionValue('y');
            if (Strings.isEmpty(yamlFile)) {
                System.out.println("You must provide a YAML name.");
                if (null != options) {
                    printHelp(options);
                }
                System.exit(-1);
                return;
            }
        }
        {
            String output = cmd.getOptionValue('o');
            if (Strings.isEmpty(output)) {
                System.out.println("You must provide an output path name.");
                if (null != options) {
                    printHelp(options);
                }
                System.exit(-1);
                return;
            }

            File outputFile = new File(output);
            File parentFile = outputFile.getParentFile();
            if (null != parentFile && !parentFile.isDirectory()) {
                System.out.println("Output path name is invalid. No such directory: " + parentFile.getPath());
                if (null != options) {
                    printHelp(options);
                }
                System.exit(-1);
                return;
            }

            if (!Strings.asciiLowerCase(output).endsWith(".pot")) {
                System.out.println("Output path name is invalid. Path name needs to end with \".pot\" suffix.");
                if (null != options) {
                    printHelp(options);
                }
                System.exit(-1);
                return;
            }

            if (outputFile.isFile() && !cmd.hasOption('f')) {
                System.out.println("Output file \"" + outputFile.getPath() + "\" does already exist. Use -f,--force option to overwrite.");
                if (null != options) {
                    printHelp(options);
                }
                System.exit(-1);
                return;
            }
        }
    }

    @Override
    protected String getFooter() {
        return "The Open-Xchange i18n YAML parser";
    }

    @Override
    protected String getName() {
        return "parsei18nyaml";
    }

}
