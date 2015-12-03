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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

    /**
     * Initializes a new {@link I18NYamlParserImpl}.
     */
    public I18NYamlParserImpl(ServiceLookup services) {
        super();
        this.services = services;
        pattern = Pattern.compile("(^|\\s)\\w+_t10e\\s*\\:\\s*\"((?:\\\\\"|[^\"])+)\"");
    }

    private Set<String> parseFrom(File file) throws OXException {
        try {
            String content = Streams.reader2string(new FileReader(file));
            Matcher m = pattern.matcher(content);
            if (!m.find()) {
                return Collections.emptySet();
            }

            Set<String> literals = new LinkedHashSet<String>(16, 0.9F);
            do {
                literals.add(m.group(2));
            } while (m.find());
            return literals;
        } catch (FileNotFoundException e) {
            throw OXException.general(file.getName() + " is not readable");
        } catch (IOException e) {
            throw OXException.general(file.getName() + " is not readable", e);
        }
    }

    @Override
    public List<String> parseTranslatableFromFile(String fileName) throws OXException {
        ConfigurationService service = services.getOptionalService(ConfigurationService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }

        File file = service.getFileByName(fileName);
        if (null == file || !file.exists()) {
            throw OXException.general(fileName + " is not readable");
        }

        return new ArrayList<String>(parseFrom(file));
    }

    @Override
    public List<String> parseTranslatableFromDirectory(String dirName) throws OXException {
        File dir = new File(dirName);
        if (!dir.isDirectory()) {
            throw OXException.general(dirName + " is not a directory");
        }

        File[] yamlFiles = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                String lc = Strings.asciiLowerCase(name);
                return null != name && (lc.endsWith(".yml") || lc.endsWith(".yaml"));
            }
        });

        if (null == yamlFiles || 0 == yamlFiles.length) {
            return Collections.emptyList();
        }

        Set<String> literals = new LinkedHashSet<String>(16, 0.9F);
        for (File yamlFile : yamlFiles) {
            literals.addAll(parseFrom(yamlFile));
        }
        return new ArrayList<String>(literals);
    }

}
