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

import java.util.Set;


import com.openexchange.index.solr.internal.config.FieldConfiguration;
import com.openexchange.index.solr.internal.querybuilder.Configuration;
import com.openexchange.index.solr.internal.querybuilder.QueryTranslator;
import com.openexchange.index.solr.internal.querybuilder.TranslationException;

/**
 * {@link IdListTranslator}
 *
 * @author Sven Maurmann
 */
public class IdListTranslator implements QueryTranslator {

    private static final String ID_FIELD = "id_field";

    private static final String INIT_ERROR = "Error getting id key";

    private static final String TRANSLATION_ERROR = "Only sets of strings are allowed";

    private String idKey;

    private String handlerName;

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdListTranslator.class);


    @Override
    public void init(String name, Configuration config, FieldConfiguration fieldConfig) throws TranslationException {
        handlerName = name.trim();

        log.info("[init]: initializing configuration for handler \'{}\'", handlerName);

        Set<String> keys = config.getKeys(handlerName);
        String key = handlerName + '.' + ID_FIELD;
        if (keys.contains(key)) {
            idKey = config.getRawMapping().get(key);
            log.info("[init]: ID key is \'{}\'", idKey);
            return;
        }

        log.error("[init]: No valid id key found.");
        throw new TranslationException(INIT_ERROR);
    }

    @Override
    public String translate(Object o) throws TranslationException {
        log.debug("[translate]: Starting");
        if (o instanceof Set<?>) {
            StringBuffer b = new StringBuffer();
            Set<?> idList = (Set<?>) o;

            for (Object idVal : idList) {
                if (idVal instanceof String) {
                    b.append(idKey + ":" + idVal + " ");
                } else {
                    log.warn("[translate]: Wrong type in list");
                }
            }

            return b.toString().trim();
        } else {
            throw new IllegalArgumentException(TRANSLATION_ERROR);
        }
    }
}
