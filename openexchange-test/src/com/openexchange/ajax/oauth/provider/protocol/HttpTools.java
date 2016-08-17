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

package com.openexchange.ajax.oauth.provider.protocol;

import static org.junit.Assert.assertNotNull;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.internal.parser.HtmlParser;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HttpTools {

    public static Map<String, String> extractQueryParams(URI uri) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        String[] redirectParamPairs = URLDecoder.decode(uri.getRawQuery(), "UTF-8").split("&");
        for (String pair : redirectParamPairs) {
            String[] split = pair.split("=");
            params.put(split[0], split[1]);
        }

        return params;
    }

    /**
     * Gets the location header from an {@link HttpResponse}.
     *
     * @param response The response
     * @return The header value as {@link URI}
     * @throws URISyntaxException if the headers value is no valid URI
     * @throws AssertionError if no location header is present
     */
    public static URI getRedirectLocation(HttpResponse response) throws URISyntaxException {
        Header header = response.getFirstHeader(HttpHeaders.LOCATION);
        assertNotNull("No location header present in response", header);
        return new URI(header.getValue());
    }

    /**
     * Returns all hidden form fields from an HTML form as name-value pairs.
     *
     * @param form The HTML form as String
     * @return A map of fields
     */
    public static Map<String, String> getHiddenFormFields(String form) {
        final Map<String, String> params = new HashMap<>();
        HtmlParser.parse(form, new HtmlHandler() {

            @Override
            public void handleXMLDeclaration(String version, Boolean standalone, String encoding) {

            }

            @Override
            public void handleText(String text, boolean ignorable) {

            }

            @Override
            public void handleStartTag(String tag, Map<String, String> attributes) {
                handleTag(tag, attributes);
            }

            @Override
            public void handleSimpleTag(String tag, Map<String, String> attributes) {
                handleTag(tag, attributes);
            }

            private void handleTag(String tag, Map<String, String> attributes) {
                if ("input".equals(tag) && "hidden".equals(attributes.get("type"))) {
                    String name = attributes.get("name");
                    String value = attributes.get("value");
                    if (name != null && value != null) {
                        params.put(name, value);
                    }
                }
            }

            @Override
            public void handleError(String errorMsg) {

            }

            @Override
            public void handleEndTag(String tag) {

            }

            @Override
            public void handleDocDeclaration(String docDecl) {

            }

            @Override
            public void handleComment(String comment) {

            }

            @Override
            public void handleCDATA(String text) {

            }
        });

        return params;
    }

}
