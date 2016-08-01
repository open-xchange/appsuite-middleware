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

package com.openexchange.tools.servlet;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link AjaxExceptionMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AjaxExceptionMessages implements LocalizableStrings {

    public static final String NON_SECURE_DENIED_MSG = "The requested operation is not permitted via a non-secure connection.";
    public static final String DISABLED_ACTION_MSG = "The requested operation is disabled.";
    public static final String NO_PERMISSION_FOR_MODULE = "You do not have appropriate permissions for module %1$s.";
    public static final String CONFLICT = "The object has been changed in the meantime. Please reload the view and try again.";
    public static final String NO_IMAGE_FILE_MSG = "The file \"%1$s\" (\"%2$s\") can't be imported as image. Only image types (JPG, GIF, BMP or PNG) are supported.";
    public static final String MISSING_COOKIE_MSG = "The requested operation requires a valid session. Please login and try again.";
    public static final String MISSING_FIELD_MSG = "Missing the following field in JSON data: %1$s";
    public static final String HTML_TOO_BIG_MSG = "The HTML content is too big and therefore cannot be safely displayed. Please select to download its content if you want to see it.";
    public static final String INVALID_JSON_REQUEST_BODY = "Invalid JSON data received from client";
    public static final String NOT_ALLOWED_URI_PARAM_MSG = "Client sent not allowed request parameter \"%1$s\" within the URI.";

    /**
     * Initializes a new {@link AjaxExceptionMessages}
     */
    private AjaxExceptionMessages() {
        super();
    }

}
