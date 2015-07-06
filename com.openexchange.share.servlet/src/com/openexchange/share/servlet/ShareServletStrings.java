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

    public static final String GUEST = "guest";

    public static final String FILE = "file";

    public static final String FOLDER = "folder";

    public static final String UNLIMITED = "unlimited";

    public static final String SHARE_WITH_TARGET = "%1$s has shared the %2$s \"%3$s\" with you. Please log in to view it. ";

    public static final String SHARE_WITHOUT_TARGET = "Files have been shared with you. Please log in to view them. ";

    public static final String SHARE_WITHOUT_TARGET_WITH_DISPLAYNAME = "%1$s has shared some files with you. ";

    public static final String ASK_PASSWORD_WITH_TARGET = "Please create a password to continue accessing it. ";

    public static final String ASK_PASSWORD_WITHOUT_TARGET = "Please create a password to continue accessing them. ";

    public static final String ASK_PASSWORD = "For additional security you can create a password.";

    public static final String REQUIRE_PASSWORD_WITH_TARGET = "Please create a password to log in and view it.";

    public static final String REQUIRE_PASSWORD_WITHOUT_TARGET = "Please create a password to log in and view them.";

    public static final String RESET_PASSWORD = "Email sent to %1$s with further instructions on how to reset your password.";

    public static final String CHOOSE_PASSWORD = "Please create a new password to regain access to your shares.";

    public static final String RESET_PASSWORD_DONE = "Your password has been reset.";

    public static final String SHARE_NOT_FOUND = "The share you are looking for does not exist.";

}
