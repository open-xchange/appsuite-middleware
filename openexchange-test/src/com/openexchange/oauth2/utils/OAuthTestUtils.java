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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth2.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import com.openexchange.java.Charsets;

/**
 * {@link OAuthTestUtils}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class OAuthTestUtils {

    /**
     *
     * @param queryOrFragment
     * @return
     */
    private static Map<String, String> extractRedirectParams(String queryOrFragment) {
        if (queryOrFragment == null) {
            return Collections.emptyMap();
        }
        Map<String, String> redirectParams = new HashMap<String, String>();

        List<NameValuePair> parse = URLEncodedUtils.parse(queryOrFragment, Charsets.UTF_8);
        for (NameValuePair current : parse) {
            redirectParams.put(current.getName(), current.getValue());
        }

        return redirectParams;
    }

    public static URI getDecodedURI(String redirectLocation) throws URISyntaxException {
        Map<String, String> redirectParamsFromParams = OAuthTestUtils.extractRedirectParamsFromQuery(redirectLocation);

        URIBuilder uriBuilder = new URIBuilder(redirectLocation);
        for (Entry<String, String> s : redirectParamsFromParams.entrySet()) {
            uriBuilder.addParameter(s.getKey(), s.getValue());
        }

        Map<String, String> redirectParamsFromFragment = OAuthTestUtils.extractRedirectParamsFromFragment(redirectLocation);
        StringBuilder fragment = new StringBuilder();
        for (Entry<String, String> lFragment : redirectParamsFromFragment.entrySet()) {
            fragment.append("&"+lFragment.getKey()+"="+lFragment.getValue());
        }
        uriBuilder.setFragment(fragment.toString());
        return uriBuilder.build();
    }

    /**
     * @param redirectLocation
     * @return
     * @throws URISyntaxException
     */
    public static Map<String, String> extractRedirectParamsFromFragment(String redirectLocation) throws URISyntaxException {
        URI uri = new URI(redirectLocation);
        return extractRedirectParams(uri.getFragment());
    }

    /**
     * @param redirectLocation
     * @return
     * @throws URISyntaxException
     */
    public static Map<String, String> extractRedirectParamsFromQuery(String redirectLocation) throws URISyntaxException {
        URI uri = new URI(redirectLocation);
        return extractRedirectParams(uri.getQuery());
    }

    public static List<NameValuePair> fromParamsMap(Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<>(params.size());
        for (Entry<String, String> entry : params.entrySet()) {
            String name = entry.getKey();
            if (name != null) {
                pairs.add(new BasicNameValuePair(name, entry.getValue()));
            }
        }
        return pairs;
    }
}
