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

import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Categories;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.Localizable;
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
     * Name of the JSON attribute containing the error message.
     */
    public static final String ERROR = "error";

    /**
     * Name of the JSON attribute containing the error categories.
     */
    public static final String ERROR_CATEGORIES = "categories";

    /**
     * <b>Deprecated</b>: Name of the JSON attribute containing the error category.
     */
    public static final String ERROR_CATEGORY = "category";

    /**
     * Name of the JSON attribute containing the error code.
     */
    public static final String ERROR_CODE = "code";

    /**
     * Name of the JSON attribute containing the unique error identifier.
     */
    public static final String ERROR_ID = "error_id";

    /**
     * Name of the JSON attribute containing the array of the error message attributes.
     */
    public static final String ERROR_PARAMS = "error_params";

    /**
     * Name of the JSON attribute containing the stacks of the error.
     */
    public static final String ERROR_STACK = "error_stack";

    /**
     * Name of the JSON attribute containing the rather technical error description.
     */
    public static final String ERROR_DESC = "error_desc";

    /**
     * Initializes a new {@link SwitchAction}.
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
                JSONObject o = new JSONObject(2).put(CATEGORY_FIELD, resultObject.getCategory());
                addException(o, ERROR, resultObject.getException(), session.getUser().getLocale());
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

    /**
     * Writes specified exception to given JSON object using passed locale (if no other locale specified through {@link OXExceptionConstants#PROPERTY_LOCALE}.
     *
     * @param json The JSON object
     * @param errorKey The key value for the error value inside the JSON object
     * @param exception The exception to write
     * @param locale The locale
     * @param includeStackTraceOnError <code>true</code> to append stack trace elements to JSON object; otherwise <code>false</code>
     * @throws JSONException If writing JSON fails
     * @see OXExceptionConstants#PROPERTY_LOCALE
     */
    public static void addException(final JSONObject json, String errorKey, final OXException exception, final Locale locale) throws JSONException {
        final Locale l;
        {
            final String property = exception.getProperty(OXExceptionConstants.PROPERTY_LOCALE);
            if (null == property) {
                l = LocaleTools.getSaneLocale(locale);
            } else {
                final Locale parsedLocale = LocaleTools.getLocale(property);
                l = null == parsedLocale ? LocaleTools.getSaneLocale(locale) : parsedLocale;
            }
        }
        json.put(errorKey, exception.getDisplayMessage(l));
        /*
         * Put argument JSON array for compatibility reasons
         */
        {
            Object[] args = exception.getLogArgs();
            if ((null == args) || (0 == args.length)) {
                args = exception.getDisplayArgs();
            }
            // For compatibility
            if ((null == args) || (0 == args.length)) {
                json.put(ERROR_PARAMS, new JSONArray(0));
            } else {
                final JSONArray jArgs = new JSONArray(args.length);
                for (int i = 0; i < args.length; i++) {
                    Object obj = args[i];
                    jArgs.put(obj instanceof Localizable ? obj.toString() : obj);
                }
                json.put(ERROR_PARAMS, jArgs);
            }
        }
        /*
         * Categories
         */
        {
            List<Category> categories = exception.getCategories();
            int size = categories.size();
            if (1 == size) {
                Category category = categories.get(0);
                json.put(ERROR_CATEGORIES, category.toString());
                // For compatibility
                int number = Categories.getFormerCategoryNumber(category);
                if (number > 0) {
                    json.put(ERROR_CATEGORY, number);
                }
            } else {
                if (size <= 0) {
                    // Empty JSON array
                    json.put(ERROR_CATEGORIES, new JSONArray(0));
                } else {
                    JSONArray jArray = new JSONArray(size);
                    for (final Category category : categories) {
                        jArray.put(category.toString());
                    }
                    json.put(ERROR_CATEGORIES, jArray);
                    // For compatibility
                    int number = Categories.getFormerCategoryNumber(categories.get(0));
                    if (number > 0) {
                        json.put(ERROR_CATEGORY, number);
                    }
                }
            }
        }
        json.put(ERROR_CODE, exception.getErrorCode());
        json.put(ERROR_ID, exception.getExceptionId());
        json.put(ERROR_DESC, exception.getSoleMessage());
    }

}
