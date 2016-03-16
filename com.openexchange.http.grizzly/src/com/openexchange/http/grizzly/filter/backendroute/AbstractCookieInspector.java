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

package com.openexchange.http.grizzly.filter.backendroute;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.glassfish.grizzly.http.Cookie;


/**
 * {@link AbstractCookieInspector}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class AbstractCookieInspector {

    private static final Pattern JSESSIONID_WITHOUT_ROUTE = Pattern.compile("^\\w*$");
    /** Value of the backendRoute in pattern group 1 */
    private static final Pattern JSESSIONID_ROUTE_MATCHES_PATTERN = Pattern.compile("^\\w+\\.(.+)$");
    /** Value of the routeless JSessionId in froup 1 */
    private static final Pattern JSESSIONID_REMOVE_ROUTE_PATTERN = Pattern.compile("^(\\w+)(\\.(.+))?$");
    protected String backendRoute;
    protected Map<String, Cookie> cookieMap;

    /**
     * Fix the JSessionId
     * The JSessionId has to be fixed when:
     * <ol>
     *   <li>id: no route <-> backend: routeA</li>
     *   <li>id: routeB <-> backend: routeA</li>
     *   <li>id: routeB <-> backend: no route</li>
     * </ol>
     * The fix is to simply remove the current route from the id (if there is one to remove)
     * and add the new one (if there is one to add).
     * @return true if the jSessionId could be fixed, false if no matching cookie could be found.
     */
    public boolean fixJSessionId() {
        boolean result = false;
        Cookie jSessionIdCookie = cookieMap.get("JSESSIONID");
        if(jSessionIdCookie != null) {
            String jSessionId = jSessionIdCookie.getValue();
            String fixedValue = removeRouteFromJSessionId(jSessionId);
            if(!backendRoute.isEmpty()) {
                fixedValue= appendRouteToJSessionId(fixedValue, backendRoute);
            }
            jSessionIdCookie.setValue(fixedValue);
            result = true;
        }
        return result;
    }

    /**
     *
     * @param cookieValue
     * @return
     */
    private String removeRouteFromJSessionId(String cookieValue) {
        String result;
        Matcher idMatcher = JSESSIONID_REMOVE_ROUTE_PATTERN.matcher(cookieValue);
        if(idMatcher.find()) {
            result = idMatcher.group(1);
        } else {
            throw new IllegalStateException("Pattern didn't match cookie!");
        }
        return result;
    }

    /**
     * Append the backend route to the JSessionId.
     * @param jSessionId the current JSessionId value
     * @param backendRoute the backend route to append
     * @return JSessionId with backendRoute appended
     */
    private String appendRouteToJSessionId(String jSessionId, String backendRoute) {
        return new StringBuilder(jSessionId).append('.').append(backendRoute).toString();
    }

    /**
     * Check if the Cookie: header contains a JSESSIONID Cookie.
     * @return true if a JSESSIONID cookie was found in the header line
     */
    public boolean isJSessionIdExistant() {
        return cookieMap.containsKey("JSESSIONID");
    }

    /**
     * Check if the JSessionId value is valid when compared to the currently configured backend route.
     * @param backendRoute the currently configured backend route
     * @return true only if either the backend has no route configured and the JSessionId doesn't have a route appended
     *          or if the backend has a route configured and the JSessionid has that route appended.
     */
    public boolean isJSessionIdValid() {
        Cookie jSessionIdCookie = cookieMap.get("JSESSIONID");
        if(jSessionIdCookie == null) {
            return false;
        }
        String jSessionIdValue = jSessionIdCookie.getValue();
        return (backendRoute.isEmpty() && isJSessionIdWithoutRoute(jSessionIdValue)) || isJSessionIdWithCorrectRoute(backendRoute, jSessionIdValue);
    }

    /**
     * Check the JSessionId for an appended backend route in the form of <code>.[route];</code>
     * @param jSessionId the id to check
     * @return false if the id doesn't have a route attached, true otherwise
     */
    private boolean isJSessionIdWithoutRoute(String jSessionId) {
        return JSESSIONID_WITHOUT_ROUTE.matcher(jSessionId).find();
    }

    /**
     * Check if the appended backend route of the JSessionID matches the current backend configuration.
     * @param jSessionId the id to check
     * @return
     */
    private boolean isJSessionIdWithCorrectRoute(String backendRoute, String jSessionId) {
        boolean result = false;
        Matcher routeMatcher = JSESSIONID_ROUTE_MATCHES_PATTERN.matcher(jSessionId);
        if(routeMatcher.find()) {
            result = backendRoute.equals(routeMatcher.group(1));
        }
        return result;
    }

    /**
     * Get the JSessionid value from the header
     * @return the empty String if no value can be fond, the value otherwise
     */
    public String getJSessionIdValue() {
        Cookie result = cookieMap.get("JSESSIONID");
        return result == null ? "" : result.getValue();
    }
}
