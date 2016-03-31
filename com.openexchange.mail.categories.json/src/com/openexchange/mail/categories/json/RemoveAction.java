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

import static com.openexchange.ajax.writer.ResponseWriter.addException;
import static com.openexchange.ajax.writer.ResponseWriter.newWriteExceptionProps;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoriesServiceResult;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RemoveAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@Action(method = RequestMethod.GET, name = "remove", description = "Removes a mail user category.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "category", description = "The category identifier"),
}, responseDescription = "Response: If successfull a JSON response, otherwise an exception")
public class RemoveAction extends AbstractCategoriesAction {

    private static final String PARAMETER_CATEGORY_IDS = "category_ids";

    private static final String CATEGORY_FIELD = "mail_category";

    /**
     * Initializes a new {@link RemoveAction}.
     *
     * @param services The service look-up
     */
    public RemoveAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, ServerSession session) throws OXException, JSONException {
        String[] ids = Strings.splitByComma(requestData.requireParameter(PARAMETER_CATEGORY_IDS));
        if (null == ids || ids.length == 0) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_CATEGORY_IDS);
        }

        MailCategoriesConfigService categoriesService = services.getService(MailCategoriesConfigService.class);
        if (categoriesService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MailCategoriesConfigService.class);
        }

        List<MailCategoriesServiceResult> resultObjects = categoriesService.removeUserCategories(ids, session);
        JSONObject result = new JSONObject(1);
        JSONArray array = new JSONArray(resultObjects.size());
        for (MailCategoriesServiceResult resultObject : resultObjects) {
            if (resultObject.hasError()) {
                JSONObject o = new JSONObject(10).put(CATEGORY_FIELD, resultObject.getCategory());
                addException(o, resultObject.getException(), session.getUser().getLocale(), newWriteExceptionProps().checkIncludeStackTraceOnError(false).checkProblematic(false).checkTruncated(true));
                array.put(o);
            }
        }
        result.put("errors", array);
        if (result.length() == 0) {
            return new AJAXRequestResult();
        } else {
            return new AJAXRequestResult(result, "apiResponse");
        }
    }

}
