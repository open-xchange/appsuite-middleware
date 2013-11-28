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

package com.openexchange.index.solr.internal.querybuilder.translators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.querybuilder.Configuration;
import com.openexchange.index.solr.internal.querybuilder.QueryTranslator;
import com.openexchange.index.solr.internal.querybuilder.TranslationException;
import com.openexchange.index.solr.internal.querybuilder.utils.FormalFieldParser;

/**
 * {@link SimpleTranslator}
 *
 * @author Sven Maurmann
 */
public class SimpleTranslator implements QueryTranslator {

    private Map<String, List<String>> translationDict;

    private String handlerName;

    private FormalFieldParser parser;

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleTranslator.class);

    @Override
    public void init(String name, Configuration config, FieldConfiguration fieldConfig) throws TranslationException {
        handlerName = name.trim();
        translationDict = new HashMap<String, List<String>>();

        log.info("[init]: initializing configuration for handler \'" + handlerName + "\'");

        for (String key : config.getKeys(handlerName)) {
            String scrubbedKey = key.substring(handlerName.length() + 1);

            translationDict.put(scrubbedKey, config.getIndexFields(key));
            log.info("[init]: Added translation for \'" + scrubbedKey + "\'");
        }
        parser = new FormalFieldParser(translationDict, true);
    }

    @Override
    public String translate(Object o) throws TranslationException {
        log.debug("[translate]: Starting");
        if (o instanceof String) {
            String parsedQueryString = parser.parse((String) o);
            return parsedQueryString;
        } else {
            throw new IllegalArgumentException("Only strings are allowed");
        }
    }
}
