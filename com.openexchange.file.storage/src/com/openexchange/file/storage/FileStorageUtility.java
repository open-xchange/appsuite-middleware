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

package com.openexchange.file.storage;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.internal.FileStorageConfigReloadable;
import com.openexchange.file.storage.osgi.Services;
import com.openexchange.java.Strings;


/**
 * {@link FileStorageUtility} - Utility class for file storage module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorageUtility {

    private static final Pattern IS_NUMBERED_WITH_EXTENSION = Pattern.compile("\\(\\d+\\)\\.");
    private static final Pattern IS_NUMBERED = Pattern.compile("\\(\\d+\\)$");

    /**
     * Initializes a new {@link FileStorageUtility}.
     */
    private FileStorageUtility() {
        super();
    }

    /**
     * Generates the ETag for given file meta data.
     *
     * @param fileMetadata The file meta data
     * @return The Etag
     */
    public static String getETagFor(final File fileMetadata) {
        return getETagFor(fileMetadata.getId(), fileMetadata.getVersion(), fileMetadata.getLastModified());
    }

    /**
     * Generates the ETag for given file meta data.
     *
     * @param id The file identifier
     * @param version The optional version
     * @param lastModified
     * @return The Etag
     */
    public static String getETagFor(final String id, final String version, Date lastModified) {
        final StringBuilder sb = new StringBuilder("http://www.open-xchange.com/infostore");
        if (null != id) {
            sb.append('/').append(id);
        }
        if (null != version) {
            sb.append('/').append(version);
        }

        if (null != lastModified) {
            sb.append('/').append(lastModified.getTime());
        }
        return sb.toString();
    }

    /**
     * Checks given file's URL string for syntactical correctness.
     *
     * @param file The file whose URL to cehck
     * @throws OXException If URL string is invalid
     */
    public static void checkUrl(final File file) throws OXException {
        checkUrl(file.getURL());
    }

    /**
     * Checks given URL string for syntactical correctness.
     *
     * @param sUrl The URL string
     * @throws OXException If URL string is invalid
     */
    public static void checkUrl(final String sUrl) throws OXException {
        if (Strings.isEmpty(sUrl)) {
            // Nothing to check
            return;
        }
        try {
            final java.net.URL url = new java.net.URL(sUrl);
            final String protocol = url.getProtocol();
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new MalformedURLException("Only http & https protocols supported.");
            }
        } catch (final MalformedURLException e) {
            throw FileStorageExceptionCodes.INVALID_URL.create(e, sUrl, e.getMessage());
        }
    }

    /**
     * Tries to cast a concrete {@link File} implementation to the class or interface represented by the supplied {@link Class}
     * object recursively, also considering possible underlying delegates.
     *
     * @param file The file implementation to launder
     * @param clazz The target interface or class
     * @return The target class implementation, or <code>null</code> if no suitable type found
     */
    public static <T extends File> T launderDelegate(File file, Class<T> clazz) {
        if (null == file) {
            return null;
        }
        if (clazz.isInstance(file)) {
            return clazz.cast(file);
        }
        if (DelegatingFile.class.isInstance(file)) {
            DelegatingFile delegatingFile = (DelegatingFile) file;
            File fileDelegate = delegatingFile.getDelegate();
            if (null != fileDelegate && clazz.isInstance(fileDelegate)) {
                return clazz.cast(fileDelegate);
            }
            return launderDelegate(fileDelegate, clazz);
        }
        return null;
    }

    /**
     * Appends or modifies a counter in the 'name' part of the supplied filename. For example, passing the filename
     * <code>test.txt</code> and a counter of <code>2</code> will result in the string <code>test (2).txt</code>, while the filename
     * <code>test (1).txt</code> would be changed to <code>test (2).txt</code>.
     *
     * @param filename The filename to enhance
     * @param counter The counter to append
     * @return The enhanced filename
     */
    public static String enhance(String filename, int counter) {
        if (null == filename) {
            return filename;
        }
        String counterString = '(' + String.valueOf(counter) + ')';
        Matcher matcher = IS_NUMBERED_WITH_EXTENSION.matcher(filename);
        if (matcher.find()) {
            return new StringBuilder(filename).replace(matcher.start(), matcher.end() - 1, counterString).toString();
        }
        matcher = IS_NUMBERED.matcher(filename);
        if (matcher.find()) {
            return new StringBuilder(filename).replace(matcher.start(), matcher.end(), counterString).toString();
        }

        int index = -1;
        //See Bug 40142
        if (filename.endsWith(".pgp")) {
            index = filename.substring(0, filename.length() - 4).lastIndexOf('.');
            if (index != -1 && index != filename.lastIndexOf('.')) {
                return new StringBuilder(filename).insert(index, ' ' + counterString).toString();
            }
        }
        index = filename.lastIndexOf('.');

        if (0 >= index) {
            index = filename.length();
        }

        return new StringBuilder(filename).insert(index, ' ' + counterString).toString();
    }

    private static volatile Integer numberOfPregeneratedPreviews;

    /**
     * Gets the number of previews that are supposed to be generated when listing files in a folder.
     *
     * @return The number of previews that are supposed to be generated
     */
    public static int getNumberOfPregeneratedPreviews() {
        Integer tmp = numberOfPregeneratedPreviews;
        if (null == tmp) {
            synchronized (FileStorageUtility.class) {
                tmp = numberOfPregeneratedPreviews;
                if (null == tmp) {
                    int defaultNumberOfPregeneratedPreviews = 20;
                    ConfigurationService service = Services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        return defaultNumberOfPregeneratedPreviews;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.file.storage.numberOfPregeneratedPreviews", defaultNumberOfPregeneratedPreviews));
                    numberOfPregeneratedPreviews = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    private static volatile Long threshold;

    /**
     * Gets the size threshold for ZIP archives
     *
     * @return The size threshold
     */
    public static long threshold() {
        Long tmp = threshold;
        if (null == tmp) {
            synchronized (FileStorageUtility.class) {
                tmp = threshold;
                if (null == tmp) {
                    long defaultThreshold = 1073741824;
                    ConfigurationService service = Services.getOptionalService(ConfigurationService.class);
                    if (null == service) {
                        return defaultThreshold;
                    }
                    String property = service.getProperty("com.openexchange.file.storage.zipFolderThreshold");
                    tmp = null == property ? Long.valueOf(defaultThreshold) : Long.valueOf(property.trim());
                    threshold = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    private static volatile Integer compressionLevel;

    /**
     * Gets the configured value for "com.openexchange.infostore.zipDocumentsCompressionLevel".
     *
     * @return The configured compression level
     * @throws OXException
     */
    public static int getZipDocumentsCompressionLevel() throws OXException {
        Integer tmp = compressionLevel;
        if (null == tmp) {
            synchronized (FileStorageUtility.class) {
                tmp = compressionLevel;
                if (null == tmp) {
                    ConfigurationService configService = Services.getOptionalService(ConfigurationService.class);
                    if (null == configService) {
                        return Deflater.DEFAULT_COMPRESSION;
                    }
                    int level = configService.getIntProperty("com.openexchange.infostore.zipDocumentsCompressionLevel", Deflater.DEFAULT_COMPRESSION);
                    if (level < Deflater.DEFAULT_COMPRESSION || level > Deflater.BEST_COMPRESSION) {
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("Invalid configuration for property 'com.openexchange.infostore.zipDocumentsCompressionLevel'");
                    }
                    tmp = Integer.valueOf(level);
                    compressionLevel = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    static {
        FileStorageConfigReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                numberOfPregeneratedPreviews = null;
                threshold = null;
                compressionLevel = null;
            }

            @Override
            public Map<String, String[]> getConfigFileNames() {
                return null;
            }
        });
    }

}
