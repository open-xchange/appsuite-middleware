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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.appsuite.history.clt;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.openexchange.appsuite.history.impl.HistoryUtil;

/**
 * {@link HistoryCLT} - a clt which can be used to copy files to the history folder
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class HistoryCLT {

    // @formatter:off
    private static final String CLT_DESC = "\nThis CLT checks the apps and manifests folders for new versions and copies them to the designated history folder. "
                                         + "It is also possible to force this by using the --timestamp parameter. This is necessary in case the touchappsuite clt has been used on frontend nodes."
                                         + "Please use the same timestamp here as well.\n\n";
    // @formatter:on

    /**
     * Version string always contains 3 dots. Either with a rev (e.g. "7.10.4-5.XXXX") or without (e.g. "7.10.4.XXXX")
     */
    private static Pattern p = Pattern.compile("^((:?[^.]*\\.){3})(:?.*)");

    /**
     * Checks whether the apps path ends with <code>/apps</code> or not
     */
    private static Pattern appsPathMatcher = Pattern.compile(".*\\/apps\\/?$");

    private static final String DEFAULT_MANIFESTS_PATH = "/opt/open-xchange/appsuite/manifests";
    private static final String DEFAULT_APPS_PATH = "/opt/open-xchange/appsuite/apps";

    private static final String DEFAULT_HISTORY_APPS_PATH = "/var/opt/open-xchange/frontend/history/apps";
    private static final String DEFAULT_HISTORY_MANIFESTS_PATH = "/var/opt/open-xchange/frontend/history/manifests";

    private static final String MANIFESTS_CHAR = "m";
    private static final String APPS_CHAR = "a";
    private static final String TIMESTAMP_CHAR = "t";
    private static final String HELP_CHAR = "h";
    private static final String HISTORY_MANIFESTS = "history_manifests";
    private static final String HISTORY_APPSUITE = "history_apps";

    private static Option appsOption;
    private static Option manifestsOption;
    private static Option appsHistoryOption;
    private static Option manifestsHistoryOption;
    private static Option timestampOption;

    static {
        appsOption = new Option(APPS_CHAR, "apps", true, "The optional path to the installed apps");
        appsOption.setArgName("apps_path");

        manifestsOption = new Option(MANIFESTS_CHAR, "manifests", true, "The optional path to the installed manifests");
        manifestsOption.setArgName("manifests_path");

        appsHistoryOption = new Option(null, HISTORY_APPSUITE, true, "The optional path to the apps history folder.");
        appsHistoryOption.setArgName("apps_history_path");

        manifestsHistoryOption = new Option(null, HISTORY_MANIFESTS, true, "The optional path to the manifests history folder.");
        manifestsHistoryOption.setArgName("manifests_history_path");

        timestampOption = new Option(TIMESTAMP_CHAR, "timestamp", true, "Updates the version.txt files with this timestamp before doing any other checks.");
        timestampOption.setArgName("timestamp");
    }

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption(HELP_CHAR, "help", false, "Prints this help text");
        options.addOption(timestampOption);
        options.addOption(appsOption);
        options.addOption(manifestsOption);
        options.addOption(appsHistoryOption);
        options.addOption(manifestsHistoryOption);

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);
            if (cmd.hasOption(HELP_CHAR)) {
                printHelp(options);
                System.exit(0);
            }

            File appsFolder = getAppsFolder(cmd.getOptionValue(APPS_CHAR));
            String mFolder = cmd.getOptionValue(MANIFESTS_CHAR);
            File manifestFolder = getFile(mFolder == null ? DEFAULT_MANIFESTS_PATH : mFolder);

            // Update version uid ---------------------------------------------------------------------------------------------------
            if (cmd.hasOption(TIMESTAMP_CHAR)) {
                String newUID = cmd.getOptionValue(TIMESTAMP_CHAR);
                System.out.print(String.format("Updating the version files with the the version identifier '%s'... ", newUID));
                updateVersionFile(appsFolder, newUID);
                updateVersionFile(manifestFolder, newUID);
                System.out.println("finished!");
            }

            File historyAppFolder = getFile(getHistoryAppsFolder(cmd));
            File historyManifestFolder = getFile(getHistoryManifestFolder(cmd));

            // Handle apps folder ---------------------------------------------------------------------------------------------------
            System.out.print("Checking apps folder...");
            Optional<String> installedVersion = HistoryUtil.readVersion(new File(appsFolder, "version.txt").toPath());
            if (installedVersion.isPresent() == false) {
                error(options, "Installed folder doesn't contains a version file.", 1);
            }
            Optional<String> currentVersion = HistoryUtil.readVersion(new File(historyAppFolder, "current/version.txt").toPath());
            if (installedVersion.isPresent() == false) {
                error(options, "Current folder doesn't contains a version file.", 1);
            }
            HistoryUtil.handleVersions(historyAppFolder, appsFolder, installedVersion.get(), currentVersion);
            System.out.println(" Finished");

            // Handle manifests folder ---------------------------------------------------------------------------------------------------
            System.out.println("Checking manifests folder...");
            installedVersion = HistoryUtil.readVersion(new File(manifestFolder, "version.txt").toPath());
            if (installedVersion.isPresent() == false) {
                error(options, "Installed folder doesn't contains a version file.", 1);
            }
            currentVersion = HistoryUtil.readVersion(new File(historyManifestFolder, "current/version.txt").toPath());
            if (installedVersion.isPresent() == false) {
                error(options, "Current folder doesn't contains a version file.", 1);
            }
            HistoryUtil.handleVersions(historyManifestFolder, manifestFolder, installedVersion.get(), currentVersion);
            System.out.println(" finished");
            System.out.println("Finished checking all history folders");
        } catch (IOException | ParseException e) {
            error(Optional.of(e), options, "Error: " + e.getMessage(), 1);
        }
    }

    /**
     * Gets the apps folder
     *
     * @param appsPath The apps path
     * @return The apps folder
     * @throws IOException
     */
    private static File getAppsFolder(String appsPath) throws IOException {
        if (appsPath == null) {
            return new File(DEFAULT_APPS_PATH);
        }
        return appsPathMatcher.matcher(appsPath).matches() ? getFile(appsPath) : getFile(appsPath, Optional.of("apps"));
    }

    /**
     * Gets the path to the apps history folder
     *
     * @param cmd The {@link CommandLine}
     * @return The path to the apps history folder
     */
    private static String getHistoryAppsFolder(CommandLine cmd) {
        String appsPath = cmd.getOptionValue(HISTORY_APPSUITE);
        return appsPath == null ? DEFAULT_HISTORY_APPS_PATH : appsPath;
    }

    /**
     * Gets the path to the manifests history folder
     *
     * @param cmd The {@link CommandLine}
     * @return The path to the manifests history folder
     */
    private static String getHistoryManifestFolder(CommandLine cmd) {
        String appsPath = cmd.getOptionValue(HISTORY_MANIFESTS);
        return appsPath == null ? DEFAULT_HISTORY_MANIFESTS_PATH : appsPath;
    }

    /**
     * Prints the help text
     *
     * @param options The options
     */
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.setOptionComparator(null);
        formatter.printHelp("appsuiteui-history", CLT_DESC, options, null, true);
    }

    /**
     * Prints the error and the help text and exits the application with the given error code
     *
     * @param options The options
     * @param message The message to print
     * @param code The exit code
     */
    private static void error(Options options, String message, int code) {
        error(Optional.empty(), options, message, code);
    }

    /**
     * Prints the error and the help text and exits the application with the given error code
     *
     * @param e The exception
     * @param options The options
     * @param message The message to print
     * @param code The exit code
     */
    private static void error(Optional<Exception> e, Options options, String message, int code) {
        System.err.println();
        System.err.println(message);
        e.ifPresent((ex) -> ex.printStackTrace());
        printHelp(options);
        System.exit(code);
    }

    /**
     * Gets the file for the given path and performs an exists check
     *
     * @param path The path
     * @return The {@link File}
     * @throws IOException in case the file doesn't exists
     */
    private static File getFile(String path) throws IOException {
        return getFile(path, Optional.empty());
    }

    /**
     * Gets the file for the given path and performs an exists check
     *
     * @param path The path
     * @param subPath The optional sub path
     * @return The {@link File}
     * @throws IOException in case the file doesn't exists
     */
    private static File getFile(String path, Optional<String> subPath) throws IOException {
        File result = subPath.isPresent() ? new File(path, subPath.get()) : new File(path);
        if (result.exists() == false) {
            throw new IOException(String.format("Folder %s doesn't exist.", path));
        }
        return result;
    }

    /**
     * Updates the version uid
     *
     * E.g. if the given uid is <code>1234</code> then the version string <code>7.10.4-4321</code> would be replaced with <code>7.10.4-1234</code>
     *
     * @param folder The folder to check (either the apps or manifests history folder)
     * @param uid The new uid
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void updateVersionFile(File folder, String uid) throws IOException, FileNotFoundException {
        File file = new File(folder, "version.txt");
        Optional<String> version = HistoryUtil.readVersion(file.toPath());
        Matcher matcher = p.matcher(version.orElseThrow(() -> new FileNotFoundException("Can't find version file: " + file.toPath().toString())));
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String newVersion = prefix + uid;

            OutputStream out = null;
            Writer writer = null;
            try {
                out = new FileOutputStream(file);
                writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                writer.write(newVersion);
                writer.flush();
            } finally {
                close(writer, out);
            }
        }
    }

    /**
     * Safely closes specified {@link Closeable} instances.
     *
     * @param closeables The {@link Closeable} instances
     */
    private static void close(final Closeable... closeables) {
        if (null != closeables) {
            for (final Closeable toClose : closeables) {
                if (null != toClose) {
                    try {
                        toClose.close();
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
    }
}
