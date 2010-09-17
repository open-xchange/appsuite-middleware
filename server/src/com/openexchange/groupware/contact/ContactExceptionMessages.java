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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact;

import com.openexchange.exceptions.LocalizableStrings;

/**
 * {@link ContactExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ContactExceptionMessages implements LocalizableStrings {

    public static final String INVALID_EMAIL_MSG = "Invalid E-Mail address: '%s'. Please correct the E-Mail address.";

    public static final String IMAGE_SCALE_PROBLEM_MSG = "Unable to scale this contact image. Either the file type is not supported or the image is too large. Your mime type is %1$s and your image size is %2$d. The max. allowed image size is %3$d.";

    public static final String NON_CONTACT_FOLDER_MSG = "You are not allowed to store this contact in a non-contact folder: folder id %1$d in context %2$d with user %3$d";

    public static final String NO_PERMISSION_MSG = "You do not have permission to create objects in this folder %1$d in context %2$d with user %3$d";

    public static final String ID_GENERATION_FAILED_MSG = "Got a -1 ID from IDGenerator";

    public static final String IMAGE_DOWNSCALE_FAILED_MSG = "Unable to scale image down.";

    public static final String INVALID_SQL_QUERY_MSG = "Invalid SQL Query: %s";

    public static final String INIT_CONNECTION_FROM_DBPOOL_MSG = "Unable to pick up a connection from the DBPool";

    public static final String NOT_VALID_IMAGE_MSG = "The image you tried to attach is not a valid picture. It may be broken or is not a valid file.";

    public static final String MIME_TYPE_NOT_DEFINED_MSG = "Mime type is not defined.";

    public static final String PFLAG_IN_PUBLIC_FOLDER_MSG = "A contact with private flag cannot be stored in a public folder. Folder: %1$d context %2$d user %3$d";

    public static final String IMAGE_TOO_LARGE_MSG = "Image size too large. Image size: %1$d. Max. size: %2$d.";

    private ContactExceptionMessages() {
        super();
    }
}
