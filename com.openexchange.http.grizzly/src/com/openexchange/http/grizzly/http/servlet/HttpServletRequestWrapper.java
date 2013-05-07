
package com.openexchange.http.grizzly.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.glassfish.grizzly.http.server.OXRequest;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.servlet.ServletUtils;
import com.openexchange.dispatcher.Parameterizable;

public class HttpServletRequestWrapper implements HttpServletRequest, Parameterizable {

    public final static String HTTP_SCHEME = "http";

    public final static String HTTPS_SCHEME = "https";

    private final HttpServletRequest delegate;

    private final String requestScheme;

    private final int serverPort;

    private final boolean isSecure;

    private final String remoteAddress;

        /**
         * Initializes a new {@link HttpServletRequestWrapper}.
         *
         * @param requestScheme The scheme of the incoming request: http or https
         * @param remoteAddress
         * @param serverPort The serverPort of the incoming request
         * @param httpServletRequest The incoming request
         * @throws IllegalArgumentException If the port is smaller than 1
         */
        public HttpServletRequestWrapper(String requestScheme, String remoteAddress, int serverPort, HttpServletRequest httpServletRequest) {
            if (serverPort < 1) {
                throw new IllegalArgumentException("Port is out of valid range: " + serverPort);
            }
            if (requestScheme.equalsIgnoreCase(HTTPS_SCHEME)) {
                this.requestScheme = HTTPS_SCHEME;
                this.isSecure = true;
            } else {
                this.requestScheme = HTTP_SCHEME;
                this.isSecure = false;
            }
            this.remoteAddress = remoteAddress;
            this.serverPort = serverPort;
            this.delegate = httpServletRequest;
    
            OXRequest internalRequest = (OXRequest) ServletUtils.getInternalRequest(delegate);
            internalRequest.setXForwardPort(serverPort);
            internalRequest.setxForwardProto(requestScheme);
        }
    
    /**
     * Initializes a new {@link HttpServletRequestWrapper}.
     * 
     * @param httpServletRequest the delegate to use with this wrapper
     */
    public HttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        this.delegate = httpServletRequest;
        if (HTTPS_SCHEME.equals(httpServletRequest.getScheme())) {
            this.requestScheme = HTTPS_SCHEME;
            this.isSecure = true;
        } else {
            this.requestScheme = HTTP_SCHEME;
            this.isSecure = false;
        }
        this.serverPort = httpServletRequest.getServerPort();
        this.remoteAddress = httpServletRequest.getRemoteAddr();
    }

    @Override
    public Object getAttribute(String arg0) {
        return delegate.getAttribute(arg0);
    }

    @Override
    public Enumeration getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public String getAuthType() {
        return delegate.getAuthType();
    }

    @Override
    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    @Override
    public int getContentLength() {
        return delegate.getContentLength();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public String getContextPath() {
        return delegate.getContextPath();
    }

    @Override
    public Cookie[] getCookies() {
        return delegate.getCookies();
    }

    @Override
    public long getDateHeader(String arg0) {
        return delegate.getDateHeader(arg0);
    }

    @Override
    public String getHeader(String arg0) {
        return delegate.getHeader(arg0);
    }

    @Override
    public Enumeration getHeaderNames() {
        return delegate.getHeaderNames();
    }

    @Override
    public Enumeration getHeaders(String arg0) {
        return delegate.getHeaders(arg0);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public int getIntHeader(String arg0) {
        return delegate.getIntHeader(arg0);
    }

    @Override
    public String getLocalAddr() {
        return delegate.getLocalAddr();
    }

    @Override
    public String getLocalName() {
        return delegate.getLocalName();
    }

    @Override
    public int getLocalPort() {
        return delegate.getLocalPort();
    }

    @Override
    public Locale getLocale() {
        return delegate.getLocale();
    }

    @Override
    public Enumeration getLocales() {
        return delegate.getLocales();
    }

    @Override
    public String getMethod() {
        return delegate.getMethod();
    }

    @Override
    public String getParameter(String arg0) {
        return delegate.getParameter(arg0);
    }

    @Override
    public Map getParameterMap() {
        return delegate.getParameterMap();
    }

    @Override
    public Enumeration getParameterNames() {
        return delegate.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String arg0) {
        return delegate.getParameterValues(arg0);
    }

    @Override
    public String getPathInfo() {
        return delegate.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return delegate.getPathTranslated();
    }

    @Override
    public String getProtocol() {
        return delegate.getProtocol();
    }

    @Override
    public String getQueryString() {
        return delegate.getQueryString();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return delegate.getReader();
    }

    @Override
    @Deprecated
    public String getRealPath(String arg0) {
        return delegate.getRealPath(arg0);
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddress;
    }

    @Override
    public String getRemoteHost() {
        return delegate.getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return delegate.getRemotePort();
    }

    @Override
    public String getRemoteUser() {
        return delegate.getRemoteUser();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return delegate.getRequestDispatcher(arg0);
    }

    @Override
    public String getRequestURI() {
        return delegate.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return delegate.getRequestURL();
    }

    @Override
    public String getRequestedSessionId() {
        return delegate.getRequestedSessionId();
    }

    @Override
    public String getScheme() {
        return requestScheme;
    }

    @Override
    public String getServerName() {
        return delegate.getServerName();
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getServletPath() {
        return delegate.getServletPath();
    }

    @Override
    public HttpSession getSession() {
        return delegate.getSession();
    }

    @Override
    public HttpSession getSession(boolean arg0) {
        return delegate.getSession(arg0);
    }

    @Override
    public Principal getUserPrincipal() {
        return delegate.getUserPrincipal();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return delegate.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return delegate.isRequestedSessionIdFromURL();
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return delegate.isRequestedSessionIdFromUrl();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return delegate.isRequestedSessionIdValid();
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public boolean isUserInRole(String arg0) {
        return delegate.isUserInRole(arg0);
    }

    @Override
    public void removeAttribute(String arg0) {
        delegate.removeAttribute(arg0);
    }

    @Override
    public void setAttribute(String arg0, Object arg1) {
        delegate.setAttribute(arg0, arg1);
    }

    @Override
    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        delegate.setCharacterEncoding(arg0);
    }

    @Override
    public void putParameter(String name, String value) {
        Request internalRequest = ServletUtils.getInternalRequest(delegate);
        internalRequest.putParameter(name, value);
    }

}
