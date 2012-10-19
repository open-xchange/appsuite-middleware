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

package com.openexchange.http.grizzly.servletfilter;

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
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

/**
 * Checks the Request/Response for JSessionId Cookies/Headers.
 * Invalid Cookies in the Requests are discarded in the Response. New JSessionIds are modified so they end with ".backendRoute" if a 
 * backend route is configured.  
 * {@link BackendRouteFilter}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class BackendRouteFilter implements Filter {

    private static final String JSESSIONID_COOKIE="JSESSIONID";
    private static final String JSESSIONID_URL_PARAMETER="jsessionid";
    private static final Log LOG = com.openexchange.log.Log.loggerFor(BackendRouteFilter.class);
    private String backendRoute="";
    
    /**
     * Get the backend route.
     * @return the backend route
     */
    private String getBackendRoute() {
        return backendRoute;
    }
    
    /**
     * Sets the backendRoute. Silently converts null to the empty string.
     *
     * @param backendRoute The backendRoute to set
     */
    private void setBackendRoute(String backendRoute) {
        this.backendRoute = backendRoute == null ? "" : backendRoute;
    }

    //--------------------------------------------------------------------------------------------------------------------------------------
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setBackendRoute(filterConfig.getInitParameter("backendRoute"));
    }

    /*
     * JSessionID can be passed as param or as cookie. Cookie may appear multiple times!
     * We have to look at three different conditions:
     *
     * 1: JSessionId as url param
     *  - no route
     *   - backendroute defined -> discard cookie in response
     *   - no backendroute defined -> continue
     *  - route doesn't match
     *   - discard cookie in response
     *  - route matches
     *   - continue
     *  
     * 2: JSessionID as cookie
     *  - no route appended to JSessionId 
     *   - but backendroute is defined -> discard cookie in response
     *   - no backendroute defined -> continue
     *  - route doesn't match
     *   - discard cookie in response
     *  - route matches
     *   - continue
     *  
     * 3: No JSessionId, recheck JSessionId in response
     *     
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            
            if(hasJSessionIdAsUrlParam(httpServletRequest)) { //1: JSessionId as url param
                checkUrlParam(httpServletRequest, httpServletResponse);
                chain.doFilter(httpServletRequest, httpServletResponse);
            } else if (hasJSessionIdAsCookie(httpServletRequest)) { //2: JSessionID as cookie
                checkRequestCookies(httpServletRequest, httpServletResponse);
                chain.doFilter(httpServletRequest, httpServletResponse);
            } else { //3: No JSessionId
                chain.doFilter(httpServletRequest, httpServletResponse);
                updateJSessionIdRoute(httpServletResponse);
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
    
    //--------------------------------------------------------------------------------------------------------------------------------------
    
    /**
     * Inspect the Url parameter for JSessionId cookies.
     * This gets the route from the jsessionid param contained in the request url and checks it.
     * @param httpServletRequest the request to inspect
     * @param httpServletResponse the response belonging to the request
     */
    private void checkUrlParam(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String jSessionId = getJsessionIdParameterValue(httpServletRequest);
        writeToDebugLog(new StringBuilder("Request has JSessionId set as url param: ").append(jSessionId).toString());
        checkTheRoute(jSessionId, httpServletRequest, httpServletResponse);
    }

    /**
     * Inspect the request for JSessionId cookies.
     * This gets the route from the cookies contained in the requests and checks them.
     * @param httpServletRequest the request to inspect
     * @param httpServletResponse the response belonging to the request
     */
    private void checkRequestCookies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        writeToDebugLog("Request has JSessionId set as cookie.");
        List<Cookie> jsessionIdCookies = getJsessionIdCookies(httpServletRequest);
        for (Cookie jSessionIdCookie : jsessionIdCookies) {
            String jSessionId = getJsessionIdFromCookie(jSessionIdCookie);
            writeToDebugLog(new StringBuilder("Current JSessionId: ").append(jSessionId).toString());
            checkTheRoute(jSessionId, httpServletRequest, httpServletResponse);
        }
    }
    
    /**
     * Check the JSessionId route against the currently configured backend route.
     * Implemented behaviour:
     *  - no route appended to JSessionId
     *   - but backendroute is defined -> discard cookie in response
     *   - no backendroute defined -> continue
     *  - route doesn't match
     *   - discard cookie in response
     *  - route matches
     *   - continue
     * @param jSessionId the JSessionId
     * @param httpServletRequest the incoming request
     * @param httpServletResponse the response belonging to the request
     */
    private void checkTheRoute(String jSessionId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if(jSessionId == null) {
            throw new IllegalArgumentException();
        }
        String route = getRouteFromJSessionId(jSessionId);
        if(route.isEmpty()) { //no route found in JSessionId
            if(isBackendRouteDefined()){ //but backendRoute is defined -> discard cookie
                writeToInfoLog(new StringBuilder("Route is null but backend defines one: ").append(jSessionId).toString());
                HttpSession session = httpServletRequest.getSession(false);
                session.invalidate();
                discardCookie(jSessionId,httpServletRequest, httpServletResponse);
            } //else, no backenRoute defined -> continue
        } else { //route exists
            if(isRouteInvalid(route)) { //but is invalid
                writeToInfoLog(new StringBuilder("Route exists but is invalid: ")
                    .append(jSessionId).append(" vs. ")
                    .append(backendRoute)
                    .toString());
                HttpSession httpSession = httpServletRequest.getSession(false);
                if(httpSession != null) {
                    httpSession.invalidate();
                    writeToDebugLog(new StringBuilder("Invalidated Session: ").append(httpSession.getId()).toString());
                    httpServletRequest.isRequestedSessionIdValid();
                    writeToDebugLog(new StringBuilder("Is Session ")
                    .append(httpSession.getId())
                    .append(" still valid? ")
                    .append(httpServletRequest.isRequestedSessionIdValid())
                    .toString());
                }
                discardCookie(jSessionId, httpServletRequest, httpServletResponse);
            } // else route matches -> continue
        }
    }
    
    /**
     * Update the newly set JSessionId Set-Cookie header that was set in the Response.
     * <p>
     *   Like shown below the new JSessionId currently gets generated further up the processing chain in the callhierarchy
     *   so we have to recheck the response when it comes back down the filter chain.
     * </p>
     * <pre>
     *  Response.addCookie(Cookie)
     *  Request.doGetSession(boolean)
     *  Request.getSession(boolean)
     *  HttpServletRequestImpl.getSession(boolean)
     *  HttpServletRequestImpl.getSession()
     *  CountingHttpServletRequest.getSession()
     *  SessionServlet.rememberSession(HttpServletRequest, ServerSession)
     * </pre>
     * <p>
     *   
     * </p>
     * This can only be implemented when the ServletContainer supports Servlet 3.*. In the meantime rewrite the JSessionId in a container
     * specific way
     * @param httpServletResponse the response to check for JSessionId Headers
     */
    private void updateJSessionIdRoute(HttpServletResponse httpServletResponse) {        
        /*
         * if(!backendRoute.isEmpty()) {
         *   if(containsHeader) {
         *     getHeaders
         *     BasicHeaderElement.getParameterByName(); appendRoute to JSessionId
         *     setHeader() //overwrite existing
         *     addHeader() //add others if multiple Set-Cookie headers existed
         *   }
         * }
         */
    }

    /**
     * Checks if the given route is invalid by comparing it to the currently configured backend route
     * @param route the route to compare
     * @return false if the routes don't match, true otherwise
     */
    private boolean isRouteInvalid(String route) {
        return !route.equals(getBackendRoute());
    }

    /**
     * Checks if a backendRoute is defined for this backend.
     * @return false if no backendRoute is defined, true otherwise
     */
    private boolean isBackendRouteDefined() {
        return !getBackendRoute().isEmpty();
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
        return !getJsessionIdParameterValue(httpServletRequest).isEmpty();
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
     * Get the value of a JSessionId cookie
     * @param jSessionIdCookie the cookie
     * @return the value of the cookie
     */
    private String getJsessionIdFromCookie(Cookie jSessionIdCookie) {
        return jSessionIdCookie.getValue();
    }

    /**
     * Get the backendRoute as url parameter in the form of ;jsessionid=.
     * @param request the HttpServletRequest
     * @return Empty String if the request doesn't contain a route parameter, the value of the parameter otherwise 
     */
    private String getJsessionIdParameterValue(final HttpServletRequest request) {
        String parameter = request.getParameter(JSESSIONID_URL_PARAMETER);
        return parameter == null ? "" : parameter;
    }

    /**
     * Takes a JSessionId and returns the trailing backend route
     * @param session the JSessionId 
     * @return Empty String if no route can be found, the route otherwise
     */
    private String getRouteFromJSessionId(String session) {
        if(session == null) {
            throw new IllegalArgumentException();
        }
        String route = "";
        String[] tokens = session.split("\\.");
        if(tokens.length>=2) {
            route = tokens[1];
        }
        return route;
    }
    
    /**
     * Discard a cookie by setting its max age to 0.
     * A cookie can only be overwritten (or deleted) by a subsequent cookie exactly matching the name, path and domain of the original
     * cookie.
     * @param jSessionId the JSessionId value
     * @param httpServletResponse the response the cookie is added to
     */
    private void discardCookie(String jSessionId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        writeToInfoLog(new StringBuilder("Removing JSessionId cookie: ").append(jSessionId).toString());
        Cookie cookie = new Cookie(JSESSIONID_COOKIE, jSessionId);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
    }
    
    /**
     * Write a String to the info log.
     * @param logValue the String that should be logged
     */
    private static void writeToInfoLog(String logValue) {
        if(LOG.isInfoEnabled()) {
            LOG.info(logValue);
        }
    }
    
    /**
     * Write a String to the debug log.
     * @param logValue the String that should be logged
     */
    private static void writeToDebugLog(String logValue) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(logValue);
        }
    }
    
}
