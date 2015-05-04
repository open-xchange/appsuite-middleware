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

import static com.openexchange.share.servlet.utils.ShareRedirectUtils.translate;
import static com.openexchange.share.servlet.utils.ShareRedirectUtils.urlEncode;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.share.notification.DefaultLinkProvider;
import com.openexchange.share.notification.LinkProvider;
import com.openexchange.share.notification.ShareNotification;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.mail.MailNotifications;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.utils.ShareRedirectUtils;
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

    private final ShareLoginConfiguration loginConfig;

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

            // Get UUID to confirm
            String confirm = request.getParameter("confirm");

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

            GuestService guestService = ShareServiceLookup.getService(GuestService.class);
            if ((guestService != null) && (guestInfo.getAuthentication() == AuthenticationMode.GUEST_PASSWORD) && (storageUser.isGuest()) && (guestService.isCrossContextGuestHandlingEnabled())) {
                guestService.alignUserWithGuest(storageUser, context.getContextId());
            }

            String hash = getHash(storageUser.getUserPassword());
            if (null == confirm) {
                // Generate hash and send link to confirm
                GuestShare guestShare = shareService.resolveToken(token);

                /*
                 * Send notifications. For now we only have a mail transport. The API might get expanded to allow additional transports.
                 */
                ShareNotificationService shareNotificationService = ShareServiceLookup.getService(ShareNotificationService.class);
                String protocol = com.openexchange.tools.servlet.http.Tools.getProtocol(request);
                shareNotificationService.sendPasswordResetConfirmationNotification(Transport.MAIL, guestShare, token, request.getServerName(), protocol, hash);

                /*
                 * Redirect after notification was sent.
                 */
                String redirectUrl = ShareRedirectUtils.getRedirectUrl(guestShare.getGuest(), guestShare.getSingleTarget(), this.loginConfig.getLoginConfig(),
                    urlEncode(String.format(translate(ShareServletStrings.RESET_PASSWORD, guestShare.getGuest().getLocale()), guestShare.getGuest().getEmailAddress())), "INFO",
                    "resetPassword");
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.sendRedirect(redirectUrl);
            } else {
                // Try to set new password
                if (confirm.equals(hash)) {
                    GuestShare guestShare = shareService.resolveToken(token);
                    User guest = userService.getUser(guestInfo.getGuestID(), guestInfo.getContextID());
                    
                    UserImpl user = new UserImpl();
                    user.setId(guestID);
                    user.setPasswordMech(guest.getPasswordMech());
                    user.setUserPassword(PasswordMech.BCRYPT.encode(" "));
                    ShareServiceLookup.getService(GuestService.class).updateGuestUser(user, guestInfo.getContextID());

                    String redirectUrl = ShareRedirectUtils.getRedirectUrl(guestShare.getGuest(), guestShare.getSingleTarget(), this.loginConfig.getLoginConfig(),
                        urlEncode(String.format(translate(ShareServletStrings.RESET_PASSWORD_DONE, guestShare.getGuest().getLocale()), guestShare.getGuest().getEmailAddress())), "INFO",
                        "resetPassword");
                    response.setStatus(HttpServletResponse.SC_FOUND);
                    response.sendRedirect(redirectUrl);
                } else {
                    LOG.debug("Bad attempt to reset password for share '{}'", token);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
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
        }
    }

    private String getHash(String toHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(toHash.getBytes("UTF-8"));
        md.update(loginConfig.getCookieHashSalt());
        byte[] hash = md.digest();
        return com.openexchange.tools.encoding.Base64.encode(hash);
    }
}
