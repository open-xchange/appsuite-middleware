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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoriesConstants;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
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
        ConfigViewFactory viewFactory = services.getService(ConfigViewFactory.class);
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

        // Update category
        MailCategoriesConfigService mailCategoriesService = services.getService(MailCategoriesConfigService.class);
        if (mailCategoriesService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class.getSimpleName());
        }

        // Check for filter description
        Map<String, Object> filterDesc = null;
        {
            Object data = requestData.getData();
            if (data instanceof JSONObject) {
                filterDesc = ((JSONObject) data).asMap();
            }
        }

        // Check for re-organize flag
        boolean reorganize = AJAXRequestDataTools.parseBoolParameter("reorganize", requestData);

        // Warnings
        List<OXException> warnings = new LinkedList<OXException>();

        mailCategoriesService.updateUserCategory(category, name, filterDesc, reorganize, warnings, session);

        MailCategoryConfig config = mailCategoriesService.getConfigByCategory(session, category);
        AJAXRequestResult result = new AJAXRequestResult(config, "mailCategoriesConfig");
        if (!warnings.isEmpty()) {
            result.addWarnings(warnings);
        }
        return result;
    }
}
