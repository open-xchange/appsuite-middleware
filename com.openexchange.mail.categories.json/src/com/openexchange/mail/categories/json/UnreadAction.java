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

import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.mail.search.ANDTerm;
import com.openexchange.mail.search.BooleanTerm;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.UserFlagTerm;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UnreadAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
@Action(method = RequestMethod.GET, name = "unread", description = "Retrieves the unread count of all categories", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "category_ids", description = "A comma separated list of category identifiers. If set only the unread counters of this categories are retrieved.")
}, responseDescription = "Response: A JSON Object containing the category identifiers and the corresponding unread count as key value pairs")
public class UnreadAction extends AbstractCategoriesAction {

    private static final String ACTION = "categories";

    private static final String PARAMETER_CATEGORY_IDS = "category_ids";

    /**
     * Initializes a new {@link SwitchAction}.
     */
    public UnreadAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        if (!session.getUserPermissionBits().hasWebMail()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("mail/categories");
        }

        MailCategoriesConfigService categoriesConfigService = services.getService(MailCategoriesConfigService.class);
        if (categoriesConfigService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class.getSimpleName());
        }

        if (!categoriesConfigService.isEnabled(session)) {
            throw AjaxExceptionCodes.DISABLED_ACTION.create(ACTION);
        }

        String idsString = requestData.getParameter(PARAMETER_CATEGORY_IDS);
        List<String> idsList = null;
        if (idsString != null) {
            String[] ids = Strings.splitByComma(idsString);
            idsList = Arrays.asList(ids);
        }

        List<MailCategoryConfig> categories = categoriesConfigService.getAllCategories(session, session.getUser().getLocale(), true, false);
        String[] unkeywords = categoriesConfigService.getAllFlags(requestData.getSession(), true, true);
        String[] flags = getFlagsFrom(categories);

        JSONObject resultObject = new JSONObject(4);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = MailAccess.getInstance(session);
            mailAccess.connect();

            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            SearchTerm<?> searchTerm = null;

            // General case
            int unread = 0;
            if (flags != null && flags.length != 0) {
                searchTerm = new UserFlagTerm(flags, false);
                unread = messageStorage.getUnreadCount("INBOX", searchTerm);
            } else {
                unread = messageStorage.getUnreadCount("INBOX", BooleanTerm.TRUE);
            }
            resultObject.put("general", unread);

            for (MailCategoryConfig category : categories) {
                if (idsList != null && !idsList.contains(category.getCategory())) {
                    continue;
                }
                if (unkeywords != null && unkeywords.length != 0 && categoriesConfigService.isSystemCategory(category.getCategory(), session)) {
                    searchTerm = new ANDTerm(new UserFlagTerm(category.getFlag(), true), new UserFlagTerm(unkeywords, false));
                } else {
                    searchTerm = new UserFlagTerm(category.getFlag(), true);
                }
                unread = messageStorage.getUnreadCount("INBOX", searchTerm);
                resultObject.put(category.getCategory(), unread);
            }

            return new AJAXRequestResult(resultObject, "json");
        } finally {
            if (null != mailAccess) {
                mailAccess.close();
            }
        }
    }

    private String[] getFlagsFrom(List<MailCategoryConfig> categories) {
        String[] flags = new String[categories.size()];
        int x = 0;
        for (MailCategoryConfig category : categories) {
            flags[x++] = category.getFlag();
        }
        return flags;
    }

}
