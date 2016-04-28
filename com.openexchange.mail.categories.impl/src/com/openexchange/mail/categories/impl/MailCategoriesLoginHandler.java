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
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.mail.categories.MailCategoriesConstants;
import com.openexchange.mail.categories.ruleengine.MailCategoriesRuleEngine;
import com.openexchange.mail.categories.ruleengine.MailCategoryRule;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesLoginHandler} initializes the MailCategoriesRuleEngine if it is the first login after activation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesLoginHandler implements LoginHandlerService {

    private static final String INIT_TASK_RUN_PROPERTY = "com.openexchange.mail.categories.ruleengine.sieve.init.run";
    private static final String RULE_DEFINITION_PROPERTY_PREFIX = "com.openexchange.mail.categories.rules.";
    private static final String FROM_HEADER = "from";
    private ServiceLookup services;


    /**
     * Initializes a new {@link MailCategoriesLoginHandler}.
     */
    public MailCategoriesLoginHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void handleLogin(LoginResult login) throws OXException {

        Session session = login.getSession();
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        if (configViewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class);
        }
        ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());

        Boolean apply = view.get(MailCategoriesConstants.APPLY_OX_RULES_PROPERTY, Boolean.class);
        if (apply == null || !apply) {
            return;
        }

        final ConfigProperty<Boolean> hasRun = view.property("user", INIT_TASK_RUN_PROPERTY, Boolean.class);
        if (hasRun.isDefined() && hasRun.get()) {
            return;
        } else {
            hasRun.set(true);
        }
        try {
            MailCategoriesRuleEngine engine = services.getService(MailCategoriesRuleEngine.class);
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
        } catch (Exception e) {
            try {
                hasRun.set(false);
            } catch (OXException ox) {
            }
        }
    }

    @Override
    public void handleLogout(LoginResult logout) throws OXException {
        // nothing to do
    }

}
