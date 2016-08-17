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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.io.BaseEncoding;
import com.openexchange.authentication.Authenticated;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.guest.GuestService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.passwordmechs.IPasswordMech;
import com.openexchange.passwordmechs.PasswordMechFactory;
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
        Translator translator = Translator.EMPTY;
        try {
            TranslatorFactory translatorFactory = ShareServiceLookup.getService(TranslatorFactory.class, true);
            translator = translatorFactory.translatorFor(determineLocale(request, null));

            request.getSession(true);

            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                sendInvalidRequest(translator, response);
                return;
            }

            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            GuestInfo guestInfo = shareService.resolveGuest(token);
            if (guestInfo == null) {
                sendInvalidRequest(translator, response);
                return;
            }

            if (AuthenticationMode.GUEST_PASSWORD != guestInfo.getAuthentication()) {
                LoginLocation location = new LoginLocation().loginType(LoginType.MESSAGE).message(MessageType.INFO, translator.translate(ShareServletStrings.NO_GUEST_PASSWORD_REQUIRED));
                LoginLocationRegistry.getInstance().putAndRedirect(location, response);
                return;
            }

            int contextID = guestInfo.getContextID();
            int guestID = guestInfo.getGuestID();
            translator = translatorFactory.translatorFor(determineLocale(request, guestInfo));
            Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(contextID);
            User storageUser = ShareServiceLookup.getService(UserService.class, true).getUser(guestID, context);

            GuestService guestService = ShareServiceLookup.getService(GuestService.class);
            if (guestService != null && guestService.isCrossContextGuestHandlingEnabled()) {
                storageUser = guestService.alignUserWithGuest(storageUser, context.getContextId());
            }

            String userPassword = storageUser.getUserPassword();
            if (userPassword == null) {
                // Should not happen due to previous auth mode check but a race condition could cause a NPE here
                sendInvalidRequest(translator, response);
                return;
            }

            String hash = getHash(userPassword);
            String confirm = request.getParameter("confirm");
            if (Strings.isEmpty(confirm)) {
                /*
                 * Send notifications. For now we only have a mail transport. The API might get expanded to allow additional transports.
                 */
                ShareNotificationService shareNotificationService = ShareServiceLookup.getService(ShareNotificationService.class);
                shareNotificationService.sendPasswordResetConfirmationNotification(Transport.MAIL, guestInfo, hash, Tools.createHostData(request, contextID, guestID, storageUser.isGuest()));

                /*
                 * Redirect after notification was sent.
                 */
                LoginLocation location = new LoginLocation()
                    .loginType(LoginType.MESSAGE)
                    .message(MessageType.INFO, String.format(translator.translate(ShareServletStrings.RESET_PASSWORD), guestInfo.getEmailAddress()))
                    .share(guestInfo.getBaseToken());
                LoginLocationRegistry.getInstance().putAndRedirect(location, response);
            } else {
                if (confirm.equals(hash)) {
                    LoginLocation location = new LoginLocation()
                        .loginType(LoginType.RESET_PASSWORD)
                        .message(MessageType.INFO, String.format(translator.translate(ShareServletStrings.CHOOSE_PASSWORD), guestInfo.getEmailAddress()))
                        .parameter("confirm", confirm)
                        .share(guestInfo.getBaseToken());
                    LoginLocationRegistry.getInstance().putAndRedirect(location, response);
                } else {
                    sendInvalidRequest(translator, response);
                }
            }
        } catch (RateLimitedException e) {
            e.send(response);
        } catch (OXException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(translator, response);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(translator, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Tools.disableCaching(response);
        Translator translator = Translator.EMPTY;
        try {
            TranslatorFactory translatorFactory = ShareServiceLookup.getService(TranslatorFactory.class, true);
            translator = translatorFactory.translatorFor(determineLocale(request, null));

            request.getSession(true);

            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                sendInvalidRequest(translator, response);
                return;
            }

            String confirm = request.getParameter("confirm");
            if (Strings.isEmpty(confirm)) {
                sendInvalidRequest(translator, response);
                return;
            }

            String newPassword = request.getParameter("password");
            if (Strings.isEmpty(newPassword)) {
                sendInvalidRequest(translator, response);
                return;
            }

            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            GuestInfo guestInfo = shareService.resolveGuest(token);
            if (AuthenticationMode.GUEST_PASSWORD != guestInfo.getAuthentication()) {
                sendInvalidRequest(translator, response);
                return;
            }

            int contextID = guestInfo.getContextID();
            int guestID = guestInfo.getGuestID();
            translator = translatorFactory.translatorFor(determineLocale(request, guestInfo));
            User storageUser = loadAndPrepareGuest(guestID, contextID);

            String hash = getHash(storageUser.getUserPassword());
            if (confirm.equals(hash)) {
                ModuleSupport moduleSupport = ShareServiceLookup.getService(ModuleSupport.class, true);
                List<TargetProxy> possibleTargets = moduleSupport.listTargets(contextID, guestID);
                if (possibleTargets.isEmpty()) {
                    sendInvalidRequest(translator, response);
                }
                Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(contextID);
                User updatedGuest = updatePassword(guestID, context, newPassword);
                if (!ShareServletUtils.createSessionAndRedirect(guestInfo, possibleTargets.get(0).getTarget(), request, response, loginMethod(updatedGuest, context))) {
                    sendInternalError(translator, response);
                }
            } else {
                sendInvalidRequest(translator, response);
            }
        } catch (RateLimitedException e) {
            e.send(response);
        } catch (OXException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(translator, response);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Error processing reset-password '{}': {}", request.getPathInfo(), e.getMessage(), e);
            sendInternalError(translator, response);
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

        PasswordMechFactory passwordMechFactory = ShareServiceLookup.getService(PasswordMechFactory.class, true);
        IPasswordMech iPasswordMech = passwordMechFactory.get(IPasswordMech.BCRYPT);
        user.setPasswordMech(iPasswordMech.getIdentifier());
        user.setUserPassword(iPasswordMech.encode(newPassword));
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

    private static void sendInvalidRequest(Translator translator, HttpServletResponse response) throws IOException {
        LoginLocation location = new LoginLocation().loginType(LoginType.MESSAGE).message(MessageType.ERROR, translator.translate(ShareServletStrings.INVALID_REQUEST));
        LoginLocationRegistry.getInstance().putAndRedirect(location, response);
    }

    private static void sendInternalError(Translator translator, HttpServletResponse response) throws IOException {
        LoginLocation location = new LoginLocation().loginType(LoginType.MESSAGE).message(MessageType.ERROR, translator.translate(OXExceptionStrings.MESSAGE_RETRY));
        LoginLocationRegistry.getInstance().putAndRedirect(location, response);
    }

}
