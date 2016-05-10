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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
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
import com.openexchange.mail.categories.impl.osgi.Services;
import com.openexchange.mail.categories.organizer.MailCategoriesOrganizeExceptionCodes;
import com.openexchange.mail.categories.organizer.MailCategoriesOrganizer;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngine;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngineExceptionCodes;
import com.openexchange.mail.categories.ruleengine.MailCategoryRule;
import com.openexchange.mail.categories.ruleengine.RuleType;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.HeaderTerm;
import com.openexchange.mail.search.ORTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link MailCategoriesConfigServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesConfigServiceImpl implements MailCategoriesConfigService {

    static final String INIT_TASK_STATUS_PROPERTY = "com.openexchange.mail.categories.ruleengine.sieve.init.run";

    private final static String FLAG_PREFIX = "$ox_";
    private static final String FROM_HEADER = "from";

    private static final String RULE_DEFINITION_PROPERTY_PREFIX = "com.openexchange.mail.categories.rules.";
    private static final String STATUS_NOT_YET_STARTED = "notyetstarted";
    private static final String STATUS_RUNNING = "running";
    private static final String STATUS_FINISHED = "finished";

    private static MailCategoriesConfigServiceImpl INSTANCE;

    public static MailCategoriesConfigServiceImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MailCategoriesConfigServiceImpl();
        }
        return INSTANCE;
    }

    private MailCategoriesConfigServiceImpl() {
        super();
    }

    @Override
    public List<MailCategoryConfig> getAllCategories(Session session, Locale locale, boolean onlyEnabled, boolean includeGeneral) throws OXException {
        String[] categories = getSystemCategoryNames(session);
        String[] userCategories = getUserCategoryNames(session);
        List<MailCategoryConfig> result = new ArrayList<>(categories.length);

        if (includeGeneral) {
            String name = getLocalizedName(session, locale, MailCategoriesConstants.GENERAL_CATEGORY_ID);
            MailCategoryConfig generalConfig = new MailCategoryConfig.Builder().category(MailCategoriesConstants.GENERAL_CATEGORY_ID).isSystemCategory(true).enabled(true).force(true).name(name).build();
            result.add(generalConfig);
        }

        if (categories.length == 0 && userCategories.length == 0) {
            return new ArrayList<>();
        }

        for (String category : categories) {
            MailCategoryConfig config = getConfigByCategory(session, locale, category);
            if (onlyEnabled && !config.isActive()) {
                continue;
            }
            result.add(config);
        }
        for (String category : userCategories) {
            MailCategoryConfig config = getUserConfigByCategory(session, locale, category);
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
                    boolean active = MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
                    if (!active) {
                        continue;
                    }
                }
                result.add(MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
            }

            return result.toArray(new String[result.size()]);
        }

        // Include system categories
        String[] categories = getSystemCategoryNames(session);
        String[] userCategories = getUserCategoryNames(session);
        if (categories.length == 0 && userCategories.length == 0) {
            return new String[0];
        }

        ArrayList<String> result = new ArrayList<>(categories.length + userCategories.length);
        for (String category : categories) {
            if (onlyEnabled) {
                boolean active = MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
                if (!active) {
                    boolean forced = MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FORCE, false, session);
                    if (!forced) {
                        continue;
                    }
                }
            }
            result.add(MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        }
        for (String category : userCategories) {
            if (onlyEnabled) {
                boolean active = MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session);
                if (!active) {
                    continue;
                }
            }
            result.add(MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        }

        return result.toArray(new String[result.size()]);
    }

    private MailCategoryConfig getConfigByCategory(Session session, Locale locale, String category) throws OXException {
        Builder builder = new Builder();
        builder.category(category);
        String name = MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME, null, session);
        if (Strings.isEmpty(name)) {
            name = getLocalizedName(session, locale, category);
        }
        builder.name(name);
        builder.enabled(MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session));
        builder.force(MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FORCE, false, session));
        builder.flag(MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        builder.isSystemCategory(isSystemCategory(category, session));
        MailCategoryConfig result = builder.build();
        if (result.getFlag() == null) {
            throw MailCategoriesExceptionCodes.INVALID_CONFIGURATION_EXTENDED.create(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG);
        }

        return result;
    }

    private MailCategoryConfig getUserConfigByCategory(Session session, Locale locale, String category) throws OXException {
        Builder builder = new Builder();
        builder.category(category);
        String name = MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_NAME, null, session);
        if (Strings.isEmpty(name)) {
            name = getLocalizedName(session, locale, category);
        }
        builder.name(name);
        builder.enabled(MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_ACTIVE, true, session));
        builder.flag(MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session));
        builder.isSystemCategory(false);
        MailCategoryConfig result = builder.build();
        return result;
    }

    @Override
    public String getFlagByCategory(Session session, String category) throws OXException {
        return MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, null, session);
    }

    private String getLocalizedName(Session session, Locale locale, String category) throws OXException {

        String translation = MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_LANGUAGE_PREFIX + locale.toString(), null, session);
        if (translation != null && !translation.isEmpty()) {
            return translation;
        }
        return MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FALLBACK, category, session);
    }

    String[] getSystemCategoryNames(Session session) throws OXException {
        String categoriesString = MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_IDENTIFIERS, null, session);
        if (Strings.isEmpty(categoriesString)) {
            return new String[0];
        }

        String result[] = Strings.splitByComma(categoriesString);
        return result == null ? new String[0] : result;
    }

    private String[] getUserCategoryNames(Session session) throws OXException {
        String categoriesString = MailCategoriesConfigUtil.getValueFromProperty(MailCategoriesConstants.MAIL_USER_CATEGORIES_IDENTIFIERS, null, session);
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
        return MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_SWITCH, true, session);
    }

    @Override
    public boolean isForced(Session session) throws OXException {
        return MailCategoriesConfigUtil.getBoolFromProperty(MailCategoriesConstants.MAIL_CATEGORIES_FORCE_SWITCH, false, session);
    }

    @Override
    public void enable(Session session, boolean enable) throws OXException {
        MailCategoriesConfigUtil.setProperty(MailCategoriesConstants.MAIL_CATEGORIES_SWITCH, String.valueOf(enable), session);
        if (enable) {
            initMailCategories(session);
        }
    }

    String generateFlag(String category) {
        StringBuilder builder = new StringBuilder(FLAG_PREFIX);
        builder.append(category.hashCode());
        return builder.toString();
    }

    private void setRule(Session session, String category, MailCategoryRule rule) throws OXException {
        String flag = null;
        if (!category.equals(MailCategoriesConstants.GENERAL_CATEGORY_ID)) {
            flag = getFlagByCategory(session, category);
            if (Strings.isEmpty(flag)) {
                flag = generateFlag(category);
            }
        }
        if ((flag == null && rule.getFlag() != null) || (flag != null && !flag.equals(rule.getFlag()))) {
            throw MailCategoriesRuleEngineExceptionCodes.INVALID_RULE.create();
        }
        MailCategoriesRuleEngine ruleEngine = Services.getService(MailCategoriesRuleEngine.class);
        if (ruleEngine == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailCategoriesRuleEngine.class.getSimpleName());
        }
        ruleEngine.setRule(session, rule, RuleType.CATEGORY);
    }

    private MailCategoryRule getRule(Session session, String category) throws OXException {
        MailCategoriesRuleEngine ruleEngine = Services.getService(MailCategoriesRuleEngine.class);
        if (ruleEngine == null) {
            throw MailCategoriesExceptionCodes.SERVICE_UNAVAILABLE.create(MailCategoriesRuleEngine.class.getSimpleName());
        }
        String flag = null;
        if (!category.equals(MailCategoriesConstants.GENERAL_CATEGORY_ID)) {
            flag = getFlagByCategory(session, category);
            if (Strings.isEmpty(flag)) {
                flag = generateFlag(category);
                MailCategoriesConfigUtil.setProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + category + MailCategoriesConstants.MAIL_CATEGORIES_FLAG, flag, session);
            }
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

    SearchTerm<?> getSearchTerm(MailCategoryRule rule) {
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
    public void trainCategory(String category, List<String> addresses, boolean createRule, ReorganizeParameter reorganize, Session session) throws OXException {
        if (!createRule && !reorganize.isReorganize()) {
            // nothing to do
            return;
        }

        String flag = null;
        if (!category.equals(MailCategoriesConstants.GENERAL_CATEGORY_ID)) {
            flag = getFlagByCategory(session, category);
            if (Strings.isEmpty(flag)) {
                flag = generateFlag(category);
            }
        }

        MailCategoryRule newRule = null;
        if (createRule) {
            // create new rule

            // Remove from old rules
            for (String mailAddress : addresses) {
                removeMailFromRules(session, mailAddress);
            }

            // Update rule
            MailCategoryRule oldRule = getRule(session, category);
            for (String mailAddress : addresses) {
                if (newRule == null) {
                    if (null == oldRule) {        // Create new rule
                        newRule = new MailCategoryRule(Collections.singletonList(FROM_HEADER), new ArrayList<>(Collections.singleton(mailAddress)), flag);
                    } else {

                        if (oldRule.getSubRules() != null && !oldRule.getSubRules().isEmpty()) {
                            if (oldRule.isAND()) {
                                newRule = new MailCategoryRule(flag, false);
                                newRule.addSubRule(oldRule);
                                newRule.addSubRule(new MailCategoryRule(Collections.singletonList(FROM_HEADER), new ArrayList<>(Collections.singleton(mailAddress)), flag));
                            } else {
                                newRule = oldRule;
                                newRule.addSubRule(new MailCategoryRule(Collections.singletonList(FROM_HEADER), new ArrayList<>(Collections.singleton(mailAddress)), flag));
                            }
                        } else {
                            if (!oldRule.getHeaders().contains(FROM_HEADER)) {
                                oldRule.getHeaders().add(FROM_HEADER);
                            }
                            if (!oldRule.getValues().contains(mailAddress)) {
                                oldRule.getValues().add(mailAddress);
                            }
                            newRule = oldRule;
                        }

                    }
                } else {
                    if (!newRule.getValues().contains(mailAddress)) {
                        newRule.getValues().add(mailAddress);
                    }
                }
            }

            // remove all previous category flags
            String[] flagsToRemove = getAllFlags(session, false, false);
            newRule.addFlagsToRemove(flagsToRemove);

            setRule(session, category, newRule);

        } else {
            // create rule for reorganize only
            for (String mailAddress : addresses) {
                if (newRule == null) {
                    newRule = new MailCategoryRule(Collections.singletonList(FROM_HEADER), new ArrayList<>(Collections.singleton(mailAddress)), flag);
                } else {
                    if (!newRule.getValues().contains(mailAddress)) {
                        newRule.getValues().add(mailAddress);
                    }
                }
            }

            // remove all previous category flags
            String[] flagsToRemove = getAllFlags(session, false, false);
            newRule.addFlagsToRemove(flagsToRemove);
        }

        // Reorganize if necessary
        if (reorganize.isReorganize()) {
            List<OXException> warnings = reorganize.getWarnings();
            try {
                SearchTerm<?> searchTerm = getSearchTerm(newRule);
                FullnameArgument fa = new FullnameArgument("INBOX");
                if (searchTerm != null) {
                    MailCategoriesOrganizer.organizeExistingMails(session, fa.getFullName(), searchTerm, flag, newRule.getFlagsToRemove());
                }
            } catch (OXException e) {
                if (warnings.isEmpty()) {
                    warnings.add(MailCategoriesOrganizeExceptionCodes.UNABLE_TO_ORGANIZE.create());
                }
            }
        }

    }

    @Override
    public void updateConfigurations(List<MailCategoryConfig> configs, Session session, Locale locale) throws OXException {
        List<MailCategoryConfig> oldConfigs = getAllCategories(session, locale, false, true);

        for (MailCategoryConfig newConfig : configs) {

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
                String name = oldConfig.getName();
                if (!newConfig.getName().equals(name)) {
                    if (isSystemCategory(oldConfig.getCategory(), session)) {
                        throw MailCategoriesExceptionCodes.CHANGE_NAME_NOT_ALLOWED.create(oldConfig.getCategory());
                    }
                    rename = true;
                }

                if (switchStatus) {
                    MailCategoriesConfigUtil.activateProperty(oldConfig.getCategory(), newConfig.isEnabled(), session);
                }
                if (rename) {
                    MailCategoriesConfigUtil.setProperty(MailCategoriesConstants.MAIL_CATEGORIES_PREFIX + oldConfig.getCategory() + MailCategoriesConstants.MAIL_CATEGORIES_NAME, newConfig.getName(), session);
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

    @Override
    public String getInitStatus(Session session) throws OXException {
        return MailCategoriesConfigUtil.getValueFromProperty(INIT_TASK_STATUS_PROPERTY, "notyetstarted", session);
    }

    void initMailCategories(Session session) throws OXException {

        CapabilityService capService = Services.getService(CapabilityService.class);
        Boolean capability = capService.getCapabilities(session).contains(new Capability("mail_categories"));
        if (!capability) {
            return;
        }
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        if (configViewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class);
        }
        ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());


        Boolean apply = view.get(MailCategoriesConstants.APPLY_OX_RULES_PROPERTY, Boolean.class);


        if (apply == null || !apply) {
            return;
        }

        final ConfigProperty<String> hasRun = view.property("user", MailCategoriesConfigServiceImpl.INIT_TASK_STATUS_PROPERTY, String.class);
        if (hasRun.isDefined() && !hasRun.get().equals(STATUS_NOT_YET_STARTED)) {
            return;
        }
        ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
        threadPoolService.submit(new InitTask(session, hasRun));

    }

    private class InitTask implements Task<Boolean> {

        /**
         * Initializes a new {@link MailCategoriesConfigServiceImpl.InitTask}.
         */
        public InitTask(Session session, ConfigProperty<String> hasRun) {
            super();
            this.session = session;
            this.hasRun = hasRun;
        }

        Session session;
        ConfigProperty<String> hasRun;

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {}

        @Override
        public void beforeExecute(Thread t) {}

        @Override
        public void afterExecute(Throwable t) {}

        @Override
        public Boolean call() throws Exception {
            try {
                hasRun.set(STATUS_RUNNING);
                MailCategoriesRuleEngine engine = Services.getService(MailCategoriesRuleEngine.class);
                if (engine == null) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesRuleEngine.class);
                }

                MailCategoriesConfigServiceImpl mailCategoriesService = MailCategoriesConfigServiceImpl.getInstance();
                String categoryNames[] = mailCategoriesService.getSystemCategoryNames(session);
                List<MailCategoryRule> rules = new ArrayList<>();
                for (String categoryName : categoryNames) {
                    String propValue = MailCategoriesConfigUtil.getValueFromProperty(RULE_DEFINITION_PROPERTY_PREFIX + categoryName, "", session);
                    if (propValue.length() == 0) {
                        continue;
                    }
                    String addresses[] = Strings.splitByComma(propValue.trim());
                    String flag = mailCategoriesService.getFlagByCategory(session, categoryName);
                    if (flag == null) {
                        flag = mailCategoriesService.generateFlag(categoryName);
                    }
                    MailCategoryRule rule = new MailCategoryRule(Collections.singletonList(FROM_HEADER), Arrays.asList(addresses), flag);
                    rules.add(rule);
                }
                engine.initRuleEngineForUser(session, rules);
                FullnameArgument fa = new FullnameArgument("INBOX");
                for (MailCategoryRule rule : rules) {
                    SearchTerm<?> searchTerm = mailCategoriesService.getSearchTerm(rule);
                    MailCategoriesOrganizer.organizeExistingMails(session, fa.getFullname(), searchTerm, rule.getFlag(), null);
                }
                hasRun.set(STATUS_FINISHED);
            } catch (Exception e) {
                try {
                    hasRun.set(STATUS_NOT_YET_STARTED);
                } catch (OXException ox) {
                }
                return false;
            }
            return true;
        }

    }

}
