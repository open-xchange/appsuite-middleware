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

package com.openexchange.filestore.utils;

import java.io.File;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.configuration.ServerConfig;

/**
 * {@link TempFileHelper}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class TempFileHelper implements Reloadable {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TempFileHelper.class);
    }

    private static final TempFileHelper INSTANCE = new TempFileHelper();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static TempFileHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     *
     * @param tmpFile The file or directory to delete, can be {@code null}
     * @return {@code true} if the file or directory was deleted, otherwise {@code false}
     */
    public static boolean deleteQuietly(final File tmpFile) {
        return FileUtils.deleteQuietly(tmpFile);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private volatile File uploadDirectory;

    /**
     * Initializes a new {@link TempFileHelper}.
     */
    private TempFileHelper() {
        super();
    }

    private File uploadDirectory() {
        File tmp = uploadDirectory;
        if (null == tmp) {
            synchronized (TempFileHelper.class) {
                tmp = uploadDirectory;
                if (null == tmp) {
                    tmp = new File(ServerConfig.getProperty(ServerConfig.Property.UploadDirectory));
                    uploadDirectory = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(ServerConfig.Property.UploadDirectory.getPropertyName());
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        uploadDirectory = null;
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @return The optional abstract pathname denoting a newly-created empty file
     */
    public Optional<File> newTempFile() {
        return newTempFile("open-xchange-spoolfile-");
    }

    /**
     * Creates a new empty file. If this method returns successfully then it is guaranteed that:
     * <ol>
     * <li>The file denoted by the returned abstract pathname did not exist before this method was invoked, and
     * <li>Neither this method nor any of its variants will return the same abstract pathname again in the current invocation of the virtual
     * machine.
     * </ol>
     *
     * @param prefix The prefix to use for generated file
     * @return The optional abstract pathname denoting a newly-created empty file
     */
    public Optional<File> newTempFile(String prefix) {
        try {
            File tmpFile = File.createTempFile(null == prefix ? "open-xchange-spoolfile-" : prefix, ".tmp", uploadDirectory());
            tmpFile.deleteOnExit();
            return Optional.of(tmpFile);
        } catch (Exception e) {
            LoggerHolder.LOG.warn("Failed to create new temporary file", e);
            return Optional.empty();
        }
    }

}
