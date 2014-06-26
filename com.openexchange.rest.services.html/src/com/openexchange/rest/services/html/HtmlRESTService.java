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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rest.services.html;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.rest.services.OXRESTService;
import com.openexchange.rest.services.annotations.PUT;
import com.openexchange.rest.services.annotations.ROOT;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * The {@link HtmlRESTService} allows clients to process HTML content.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@ROOT("/htmlproc/v1")
public class HtmlRESTService extends OXRESTService<HtmlService> {

    /**
     * Initializes a new {@link HtmlRESTService}.
     */
    public HtmlRESTService() {
        super();
    }

    /**
     * <pre>
     * PUT /rest/htmlproc/v1/sanitize
     * &lt;HTML-content&gt;
     * </pre>
     *
     * Retrieves the sanitized version of passed content.<br>
     * Return an Object with a property to value mapping or a status 404 if a property is not set.
     */
    @PUT("/sanitize")
    public Object getSanitizedHtmlContent() throws OXException {
        Object data = request.getData();
        if (data instanceof String) {
            return context.sanitize((String) data, null, true, new boolean[1], null);
        }

        if (data instanceof JSONObject) {
            try {
                final String sanitized = context.sanitize(((JSONObject) data).getString("content"), null, true, new boolean[1], null);
                return new JSONObject(2).put("content", sanitized);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }

        throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
    }

    /**
     * <pre>
     * PUT /rest/htmlproc/v1/sanitizeKeepImages
     * &lt;HTML-content&gt;
     * </pre>
     *
     * Retrieves the sanitized version of passed content.<br>
     * Return an Object with a property to value mapping or a status 404 if a property is not set.
     */
    @PUT("/sanitizeKeepImages")
    public Object getSanitizedHtmlContentWithoutExternalImages() throws OXException {
        Object data = request.getData();
        if (data instanceof String) {
            response.setHeader("Content-Type", OXRESTService.CONTENT_TYPE_HTML);
            return context.sanitize((String) data, null, false, new boolean[1], null);
        }

        if (data instanceof JSONObject) {
            try {
                final String sanitized = context.sanitize(((JSONObject) data).getString("content"), null, false, new boolean[1], null);
                response.setContentType(OXRESTService.CONTENT_TYPE_JAVASCRIPT);
                return new JSONObject(2).put("content", sanitized);
            } catch (JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }

        throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
    }

}
