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

package com.openexchange.http.grizzly.servletfilters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import com.openexchange.http.grizzly.Configuration;
import com.openexchange.log.LogFactory;
//import com.google.common.base.Optional;

/**
 * {@link BackendRouteFilter}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class BackendRouteFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(BackendRouteFilter.class);
    private static final String JSESSIONID_COOKIE="JSESSIONID";
    private static final String JSESSIONID_URL_PARAMETER="JSESSIONID";
    private String backendRoute="";
    private String alias="";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        backendRoute = filterConfig.getInitParameter("backendRoute");
        alias = filterConfig.getInitParameter("alias");
    }

    /*
     * JSessionID can be passed as param or as cookie. Cookie may appear multiple times!
     * We have to look at three different conditions:
     * 
     * 1: If the request contains a jsessionid as url parameter:
     *      - if no route OR a matching route is appended to the jsessionid:
     *          - check if session is still valid
     *              - if still valid
     *                  - joined to false
     *                  - decide if cookie should only be set for https connections
     *                  - Set cookie with joined!
     *              - if invalid
     *                  - joined to true
     *                  - create new unique id in sessionmanagement
     *                  - append route to new id if route is specified
     *                  - decide if cookie should only be set for https connections
     *                  - Set cookie with joined!
     *      - if a route is appended to the id but doesn't fit:
     *          - create new unique id in sessionmanagement
     *          - append route to new id if route is specified
     *          - joined to true
     *          - decide if cookie should only be set for https connections
     *          - Set cookie with joined!
     *          
     * 2: If the request contains one or more JSsessionIds as cookies:
     *      - check Cookies
     *          - no route:
     *              - backend defines route:
     *                  - discard cookie
     *              - backend doesn't define route:
     *                  - check if session is still valid
     *                      - invalid: discard cookie
     *                      - valid:
     *                          - decide if cookie should only be set for https connections
     *                          - set cookie with joined!
     *          - route doesn't match:
     *              - discard cookie
     *          - route matches:
     *              - check if session is still valid
     *                  - invalid: discard cookie
     *                  - valid:
     *                      - decide if cookie should only be set for https connections
     *                      - set cookie with joined!
     *      
     *      
     * 3: If the request doesn't contain a jsessionid cookie:
     *      - create new unique id in sessionmanagement
     *      - append route to new id if route is specified
     *      - joined to true
     *      - decide if cookie should only be set for https connections
     *      - Set cookie with joined!
     * 
     *      
     *      
     * - decide if cookie should only be set for https connections
     * - Set cookie with joined!
     *      
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            //does the request contain a jSessonId as parameter, as cookie, or not at all?
            if(hasJSessionIdAsUrlParam(httpServletRequest)) { //1:
                String route = getRouteFromSession(getJsessionIdParameterValue(httpServletRequest));
                
            } else if (hasJSessionIdAsCookie(httpServletRequest)) { //2:
                
            } else { //3:
                
            }
        }
    }

    

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }
    
    /**
     * Checks if the request contains a JSessionId as cookie.
     * @param httpServletRequest the incoming request
     * @return false if the request doesn't contain a JSessionId as cookie, otherwise true
     */
    private boolean hasJSessionIdAsCookie(HttpServletRequest httpServletRequest) {
        return !getJsessionIdCookies(httpServletRequest).isEmpty();
    }

    /**
     * Checks if the request contains a JSessionId as url parameter.
     * @param httpServletRequest the incoming request
     * @return false if the request doesn't contain a JSessionId as url parameter, otherwise true
     */
    private boolean hasJSessionIdAsUrlParam(HttpServletRequest httpServletRequest) {
        return getJsessionIdParameterValue(httpServletRequest) != null;
    }
    
    /**
     * Get JSESSIONID cookies from the request
     * @param request the HttpServletRequest
     * @return Empty list if the request doesn't contain any, otherwise a List of JSESSIONID cookies
     */
    private List<Cookie> getJsessionIdCookies(final HttpServletRequest request) {
        List<Cookie> ids = new ArrayList<Cookie>();
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(JSESSIONID_COOKIE)) {
                ids.add(cookie);
            }
        }
        return ids;
    }

    /**
     * Test if the request contains the backendRoute as url parameter in the form of ;jsessionid=.
     * @param request the HttpServletRequest
     * @return null if the request doesn't contain a route parameter, the value of the parameter otherwise 
     */
    private String getJsessionIdParameterValue(final HttpServletRequest request) {
        return request.getParameter(JSESSIONID_URL_PARAMETER);
    }

    /**
     * Compare a given route against the current route configuration of this server.
     * @param route The route to validate
     * @return false if the routes don't match, true otherwise
     */
    private boolean isBackendRouteValid(String route) {
        return route.equals(backendRoute);
    }
    
    /**
     * Takes a JSessionId and returns the trailing backend route
     * @param session the JSessionId 
     * @return null if no route can be found, the route otherwise
     */
    private String getRouteFromSession(String session) {
        String route = null;
        String[] tokens = session.split("\\.");
        if(tokens.length>=2) {
            route = tokens[1];
        }
        return route;
    }
    
    private void discardCookie(Cookie cookie, HttpServletResponse response) {
//        if (DEBUG) {
//            LOG.debug(new StringBuilder("\n\tDifferent JVM route detected. Removing JSESSIONID cookie: ").append(id));
//        }
//        current.setMaxAge(0); // delete
//        current.setSecure(forceHttps || servletRequest.isSecure());
//        resp.addCookie(current);
    }
    
    private boolean isJSessionIdValid(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
