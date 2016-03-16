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

package com.openexchange.login;

import com.openexchange.exception.OXException;

/**
 * {@link LoginHandlerService} - Handles a performed login.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface LoginHandlerService {

    /**
     * Handles the specified performed login.
     *
     * @param login The parameter object transporting all known values regarding this login process.
     * @throws OXException If an error occurs while handling the login
     */
    void handleLogin(LoginResult login) throws OXException;

    /**
     * Handles the specified performed logout.
     * This method is called in a very early step of the complete logout process and only handles the logout triggered by the user not any
     * other kinds of session terminations. It can be used to terminate resources bound to a session that are not required anymore in the
     * complete logout process.
     * If you need to perform any actions after the complete logout process then listen to the OSGi events of the session daemon under the
     * topic of removed sessions. The session object is passed in this event and your actions on it are performed as last steps right before
     * the session is finally gone. You need to implement the OSGi {@link org.osgi.service.event.EventHandler} and listen to the topics
     * defined in {@link com.openexchange.sessiond.SessiondEventConstants}.
     *
     * @param logout The parameter object transporting all known values regarding this logout process.
     * @throws OXException If an error occurs while handling the logout
     */
    void handleLogout(LoginResult logout) throws OXException;
}
