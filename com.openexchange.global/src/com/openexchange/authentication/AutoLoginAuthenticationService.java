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

package com.openexchange.authentication;

import com.openexchange.exception.OXException;

/**
 * This service is called when the UI does an autologin request and the login servlet is not able to find OX session cookies. Therefore the
 * {@link LoginInfo} will not contain login and password. Only header and cookies can be investigated to find a global web services session.
 * Use that information to return the necessary {@link Authenticated} data. If the {@link LoginInfo} does not contain a session, through a
 * {@link LoginException}.
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.org">Dennis Sieben</a>
 */
public interface AutoLoginAuthenticationService {

    /**
     * This method authenticates a user using a global web services session which is useful in single sign on scenarios. If no such global
     * web services session exists either throw a {@link LoginException} or redirect the browser to some global login site with
     * {@link ResultCode#REDIRECT}. This method should never return <code>null</code>.
     *
     * @param loginInfo the complete login information from the autologin request. It does never contain login and password.
     * @return an {@link Authenticated} containing context information to resolve the context and user information to resolve the user.
     * This return type can be enhanced with {@link SessionEnhancement} and/or {@link ResponseEnhancement}.
     * @throws OXException if something with the login info is wrong and no {@link Authenticated} can be returned.
     */
    Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException;

}
