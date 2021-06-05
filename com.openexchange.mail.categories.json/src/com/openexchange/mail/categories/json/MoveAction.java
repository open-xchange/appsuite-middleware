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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailObjectParameter;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MoveAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MoveAction extends AbstractCategoriesAction {

    private static final String CATEGORY_ID_PARAMETER = "category_id";
    // Request body fields
    private static final String FIELD_MAIL_ID = "id";
    private static final String FIELD_FOLDER_ID = "folder_id";

    /**
     * Initializes a new {@link MoveAction}.
     * 
     * @param services
     */
    protected MoveAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException {
        MailCategoriesConfigService mailCategoriesService = services.getService(MailCategoriesConfigService.class);
        if (mailCategoriesService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class.getSimpleName());
        }

        String category = requestData.requireParameter(CATEGORY_ID_PARAMETER);

        Object data = requestData.getData();
        if (!(data instanceof JSONArray)) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        JSONArray array = (JSONArray) data;
        List<MailObjectParameter> mails = new ArrayList<>();
        for (Object mailObj : array.asList()) {
            if (!(mailObj instanceof Map<?, ?>)) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            @SuppressWarnings("unchecked") 
            Map<String, Object> mailData = (Map<String, Object>) mailObj;
            if (!mailData.containsKey(FIELD_FOLDER_ID) || !mailData.containsKey(FIELD_MAIL_ID)) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
            try {
                mails.add(new MailObjectParameter((String) mailData.get(FIELD_MAIL_ID), (String) mailData.get(FIELD_FOLDER_ID)));
            } catch (ClassCastException e) {
                throw AjaxExceptionCodes.BAD_REQUEST.create();
            }
        }

        mailCategoriesService.addMails(session, mails, category);
        
        return new AJAXRequestResult();
    }

}
