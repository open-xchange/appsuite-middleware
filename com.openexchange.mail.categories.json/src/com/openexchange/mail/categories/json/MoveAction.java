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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
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
@Action(method = RequestMethod.PUT, name = "move", description = "Move given mails to the given category", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."), 
    @Parameter(name = "category_id", description = "The category identifier"),
}, responseDescription = "Response: Empty")
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
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
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
