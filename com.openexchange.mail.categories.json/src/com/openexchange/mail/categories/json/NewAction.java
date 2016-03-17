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

import javax.security.auth.Subject;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.ConfigurationService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.mail.categories.util.MailCategoriesOrganizer;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.MailFilterProperties;
import com.openexchange.mailfilter.MailFilterService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link NewAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
@Action(method = RequestMethod.PUT, name = "new", description = "Creates a new mail user category.", parameters = { 
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."), 
    @Parameter(name = "category", description = "The category identifier"), 
    @Parameter(name = "name", description = "The name of the category"), 
    @Parameter(name = "flag", description = "The name of the flag"),
    @Parameter(name = "reorganize", description = "A optional flag indicating if old mails should be reorganized."),
}, responseDescription = "Response: The newly created category configuration")
public class NewAction implements AJAXActionService {

    private final ServiceLookup LOOKUP;

    private static final Logger LOG = LoggerFactory.getLogger(NewAction.class);

    /**
     * Initializes a new {@link SwitchAction}.
     */
    public NewAction(ServiceLookup services) {
        super();
        LOOKUP = services;

    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {

        String flag = requestData.getParameter("flag");
        String category = requestData.getParameter("category");
        String name = requestData.getParameter("name");

        // create category
        MailCategoriesConfigService mailCategoriesService = LOOKUP.getService(MailCategoriesConfigService.class);
        if (mailCategoriesService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class.getSimpleName());
        }
        mailCategoriesService.addUserCategory(category, flag, name, session);

        // create filter
        
        Object data = requestData.getData();
        
        Rule rule = null;
        if (data != null && data instanceof JSONObject) {
            SearchableMailFilterTest mailFilterTest = null;
            try {
                mailFilterTest = new SearchableMailFilterTest((JSONObject) data, flag);
            } catch (JSONException e) {
                throw OXJSONExceptionCodes.JSON_READ_ERROR.create(data.toString());
            }
            rule = mailFilterTest.getRule();
            final MailFilterService mailFilterService = LOOKUP.getService(MailFilterService.class);
            int id = mailFilterService.createFilterRule(getCredentials(session, requestData), rule);
            LOG.debug("Created sieve filter '" + category + "' for user " + session.getUserId() + " in context " + session.getContextId() + " with id " + id);

            // reorganize if necessary
            String reorganize = requestData.getParameter("reorganize");
            if (reorganize != null && Boolean.valueOf(reorganize)) {
                SearchTerm<?> searchTerm = null;
                if (rule != null) {
                    searchTerm = mailFilterTest.getSearchTerm();
                }
                FullnameArgument fa = new FullnameArgument("INBOX");
                if (searchTerm != null) {
                    MailCategoriesOrganizer.organizeExistingMails(session, fa.getFullName(), searchTerm, flag, true);
                }
            }
        }

        MailCategoryConfig config = mailCategoriesService.getConfigByCategory(session, category);

        return new AJAXRequestResult(config, "mailCategoriesConfig");

    }

    public Credentials getCredentials(Session session, AJAXRequestData request) throws OXException {
        final ConfigurationService config = LOOKUP.getService(ConfigurationService.class);
        final String credsrc = config.getProperty(MailFilterProperties.Values.SIEVE_CREDSRC.property);
        final String loginName;
        if (MailFilterProperties.CredSrc.SESSION_FULL_LOGIN.name.equals(credsrc)) {
            loginName = session.getLogin();
        } else {
            loginName = session.getLoginName();
        }
        final String password = session.getPassword();
        final int userId = session.getUserId();
        final int contextId = session.getContextId();
        final Subject subject = (Subject) session.getParameter("kerberosSubject");
        return new Credentials(loginName, password, userId, contextId, null, subject);
    }

   
}
