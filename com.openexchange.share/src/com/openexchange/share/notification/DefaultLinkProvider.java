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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share.notification;



/**
 * A default implementation of {@link LinkProvider}. All necessary data has to be
 * set via the constructor.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultLinkProvider implements LinkProvider {

    private final String protocol;

    private final String domain;

    private final String servletPrefix;

    private final String shareToken;

    /**
     * Initializes a new {@link DefaultLinkProvider}.
     *
     * @param protocol The protocol to use for the URLs, i.e. "http" or "https"
     * @param domain The domain to use within the URLs
     * @param servletPrefix The servlet prefix, i.e. "/ajax/" or "/appsuite/api/". Leading and trailing slashes must be contained!
     * @param shareToken The share token, can be a base token or a concrete one
     */
    public DefaultLinkProvider(String protocol, String domain, String servletPrefix, String shareToken) {
        super();
        this.protocol = protocol;
        this.domain = domain;
        this.servletPrefix = servletPrefix;
        this.shareToken = shareToken;
    }

    @Override
    public String getShareUrl() {
        return baseUrl() + "share" + '/' + shareToken;
    }

    @Override
    public String getPasswordResetUrl() {
        return baseUrl() + "share/reset/password?share=" + shareToken;
    }

    private String baseUrl() {
        return protocol + domain + servletPrefix;
    }

}
