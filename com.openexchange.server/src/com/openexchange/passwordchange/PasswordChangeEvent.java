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


package com.openexchange.passwordchange;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;

/**
 * {@link PasswordChangeEvent} - Event for password change containing the session of the user whose password shall be changed, the context,
 * the new password, and the old password (needed for verification)
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PasswordChangeEvent {

    private final Session session;

    private final Context ctx;

    private final String newPassword;

    private final String oldPassword;

    /**
     * Initializes a new {@link PasswordChangeEvent}
     *
     * @param session The session of the user whose password shall be changed
     * @param ctx The context
     * @param newPassword The new password
     * @param oldPassword The old password (needed for verification)
     */
    public PasswordChangeEvent(final Session session, final Context ctx, final String newPassword, final String oldPassword) {
        this.session = session;
        this.ctx = ctx;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    /**
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return The context
     */
    public Context getContext() {
        return ctx;
    }

    /**
     * @return The new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @return The old password
     */
    public String getOldPassword() {
        return oldPassword;
    }
}
