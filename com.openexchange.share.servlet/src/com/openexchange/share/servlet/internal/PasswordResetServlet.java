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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.context.ContextService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.notification.DefaultLinkProvider;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.ShareNotification;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.servlet.utils.ShareRedirectUtils;
import com.openexchange.share.tools.PasswordUtility;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.user.UserService;

/**
 * {@link PasswordResetServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PasswordResetServlet extends HttpServlet {

    private static final long serialVersionUID = -598655895873570676L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordResetServlet.class);

    private ShareLoginConfiguration loginConfig;

    // --------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link PasswordResetServlet}.
     *
     * @param loginConfig
     */
    public PasswordResetServlet(ShareLoginConfiguration loginConfig) {
        super();
        this.loginConfig = loginConfig;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String mailAddress = "";
        try {
            // Create a new HttpSession if it is missing
            request.getSession(true);

            // Read share token
            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            GuestInfo guestInfo = shareService.resolveGuest(token);

            if (AuthenticationMode.GUEST_PASSWORD != guestInfo.getAuthentication()) {
                LOG.debug("Bad attempt to reset password for share '{}'", token);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            int contextID = guestInfo.getContextID();

            UserService userService = ShareServiceLookup.getService(UserService.class, true);
            Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(contextID);

            int guestID = guestInfo.getGuestID();
            User storageUser = userService.getUser(guestID, context);

            String password = PasswordUtility.generate();
            String encodedPassword = PasswordMech.BCRYPT.encode(password);

            UserImpl updatedUser = new UserImpl(storageUser);
            updatedUser.setUserPassword(encodedPassword);

            userService.updateUser(updatedUser, context);
            // Invalidate
            userService.invalidateUser(context, guestID);

            GuestShare guestShare = shareService.resolveToken(token);
            User guest = userService.getUser(guestInfo.getGuestID(), guestInfo.getContextID());
            ShareNotificationService notificationService = ShareServiceLookup.getService(ShareNotificationService.class, true);
            LinkProvider linkProvider = new DefaultLinkProvider(Tools.getProtocol(request), request.getServerName(), DispatcherPrefixService.DEFAULT_PREFIX, token); // FIXME
            mailAddress = guestShare.getGuest().getEmailAddress();
            ShareNotification<InternetAddress> notification = MailNotifications.passwordReset()
                .setTransportInfo(new InternetAddress(mailAddress, true))
                .setLinkProvider(linkProvider)
                .setContext(guestInfo.getContextID())
                .setLocale(guest.getLocale())
                .setUsername(guestInfo.getEmailAddress())
                .setPassword(password)
                .build();
             notificationService.send(notification);

             setRedirect(guestShare, null, response);
        } catch (RateLimitedException e) {
            response.setContentType("text/plain; charset=UTF-8");
            if (e.getRetryAfter() > 0) {
                response.setHeader("Retry-After", String.valueOf(e.getRetryAfter()));
            }
            response.sendError(429, "Too Many Requests - Your request is being rate limited.");
        } catch (OXException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (AddressException e) {
            OXException oxe = ShareExceptionCodes.INVALID_MAIL_ADDRESS.create(mailAddress);
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), oxe.getMessage(), oxe);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, oxe.getMessage());
        }
    }

    /**
     * Adds the redirect to the given response
     *
     * @param guest - the guest info
     * @param target - target to get the redirect to
     * @param serverName - server name to redirect to
     * @param response - response that should be enriched by the redirect
     * @throws OXException
     */
    private void setRedirect(GuestShare share, ShareTarget target, HttpServletResponse response) throws OXException {
        try {
            String redirectUrl = ShareRedirectUtils.getRedirectUrl(share.getGuest(), target, this.loginConfig.getLoginConfig());
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw ShareExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }
}
