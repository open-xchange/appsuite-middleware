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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
package com.openexchange.index.solr.internal.querybuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.java.Streams;

/**
 * This class encapsulates a simple file-based dictionary for the field-mappings. The syntax for the mapping is <br>
 * <code>symb_name = real_index_field</code><br>
 * where <code>real_index_field</code> is either of the form of the name of a field in the Solr schema or of the form<br>
 * <code>name{suffix1,suffix2,...suffixn}</code><br>
 * The latter form is expanded into a series of fields of the form<br>
 * <code>name_suffix1</code> to <code>name_suffixn</code>.
 *
 * @author Sven Maurmann
 */
public class SimpleConfiguration implements Configuration {

    private static final Log log = com.openexchange.log.Log.loggerFor(SimpleConfiguration.class);

    private final Map<String, String> rawMapping;
    private final Map<String, List<String>> dictionary;
    private final Map<String, String> translators;

    public SimpleConfiguration(String configPath) throws BuilderException {
        super();
        dictionary = new HashMap<String, List<String>>();
        rawMapping = new HashMap<String, String>();
        translators = new HashMap<String, String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(configPath)));
            final String translatorPrefix = Configuration.TRANSLATOR + ".";
            final Pattern patternSplit = Pattern.compile("=");
            int lineCount = 0;
            while (reader.ready()) {
                final String line = reader.readLine();
                if (null != line) {
                    lineCount++;
                    if (isEmpty(line) || line.trim().charAt(0) == '#') {
                        continue;
                    }
                    String[] parts = patternSplit.split(line, 0);
                    if (parts.length != 2) {
                        log.warn("[SimpleConfiguration]: Invalid line " + lineCount + ": " + line);
                        continue;
                    }
                    rawMapping.put(parts[0].trim(), parts[1].trim());
                    if (parts[0].startsWith(translatorPrefix)) {
                        log.debug("[SimpleConfiguration]: Extracting translator ...");
                        String handlerName = parts[0].substring(parts[0].indexOf('.') + 1).trim();
                        log.debug("[SimpleConfiguration]: Handler is " + handlerName);
                        log.debug("[SimpleConfiguration]: Translator is " + parts[1].trim());
                        translators.put(handlerName, parts[1].trim());
                        continue;
                    }
                    if (parts[1].indexOf('{') >= 0) {
                        log.debug("[SimpleConfiguration]: found a compound field");
                        dictionary.put(parts[0].trim(), this.assembleFieldList(parts[1].trim()));
                    } else {
                        log.debug("[SimpleConfiguration]: found a simple field");
                        List<String> val = new ArrayList<String>();
                        val.add(parts[1].trim());
                        dictionary.put(parts[0].trim(), val);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.error("[SimpleConfiguration]: Error during instantiation: " + e.getMessage());
            throw new BuilderException(e);
        } catch (IOException e) {
            log.error("[SimpleConfiguration]: Error during instantiation: " + e.getMessage());
            throw new BuilderException(e);
        } finally {
            Streams.close(reader);
        }
    }

    @Override
    public List<String> getIndexFields(String key) {
        return dictionary.get(key);
    }

    @Override
    public Set<String> getKeys() {
        return dictionary.keySet();
    }

    @Override
    public Set<String> getKeys(String handlerName) {
        Set<String> dictKeys = new HashSet<String>();
        for (String key : dictionary.keySet()) {
            if (key.startsWith(handlerName)) {
                dictKeys.add(key);
            }
        }
        return dictKeys;
    }

    @Override
    public Map<String, String> getRawMapping() {
        return this.rawMapping;
    }

    @Override
    public Map<String, String> getTranslatorMap() {
        return this.translators;
    }

    @Override
    public boolean haveTranslatorForHandler(String handler) {
        return translators.containsKey(handler);
    }

    @Override
    public String getTranslatorForHandler(String handler) {
        return translators.get(handler);
    }

    @Override
    public Set<String> getHandlers() {
        return translators.keySet();
    }

    // -------------------------- private methods below ----------------------------------- //

    private static final Pattern PATTERN_SPLIT_COMMA = Pattern.compile(" *, *");

    private List<String> assembleFieldList(String input) {
        int prefixIdx = input.indexOf('{');
        String prefix = input.substring(0, prefixIdx).trim();
        String suffixes = input.substring(prefixIdx + 1, input.length() - 1);
        String[] suffixArray = PATTERN_SPLIT_COMMA.split(suffixes, 0);

        List<String> fieldList = new ArrayList<String>(suffixArray.length);
        for (int i = 0; i < suffixArray.length; i++) {
            fieldList.add(prefix + "_" + suffixArray[i].trim());
        }

        return fieldList;
    }

    // -------------------------- helper methods below ----------------------------------- //

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
