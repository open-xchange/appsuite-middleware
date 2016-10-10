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

package com.openexchange.mail.autoconfig.sources;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.Autoconfig;

/**
 * {@link ConfigSource} - Generates an {@code Autoconfig} instance.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public interface ConfigSource {

    /**
     * Generates an {@code Autoconfig} instance for given arguments.
     *
     * @param emailLocalPart The local part of the Email address; <code>"<b>someone</b>@somewhere.org"</code>
     * @param emailDomain The domain part of the Email address; <code>"someone@<b>somewhere.org</b>"</code>
     * @param password The associated password
     * @param user The associated user
     * @param context The associated context
     * @return An {@code Autoconfig} instance or <code>null</code> if generation fails.
     * @throws OXException If operation fails for any reason
     */
    Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context) throws OXException;

    /**
     * Generates an {@code Autoconfig} instance for given arguments.
     *
     * @param emailLocalPart The local part of the Email address; <code>"<b>someone</b>@somewhere.org"</code>
     * @param emailDomain The domain part of the Email address; <code>"someone@<b>somewhere.org</b>"</code>
     * @param password The associated password
     * @param user The associated user
     * @param context The associated context
     * @param forceSecure <code>true</code> if a secure connection should be enforced; otherwise <code>false</code> to also allow plain ones
     * @param isOAuth <code>true</code> to mark passed password as an OAuth token (and thus performing XOAUTH2 authentication mechanism); otherwise <code>false</code> for a regular password
     * @return An {@code Autoconfig} instance or <code>null</code> if generation fails.
     * @throws OXException If operation fails for any reason
     */
    DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context, boolean forceSecure, boolean isOAuth) throws OXException;

}
