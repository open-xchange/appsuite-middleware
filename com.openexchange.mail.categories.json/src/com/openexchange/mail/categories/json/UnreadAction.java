/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.categories.json;

import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
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
public class UnreadAction extends AbstractCategoriesAction {

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
