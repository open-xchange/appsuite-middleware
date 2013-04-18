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

package com.openexchange.mail.mime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link MimeType2ExtMap} - Maps MIME types to file extensions and vice versa.
 * <p>
 * This class looks in various places for MIME types file entries. When requests are made to look up MIME types or file extensions, it
 * searches MIME types files in the following order:
 * <ol>
 * <li>The file <i>.mime.types</i> in the user's home directory.</li>
 * <li>The file <i>&lt;java.home&gt;/lib/mime.types</i>.</li>
 * <li>The file or resources named <i>META-INF/mime.types</i>.</li>
 * <li>The file or resource named <i>META-INF/mimetypes.default</i>.</li>
 * <li>The file or resource denoted by property <i>MimeTypeFileName</i>.</li>
 * </ol>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeType2ExtMap {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MimeType2ExtMap.class));

    private static volatile Map<String, String> typeMap;

    private static volatile Map<String, List<String>> extMap;

    /**
     * No instance.
     */
    private MimeType2ExtMap() {
        super();
    }

    /**
     * Resets MIME type file map.
     */
    public static void reset() {
        if (null != typeMap) {
            synchronized (MimeType2ExtMap.class) {
                if (null == typeMap) {
                    return;
                }
                typeMap = null;
                extMap = null;
            }
        }
    }

    /**
     * Initializes MIME type file map.
     */
    public static void init() {
        if (null == typeMap) {
            synchronized (MimeType2ExtMap.class) {
                if (null != typeMap) {
                    return;
                }
                try {
                    typeMap = new HashMap<String, String>();
                    extMap = new HashMap<String, List<String>>();
                    final StringBuilder sb = new StringBuilder(128);
                    final boolean debugEnabled = LOG.isDebugEnabled();
                    {
                        final String homeDir = System.getProperty("user.home");
                        if (homeDir != null) {
                            final File file = new File(sb.append(homeDir).append(File.separatorChar).append(".mime.types").toString());
                            if (file.exists()) {
                                if (debugEnabled) {
                                    sb.setLength(0);
                                    LOG.debug(sb.append("Loading MIME type file \"").append(file.getPath()).append('"').toString());
                                }
                                loadInternal(file);
                            }
                        }
                    }
                    {
                        final String javaHome = System.getProperty("java.home");
                        if (javaHome != null) {
                            sb.setLength(0);
                            final File file =
                                new File(sb.append(javaHome).append(File.separatorChar).append("lib").append(File.separator).append(
                                    "mime.types").toString());
                            if (file.exists()) {
                                if (debugEnabled) {
                                    sb.setLength(0);
                                    LOG.debug(sb.append("Loading MIME type file \"").append(file.getPath()).append('"').toString());
                                }
                                loadInternal(file);
                            }
                        }
                    }
                    {
                        for (final Enumeration<URL> e = ClassLoader.getSystemResources("META-INF/mime.types"); e.hasMoreElements();) {
                            final URL url = e.nextElement();
                            if (debugEnabled) {
                                sb.setLength(0);
                                LOG.debug(sb.append("Loading MIME type file \"").append(url.getFile()).append('"').toString());
                            }
                            loadInternal(url);
                        }
                    }
                    {
                        for (final Enumeration<URL> e = ClassLoader.getSystemResources("META-INF/mimetypes.default"); e.hasMoreElements();) {
                            final URL url = e.nextElement();
                            if (debugEnabled) {
                                sb.setLength(0);
                                LOG.debug(sb.append("Loading MIME type file \"").append(url.getFile()).append('"').toString());
                            }
                            loadInternal(url);
                        }
                    }
                    {
                        String mimeTypesFileName = SystemConfig.getProperty(SystemConfig.Property.MimeTypeFileName);
                        if ((mimeTypesFileName != null) && ((mimeTypesFileName = mimeTypesFileName.trim()).length() > 0)) {
                            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                            final File file = service.getFileByName(mimeTypesFileName);
                            if (file.exists()) {
                                if (debugEnabled) {
                                    sb.setLength(0);
                                    LOG.debug(sb.append("Loading MIME type file \"").append(file.getPath()).append('"').toString());
                                }
                                loadInternal(file);
                            }
                        }

                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("MIMEType2ExtMap successfully initialized");
                    }
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Gets the MIME type associated with given file.
     *
     * @param file The file
     * @return The MIME type associated with given file or <code>application/octet-stream</code> if none found
     */
    public static String getContentType(final File file) {
        return getContentType(file.getName());
    }

    /**
     * Gets the MIME type associated with given file name.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @return The MIME type associated with given file name or <code>application/octet-stream</code> if none found
     */
    public static String getContentType(final String fileName) {
        init();
        if (null == fileName) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        final int pos = fileName.lastIndexOf('.');
        if (pos < 0) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        final String s1 = fileName.substring(pos + 1);
        if (s1.length() == 0) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        final String type = typeMap.get(s1.toLowerCase(Locale.ENGLISH));
        if (null == type) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        return type;
    }

    /**
     * Gets the MIME type associated with given file extension.
     *
     * @param extension The file extension; e.g. <code>"txt"</code>
     * @return The MIME type associated with given file extension or <code>application/octet-stream</code> if none found
     */
    public static String getContentTypeByExtension(final String extension) {
        init();
        if (null == extension || 0 == extension.length()) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        final String type = typeMap.get(extension.toLowerCase(Locale.ENGLISH));
        if (null == type) {
            return MimeTypes.MIME_APPL_OCTET;
        }
        return type;
    }

    private static final List<String> DEFAULT_EXT = Collections.unmodifiableList(Arrays.asList("dat"));

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @return The file extension for given MIME type or <code>dat</code> if none found
     */
    public static List<String> getFileExtensions(String mimeType) {
        init();
        if (!extMap.containsKey(mimeType.toLowerCase(Locale.ENGLISH))) {
            return DEFAULT_EXT;
        }
        final List<String> list = extMap.get(mimeType);
        return null == list ? DEFAULT_EXT : Collections.unmodifiableList(list);
    }

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @return The file extension for given MIME type or <code>dat</code> if none found
     */
    public static String getFileExtension(String mimeType) {
        init();
        if (!extMap.containsKey(mimeType.toLowerCase(Locale.ENGLISH))) {
            return "dat";
        }
        final List<String> list = extMap.get(mimeType);
        return null == list || list.isEmpty() ? "dat" : list.get(0);
    }

    /**
     * Loads the MIME type file specified through <code>fileStr</code>.
     *
     * @param fileStr The MIME type file to load
     */
    public static void load(final String fileStr) {
        init();
        load(new File(fileStr));
    }

    /**
     * Loads the MIME type file specified through given file.
     *
     * @param file The MIME type file to load
     */
    public static void load(final File file) {
        init();
        loadInternal(file);
    }

    /**
     * Loads the MIME type file specified through given file.
     *
     * @param file The MIME type file to load
     */
    private static void loadInternal(final File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), com.openexchange.java.Charsets.ISO_8859_1));
            parse(reader);
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Loads the MIME type file specified through given URL.
     *
     * @param url The URL to a MIME type file
     */
    private static void loadInternal(final URL url) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream(), com.openexchange.java.Charsets.ISO_8859_1));
            parse(reader);
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        } catch (final FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private static void parse(final BufferedReader reader) throws IOException {
        String line = null;
        final StringBuilder strBuilder = new StringBuilder(64);
        while ((line = reader.readLine()) != null) {
            final int i = strBuilder.length();
            strBuilder.append(line);
            if ((i > 0) && (strBuilder.charAt(i - 1) == '\\')) {
                strBuilder.delete(0, i - 1);
            } else {
                parseEntry(strBuilder.toString().trim());
                strBuilder.setLength(0);
            }
        }
        if (strBuilder.length() > 0) {
            parseEntry(strBuilder.toString().trim());
        }
    }

    private static void parseEntry(final String entry) {
        if (entry.length() == 0) {
            return;
        } else if (entry.charAt(0) == '#') {
            return;
        }
        final Map<String, List<String>> extMap = MimeType2ExtMap.extMap;
        final Map<String, String> typeMap = MimeType2ExtMap.typeMap;
        if (entry.indexOf('=') > 0) {
            final MimeTypeFileLineParser parser = new MimeTypeFileLineParser(entry);
            final String type = parser.getType();
            final List<String> exts = parser.getExtensions();
            if ((type != null) && (exts != null)) {
                for (final String ext : exts) {
                    typeMap.put(ext, type);
                }
                if (extMap.containsKey(type)) {
                    extMap.get(type).addAll(exts);
                } else {
                    extMap.put(type, exts);
                }
            }
        } else {
            final String[] tokens = entry.split("[ \t\n\r\f]+");
            if (tokens.length > 1) {
                final String type = tokens[0].toLowerCase(Locale.ENGLISH);
                final List<String> set = new ArrayList<String>();
                for (int i = 1; i < tokens.length; i++) {
                    final String ext = tokens[i].toLowerCase(Locale.ENGLISH);
                    set.add(ext);
                    typeMap.put(ext, type);
                }
                if (extMap.containsKey(type)) {
                    extMap.get(type).addAll(set);
                } else {
                    extMap.put(type, set);
                }
            }
        }
    }

}
