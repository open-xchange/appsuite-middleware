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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.mail.categories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.MailCategoryConfig.Builder;
import com.openexchange.session.Session;


/**
 * {@link MailCategoriesConfigServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesConfigServiceImpl implements MailCategoriesConfigService {

    @Override
    public List<MailCategoryConfig> getAllCategories(Session session, boolean onlyEnabled) throws OXException {
        String[] categories = getCategoryNames();
        if (categories == null || categories.length == 0) {
            return new ArrayList<>();
        }
        List<MailCategoryConfig> result = new ArrayList<>(categories.length);
        for (String category : categories) {
            MailCategoryConfig config = getConfigByCategory(session, category);
            if (onlyEnabled && (config.isActive() || config.isForced())) {
                continue;
            }
            result.add(config);
        }
        return result;
    }
    
    @Override
    public String[] getAllFlags(Session session, boolean onlyEnabled) throws OXException {
        String[] categories = getCategoryNames();
        if (categories == null || categories.length == 0) {
            return new String[0];
        }
        String[] result = new String[categories.length];
        int x=0;
        for (String category : categories) {
            boolean active = MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
            if(!active){
                boolean forced = MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FORCE, false, session);
                if(!forced){
                    continue;    
                }
            }
            result[x++]=MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session);;
        }
        return result;
    }

    @Override
    public MailCategoryConfig getConfigByCategory(Session session, String category) throws OXException {
        Builder builder = new Builder();
        builder.category(category);
        builder.name(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME, null, session));
        builder.active(MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session));
        builder.force(MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FORCE, false, session));
        builder.flag(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        builder.addLocalizedNames(getLocalizedNames(session, category));
        MailCategoryConfig result = builder.build();
        if (result.getFlag() == null) {
            throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG);
        }

        return result;
    }
    
    @Override
    public String getFlagByCategory(Session session, String category) throws OXException {
        return MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session);
    }

    private Map<Locale, String> getLocalizedNames(Session session, String category) throws OXException {
        String languagesString = MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_LANGUAGES, "de_DE, cs_CZ, en_GB, es_ES, fr_FR, hu_HU, it_IT, jp_JP, lv_LV, nl_NL, pl_PL, sv_SV, sk_SK, zh_CN", session);
        Map<Locale, String> result = new HashMap<>();
        if(languagesString==null || languagesString.isEmpty()){
            return result;
        }
        String languages[] = languagesString.replaceAll(" ", "").split(",");
        if(languages==null || languages.length==0){
            return result;
        }
        for(String language: languages){
            String translation = MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME + "." + language, null, session);
            if (translation != null && !translation.isEmpty()) {
                result.put(Locale.forLanguageTag(language), translation);
            }
        }
        return result;
    }

    @Override
    public MailCategoryConfig getConfigByFlag(Session session, String flag) throws OXException {
        List<MailCategoryConfig> all = getAllCategories(session, false);
        for (MailCategoryConfig config : all) {
            if (config.getFlag().equals(flag)) {
                return config;
            }
        }
        return null;
    }

    private String[] getCategoryNames() {
        //TODO get Category names
        return null;
    }

}
