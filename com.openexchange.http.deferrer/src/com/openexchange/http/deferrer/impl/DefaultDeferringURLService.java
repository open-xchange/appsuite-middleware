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

package com.openexchange.http.deferrer.impl;

import com.openexchange.ajax.AJAXUtility;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.java.Strings;

/**
 * {@link DefaultDeferringURLService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class DefaultDeferringURLService implements DeferringURLService {

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    @Override
    public String getDeferredURL(final String url, int userId, int contextId) {
        return deferredURLUsing(url, getDeferrerURL(userId, contextId), userId, contextId);
    }

    @Override
    public String deferredURLUsing(final String url, final String domain, int userId, int contextId) {
        if (url == null) {
            return null;
        }
        if (Strings.isEmpty(domain)) {
            return url;
        }
        String deferrerURL = domain.trim();
        final String path = new StringBuilder(PREFIX.get().getPrefix()).append("defer").toString();
        if (seemsAlreadyDeferred(url, deferrerURL, path)) {
            // Already deferred
            return url;
        }
        // Return deferred URL
        return new StringBuilder(deferrerURL).append(path).append("?redirect=").append(AJAXUtility.encodeUrl(url, false, false)).toString();
    }

    @Override
    public boolean seemsDeferred(String url, int userId, int contextId) {
        if (url == null) {
            return false;
        }
        String deferrerURL = getDeferrerURL(userId, contextId);
        if (Strings.isEmpty(deferrerURL)) {
            return false;
        }
        deferrerURL = deferrerURL.trim();
        final String path = new StringBuilder(PREFIX.get().getPrefix()).append("defer").toString();
        return seemsAlreadyDeferred(url, deferrerURL, path);
    }

    private static boolean seemsAlreadyDeferred(final String url, final String deferrerURL, final String path) {
        final String str = "://";
        final int pos1 = url.indexOf(str);
        final int pos2 = deferrerURL.indexOf(str);
        if (pos1 > 0 && pos2 > 0) {
            final String deferrerPrefix = new StringBuilder(deferrerURL.substring(pos2)).append(path).toString();
            return url.substring(pos1).startsWith(deferrerPrefix);
        }
        final String deferrerPrefix = new StringBuilder(deferrerURL).append(path).toString();
        return url.startsWith(deferrerPrefix);
    }

    /**
     * Gets the deferrer URL; e.g. "https://my.maindomain.org"
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The deferrer URL
     */
    protected abstract String getDeferrerURL(int userId, int contextId);

    @Override
    public boolean isDeferrerURLAvailable(int userId, int contextId) {
        return !Strings.isEmpty(getDeferrerURL(userId, contextId));
    }

    @Override
    public String getBasicDeferrerURL(int userId, int contextId) {
        final String deferrerURL = getDeferrerURL(userId, contextId);
        if (deferrerURL == null) {
            return new StringBuilder(PREFIX.get().getPrefix()).append("defer").toString();
        }
        return new StringBuilder(deferrerURL).append(PREFIX.get().getPrefix()).append("defer").toString();
    }
}
