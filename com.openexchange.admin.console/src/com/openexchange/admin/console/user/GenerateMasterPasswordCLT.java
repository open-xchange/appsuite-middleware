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

package com.openexchange.admin.console.user;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.IPasswordMech;
import com.openexchange.passwordmechs.PasswordMech;

/**
 * {@link GenerateMasterPasswordCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GenerateMasterPasswordCLT {

    private enum Parameter {
        adminuser, adminpass, encryption, mpasswdfile
    }

    private static final Options options = new Options();
    static {
        options.addOption(createOption("A", "adminuser", true, false, "Account name of superadmin (Default: oxadminmaster)", false));
        options.addOption(createOption("P", "adminpass", true, false, "Password of superadmin", false));
        options.addOption(createOption("f", "mpasswdfile", true, false, "Path to mpasswd (Default: /opt/open-xchange/etc/mpasswd)", false));
        options.addOption(createOption("e", "encryption", true, false, "Encryption algorithm to use for the password (Default: bcrypt)", false));
        options.addOption(createOption("h", "help", false, false, "Prints this help text", false));
    }

    /**
     * Create an {@link Option} with the {@link OptionBuilder}
     *
     * @param shortName short name of the option
     * @param longName long name of the option
     * @param hasArgs whether it has arguments
     * @param hasOptArgs whether it has optional arguments
     * @param description short description
     * @param mandatory whether it is mandatory
     * @return
     */
    @SuppressWarnings("static-access")
    private static final Option createOption(String shortName, String longName, boolean hasArgs, boolean hasOptArgs, String description, boolean mandatory) {
        return OptionBuilder.withLongOpt(longName).hasArg(hasArgs).withDescription(description).isRequired(mandatory).create(shortName);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        StringBuilder builder = new StringBuilder();
        Map<Parameter, String> parameters = new HashMap<Parameter, String>();
        initParameters(parameters);
        boolean printUsage = false;
        String exceptionMessage = new String();
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("h")) {
                printUsage(0);
            }
            if (cl.hasOption("A")) {
                parameters.put(Parameter.adminuser, cl.getOptionValue("A"));
            }
            if (cl.hasOption("e")) {
                parameters.put(Parameter.encryption, cl.getOptionValue("e"));
            }
            String clearPassword;
            if (cl.hasOption("P")) {
                clearPassword = cl.getOptionValue("P");
            } else {
                builder.append("Enter password for user ").append(parameters.get(Parameter.adminuser)).append(": ");
                Console console = System.console();
                char[] passwd;
                if (console != null && (passwd = console.readPassword("[%s]", builder.toString())) != null) {
                    clearPassword = new String(passwd);
                } else {
                    BufferedWriter bufferWrite = new BufferedWriter(new OutputStreamWriter(System.out));
                    bufferWrite.write(builder.toString());
                    bufferWrite.flush();

                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    clearPassword = bufferRead.readLine();
                }
            }
            final String encPassword = encryptPassword(parameters.get(Parameter.encryption), clearPassword);
            clearPassword = null;
            parameters.put(Parameter.adminpass, encPassword);
            if (cl.hasOption("f")) {
                parameters.put(Parameter.mpasswdfile, cl.getOptionValue("f"));
            }

            builder.setLength(0);

            invoke(parameters);
            builder.append("Saved password for user '").append(parameters.get(Parameter.adminuser)).append("' and encryption '").append(parameters.get(Parameter.encryption)).append("' in '").append(parameters.get(Parameter.mpasswdfile)).append("'.");
            System.out.println(builder.toString());
            System.exit(0);
        } catch (ParseException | IllegalArgumentException | OXException e) {
            exceptionMessage = e.getMessage();
            printUsage = true;
        } catch (IOException e) {
            exceptionMessage = e.getMessage();
        }
        builder.append("Unable to save password for user '").append(parameters.get(Parameter.adminuser)).append("' and encryption '").append(parameters.get(Parameter.encryption)).append("' in '").append(parameters.get(Parameter.mpasswdfile)).append("'.");
        builder.append("\n").append(exceptionMessage);
        System.err.println(builder.toString());
        if (printUsage) {
            printUsage(-1);
        }
    }

    /**
     * Invoke
     *
     * @param parameters
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void invoke(Map<Parameter, String> parameters) throws IOException {
        File file = new File(parameters.get(Parameter.mpasswdfile));
        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedReader br = null;
        PrintWriter writer = null;
        try {
            br = new BufferedReader(new FileReader(file));

            List<String> lines = new LinkedList<String>();
            boolean updated = false;
            for (String line; (line = br.readLine()) != null;) {
                if (!line.startsWith("#") && !Strings.isEmpty(line)) {
                    lines.add(new StringBuilder(96).append(parameters.get(Parameter.adminuser)).append(":").append(parameters.get(Parameter.encryption)).append(":").append(parameters.get(Parameter.adminpass)).toString());
                    updated = true;
                } else {
                    lines.add(line);
                }
            }

            if (!updated) {
                lines.add(new StringBuilder(96).append(parameters.get(Parameter.adminuser)).append(":").append(parameters.get(Parameter.encryption)).append(":").append(parameters.get(Parameter.adminpass)).toString());
            }

            writer = new PrintWriter(file);
            for (String line : lines) {
                writer.println(line);
            }
            writer.flush();
        } catch (FileNotFoundException e) {
            throw e;
        } finally {
            Streams.close(br, writer);
        }
    }

    /**
     * Encrypt the specified password
     *
     * @param encryption The encryption algorithm
     * @param password The plain-text password to encrypt
     * @return The encrypted password
     * @throws OXException
     */
    private static String encryptPassword(final String encryption, final String password) throws OXException {
        IPasswordMech pm = getPasswordMechFor(encryption);
        return pm.encode(password);
    }

    /**
     * Initialise defaults
     *
     * @param parameters
     */
    private static void initParameters(Map<Parameter, String> parameters) {
        parameters.put(Parameter.adminuser, "oxadminmaster");
        parameters.put(Parameter.adminpass, null);
        parameters.put(Parameter.encryption, "bcrypt");
        parameters.put(Parameter.mpasswdfile, "/opt/open-xchange/etc/mpasswd");
    }

    /**
     * Print usage
     *
     * @param exitCode
     */
    private static final void printUsage(int exitCode) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(120);
        hf.printHelp("generatempasswd [-A <adminuser>] [-P <adminpassword>] [-e <encryption>] [-f </path/for/mpasswdfile>]", null, options, "\n\nValid encryption/hashing algorithms: " + getValidEncHashAlgos());
        System.exit(exitCode);
    }

    /**
     * Get valid encryption/hashing algorithms
     *
     * @return
     */
    private static String getValidEncHashAlgos() {
        StringBuilder builder = new StringBuilder();
        for (PasswordMech p : PasswordMech.values()) {
            builder.append(Strings.toLowerCase(p.toString())).append(", ");
        }
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    /**
     * Gets the password mechanism for given identifier
     *
     * @param identifier The identifier
     * @return The password mechanism or <code>null</code>
     */
    public static IPasswordMech getPasswordMechFor(String identifier) {
        if (Strings.isEmpty(identifier)) {
            return null;
        }
        String id = Strings.toUpperCase(identifier);
        if (false == id.startsWith("{")) {
            id = new StringBuilder(id.length() + 1).append('{').append(id).toString();
        }
        if (false == id.endsWith("}")) {
            id = new StringBuilder(id.length() + 1).append(id).append('}').toString();
        }

        for (IPasswordMech pm : PasswordMech.values()) {
            if (id.equals(pm.getIdentifier())) {
                return pm;
            }
        }
        return null;
    }
}
