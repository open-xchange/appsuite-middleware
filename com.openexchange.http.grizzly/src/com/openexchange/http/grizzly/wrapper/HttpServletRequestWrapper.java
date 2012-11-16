
package com.openexchange.http.grizzly.wrapper;

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
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.servlet.ServletUtils;
import com.openexchange.dispatcher.Parameterizable;

public class HttpServletRequestWrapper implements HttpServletRequest, Parameterizable {

    private final HttpServletRequest httpServletRequest;

    public HttpServletRequestWrapper(HttpServletRequest httpServletRequest) {
        super();
        this.httpServletRequest = httpServletRequest;
    }

    public Object getAttribute(String arg0) {
        return httpServletRequest.getAttribute(arg0);
    }

    public Enumeration getAttributeNames() {
        return httpServletRequest.getAttributeNames();
    }

    public String getAuthType() {
        return httpServletRequest.getAuthType();
    }

    public String getCharacterEncoding() {
        return httpServletRequest.getCharacterEncoding();
    }

    public int getContentLength() {
        return httpServletRequest.getContentLength();
    }

    public String getContentType() {
        return httpServletRequest.getContentType();
    }

    public String getContextPath() {
        return httpServletRequest.getContextPath();
    }

    public Cookie[] getCookies() {
        return httpServletRequest.getCookies();
    }

    public long getDateHeader(String arg0) {
        return httpServletRequest.getDateHeader(arg0);
    }

    public String getHeader(String arg0) {
        return httpServletRequest.getHeader(arg0);
    }

    public Enumeration getHeaderNames() {
        return httpServletRequest.getHeaderNames();
    }

    public Enumeration getHeaders(String arg0) {
        return httpServletRequest.getHeaders(arg0);
    }

    public ServletInputStream getInputStream() throws IOException {
        return httpServletRequest.getInputStream();
    }

    public int getIntHeader(String arg0) {
        return httpServletRequest.getIntHeader(arg0);
    }

    public String getLocalAddr() {
        return httpServletRequest.getLocalAddr();
    }

    public String getLocalName() {
        return httpServletRequest.getLocalName();
    }

    public int getLocalPort() {
        return httpServletRequest.getLocalPort();
    }

    public Locale getLocale() {
        return httpServletRequest.getLocale();
    }

    public Enumeration getLocales() {
        return httpServletRequest.getLocales();
    }

    public String getMethod() {
        return httpServletRequest.getMethod();
    }

    public String getParameter(String arg0) {
        return httpServletRequest.getParameter(arg0);
    }

    public Map getParameterMap() {
        return httpServletRequest.getParameterMap();
    }

    public Enumeration getParameterNames() {
        return httpServletRequest.getParameterNames();
    }

    public String[] getParameterValues(String arg0) {
        return httpServletRequest.getParameterValues(arg0);
    }

    public String getPathInfo() {
        return httpServletRequest.getPathInfo();
    }

    public String getPathTranslated() {
        return httpServletRequest.getPathTranslated();
    }

    public String getProtocol() {
        return httpServletRequest.getProtocol();
    }

    public String getQueryString() {
        return httpServletRequest.getQueryString();
    }

    public BufferedReader getReader() throws IOException {
        return httpServletRequest.getReader();
    }

    public String getRealPath(String arg0) {
        return httpServletRequest.getRealPath(arg0);
    }

    public String getRemoteAddr() {
        return httpServletRequest.getRemoteAddr();
    }

    public String getRemoteHost() {
        return httpServletRequest.getRemoteHost();
    }

    public int getRemotePort() {
        return httpServletRequest.getRemotePort();
    }

    public String getRemoteUser() {
        return httpServletRequest.getRemoteUser();
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return httpServletRequest.getRequestDispatcher(arg0);
    }

    public String getRequestURI() {
        return httpServletRequest.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return httpServletRequest.getRequestURL();
    }

    public String getRequestedSessionId() {
        return httpServletRequest.getRequestedSessionId();
    }

    public String getScheme() {
        return httpServletRequest.getScheme();
    }

    public String getServerName() {
        return httpServletRequest.getServerName();
    }

    public int getServerPort() {
        return httpServletRequest.getServerPort();
    }

    public String getServletPath() {
        return httpServletRequest.getServletPath();
    }

    public HttpSession getSession() {
        return httpServletRequest.getSession();
    }

    public HttpSession getSession(boolean arg0) {
        return httpServletRequest.getSession(arg0);
    }

    public Principal getUserPrincipal() {
        return httpServletRequest.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return httpServletRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return httpServletRequest.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return httpServletRequest.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdValid() {
        return httpServletRequest.isRequestedSessionIdValid();
    }

    public boolean isSecure() {
        return httpServletRequest.isSecure();
    }

    public boolean isUserInRole(String arg0) {
        return httpServletRequest.isUserInRole(arg0);
    }

    public void removeAttribute(String arg0) {
        httpServletRequest.removeAttribute(arg0);
    }

    public void setAttribute(String arg0, Object arg1) {
        httpServletRequest.setAttribute(arg0, arg1);
    }

    public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
        httpServletRequest.setCharacterEncoding(arg0);
    }

    @Override
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
        Request internalRequest = ServletUtils.getInternalRequest(httpServletRequest);
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        if (null == value) {
            parameters.remove(name);
        } else {
            setParameter(name, value);
        }
    }

}
