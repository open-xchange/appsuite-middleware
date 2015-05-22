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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.servlet;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link ShareServletStrings}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class ShareServletStrings implements LocalizableStrings {

    public static final String FILE = "file";

    public static final String FOLDER = "folder";

    public static final String UNLIMITED = "unlimited";

    public static final String SHARE_WITH_TARGET = "%1$s would like you to view the %2$s \"%3$s\". ";

    public static final String SHARE_WITHOUT_TARGET = "Log in to view files shared with you. ";

    public static final String SHARE_WITHOUT_TARGET_WITH_DISPLAYNAME = "Log in to view files %1$s shared with you. ";

    public static final String ASK_PASSWORD_WITH_TARGET = "You can access \"%1$s\" %2$s more times without password. ";

    public static final String ASK_PASSWORD_WITHOUT_TARGET = "You can access these shares %1$s more times without password. ";

    public static final String ASK_PASSWORD = "You can set a password now.";

    public static final String REQUIRE_PASSWORD_WITH_TARGET = "To access \"%1$s\" you have to set a password.";

    public static final String REQUIRE_PASSWORD_WITHOUT_TARGET = "To access your shares you have to set a password.";

    public static final String RESET_PASSWORD = "Email sent to %1$s with further instructions on how to reset your password.";

    public static final String RESET_PASSWORD_DONE = "Your password has been reset.";

    public static final String SHARE_NOT_FOUND = "The share you are looking for does not exist.";

}
