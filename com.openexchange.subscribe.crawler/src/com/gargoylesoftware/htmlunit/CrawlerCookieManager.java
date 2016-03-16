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

package com.gargoylesoftware.htmlunit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;


/**
 * {@link CrawlerCookieManager}
 * This was copied from CookieManager to support a more lenient CookiePolicy ("crawler-special").
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CrawlerCookieManager extends CookieManager {

    /**
     *
     */
    private static final long serialVersionUID = -970080965438974839L;

    /**
     * Creates a new instance.
     */
    public CrawlerCookieManager() {
        cookiesEnabled_ = true;
        cookies_ = new LinkedHashSet<Cookie>();
    }

    /**
     * HtmlUnit's cookie policy is to be browser-compatible. Code which requires access to
     * HtmlUnit's cookie policy should use this constant, rather than making assumptions and using
     * one of the HttpClient {@link CookiePolicy} constants directly.
     */
    public static final String HTMLUNIT_COOKIE_POLICY = "crawler-special";

    /** Whether or not cookies are enabled. */
    private boolean cookiesEnabled_;

    /** The cookies added to this cookie manager. */
    private final Set<Cookie> cookies_;


    /**
     * Enables/disables cookie support. Cookies are enabled by default.
     * @param enabled <tt>true</tt> to enable cookie support, <tt>false</tt> otherwise
     */
    @Override
    public synchronized void setCookiesEnabled(final boolean enabled) {
        cookiesEnabled_ = enabled;
    }

    /**
     * Returns <tt>true</tt> if cookies are enabled. Cookies are enabled by default.
     * @return <tt>true</tt> if cookies are enabled, <tt>false</tt> otherwise
     */
    @Override
    public synchronized boolean isCookiesEnabled() {
        return cookiesEnabled_;
    }

    /**
     * Returns the currently configured cookies, in an unmodifiable set.
     * @return the currently configured cookies, in an unmodifiable set
     */
    @Override
    public synchronized Set<Cookie> getCookies() {
        return Collections.unmodifiableSet(cookies_);
    }

    /**
     * Returns the currently configured cookies for the specified domain, in an unmodifiable set.
     * @param domain the domain on which to filter the returned cookies
     * @return the currently configured cookies for the specified domain, in an unmodifiable set
     */
    @Override
    public synchronized Set<Cookie> getCookies(final String domain) {
        final Set<Cookie> cookies = new LinkedHashSet<Cookie>();
        for (Cookie cookie : cookies_) {
            if (StringUtils.equals(cookie.getDomain(), domain)) {
                cookies.add(cookie);
            }
        }
        return Collections.unmodifiableSet(cookies);
    }

    /**
     * Returns the currently configured cookie with the specified name, or <tt>null</tt> if one does not exist.
     * @param name the name of the cookie to return
     * @return the currently configured cookie with the specified name, or <tt>null</tt> if one does not exist
     */
    @Override
    public synchronized Cookie getCookie(final String name) {
        for (Cookie cookie : cookies_) {
            if (StringUtils.equals(cookie.getName(), name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * Adds the specified cookie.
     * @param cookie the cookie to add
     */
    @Override
    public synchronized void addCookie(final Cookie cookie) {
        cookies_.remove(cookie);
        cookies_.add(cookie);
    }

    /**
     * Removes the specified cookie.
     * @param cookie the cookie to remove
     */
    @Override
    public synchronized void removeCookie(final Cookie cookie) {
        cookies_.remove(cookie);
    }

    /**
     * Removes all cookies.
     */
    @Override
    public synchronized void clearCookies() {
        cookies_.clear();
    }

    /**
     * Updates the specified HTTP state's cookie configuration according to the current cookie settings.
     * @param state the HTTP state to update
     * @see #updateFromState(HttpState)
     */
    @Override
    protected synchronized void updateState(final HttpState state) {
        if (!cookiesEnabled_) {
            return;
        }
        state.clearCookies();
        for (Cookie cookie : cookies_) {
            state.addCookie(cookie);
        }
    }

    /**
     * Updates the current cookie settings from the specified HTTP state's cookie configuration.
     * @param state the HTTP state to update from
     * @see #updateState(HttpState)
     */
    @Override
    protected synchronized void updateFromState(final HttpState state) {
        if (!cookiesEnabled_) {
            return;
        }
        cookies_.clear();
        cookies_.addAll(Arrays.asList(state.getCookies()));
    }

}
