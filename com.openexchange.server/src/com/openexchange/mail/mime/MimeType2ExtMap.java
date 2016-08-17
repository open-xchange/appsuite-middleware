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

package com.openexchange.mail.mime;

import static com.openexchange.java.Strings.toLowerCase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mime.MimeTypeMap;
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
 * <p>
 * Available as OSGi service through {@link MimeTypeMap} interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeType2ExtMap {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MimeType2ExtMap.class);

    /** The file extension to MIME type mapping; e.g. <code>"txt"</code> --&gt; <code>"text/plain"</code> */
    private static volatile ConcurrentMap<String, MimeTypeAssociation> typeMap;

    /** The MIME type to file extensions mapping; e.g. <code>"text/html"</code> --&gt; <code>"html", "htm"</code> */
    private static volatile ConcurrentMap<String, List<String>> extMap;

    /**
     * No instance.
     */
    private MimeType2ExtMap() {
        super();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                reset();
                init();
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("MimeTypeFileName");
            }
        });
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
                    ConcurrentMap<String, MimeTypeAssociation> typeMapping = new ConcurrentHashMap<String, MimeTypeAssociation>(1024, 0.9f, 1);
                    typeMap = typeMapping;
                    ConcurrentMap<String, List<String>> extMapping = new ConcurrentHashMap<String, List<String>>(1024, 0.9f, 1);
                    extMap = extMapping;

                    StringBuilder sb = new StringBuilder(128);
                    boolean debugEnabled = LOG.isDebugEnabled();
                    {
                        String homeDir = System.getProperty("user.home");
                        if (homeDir != null) {
                            File file = new File(sb.append(homeDir).append(File.separatorChar).append(".mime.types").toString());
                            if (file.exists()) {
                                if (debugEnabled) {
                                    sb.setLength(0);
                                    LOG.debug(sb.append("Loading MIME type file \"").append(file.getPath()).append('"').toString());
                                }
                                loadInternal(file, typeMapping, extMapping);
                            }
                        }
                    }
                    {
                        String javaHome = System.getProperty("java.home");
                        if (javaHome != null) {
                            sb.setLength(0);
                            File file =
                                new File(sb.append(javaHome).append(File.separatorChar).append("lib").append(File.separator).append("mime.types").toString());
                            if (file.exists()) {
                                if (debugEnabled) {
                                    sb.setLength(0);
                                    LOG.debug(sb.append("Loading MIME type file \"").append(file.getPath()).append('"').toString());
                                }
                                loadInternal(file, typeMapping, extMapping);
                            }
                        }
                    }
                    {
                        for (Enumeration<URL> e = ClassLoader.getSystemResources("META-INF/mime.types"); e.hasMoreElements();) {
                            URL url = e.nextElement();
                            if (debugEnabled) {
                                sb.setLength(0);
                                LOG.debug(sb.append("Loading MIME type file \"").append(url.getFile()).append('"').toString());
                            }
                            loadInternal(url, typeMapping, extMapping);
                        }
                    }
                    {
                        for (Enumeration<URL> e = ClassLoader.getSystemResources("META-INF/mimetypes.default"); e.hasMoreElements();) {
                            URL url = e.nextElement();
                            if (debugEnabled) {
                                sb.setLength(0);
                                LOG.debug(sb.append("Loading MIME type file \"").append(url.getFile()).append('"').toString());
                            }
                            loadInternal(url, typeMapping, extMapping);
                        }
                    }
                    {
                        String mimeTypesFileName = SystemConfig.getProperty(SystemConfig.Property.MimeTypeFileName);
                        if ((mimeTypesFileName != null) && ((mimeTypesFileName = mimeTypesFileName.trim()).length() > 0)) {
                            ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                            File file = null == service ? null : service.getFileByName(mimeTypesFileName);
                            if (null != file && file.exists()) {
                                if (debugEnabled) {
                                    sb.setLength(0);
                                    LOG.debug(sb.append("Loading MIME type file \"").append(file.getPath()).append('"').toString());
                                }
                                loadInternal(file, typeMapping, extMapping);
                            }
                        }

                    }
                    LOG.debug("MIMEType2ExtMap successfully initialized");
                } catch (IOException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    /**
     * Adds specified MIME type to file type mapping; e.g <code>"image/png"</code> -&gt; <code>"png"</code>.
     *
     * @param mimeType The MIME type
     * @param fileExtension The file extension
     */
    public static void addMimeType(String mimeType, String fileExtension) {
        addMimeType(mimeType, Collections.singletonList(fileExtension));
    }

    /**
     * Adds specified MIME type to file type mapping; e.g <code>"image/jpeg"</code> -&gt; [ <code>"jpeg"</code>, <code>"jpg"</code>, <code>"jpe"</code> ].
     *
     * @param mimeType The MIME type
     * @param fileExtensions The file extensions
     */
    public static void addMimeType(String mimeType, List<String> fileExtensions) {
        init();
        ConcurrentMap<String, MimeTypeAssociation> tm = typeMap;
        for (String ext : fileExtensions) {
            addTypeMapping(ext, mimeType, tm);
        }

        ConcurrentMap<String, List<String>> em = extMap;
        List<String> list = em.get(mimeType);
        if (null == list) {
            List<String> nl = new CopyOnWriteArrayList<String>();
            list = em.putIfAbsent(mimeType, nl);
            if (null == list) {
                list = nl;
            }
        }
        list.add(mimeType);
    }

    private static final String MIME_APPL_OCTET = MimeTypes.MIME_APPL_OCTET;

    /**
     * Gets the MIME type associated with given file.
     *
     * @param file The file
     * @return The MIME type associated with given file or <code>"application/octet-stream"</code> if none found
     */
    public static String getContentType(File file) {
        return getContentType(file.getName(), MIME_APPL_OCTET);
    }

    /**
     * Gets the MIME type associated with given file.
     *
     * @param file The file
     * @param fallBack The fall-back value to return in case file extension is unknown
     * @return The MIME type associated with given file or <code>"application/octet-stream"</code> if none found
     */
    public static String getContentType(File file, String fallBack) {
        return getContentType(file.getName(), fallBack);
    }

    /**
     * Gets the MIME type associated with given file name.
     * <p>
     * This is a convenience method that invokes {@link #getContentType(String, String)} with latter argument set to <code>"application/octet-stream"</code>.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @return The MIME type associated with given file name or <code>"application/octet-stream"</code> if none found
     * @see #getContentType(String, String)
     */
    public static String getContentType(String fileName) {
        return getContentType(fileName, MIME_APPL_OCTET);
    }

    /**
     * Gets the MIME type associated with given file name.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @param fallBack The fall-back value to return in case file extension is unknown
     * @return The MIME type associated with given file name or <code>fallBack</code> if none found
     */
    public static String getContentType(String fileName, String fallBack) {
        init();
        if (Strings.isEmpty(fileName)) {
            return fallBack;
        }
        String fn = Strings.unquote(fileName);
        int pos = fn.lastIndexOf('.');
        if (pos < 0) {
            return fallBack;
        }
        String s1 = fn.substring(pos + 1);
        if (s1.length() == 0) {
            return fallBack;
        }

        MimeTypeAssociation mta = typeMap.get(toLowerCase(s1));
        if (null == mta) {
            return fallBack;
        }
        String type = mta.get();
        return null == type ? fallBack : type;
    }

    private static final List<String> DEFAULT_MIME_TYPES = Collections.singletonList(MIME_APPL_OCTET);

    /**
     * Gets the MIME type associated with given file name.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @return The MIME types associated with given file name or a singleton list with <code>"application/octet-stream"</code>
     */
    public static List<String> getContentTypes(String fileName) {
        return getContentTypes(fileName, DEFAULT_MIME_TYPES);
    }

    /**
     * Gets the MIME type associated with given file name.
     *
     * @param fileName The file name; e.g. <code>"file.html"</code>
     * @param fallBack The fall-back list to return
     * @return The MIME types associated with given file name or <code>fallBack</code> argument
     */
    public static List<String> getContentTypes(String fileName, List<String> fallBack) {
        init();
        if (Strings.isEmpty(fileName)) {
            return fallBack;
        }
        String fn = Strings.unquote(fileName);
        int pos = fn.lastIndexOf('.');
        if (pos < 0) {
            return fallBack;
        }
        String s1 = fn.substring(pos + 1);
        if (s1.length() == 0) {
            return fallBack;
        }

        MimeTypeAssociation mta = typeMap.get(toLowerCase(s1));
        if (null == mta) {
            return fallBack;
        }

        List<String> all = mta.getAll();
        return null == all || all.isEmpty() ? fallBack : all;
    }

    /**
     * Gets the MIME type associated with given file extension.
     *
     * <p>
     * This is a convenience method that invokes {@link #getContentTypeByExtension(String, String)} with latter argument set to <code>"application/octet-stream"</code>.
     *
     * @param extension The file extension; e.g. <code>"txt"</code>
     * @param fallBack The fall-back value to return in case file extension is unknown
     * @return The MIME type associated with given file extension or <code>application/octet-stream</code> if none found
     * @see #getContentTypeByExtension(String, String)
     */
    public static String getContentTypeByExtension(String extension) {
        return getContentTypeByExtension(extension, MIME_APPL_OCTET);
    }

    /**
     * Gets the MIME type associated with given file extension.
     *
     * @param extension The file extension; e.g. <code>"txt"</code>
     * @param fallBack The fall-back value to return in case file extension is unknown
     * @return The MIME type associated with given file extension or <code>fallBack</code> if none found
     */
    public static String getContentTypeByExtension(String extension, String fallBack) {
        init();
        if (Strings.isEmpty(extension)) {
            return fallBack;
        }

        MimeTypeAssociation mta = typeMap.get(toLowerCase(extension));
        if (null == mta) {
            return fallBack;
        }
        String type = mta.get();
        return null == type ? fallBack : type;
    }

    private static final String DEFAULT_EXT = "dat";

    private static final List<String> DEFAULT_EXTENSIONS = Collections.singletonList(DEFAULT_EXT);

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @return The file extension for given MIME type or a singleton list with <code>dat</code> if none found
     */
    public static List<String> getFileExtensions(String mimeType) {
        init();
        if (Strings.isEmpty(mimeType)) {
            return DEFAULT_EXTENSIONS;
        }
        if (!extMap.containsKey(toLowerCase(mimeType))) {
            return DEFAULT_EXTENSIONS;
        }
        List<String> list = extMap.get(mimeType);
        return null == list ? DEFAULT_EXTENSIONS : Collections.unmodifiableList(list);
    }

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @return The file extension for given MIME type or <code>dat</code> if none found
     */
    public static String getFileExtension(String mimeType) {
        return getFileExtension(mimeType, DEFAULT_EXT);
    }

    /**
     * Gets the file extension for given MIME type.
     *
     * @param mimeType The MIME type
     * @param defaultExt The default extension to return
     * @return The file extension for given MIME type or <code>defaultExt</code> if none found
     */
    public static String getFileExtension(String mimeType, String defaultExt) {
        init();
        if (Strings.isEmpty(mimeType)) {
            return defaultExt;
        }
        if (!extMap.containsKey(toLowerCase(mimeType))) {
            return defaultExt;
        }
        List<String> list = extMap.get(mimeType);
        return null == list || list.isEmpty() ? defaultExt : list.get(0);
    }

    /**
     * Loads the MIME type file specified through <code>fileStr</code>.
     *
     * @param fileStr The MIME type file to load
     * @param extMapping The type mapping to fill
     * @param typeMapping The file extension mapping to fill
     */
    public static void load(String fileStr, ConcurrentMap<String, MimeTypeAssociation> typeMapping, ConcurrentMap<String, List<String>> extMapping) {
        init();
        load(new File(fileStr), typeMapping, extMapping);
    }

    /**
     * Loads the MIME type file specified through given file.
     *
     * @param file The MIME type file to load
     * @param extMapping The type mapping to fill
     * @param typeMapping The file extension mapping to fill
     */
    public static void load(File file, ConcurrentMap<String, MimeTypeAssociation> typeMapping, ConcurrentMap<String, List<String>> extMapping) {
        init();
        loadInternal(file, typeMapping, extMapping);
    }

    /**
     * Loads the MIME type file specified through given file.
     *
     * @param file The MIME type file to load
     * @param extMapping The type mapping to fill
     * @param typeMapping The file extension mapping to fill
     */
    private static void loadInternal(File file, ConcurrentMap<String, MimeTypeAssociation> typeMapping, ConcurrentMap<String, List<String>> extMapping) {
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(stream, com.openexchange.java.Charsets.ISO_8859_1));
            parse(reader, typeMapping, extMapping);
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            Streams.close(reader, stream);
        }
    }

    /**
     * Loads the MIME type file specified through given URL.
     *
     * @param url The URL to a MIME type file
     * @param extMapping The type mapping to fill
     * @param typeMapping The file extension mapping to fill
     */
    private static void loadInternal(URL url, ConcurrentMap<String, MimeTypeAssociation> typeMapping, ConcurrentMap<String, List<String>> extMapping) {
        InputStream stream = null;
        BufferedReader reader = null;
        try {
            stream = url.openStream();
            reader = new BufferedReader(new InputStreamReader(stream, com.openexchange.java.Charsets.ISO_8859_1));
            parse(reader, typeMapping, extMapping);
        } catch (Exception e) {
            LOG.error("", e);
        } finally {
            Streams.close(reader, stream);
        }
    }

    private static void parse(BufferedReader reader, ConcurrentMap<String, MimeTypeAssociation> typeMapping, ConcurrentMap<String, List<String>> extMapping) throws IOException {
        String line = null;
        StringBuilder strBuilder = new StringBuilder(64);
        while ((line = reader.readLine()) != null) {
            int i = strBuilder.length();
            strBuilder.append(line);
            if ((i > 0) && (strBuilder.charAt(i - 1) == '\\')) {
                strBuilder.delete(0, i - 1);
            } else {
                parseEntry(strBuilder.toString().trim(), typeMapping, extMapping);
                strBuilder.setLength(0);
            }
        }
        if (strBuilder.length() > 0) {
            parseEntry(strBuilder.toString().trim(), typeMapping, extMapping);
        }
    }

    private static void parseEntry(String entry, ConcurrentMap<String, MimeTypeAssociation> typeMapping, ConcurrentMap<String, List<String>> extMapping) {
        if (entry.length() == 0) {
            return;
        }
        if (entry.charAt(0) == '#') {
            return;
        }

        if (entry.indexOf('=') > 0) {
            MimeTypeFileLineParser parser = new MimeTypeFileLineParser(entry);
            String type = parser.getType();
             List<String> exts = parser.getExtensions();
            if ((type != null) && (exts != null)) {
                for (String ext : exts) {
                    addTypeMapping(ext, type, typeMapping);
                }
                if (extMapping.containsKey(type)) {
                    extMapping.get(type).addAll(exts);
                } else {
                    extMapping.put(type, exts);
                }
            }
        } else {
            String[] tokens = entry.split("[ \t\n\r\f]+");
            if (tokens.length > 1) {
                String type = toLowerCase(tokens[0]);
                List<String> set = new CopyOnWriteArrayList<String>();
                for (int i = 1; i < tokens.length; i++) {
                    String ext = toLowerCase(tokens[i]);
                    set.add(ext);
                    addTypeMapping(ext, type, typeMapping);
                }
                if (extMapping.containsKey(type)) {
                    extMapping.get(type).addAll(set);
                } else {
                    extMapping.put(type, set);
                }
            }
        }
    }

    private static void addTypeMapping(String ext, String mimeType, ConcurrentMap<String, MimeTypeAssociation> typeMapping) {
        MimeTypeAssociation mta = typeMapping.get(ext);
        if (null == mta) {
            MimeTypeAssociation nmta = new MimeTypeAssociation();
            mta = typeMapping.putIfAbsent(ext, nmta);
            if (null == mta) {
                mta = nmta;
            }
        }
        mta.add(mimeType);
    }

    // -------------------------------------------------- Helper classes --------------------------------------------------------- //

    private static final class MimeTypeAssociation {

        private final SortableConcurrentList<ComparableMimeTypeEntry> mimeTypeEntries;

        MimeTypeAssociation() {
            super();
            mimeTypeEntries = new SortableConcurrentList<ComparableMimeTypeEntry>();
        }

        void add(String mimeType) {
            if (null != mimeType) {
                mimeTypeEntries.addAndSort(new ComparableMimeTypeEntry(mimeType));
            }
        }

        String get() {
            List<ComparableMimeTypeEntry> snapshot = mimeTypeEntries.getSnapshot();
            ComparableMimeTypeEntry e =  snapshot.isEmpty() ? null : snapshot.get(0);
            return null == e ? null : e.mimeType;
        }

        List<String> getAll() {
            List<ComparableMimeTypeEntry> snapshot = mimeTypeEntries.getSnapshot();
            List<String> types = new ArrayList<String>(snapshot.size());
            for (ComparableMimeTypeEntry e : snapshot) {
                types.add(e.mimeType);
            }
            return types;
        }

        @Override
        public String toString() {
            return mimeTypeEntries.toString();
        }
    }

    private static final class ComparableMimeTypeEntry implements Comparable<ComparableMimeTypeEntry> {

        final String mimeType;

        ComparableMimeTypeEntry(String mimeType) {
            super();
            this.mimeType = mimeType;
        }

        @Override
        public int compareTo(ComparableMimeTypeEntry o) {
            int len1 = mimeType.length();
            int len2 = o.mimeType.length();
            return (len1 < len2) ? -1 : ((len1 == len2) ? 0 : 1);
        }

        @Override
        public String toString() {
            return mimeType;
        }
    }

}
