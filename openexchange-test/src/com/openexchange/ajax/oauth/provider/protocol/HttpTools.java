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
