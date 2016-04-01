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

package com.openexchange.snippet;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.HTMLDetector;
import com.openexchange.snippet.internal.Services;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link SnippetUtils} - Some utility methods for Snippet module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SnippetUtils {

    /**
     * Initializes a new {@link SnippetUtils}.
     */
    private SnippetUtils() {
        super();
    }

    /**
     * Sanitizes given Snippet content.
     *
     * @param content The content
     * @return The sanitized content
     */
    public static String sanitizeContent(String content) {
        if (com.openexchange.java.Strings.isEmpty(content) || !HTMLDetector.containsHTMLTags(content, true)) {
            return content;
        }

        return sanitizeHtmlContent(content);
    }

    /**
     * Sanitizes given Snippet HTML content.
     *
     * @param content The HTML content
     * @return The sanitized HTML content
     */
    public static String sanitizeHtmlContent(String content) {
        HtmlService service = Services.getService(HtmlService.class);
        if (null == service) {
            return content;
        }

        return sanitizeHtmlContent(content, service);
    }

    /**
     * Sanitizes given Snippet HTML content.
     *
     * @param content The HTML content
     * @param service The HTML service to use
     * @return The sanitized HTML content
     */
    public static String sanitizeHtmlContent(String content, HtmlService service) {
        try {
            String retval = service.sanitize(content, null, false, null, null);

            int start = retval.indexOf("<body>");
            if (start >= 0) {
                start += 6;
                int end = retval.indexOf("</body>", start);
                if (end > 0) {
                    retval = retval.substring(start, end).trim();
                }
            }

            return retval;
        } catch (final Exception e) {
            // Ignore
            return content;
        }
    }

    /**
     * Parses the content type from miscellaneous JSON data.
     *
     * @param misc The miscellaneous JSON object
     * @return The extracted content type information or <code>"text/plain"</code> as fall-back
     * @throws OXException If content type cannot be extracted
     */
    public static String parseContentTypeFromMisc(final Object misc) throws OXException {
        if (misc instanceof JSONObject) {
            return parseContentTypeFromMisc((JSONObject) misc);
        }

        try {
            return parseContentTypeFromMisc(new JSONObject(misc.toString()));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    /**
     * Parses the content type from miscellaneous JSON data.
     *
     * @param misc The miscellaneous JSON object
     * @return The extracted content type information or <code>"text/plain"</code> as fall-back
     * @throws OXException If content type cannot be extracted
     */
    public static String parseContentTypeFromMisc(final String misc) throws OXException {
        try {
            return parseContentTypeFromMisc(new JSONObject(misc));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
    }

    /**
     * Parses the content type from miscellaneous JSON data.
     *
     * @param misc The miscellaneous JSON object
     * @return The extracted content type information or <code>"text/plain"</code> as fall-back
     * @throws OXException If content type cannot be extracted
     */
    public static String parseContentTypeFromMisc(final JSONObject misc) {
        String cts = misc.optString("content-type", null);
        return null == cts ? "text/plain" : cts;
    }

    /**
     * Get the image identifier stored in the misc JSONObject of the snippet.
     *
     * @param misc The misc JSONObject of the snippet
     * @return The image identifier or null
     * @throws OXException
     */
    public static String getImageId(final Object misc) throws OXException {
        String imageId = null;
        try {
            if (misc != null) {
                JSONObject m = new JSONObject(misc.toString());
                imageId = m.optString("imageId");
            }
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_BUILD_ERROR.create(e);
        }
        return imageId;
    }

}
