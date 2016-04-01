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

package com.openexchange.i18n.yaml.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.yaml.I18NYamlParserService;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link I18NYamlParserImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class I18NYamlParserImpl implements I18NYamlParserService {

    private final ServiceLookup services;
    private final Pattern pattern;
    private final Pattern indention;
    private final File genericDir;

    /**
     * Initializes a new {@link I18NYamlParserImpl}.
     *
     * @param services The OSGi service look-up or <code>null</code>
     */
    public I18NYamlParserImpl(ServiceLookup services) {
        super();
        this.services = services;
        pattern = Pattern.compile("(^|\\s)\\w+_t10e\\s*\\:\\s*\"((?:\\\\\"|[^\"])+)\"");
        indention = Pattern.compile("(\r?\n)(\t| {2,})+([\\p{L} ])");
        genericDir = new File("/opt/open-xchange/etc");
    }

    private Set<String> parseFile0(File file) throws I18nYamlParseException {
        try {
            String content = Streams.reader2string(new FileReader(file));
            Matcher m = pattern.matcher(content);
            if (!m.find()) {
                return Collections.emptySet();
            }

            Set<String> literals = new LinkedHashSet<String>(16, 0.9F);
            do {
                literals.add(indention.matcher(m.group(2)).replaceAll("$1$3"));
            } while (m.find());
            return literals;
        } catch (FileNotFoundException e) {
            throw new I18nYamlParseException("\"" + file.getName() + "\" does not exist");
        } catch (IOException e) {
            throw I18nYamlParseException.wrapException("\"" + file.getName() + "\" is not readable", e);
        }
    }

    private void collectAllYamlFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        if (null != files) {
            for (File pathname : files) {
                if (pathname.isDirectory()) {
                    collectAllYamlFiles(pathname, fileList);
                } else {
                    String lc = Strings.asciiLowerCase(pathname.getName());
                    if (null != lc && (lc.endsWith(".yml") || lc.endsWith(".yaml"))) {
                        fileList.add(pathname);
                    }
                }
            }
        }
    }

    private File LookUpYamlFile(File dir, String name) {
        File[] files = dir.listFiles();
        if (null != files) {
            for (File pathname : files) {
                if (pathname.isDirectory()) {
                    File candidate = LookUpYamlFile(pathname, name);
                    if (null != candidate) {
                        return candidate;
                    }
                } else {
                    String curName = pathname.getName();
                    if (name.equals(curName)) {
                        return pathname;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Parses the translatable string literals from specified YAML file.
     *
     * @param fileName The file name; either a fully qualified path name or only the name to look it up in default directory <code>"/opt/open-xchange/etc"</code>
     * @return The translatable string literals or <code>null</code> if no such file can be found
     * @throws I18nYamlParseException If translatable string literals cannot be returned
     */
    public List<String> parseFile(String fileName) throws I18nYamlParseException {
        File yamlFile = new File(fileName);

        if (null == yamlFile.getParent()) {
            yamlFile = new File(genericDir, fileName);
            if (!yamlFile.isFile()) {
                yamlFile = LookUpYamlFile(genericDir, fileName);
                if (null == yamlFile || !yamlFile.isFile()) {
                    throw new I18nYamlParseException("Unable to look-up such a file \"" + fileName + "\" in \"" + genericDir.getPath() + "\"");
                }
            }
        } else if (!yamlFile.isFile()) {
            throw new I18nYamlParseException("\"" + yamlFile.getPath() + "\" does not exist");
        }

        return new ArrayList<String>(parseFile0(yamlFile));
    }

    @Override
    public List<String> parseTranslatablesFromFile(String fileName) throws OXException {
        ConfigurationService service = null == services ? null : services.getOptionalService(ConfigurationService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }

        try {
            File file = service.getFileByName(fileName);
            if (null == file) {
                throw OXException.general("Unable to look-up such a file \"" + fileName + "\"");
            }
            if (!file.exists()) {
                throw OXException.general("\"" + file.getPath() + "\" does not exist");
            }

            return new ArrayList<String>(parseFile0(file));
        } catch (I18nYamlParseException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

    @Override
    public List<String> parseTranslatablesFromDirectory(String dirName, boolean recursive) throws OXException {
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            throw OXException.general("\"" + dirName + "\" is not a directory");
        }

        File[] yamlFiles;
        if (recursive) {
            List<File> fileList = new LinkedList<File>();
            collectAllYamlFiles(dir, fileList);
            yamlFiles = fileList.toArray(new File[fileList.size()]);
        } else {
            yamlFiles = dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    String lc = Strings.asciiLowerCase(name);
                    return null != name && (lc.endsWith(".yml") || lc.endsWith(".yaml"));
                }
            });
        }

        if (null == yamlFiles || 0 == yamlFiles.length) {
            return Collections.emptyList();
        }

        try {
            Set<String> literals = new LinkedHashSet<String>(16, 0.9F);
            for (File yamlFile : yamlFiles) {
                literals.addAll(parseFile0(yamlFile));
            }
            return new ArrayList<String>(literals);
        } catch (I18nYamlParseException e) {
            throw OXException.general(e.getMessage(), e);
        }
    }

}
