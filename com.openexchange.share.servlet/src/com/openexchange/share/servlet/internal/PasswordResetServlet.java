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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.google.common.io.BaseEncoding;
import com.openexchange.authentication.Authenticated;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestService;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.password.mechanism.PasswordMech;
import com.openexchange.password.mechanism.PasswordMechRegistry;
import com.openexchange.password.mechanism.stock.StockPasswordMechs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.servlet.ShareServletStrings;
import com.openexchange.share.servlet.auth.ShareAuthenticated;
import com.openexchange.share.servlet.utils.LoginLocation;
import com.openexchange.share.servlet.utils.LoginLocationRegistry;
import com.openexchange.share.servlet.utils.LoginType;
import com.openexchange.share.servlet.utils.MessageType;
import com.openexchange.share.servlet.utils.ShareServletUtils;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link PasswordResetServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PasswordResetServlet extends AbstractShareServlet {

    private static final long serialVersionUID = -598655895873570676L;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordResetServlet.class);

    private final byte[] hashSalt;

    /**
     * Initializes a new {@link PasswordResetServlet}.
     *
     * @param hashSalt The hash salt to use
     */
    public PasswordResetServlet(byte[] hashSalt) {
        super();
        this.hashSalt = hashSalt;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Tools.disableCaching(response);
        try {
            request.getSession(true);

            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                sendInvalidRequest(response);
                return;
            }

            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            GuestInfo guestInfo = shareService.resolveGuest(token);
            if (guestInfo == null) {
                sendInvalidRequest(response);
                return;
            }

            if (AuthenticationMode.GUEST_PASSWORD != guestInfo.getAuthentication()) {
                LoginLocation location = new LoginLocation().loginType(LoginType.MESSAGE).message(MessageType.INFO, t -> t.translate(ShareServletStrings.NO_GUEST_PASSWORD_REQUIRED));
                LoginLocationRegistry.getInstance().putAndRedirect(location, response);
                return;
            }

            int contextID = guestInfo.getContextID();
            int guestID = guestInfo.getGuestID();
            Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(contextID, UpdateBehavior.DENY_UPDATE);
            User storageUser = ShareServiceLookup.getService(UserService.class, true).getUser(guestID, context);

            GuestService guestService = ShareServiceLookup.getService(GuestService.class);
            if (guestService != null && guestService.isCrossContextGuestHandlingEnabled()) {
                storageUser = guestService.alignUserWithGuest(storageUser, context.getContextId());
            }

            String userPassword = storageUser.getUserPassword();
            if (userPassword == null) {
                // Should not happen due to previous auth mode check but a race condition could cause a NPE here
                sendInvalidRequest(response);
                return;
            }

            String hash = getHash(userPassword);
            String confirm = request.getParameter("confirm");
            if (Strings.isEmpty(confirm)) {
                /*
                 * Send notifications. For now we only have a mail transport. The API might get expanded to allow additional transports.
                 */
                ShareNotificationService shareNotificationService = ShareServiceLookup.getService(ShareNotificationService.class);
                if (shareNotificationService == null) {
                    throw ServiceExceptionCode.absentService(ShareNotificationService.class);
                }
                shareNotificationService.sendPasswordResetConfirmationNotification(Transport.MAIL, guestInfo, hash, Tools.createHostData(request, contextID, guestID, storageUser.isGuest()));

                /*
                 * Redirect after notification was sent.
                 */
                LoginLocation location = new LoginLocation()
                    .loginType(LoginType.MESSAGE)
                    .message(MessageType.INFO, t -> String.format(t.translate(ShareServletStrings.RESET_PASSWORD), guestInfo.getEmailAddress()))
                    .share(guestInfo.getBaseToken());
                LoginLocationRegistry.getInstance().putAndRedirect(location, response);
            } else {
                if (confirm.equals(hash)) {
                    LoginLocation location = new LoginLocation()
                        .loginType(LoginType.RESET_PASSWORD)
                        .message(MessageType.INFO, t -> String.format(t.translate(ShareServletStrings.CHOOSE_PASSWORD), guestInfo.getEmailAddress()))
                        .parameter("confirm", confirm)
                        .share(guestInfo.getBaseToken());
                    LoginLocationRegistry.getInstance().putAndRedirect(location, response);
                } else {
                    sendInvalidRequest(response);
                }
            }
        } catch (RateLimitedException e) {
            // Mark optional HTTP session as rate-limited
            HttpSession optionalHttpSession = request.getSession(false);
            if (optionalHttpSession != null) {
                optionalHttpSession.setAttribute(com.openexchange.servlet.Constants.HTTP_SESSION_ATTR_RATE_LIMITED, Boolean.TRUE);
            }
            // Send error response
            e.send(response);
        } catch (OXException | NoSuchAlgorithmException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Tools.disableCaching(response);
        try {
            request.getSession(true);

            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                sendInvalidRequest(response);
                return;
            }

            String confirm = request.getParameter("confirm");
            if (Strings.isEmpty(confirm)) {
                sendInvalidRequest(response);
                return;
            }

            String newPassword = request.getParameter("password");
            if (Strings.isEmpty(newPassword)) {
                sendInvalidRequest(response);
                return;
            }

            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            GuestInfo guestInfo = shareService.resolveGuest(token);
            if (guestInfo == null || AuthenticationMode.GUEST_PASSWORD != guestInfo.getAuthentication()) {
                sendInvalidRequest(response);
                return;
            }

            int contextID = guestInfo.getContextID();
            int guestID = guestInfo.getGuestID();
            User storageUser = loadAndPrepareGuest(guestID, contextID);

            String hash = getHash(storageUser.getUserPassword());
            if (confirm.equals(hash)) {
                ModuleSupport moduleSupport = ShareServiceLookup.getService(ModuleSupport.class, true);
                List<TargetProxy> possibleTargets = moduleSupport.listTargets(contextID, guestID);
                if (possibleTargets.isEmpty()) {
                    sendInvalidRequest(response);
                }
                Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(contextID);
                User updatedGuest = updatePassword(guestID, context, newPassword);
                if (!ShareServletUtils.createSessionAndRedirect(guestInfo, possibleTargets.get(0).getTarget(), request, response, loginMethod(updatedGuest, context))) {
                    sendInternalError(response);
                }
            } else {
                sendInvalidRequest(response);
            }
        } catch (RateLimitedException e) {
            // Mark optional HTTP session as rate-limited
            HttpSession optionalHttpSession = request.getSession(false);
            if (optionalHttpSession != null) {
                optionalHttpSession.setAttribute(com.openexchange.servlet.Constants.HTTP_SESSION_ATTR_RATE_LIMITED, Boolean.TRUE);
            }
            // Send error response
            e.send(response);
        } catch (OXException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(response);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(response);
        }
    }

    private static LoginMethodClosure loginMethod(final User user, final Context context) {
        return new LoginMethodClosure() {

            @Override
            public Authenticated doAuthentication(LoginResultImpl retval) throws OXException {
                return new ShareAuthenticated(user, context, null);
            }
        };
    }

    private User updatePassword(int userId, Context context, String newPassword) throws OXException {
        UserService userService = ShareServiceLookup.getService(UserService.class, true);
        User guest = userService.getUser(userId, context);
        UserImpl user = new UserImpl(guest);

        PasswordMechRegistry passwordMechRegistry = ShareServiceLookup.getService(PasswordMechRegistry.class, true);
        PasswordMech passwordMech = passwordMechRegistry.get(user.getPasswordMech());
        if (passwordMech == null) {
            passwordMech = StockPasswordMechs.BCRYPT.getPasswordMech();
        }
        PasswordDetails passwordDetails = passwordMech.encode(newPassword);
        user.setPasswordMech(passwordDetails.getPasswordMech());
        user.setUserPassword(passwordDetails.getEncodedPassword());
        user.setSalt(passwordDetails.getSalt());
        userService.updatePassword(user, context);
        userService.invalidateUser(context, userId);
        return userService.getUser(userId, context);
    }

    private User loadAndPrepareGuest(int userId, int contextId) throws OXException {
        UserService userService = ShareServiceLookup.getService(UserService.class, true);
        User storageUser = userService.getUser(userId, contextId);
        GuestService guestService = ShareServiceLookup.getService(GuestService.class);
        if (guestService != null && guestService.isCrossContextGuestHandlingEnabled()) {
            return guestService.alignUserWithGuest(storageUser, contextId);
        }
        return storageUser;
    }

    private String getHash(String toHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(toHash.getBytes("UTF-8"));
        md.update(hashSalt);
        byte[] hash = md.digest();
        // URL safe encoding without padding. Don't use plain base64 here!
        return BaseEncoding.base64Url().omitPadding().encode(hash);
    }

    private static void sendInvalidRequest(HttpServletResponse response) throws IOException {
        LoginLocation location = new LoginLocation().loginType(LoginType.MESSAGE).message(MessageType.ERROR, t -> t.translate(ShareServletStrings.INVALID_REQUEST));
        LoginLocationRegistry.getInstance().putAndRedirect(location, response);
    }

    private static void sendInternalError(HttpServletResponse response) throws IOException {
        LoginLocation location = new LoginLocation().loginType(LoginType.MESSAGE).message(MessageType.ERROR, t -> t.translate(OXExceptionStrings.MESSAGE_RETRY));
        LoginLocationRegistry.getInstance().putAndRedirect(location, response);
    }

}
