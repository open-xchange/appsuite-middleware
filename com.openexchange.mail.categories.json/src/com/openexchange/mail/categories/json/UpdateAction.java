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

package com.openexchange.mail.categories.json;

import java.util.Collections;
import java.util.List;
import org.apache.jsieve.SieveException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoriesConstants;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.mail.categories.exception.MailCategoriesJSONExceptionCodes;
import com.openexchange.mail.categories.util.MailCategoriesOrganizer;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
@Action(method = RequestMethod.PUT, name = "updates", description = "Updates an existing mail user category.", parameters = { 
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."), 
    @Parameter(name = "category", description = "The category identifier"), 
    @Parameter(name = "name", description = "The new name of the category"), 
    @Parameter(name = "reorganize", description = "A optional flag indicating if old mails should be reorganized."),
}, responseDescription = "Response: The updated category configuration")
public class UpdateAction extends AbstractCategoriesAction {

    /**
     * Initializes a new {@link UpdateAction}.
     * 
     * @param services The service lookup
     */
    public UpdateAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ConfigViewFactory viewFactory = LOOKUP.getService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getSimpleName());
        }
        ConfigView view = viewFactory.getView(requestData.getSession().getUserId(), requestData.getSession().getContextId());
        Boolean enabled = view.get(MailCategoriesConstants.MAIL_CATEGORIES_USER_SWITCH, Boolean.class);
        if (!enabled) {
            throw AjaxExceptionCodes.DISABLED_ACTION.create("new");
        }

        String category = requestData.getParameter("category");
        String name = requestData.getParameter("name");

        // update category name
        MailCategoriesConfigService mailCategoriesService = LOOKUP.getService(MailCategoriesConfigService.class);
        if (mailCategoriesService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class.getSimpleName());
        }
        String flag = mailCategoriesService.generateFlag(category);
        if (!Strings.isEmpty(name)) {
            mailCategoriesService.updateUserCategoryName(category, name, session);
        }

        final MailFilterService mailFilterService = LOOKUP.getService(MailFilterService.class);
        if (mailFilterService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailFilterService.class);
        }

        // update filter
        Rule oldRule = null;
        SearchableMailFilterRule mailFilterRule = null;
        Credentials creds = getCredentials(session, requestData);
        try {
            Object data = requestData.getData();

            List<Rule> rules = mailFilterService.listRules(creds, "category");
            for (Rule rule : rules) {
                if (rule.getRuleComment().getRulename().equals(flag)) {
                    oldRule = rule;
                    break;
                }
            }

            if (data != null && data instanceof JSONObject) {
                Rule newRule = null;
                try {
                    mailFilterRule = new SearchableMailFilterRule((JSONObject) data, flag);
                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_READ_ERROR.create(data.toString());
                }
                newRule = mailFilterRule.getRule();
                if (oldRule != null) {
                    mailFilterService.updateFilterRule(creds, newRule, oldRule.getUniqueId());
                } else {
                    mailFilterService.createFilterRule(creds, newRule);
                    mailFilterService.reorderRules(creds, new int[0]);
                }
            }
        } catch (OXException | SieveException e) {
            // undo category creation
            mailCategoriesService.removeUserCategory(category, session);
            if (e instanceof OXException) {
                throw (OXException) e;
            } else {
                throw MailFilterExceptionCode.handleSieveException((SieveException) e);
            }
        }

        OXException warning = null;
        // reorganize if necessary
        try {
            String reorganize = requestData.getParameter("reorganize");
            if (reorganize != null && Boolean.valueOf(reorganize)) {
                SearchTerm<?> searchTerm = null;
                if (mailFilterRule != null) {
                    searchTerm = mailFilterRule.getSearchTerm();
                } else {
                    if (oldRule != null) {
                        searchTerm = new SearchableMailFilterRule(oldRule.getTestCommand(), flag).getSearchTerm();
                    } else {
                        warning = MailCategoriesJSONExceptionCodes.UNABLE_TO_ORGANIZE.create();
                        throw warning;
                    }
                }
                FullnameArgument fa = new FullnameArgument("INBOX");
                if (searchTerm != null) {
                    MailCategoriesOrganizer.organizeExistingMails(session, fa.getFullName(), searchTerm, flag, true);
                }
            }
        } catch (OXException e) {
            if (warning == null) {
                warning = MailCategoriesJSONExceptionCodes.UNABLE_TO_ORGANIZE.create();
            }
        }

        MailCategoryConfig config = mailCategoriesService.getConfigByCategory(session, category);
        AJAXRequestResult result = new AJAXRequestResult(config, "mailCategoriesConfig");
        if (warning != null) {
            result.addWarnings(Collections.singleton(warning));
        }
        return result;
    }
}
