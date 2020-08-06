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

package com.openexchange.appsuite.history.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserPrincipal;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HistoryUtil} provides util methods for copying files to the history folders
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class HistoryUtil {

    private static final String CURRENT_FOLDER_NAME = "current";
    protected static final String PREVIOUS_FOLDER_NAME = "previous";
    protected static final String OX_USER_NAME = "open-xchange"; // Enter your own system user name here for testing purposes
    private static final Logger LOG = LoggerFactory.getLogger(HistoryUtil.class);

    /**
     * Perform operations required for the given situation
     *
     * @param historyFolder The root history folder
     * @param installedVersion The installed version
     * @param currentVersion Optional the current installed version
     * @throws IOException in case a operation fails
     */
    public static void handleVersions(File historyFolder, File installedFolder, String installedVersion, Optional<String> currentVersion) throws IOException {
        if (currentVersion.isPresent() == false) {
            copyToCurrent(historyFolder, installedFolder);
            // copy from installed to current
        } else if (installedVersion.equals(currentVersion.get()) == false) {
            // Move current to previous
            moveToPrevious(historyFolder);
            // copy installed to current
            copyToCurrent(historyFolder, installedFolder);
        }
    }

    /**
     * Reads the version from the given path if available
     *
     * @param path The path to the version file
     * @return The version if available
     * @throws IOException
     */
    public static Optional<String> readVersion(Path path) throws IOException {
        return path.toFile().exists() ? Files.lines(path).findFirst() : Optional.empty();
    }

    /**
     * Copies the files from the installed directory to the "current" folder below the history folder
     *
     * @param history The root history folder
     * @param installed The installed folder containing the installed files
     * @throws IOException in case the files couldn't be copied
     */
    private static void copyToCurrent(File history, File installed) throws IOException {
        File destFolder = new File(history, "/current");
        LOG.info("Current files missing or have been rotated. Copying installed files to history folder ({})", destFolder.getAbsolutePath());
        if (destFolder.exists()) {
            LOG.debug("Current files already present. Starting to delete them before copying installed files ({}).", destFolder.getAbsolutePath());
            FileUtils.deleteDirectory(destFolder);
        }
        boolean failed = !destFolder.mkdirs();
        if (failed) {
            LOG.error("Unable to create current folder in folder {}", history.getAbsolutePath());
            throw new IOException("Unable to create current folder");
        }

        destFolder.setWritable(true);
        LOG.debug("Starting to copy installed files to current (targte: {}).", destFolder.getAbsolutePath());
        @SuppressWarnings("null") CopyFileVisitor visitor = new CopyFileVisitor(installed.toPath(), destFolder.toPath());
        Files.walkFileTree(installed.toPath(), visitor);
        adjustPathOwner(destFolder.toPath());
        LOG.info("Files copied successfully (target: {}).", destFolder.getAbsolutePath());
    }

    /**
     * Deletes the previous folder below the history folder and renames the current folder to previous
     *
     * @param history The history folder
     * @throws IOException
     */
    private static void moveToPrevious(File history) throws IOException {
        File from = new File(history, CURRENT_FOLDER_NAME);
        File to = new File(history, PREVIOUS_FOLDER_NAME);
        LOG.info("Rotating current files to previous. Moving current files to history folder ({})", to.getAbsolutePath());
        if (to.exists()) {
            LOG.info("Previous files already present. Starting to delete them before moving current files (target: {}).", to.getAbsolutePath());
            FileUtils.deleteDirectory(to);
            LOG.info("Previous files deleted successfully (target: {}).", to.getAbsolutePath());
        }
        LOG.debug("Starting to move current files to previous (targte: {}).", to.getAbsolutePath());

        to.mkdirs();

        @SuppressWarnings("null") CopyFileVisitor visitor = new CopyFileVisitor(from.toPath(), to.toPath());
        Files.walkFileTree(from.toPath(), visitor);
        FileUtils.deleteDirectory(from);
        adjustPathOwner(to.toPath());
        LOG.info("Files moved successfully (target: {}).", to.getAbsolutePath());
    }

    /**
     * Adjust the owner of the folder to the open-xchange user if necessary.
     *
     * @param path The folder path to adjust
     * @throws IOException
     */
    protected static void adjustPathOwner(Path path) throws IOException {
        UserPrincipal owner = Files.getOwner(path);
        if (owner.getName().toLowerCase().equals(HistoryUtil.OX_USER_NAME) == false) {
            UserPrincipal oxUser = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(HistoryUtil.OX_USER_NAME);
            Files.setOwner(path, oxUser);
        }
    }

}
