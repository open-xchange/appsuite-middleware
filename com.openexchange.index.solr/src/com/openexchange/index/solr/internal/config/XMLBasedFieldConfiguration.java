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

package com.openexchange.index.solr.internal.config;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joox.Context;
import org.joox.Filter;
import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.openexchange.index.IndexField;

/**
 * {@link XMLBasedFieldConfiguration}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class XMLBasedFieldConfiguration implements FieldConfiguration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XMLBasedFieldConfiguration.class);

    private final Set<IndexField> indexedFields = new HashSet<IndexField>();

    private final Map<IndexField, Set<String>> indexFields = new HashMap<IndexField, Set<String>>();

    private final Map<String, IndexField> reverseIndexFields = new HashMap<String, IndexField>();

    private final Map<IndexField, SchemaField> schemaFields = new HashMap<IndexField, SchemaField>();

    private String uniqueKey = null;


    public XMLBasedFieldConfiguration(String configPath, String schemaPath) throws SAXException, IOException {
        super();
        SolrConfig solrConfig = initConfig(configPath);
        initSchema(solrConfig, schemaPath);
    }

    private SolrConfig initConfig(String configPath) throws SAXException, IOException {
        Match config = JOOX.$(new File(configPath));
        Match processorsMatch = config.xpath("//processor");
        List<Element> processors = processorsMatch.get();
        SolrConfig solrConfig = new SolrConfig();
        for (Element processor : processors) {
            if (processor.hasAttribute("class")) {
                String clazz = processor.getAttribute("class");
                if (clazz.equals("org.apache.solr.update.processor.TikaLanguageIdentifierUpdateProcessorFactory")) {
                    /*
                     * Check if this is an deactivated processor
                     */
                    Match processorMatch = JOOX.$(processor);
                    Match langid = processorMatch.find(new NameFilter("langid"));
                    if (langid.isNotEmpty() && "false".equals(langid.first().content())) {
                        continue;
                    }

                    /*
                     * If it's active, we use this one for the initialization and escape from the loop
                     */
                    Match langid_fl = processorMatch.find(new NameFilter("langid.fl"));
                    if (langid_fl.isNotEmpty()) {
                        String content = langid_fl.first().content();
                        String[] split = content.split(",");
                        for (String str : split) {
                            solrConfig.addLocalizedField(str.trim());
                        }
                    }

                    Match langid_whitelist = processorMatch.find(new NameFilter("langid.whitelist"));
                    if (langid_whitelist.isNotEmpty()) {
                        String content = langid_whitelist.first().content();
                        String[] split = content.split(",");
                        for (String str : split) {
                            solrConfig.addLanguage(str.trim());
                        }
                    }

                    Match langid_fallback = processorMatch.find(new NameFilter("langid.fallback"));
                    solrConfig.setFallback(langid_fallback.first().content());
                    break;
                }
            }
        }

        return solrConfig;
    }

    private void initSchema(SolrConfig solrConfig, String schemaPath) throws SAXException, IOException {
        Match schema = JOOX.$(new File(schemaPath));
        Match uniqueKeyMatch = schema.find("uniqueKey");
        if (uniqueKeyMatch.isNotEmpty()) {
            uniqueKey = uniqueKeyMatch.first().content();
        }

        Match fieldsMatch = schema.find("fields");
        if (fieldsMatch.isNotEmpty()) {
            Match fieldMatch = fieldsMatch.first().find("field");
            for (Element field : fieldMatch.get()) {
                String name = field.getAttribute("name");
                String type = field.getAttribute("type");
                boolean indexed = field.hasAttribute("indexed") ? Boolean.parseBoolean(field.getAttribute("indexed")) : false;
                boolean stored = field.hasAttribute("stored") ? Boolean.parseBoolean(field.getAttribute("stored")) : false;
                boolean multiValued = field.hasAttribute("multiValued") ? Boolean.parseBoolean(field.getAttribute("multiValued")) : false;
                boolean isLocalized = solrConfig.isLocalized(name);
                IndexField indexField = null;
                if (field.hasAttribute("oxIndexField")) {
                    String attribute = field.getAttribute("oxIndexField");
                    int lastIndex = attribute.lastIndexOf('.');
                    String enumClass = attribute.substring(0, lastIndex);
                    String enumValue = attribute.substring(lastIndex + 1, attribute.length());
                    try {
                        Class<?> enumClazz = Class.forName(enumClass);
                        if (enumClazz.isEnum()) {
                            Class<Enum> casted = (Class<Enum>) enumClazz;
                            indexField = (IndexField) Enum.valueOf(casted, enumValue);
                        }
                    } catch (Throwable e) {
                        LOG.warn("Could not instantiate Enum value {} for class {}", enumValue, enumClass, e);
                    }
                }

                if (indexField != null) {
                    SchemaField schemaField = new SchemaField(name, type, isLocalized ? true : indexed, stored, multiValued, isLocalized, indexField);
                    schemaFields.put(indexField, schemaField);

                    if (isLocalized) {
                        Set<String> localizedFields = solrConfig.localizeField(name);
                        indexFields.put(indexField, localizedFields);
                        for (String localizedField : localizedFields) {
                            reverseIndexFields.put(localizedField, indexField);
                        }
                    } else {
                        indexFields.put(indexField, Collections.singleton(name));
                        reverseIndexFields.put(name, indexField);
                    }

                    if (schemaField.isIndexed()) {
                        indexedFields.add(indexField);
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getSolrFields(IndexField indexField) {
        return indexFields.get(indexField);
    }

    @Override
    public Set<? extends IndexField> getIndexedFields() {
        return new HashSet<IndexField>(indexedFields);
    }

    @Override
    public String getUUIDField() {
        return uniqueKey;
    }

    @Override
    public IndexField getIndexField(String solrField) {
        return reverseIndexFields.get(solrField);
    }

    @Override
    public String getRawField(IndexField indexField) {
        SchemaField schemaField = schemaFields.get(indexField);
        if (schemaField == null) {
            return null;
        }

        return schemaField.getName();
    }

    @Override
    public boolean isLocalized(IndexField indexField) {
        SchemaField schemaField = schemaFields.get(indexField);
        return schemaField.isLocalized();
    }

    private static final class NameFilter implements Filter {

        private final String name;

        public NameFilter(String name) {
            super();
            this.name = name;
        }

        @Override
        public boolean filter(Context context) {
            Element element = context.element();
            if (element.hasAttribute("name")) {
                if (element.getAttribute("name").equals(name)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static final class SolrConfig {

        private final Set<String> localizedFields = new HashSet<String>();

        private final Set<String> whitelist = new HashSet<String>();

        private String fallback = null;

        public SolrConfig() {
            super();
        }

        public void addLocalizedField(String field) {
            localizedFields.add(field);
        }

        public void addLanguage(String language) {
            whitelist.add(language);
        }

        public void setFallback(String fallback) {
            this.fallback = fallback;
        }

        public boolean isLocalized(String field) {
            return localizedFields.contains(field);
        }

        public Set<String> localizeField(String field) {
            Set<String> fields = new HashSet<String>();
            if (fallback != null) {
                fields.add(field + '_' + fallback);
            }

            for (String language : whitelist) {
                fields.add(field + '_' + language);
            }

            return fields;
        }
    }

}
