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

package com.openexchange.http.deferrer.impl;

import static com.openexchange.ajax.AJAXServlet.encodeUrl;
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
    public String getDeferredURL(final String url) {
        if (url == null) {
            return null;
        }
        String deferrerURL = getDeferrerURL();
        if (Strings.isEmpty(deferrerURL)) {
            return url;
        }
        deferrerURL = deferrerURL.trim();
        if (seemsAlreadyDeferred(url, deferrerURL)) {
            // Already deferred
            return url;
        }
        // Return deferred URL
        return deferrerURL + PREFIX.get().getPrefix() + "defer?redirect=" + encodeUrl(url, false, false);
    }

    private static boolean seemsAlreadyDeferred(final String url, final String deferrerURL) {
        final String str = "://";
        final int pos1 = url.indexOf(str);
        final int pos2 = deferrerURL.indexOf(str);
        if (pos1 > 0 && pos2 > 0) {
            return url.substring(pos1).startsWith(deferrerURL.substring(pos2));
        }
        return url.startsWith(deferrerURL);
    }

    /**
     * Gets the deferrer URL; e.g. "https://my.maindomain.org"
     *
     * @return The deferrer URL
     */
    protected abstract String getDeferrerURL();


    @Override
    public String getBasicDeferrerURL() {
    	final String deferrerURL = getDeferrerURL();
        return deferrerURL == null ? PREFIX.get().getPrefix() + "defer" : deferrerURL + PREFIX.get().getPrefix() + "defer";
    }

}
