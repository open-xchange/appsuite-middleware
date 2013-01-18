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

package com.openexchange.templating.impl;

import static com.openexchange.templating.TemplateErrorMessage.IOException;
import static com.openexchange.templating.TemplateErrorMessage.TemplateNotFound;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplate.TemplateLevel;
import com.openexchange.templating.OXTemplateExceptionHandler;
import com.openexchange.templating.OXTemplateImpl;
import com.openexchange.templating.TemplateErrorMessage;
import com.openexchange.templating.TemplateExceptionHandlerWrapper;
import com.openexchange.templating.TemplateService;
import com.openexchange.templating.TemplatingHelper;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * This class is not thread-safe at all.
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class TemplateServiceImpl implements TemplateService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(TemplateServiceImpl.class);

    /** The property for file path to templates */
    public static final String PATH_PROPERTY = "com.openexchange.templating.path";

    /** The property for whether user templates are enabled */
    public static final String USER_TEMPLATING_PROPERTY = "com.openexchange.templating.usertemplating";

    /** The map for cached tags */
    private static final Map<String, Map<String, Set<String>>> CACHED_TAGS = new ConcurrentHashMap<String, Map<String, Set<String>>>();

    private final ConfigurationService config;
    private OXFolderHelper folders;
    private OXInfostoreHelper infostore;
    private TemplateExceptionHandler exceptionHandler;
    private final String defaultTemplatePath;

    /**
     * Initializes a new {@link TemplateServiceImpl}.
     * 
     * @param config The configuration service
     */
    public TemplateServiceImpl(final ConfigurationService config) {
        this.config = config;
        exceptionHandler = null;
        defaultTemplatePath = config.getProperty(PATH_PROPERTY);
    }

    private void checkTemplatePath(final String templatePath) throws OXException {
        try {
            if (isEmpty(templatePath)) {
                return;
            }
            final String defaultTemplatePath = this.defaultTemplatePath;
            if (defaultTemplatePath == null) {
                return;
            }
            if (toLowerCase(defaultTemplatePath).equals(toLowerCase(templatePath))) {
                // Equal directory
                return;
            }
            if (isSubDirectory(new File(defaultTemplatePath), new File(templatePath))) {
                return;
            }
            // A file is accessed in a foreign directory
            throw TemplateErrorMessage.AccessDenied.create();
        } catch (final IOException e) {
            throw TemplateErrorMessage.IOException.create(e, e.getMessage());
        }
    }

    /**
     * Checks, whether the child directory is a sub-directory of the base directory.
     * 
     * @param base The base directory.
     * @param child The suspected child directory.
     * @return <code>true</code> if the child is a sub-directory of the base directory.
     * @throws IOException If an I/O error occurred during the test.
     */
    private boolean isSubDirectory(final File base, final File child) throws IOException {
        final File b = base.getCanonicalFile();
        final File c = child.getCanonicalFile();

        File parentFile = c;
        while (parentFile != null) {
            if (b.equals(parentFile)) {
                return true;
            }
            parentFile = parentFile.getParentFile();
        }
        return false;
    }

    @Override
    public OXTemplateImpl loadTemplate(final String templateName) throws OXException {
        final String templatePath = defaultTemplatePath;
        if (templatePath == null) {
            return null;
        }

        final OXTemplateImpl retval = new OXTemplateImpl();
        retval.setLevel(TemplateLevel.SERVER);
        retval.setTemplate(loadTemplate(templatePath, templateName));
        return retval;

    }

    protected Template loadTemplate(final String templatePath, final String templateName) throws OXException {
        final File path = new File(templatePath);
        if (!path.exists() || !path.isDirectory() || !path.canRead()) {
            return null;
        }
        checkTemplatePath(templatePath);

        Template retval = null;
        try {
            final TemplateLoader templateLoader = new FileTemplateLoader(path);
            final String userDir = System.getProperty("user.dir");
            System.setProperty("user.dir", templatePath);
            final Configuration config = new Configuration();
            System.setProperty("user.dir", userDir);
            config.setTemplateLoader(templateLoader);
            if (exceptionHandler != null) {
                config.setTemplateExceptionHandler(exceptionHandler);
            }
            retval = config.getTemplate(templateName);
        } catch (final IOException e) {
            throw IOException.create(e);
        }
        if (retval == null) {
            throw TemplateNotFound.create(templateName);
        }
        return retval;
    }

    @Override
    public OXTemplate loadTemplate(final String templateName, final String defaultTemplateName, final Session session) throws OXException {
        return loadTemplate(templateName, defaultTemplateName, session, true);
    }

    @Override
    public OXTemplate loadTemplate(final String templateName, final String defaultTemplateName, final Session sess, final boolean createCopy) throws OXException {
        final ServerSession session = ServerSessionAdapter.valueOf(sess);
        if (isEmpty(templateName) || !isUserTemplatingEnabled(session)) {
            return loadTemplate(defaultTemplateName);
        }
        try {
            FolderObject folder = folders.getPrivateTemplateFolder(session);
            FolderObject privateFolder = folder;
            boolean global = false;

            if (null == folder) {
                folder = folders.getGlobalTemplateFolder(session);
                global = true;
            }
            String templateText = (folder == null) ? null : infostore.findTemplateInFolder(session, folder, templateName);

            final String userDir = System.getProperty("user.dir");
            final String templatePath = config.getProperty(PATH_PROPERTY);
            System.setProperty("user.dir", templatePath);
            final Configuration config = new Configuration();
            System.setProperty("user.dir", userDir);
            if (exceptionHandler != null) {
                config.setTemplateExceptionHandler(exceptionHandler);
            }
            if (templateText != null) {
                final OXTemplateImpl template = new OXTemplateImpl();
                template.setTemplate(new Template(templateName, new StringReader(templateText), config));
                template.setLevel(TemplateLevel.USER);
                return template;
            }

            if (!global) {
                folder = folders.getGlobalTemplateFolder(session);
                global = true;

                templateText = (folder == null) ? null : infostore.findTemplateInFolder(session, folder, templateName);
            }

            if (templateText == null) {

                if (existsInFilesystem(templateName)) {
                    templateText = loadFromFileSystem(templateName);
                    final OXTemplateImpl template = new OXTemplateImpl();
                    template.setTemplate(new Template(templateName, new StringReader(templateText), config));
                    template.setLevel(TemplateLevel.SERVER);
                    return template;
                }

                templateText = loadFromFileSystem(defaultTemplateName);
                if (privateFolder == null) {
                    folder = folders.createPrivateTemplateFolder(session);
                    privateFolder = folder;
                }
                infostore.storeTemplateInFolder(session, privateFolder, templateName, templateText);
            }
            final OXTemplateImpl template = new OXTemplateImpl();
            template.setTemplate(new Template(templateName, new StringReader(templateText), config));
            template.setLevel(TemplateLevel.USER);
            return template;
        } catch (final IOException e) {
            throw IOException.create(e);
        }
    }

    private boolean isUserTemplatingEnabled(final ServerSession session) {
        return "true".equalsIgnoreCase(config.getProperty(USER_TEMPLATING_PROPERTY, "true")) && session.getUserConfiguration().hasInfostore();
    }

    protected boolean existsInFilesystem(final String templateName) {
        final File templateFile = getTemplateFile(templateName);
        if (!templateFile.exists() || !templateFile.exists() || !templateFile.canRead()) {
            return false;
        }
        return true;
    }

    protected String loadFromFileSystem(final String defaultTemplateName) throws OXException {
        final File templateFile = getTemplateFile(defaultTemplateName);
        if (!templateFile.exists() || !templateFile.exists() || !templateFile.canRead()) {
            throw TemplateErrorMessage.TemplateNotFound.create(defaultTemplateName);
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(templateFile));
            final StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } catch (final IOException e) {
            throw IOException.create(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    // IGNORE
                }
            }
        }
    }

    private File getTemplateFile(final String defaultTemplateName) {
        return new File(defaultTemplatePath, defaultTemplateName);
    }

    public void setOXFolderHelper(final OXFolderHelper helper) {
        this.folders = helper;
    }

    public void setInfostoreHelper(final OXInfostoreHelper helper) {
        this.infostore = helper;
    }

    @Override
    public List<String> getBasicTemplateNames(final String... filter) {
        final String templatePath = defaultTemplatePath;
        final File templateDir = new File(templatePath);
        if (!templateDir.isDirectory() || !templateDir.exists()) {
            return new ArrayList<String>(0);
        }

        final Set<String> sieve = new HashSet<String>(Arrays.asList(filter));

        final Map<String, Set<String>> tagMap = getTagMap(templateDir);

        final File[] files = templateDir.listFiles();
        if (files == null) {
            return new ArrayList<String>(0);
        }
        final Set<String> names = new HashSet<String>();
        final Set<String> defaults = new HashSet<String>();
        for (final File file : files) {
            Set<String> tags = tagMap.get(file.getName());
            if (tags == null) {
                tags = Collections.emptySet();
            }
            if (file.isFile() && file.canRead() && file.getName().endsWith(".tmpl") && (tags.containsAll(sieve))) {
                if (tags.contains("default")) {
                    defaults.add(file.getName());
                } else {
                    names.add(file.getName());
                }

            }
        }
        final List<String> a = new ArrayList<String>(defaults);
        final List<String> b = new ArrayList<String>(names);
        Collections.sort(a);
        Collections.sort(b);
        a.addAll(b);

        return a;
    }

    private Map<String, Set<String>> getTagMap(final File templateDir) {
        final String absolutePath = templateDir.getAbsolutePath();

        if (CACHED_TAGS.containsKey(absolutePath)) {
            return CACHED_TAGS.get(absolutePath);
        }

        final File[] files = templateDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File pathname) {
                return pathname.getName().endsWith(".properties") && pathname.canRead() && pathname.isFile();
            }
        });
        if (files == null) {
            final Map<String, Set<String>> emptyMap = Collections.emptyMap();
            CACHED_TAGS.put(absolutePath, emptyMap);
            return emptyMap;
        }

        final Map<String, Set<String>> tagMap = new HashMap<String, Set<String>>(files.length);
        for (final File file : files) {
            InputStream inStream = null;
            try {
                inStream = new BufferedInputStream(new FileInputStream(file));
                final Properties index = new Properties();
                index.load(inStream);
                for (final Entry<Object, Object> entry : index.entrySet()) {
                    final String filename = entry.getKey().toString();
                    final String[] categoriesArr = Strings.splitByComma(entry.getValue().toString());
                    final Set<String> categories = new HashSet<String>(Arrays.asList(categoriesArr));
                    tagMap.put(filename, categories);
                }
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            } finally {
                Streams.close(inStream);
            }
        }
        CACHED_TAGS.put(absolutePath, tagMap);
        return tagMap;

    }

    @Override
    public List<String> getTemplateNames(final Session sess, String... filter) throws OXException {
        final ServerSession session = ServerSessionAdapter.valueOf(sess);
        if (filter == null) {
            filter = new String[0];
        }
        final Set<String> names = new HashSet<String>();
        if (!isUserTemplatingEnabled(session)) {
            return getBasicTemplateNames(filter);
        }

        try {
            final FolderObject globalTemplateFolder = folders.getGlobalTemplateFolder(session);
            final FolderObject privateTemplateFolder = folders.getPrivateTemplateFolder(session);

            if (globalTemplateFolder != null) {
                names.addAll(infostore.getNames(session, globalTemplateFolder, filter));
            }
            if (privateTemplateFolder != null) {
                names.addAll(infostore.getNames(session, privateTemplateFolder, filter));
            }

        } catch (final OXException e) {
            throw e;
        }
        final List<String> basicTemplateNames = getBasicTemplateNames(filter);
        final ArrayList<String> userTemplates = new ArrayList<String>(names);
        Collections.sort(userTemplates);
        basicTemplateNames.addAll(userTemplates);
        return basicTemplateNames;
    }

    @Override
    public OXTemplate loadTemplate(final String templateName, final OXTemplateExceptionHandler exceptionHandler) throws OXException {
        return loadTemplate(templateName);
    }

    @Override
    public OXTemplate loadTemplate(final String templateName, final String defaultTemplateName, final Session session, final OXTemplateExceptionHandler exceptionHandler) throws OXException {
        setExceptionHandler(exceptionHandler);
        return loadTemplate(templateName, defaultTemplateName, session);
    }

    @Override
    public TemplatingHelper createHelper(final Object rootObject, final Session session, final boolean createCopy) {
        return new TemplatingHelperImpl(rootObject, session, this, createCopy);
    }

    private void setExceptionHandler(final OXTemplateExceptionHandler exceptionHandler) {
        this.exceptionHandler = new TemplateExceptionHandlerWrapper(exceptionHandler);
    }

    private boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    private String toLowerCase(final CharSequence chars) {
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
