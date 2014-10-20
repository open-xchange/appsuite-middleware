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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.RandomStringUtils;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.java.Strings;
import com.openexchange.passwordmechs.PasswordMech;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.Share;
import com.openexchange.share.ShareService;
import com.openexchange.share.notification.ShareNotification.NotificationType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.mail.MailNotification;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.ratelimit.RateLimitedException;
import com.openexchange.user.UserService;

/**
 * {@link ResetPasswordServlet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = -598655895873570676L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ResetPasswordServlet.class);

    // --------------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link ResetPasswordServlet}.
     */
    public ResetPasswordServlet() {
        super();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Create a new HttpSession if it is missing
            request.getSession(true);

            // Read share token
            String token = request.getParameter("share");
            if (Strings.isEmpty(token)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Read guest E-Mail address
            String mail = request.getParameter("mail");
            if (Strings.isEmpty(mail)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // Resolve to share
            ShareService shareService = ShareServiceLookup.getService(ShareService.class, true);
            Share share = null == token ? null : shareService.resolveToken(token);
            if (null == share) {
                LOG.debug("No share found for '{}'", null == token ? "null" : token);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Check #1
            if (AuthenticationMode.GUEST_PASSWORD != share.getAuthentication()) {
                LOG.debug("Bad attempt to reset password for share '{}'", token);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Check #2
            UserService userService = ShareServiceLookup.getService(UserService.class, true);
            Context context = ShareServiceLookup.getService(ContextService.class, true).getContext(share.getContextID());
            User guest = userService.getUser(share.getGuest(), context);
            if (false == guest.isGuest() || false == mail.equals(guest.getMail())) {
                LOG.debug("Bad attempt to reset password for share '{}'", token);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // Generate a new password
            String newPassword = RandomStringUtils.random(10, true, true);
            PasswordMech passwordMech = PasswordMech.getPasswordMechFor(guest.getPasswordMech());
            // FIXME:
            if (passwordMech == null) {
                passwordMech = PasswordMech.BCRYPT;
            }

            // Update guest entry in database
            update(passwordMech.encode(newPassword), share.getGuest(), share.getContextID());

            // Invalidate
            userService.invalidateUser(context, share.getGuest());

            // Notify
            ShareNotificationService notificationService = ShareServiceLookup.getService(ShareNotificationService.class, true);
            String url = shareService.generateShareURLs(Collections.singletonList(share), Tools.getProtocol(request), request.getServerName()).get(0);

            MailNotification notification = new MailNotification(NotificationType.PASSWORD_RESET, share, url, null, mail);
            notificationService.notify(notification, new ResetPasswordSession(share.getGuest(), share.getContextID(), newPassword, request));
        } catch (RateLimitedException e) {
            response.setContentType("text/plain; charset=UTF-8");
            if(e.getRetryAfter() > 0) {
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

    // --------------------------------------------------------------------------------------------------------------------------------- //

    private void update(String encodedPassword, int guestId, int contextId) throws OXException {
        // Update database
        DatabaseService databaseService = ShareServiceLookup.getService(DatabaseService.class, true);

        Connection writeCon = databaseService.getWritable(contextId);
        boolean rollback = false;
        try {
            writeCon.setAutoCommit(false);
            rollback = true;
            update(writeCon, encodedPassword,guestId, contextId);
            deleteAttr(writeCon, guestId, contextId);
            writeCon.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(writeCon);
            }
            Databases.autocommit(writeCon);
            databaseService.backWritable(contextId, writeCon);
        }
    }

    private void update(Connection writeCon, String encodedPassword, int userId, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement("UPDATE user SET userPassword = ?, shadowLastChange = ? WHERE cid = ? AND id = ?");
            int pos = 1;
            stmt.setString(pos++, encodedPassword);
            stmt.setInt(pos++,(int)(System.currentTimeMillis()/1000));
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private void deleteAttr(Connection writeCon, int userId, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ? AND name = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos++, "passcrypt");
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
