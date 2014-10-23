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

package com.openexchange.share.servlet.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.ShareList;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.Share;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link LoginShareHandler} - The share handler that redirects to standard login page.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class LoginShareHandler extends AbstractShareHandler {

    /**
     * Initializes a new {@link LoginShareHandler}.
     *
     * @param shareLoginConfiguration The login configuration for shares
     */
    public LoginShareHandler() {
        super();
    }

    private String getLoginPageLink(int userId, int contextId, HttpServletRequest request) {
        // By property
        {
            ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
            String sLink = configService.getProperty("com.openexchange.share.loginLink");

            if (false == Strings.isEmpty(sLink)) {
                return sLink;
            }
        }

        // By HostnameService or given HTTP request
        String serverName = null;
        {
            HostnameService hostnameService = ShareServiceLookup.getService(HostnameService.class);
            if (null != hostnameService) {
                try {
                    String hostName = hostnameService.getHostname(userId, contextId);
                    if (false == Strings.isEmpty(hostName)) {
                        // Set server name as returned by HostnameService
                        serverName = hostName;
                    }
                } catch (Exception e) {
                    // Unable to check for proper host name
                }
            }
            if (null == serverName) {
                serverName = request.getServerName();
            }
        }

        return serverName + "/signin";
    }

    @Override
    public int getRanking() {
        return 10;
    }

    /**
     * Checks if this redirecting share handler fees responsible for passed share
     *
     * @param share The associated share
     * @param target The share target within the share, or <code>null</code> if not addressed
     * @return <code>true</code> if share can be handled; otherwise <code>false</code>
     */
    protected boolean handles(ShareList share, Share target) {
        AuthenticationMode authentication = share.getAuthentication();
        return null != authentication &&
            (AuthenticationMode.ANONYMOUS_PASSWORD == authentication || AuthenticationMode.GUEST_PASSWORD == authentication);
    }

    @Override
    public boolean handle(ShareList share, Share target, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (false == handles(share, target)) {
            // no password prompt required
            return false;
        }

        try {
            int contextId = share.getContextID();
            int guestId = share.getGuest();
            String loginPageLink = getLoginPageLink(guestId, contextId, request);

            // Build URL
            StringBuilder url = new StringBuilder(loginPageLink);

            // Start fragment portion
            url.append('#').append("share=").append(urlEncode(share.getToken()));
            if (null != target) {
                url.append('&').append("target=").append(urlEncode(target.getPath()));
            }
            if (AuthenticationMode.ANONYMOUS_PASSWORD == share.getAuthentication()) {
                url.append('&').append("login_type=anonymous");
            } else {
                url.append('&').append("login_type=guest");
                String mail;
                {
                    // Special anonymous guests do not have a E-Mail address applied
                    UserService service = ShareServiceLookup.getService(UserService.class);
                    User user = service.getUser(guestId, contextId);
                    mail = user.getMail();
                }
                url.append('&').append("login_name=").append(urlEncode(mail));
            }

            // Do the redirect
            response.sendRedirect(url.toString());

            return true;
        } catch (IOException e) {
            throw ShareExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }

}
