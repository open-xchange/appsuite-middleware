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
        return Strings.isNotEmpty(getDeferrerURL(userId, contextId));
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
