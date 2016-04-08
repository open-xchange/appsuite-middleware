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

package com.openexchange.mail.categories.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoriesConstants;
import com.openexchange.mail.categories.MailCategoriesExceptionCodes;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.mail.categories.MailCategoryConfig.Builder;
import com.openexchange.mail.categories.MailObjectParameter;
import com.openexchange.mail.categories.ReorganizeParameter;
import com.openexchange.mail.categories.impl.mailfilter.MailCategoriesOrganizeExceptionCodes;
import com.openexchange.mail.categories.impl.mailfilter.MailCategoriesOrganizer;
import com.openexchange.mail.categories.impl.osgi.Services;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngine;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngineExceptionCodes;
import com.openexchange.mail.categories.ruleengine.MailCategoryRule;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;


/**
 * {@link MailCategoriesConfigServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesConfigServiceImpl implements MailCategoriesConfigService {

    private final static String FLAG_PREFIX = "$ox_";

    private static final String FROM_HEADER = "from";

    public MailCategoriesConfigServiceImpl() {
        super();
    }

    @Override
    public List<MailCategoryConfig> getAllCategories(Session session, boolean onlyEnabled) throws OXException {
        String[] categories = getSystemCategoryNames(session);
        String[] userCategories = getUserCategoryNames(session);
        if (categories.length == 0 && userCategories.length==0) {
            return new ArrayList<>();
        }
        List<MailCategoryConfig> result = new ArrayList<>(categories.length);
        for (String category : categories) {
            MailCategoryConfig config = getConfigByCategory(session, category);
            if (onlyEnabled && !config.isActive()) {
                continue;
            }
            result.add(config);
        }
        for (String category : userCategories) {
            MailCategoryConfig config = getUserConfigByCategory(session, category);
            if (onlyEnabled && !config.isActive()) {
                continue;
            }
            result.add(config);
        }
        return result;
    }

    @Override
    public String[] getAllFlags(Session session, boolean onlyEnabled, boolean onlyUserCategories) throws OXException {
        if (onlyUserCategories) {
            String[] userCategories = getUserCategoryNames(session);
            if (userCategories.length == 0) {
                return new String[0];
            }
            ArrayList<String> result = new ArrayList<>(userCategories.length);
            for (String category : userCategories) {
                if (onlyEnabled) {
                    boolean active = MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
                    if (!active) {
                        continue;
                    }
                }
                result.add(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
            }

            return result.toArray(new String[result.size()]);
        }

        // Include system categories
        String[] categories = getSystemCategoryNames(session);
        String[] userCategories = getUserCategoryNames(session);
        if (categories.length == 0 && userCategories.length==0) {
            return new String[0];
        }

        ArrayList<String> result = new ArrayList<>(categories.length + userCategories.length);
        for (String category : categories) {
            if (onlyEnabled) {
                boolean active = MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
                if (!active) {
                    boolean forced = MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FORCE, false, session);
                    if (!forced) {
                        continue;
                    }
                }
            }
           result.add(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        }
        for (String category : userCategories) {
            if (onlyEnabled) {
                boolean active = MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
                if (!active) {
                    continue;
                }
            }
           result.add(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        }

        return result.toArray(new String[result.size()]);
    }

    private MailCategoryConfig getConfigByCategory(Session session, String category) throws OXException {
        Builder builder = new Builder();
        builder.category(category);
        builder.name(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME, null, session));
        builder.enabled(MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session));
        builder.force(MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FORCE, false, session));
        builder.flag(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        builder.addLocalizedNames(getLocalizedNames(session, category));
        builder.isSystemCategory(isSystemCategory(category, session));
        MailCategoryConfig result = builder.build();
        if (result.getFlag() == null) {
            throw MailCategoriesExceptionCodes.INVALID_CONFIGURATION_EXTENDED.create(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG);
        }

        return result;
    }

    private MailCategoryConfig getUserConfigByCategory(Session session, String category) throws OXException {
        Builder builder = new Builder();
        builder.category(category);
        builder.name(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME, null, session));
        builder.enabled(MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session));
        builder.flag(MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        builder.isSystemCategory(false);
        MailCategoryConfig result = builder.build();
        if (result.getFlag() == null) {
            throw MailCategoriesExceptionCodes.INVALID_CONFIGURATION_EXTENDED.create(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG);
        }

        return result;
    }

    @Override
    public String getFlagByCategory(Session session, String category) throws OXException {
        return MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session);
    }

    private Map<Locale, String> getLocalizedNames(Session session, String category) throws OXException {
        String languagesString = MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_LANGUAGES, "de_DE, cs_CZ, en_GB, es_ES, fr_FR, hu_HU, it_IT, jp_JP, lv_LV, nl_NL, pl_PL, sv_SV, sk_SK, zh_CN", session);
        if (Strings.isEmpty(languagesString)){
            return java.util.Collections.emptyMap();
        }

        String languages[] = Strings.splitByComma(languagesString);
        if (languages==null || languages.length==0){
            return java.util.Collections.emptyMap();
        }

        Map<Locale, String> result = new HashMap<>(languages.length);
        for(String language: languages){
            String translation = MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME + "." + language, null, session);
            if (translation != null && !translation.isEmpty()) {
                result.put(new Locale(language), translation);
            }
        }
        return result;
    }

    private String[] getSystemCategoryNames(Session session) throws OXException {
        String categoriesString = MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_IDENTIFIERS, null, session);
        if (Strings.isEmpty(categoriesString)){
            return new String[0];
        }

        String result[] = Strings.splitByComma(categoriesString);
        return result==null ? new String[0] : result;
    }

    private String[] getUserCategoryNames(Session session) throws OXException {
        String categoriesString = MailCategories.getValueFromProperty(MailCategoriesConstants.MAIL_USER_CATEGORIES_IDENTIFIERS, null, session);
        if (Strings.isEmpty(categoriesString)) {
            return new String[0];
        }

        String result[] = Strings.splitByComma(categoriesString);
        return result == null ? new String[0] : result;
    }

    @Override
    public boolean isSystemCategory(String category, Session session) throws OXException {
        String[] systemCategories = getSystemCategoryNames(session);
        for (String systemCategory : systemCategories) {
            if (category.equals(systemCategory)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isEnabled(Session session) throws OXException {
        return MailCategories.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_SWITCH, false, session);
    }

    private String generateFlag(String category) {
        StringBuilder builder = new StringBuilder(FLAG_PREFIX);
        builder.append(category.hashCode());
        return builder.toString();
    }

    private void setRule(Session session, String category, MailCategoryRule rule) throws OXException {
        if (!generateFlag(category).equals(rule.getFlag())) {
            throw MailCategoriesRuleEngineExceptionCodes.INVALID_RULE.create();
        }
        MailCategoriesRuleEngine ruleEngine = Services.getService(MailCategoriesRuleEngine.class);
        if (ruleEngine == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailCategoriesRuleEngine.class.getSimpleName());
        }
        ruleEngine.setRule(session, rule);
    }

    private MailCategoryRule getRule(Session session, String category) throws OXException {
        MailCategoriesRuleEngine ruleEngine = Services.getService(MailCategoriesRuleEngine.class);
        if (ruleEngine == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailCategoriesRuleEngine.class.getSimpleName());
        }

        String flag = getFlagByCategory(session, category);
        if (Strings.isEmpty(flag)) {
            flag = generateFlag(category);
            MailCategories.setProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, flag, session);
        }
        return ruleEngine.getRule(session, flag);
    }

    private void removeMailFromRules(Session session, String mailAddress) throws OXException {
        MailCategoriesRuleEngine ruleEngine = Services.getService(MailCategoriesRuleEngine.class);
        if (ruleEngine == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailCategoriesRuleEngine.class.getSimpleName());
        }
        ruleEngine.removeValueFromHeader(session, mailAddress, "from");
    }

    private SearchTerm<?> getSearchTerm(MailCategoryRule rule) {
        if (!rule.hasSubRules()) {
            if (rule.getHeaders().size() == 1 && rule.getValues().size() == 1) {

                return new HeaderTerm(rule.getHeaders().get(0), rule.getValues().get(0));
            }
            SearchTerm<?> result = null;
            for (String header : rule.getHeaders()) {
                for (String value : rule.getValues()) {
                    if (result == null) {
                        result = new HeaderTerm(header, value);
                    } else {
                        result = new ORTerm(result, new HeaderTerm(header, value));
                    }
                }
            }
            return result;

        }

        SearchTerm<?> result = null;
        if (rule.isAND()) {
            for (MailCategoryRule subRule : rule.getSubRules()) {
                result = result == null ? getSearchTerm(subRule) : new ANDTerm(result, getSearchTerm(subRule));
            }
        } else {
            for (MailCategoryRule subRule : rule.getSubRules()) {
                result = result == null ? getSearchTerm(subRule) : new ORTerm(result, getSearchTerm(subRule));
            }
        }
        return result;
    }

    @Override
    public void teachCategory(String category, String email, ReorganizeParameter reorganize, Session session) throws OXException {
        String flag = generateFlag(category);

        // Remove from old rules
        removeMailFromRules(session, email);

        // Update rule

        MailCategoryRule oldRule = getRule(session, category);
        MailCategoryRule newRule = null;
        if (null == oldRule) {        // Create new rule
            newRule = new MailCategoryRule(Collections.singletonList(FROM_HEADER), Collections.singletonList(email), flag);
        } else {
            if(oldRule.getSubRules()!=null && !oldRule.getSubRules().isEmpty()){
                if (oldRule.isAND()) {
                    newRule = new MailCategoryRule(flag, false);
                    newRule.addSubRule(oldRule);
                    newRule.addSubRule(new MailCategoryRule(Collections.singletonList(FROM_HEADER), Collections.singletonList(email), flag));
                } else {
                    newRule = oldRule;
                    newRule.addSubRule(new MailCategoryRule(Collections.singletonList(FROM_HEADER), Collections.singletonList(email), flag));
                }
            } else {
                if (!oldRule.getHeaders().contains(FROM_HEADER)) {
                    oldRule.getHeaders().add(FROM_HEADER);
                }
                if (!oldRule.getValues().contains(email)) {
                    oldRule.getValues().add(email);
                }
                newRule = oldRule;
            }
        }

        // remove all previous category flags
        newRule.addFlagsToRemove(getAllFlags(session, false, false));

        setRule(session, category, newRule);

        // Reorganize if necessary
        if (reorganize.isReorganize()) {
            List<OXException> warnings = reorganize.getWarnings();
            try {
                SearchTerm<?> searchTerm = getSearchTerm(newRule);
                FullnameArgument fa = new FullnameArgument("INBOX");
                if (searchTerm != null) {
                    MailCategoriesOrganizer.organizeExistingMails(session, fa.getFullName(), searchTerm, flag, true);
                }
            } catch (OXException e) {
                if (warnings.isEmpty()) {
                    warnings.add(MailCategoriesOrganizeExceptionCodes.UNABLE_TO_ORGANIZE.create());
                }
            }
        }

    }

    @Override
    public void updateConfigurations(List<MailCategoryConfig> configs, Session session) throws OXException {
        List<MailCategoryConfig> oldConfigs = getAllCategories(session, false);

        for (MailCategoryConfig newConfig : configs) {

            if (newConfig.getNames() != null && !newConfig.getNames().isEmpty()) {
                throw MailCategoriesExceptionCodes.INVALID_CONFIGURATION.create();
            }

            int index = oldConfigs.indexOf(newConfig);
            if (index >= 0) {
                MailCategoryConfig oldConfig = oldConfigs.get(index);
                boolean rename = false;
                boolean switchStatus = false;
                if (newConfig.isEnabled() != oldConfig.isActive() && newConfig.isEnabled() != oldConfig.isEnabled()) {
                    if (oldConfig.isForced()) {
                        throw MailCategoriesExceptionCodes.SWITCH_NOT_ALLOWED.create(oldConfig.getCategory());
                    }
                    switchStatus = true;
                }
                
                if (!newConfig.getName().equals(oldConfig.getName())) {
                    if (isSystemCategory(oldConfig.getCategory(), session)) {
                        throw MailCategoriesExceptionCodes.CHANGE_NAME_NOT_ALLOWED.create(oldConfig.getCategory());
                    }
                    rename = true;
                }

                if (switchStatus) {
                    MailCategories.activateProperty(oldConfig.getCategory(), newConfig.isEnabled(), session);
                }
                if (rename) {
                    MailCategories.setProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + oldConfig.getCategory() + MailCategoriesConstants.MAIL_CATEGORIES_NAME, newConfig.getName(), session);
                }
            } else {
                throw MailCategoriesExceptionCodes.USER_CATEGORY_DOES_NOT_EXIST.create(newConfig.getCategory());
            }
        }

    }

    @Override
    public void addMails(Session session, List<MailObjectParameter> mails, String category) throws OXException {
        FullnameArgument fa = new FullnameArgument("INBOX");
        String flag = getFlagByCategory(session, category);
        String[] allFlags = getAllFlags(session, false, false);
        MailCategoriesOrganizer.organizeMails(session, fa.getFullName(), mails, flag, allFlags);
    }

}
