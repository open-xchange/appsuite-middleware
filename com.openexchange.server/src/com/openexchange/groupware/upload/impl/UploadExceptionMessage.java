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

package com.openexchange.groupware.upload.impl;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link UploadExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UploadExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link UploadExceptionMessage}.
     */
    private UploadExceptionMessage() {
        super();
    }

    // File upload failed: %1$s
    public final static String UPLOAD_FAILED_MSG = "File upload failed: \"%1$s\".";

    // Missing affiliation id
    public final static String MISSING_AFFILIATION_ID_MSG = "Missing affiliation id.";

    // Unknown action value: %1$s
    public final static String UNKNOWN_ACTION_VALUE_MSG = "Unknown action value: \"%1$s\".";

    // Header "content-type" does not indicate multipart content
    public final static String NO_MULTIPART_CONTENT_MSG = "Header \"content-type\" does not indicate multipart content.";

    // Request rejected because its size (%1$s) exceeds the maximum configured size of %2$s
    public final static String MAX_UPLOAD_SIZE_EXCEEDED_MSG = "Request rejected because its size (%1$s) exceeds the maximum configured size of %2$s.";

    // Missing parameter %1$s
    public final static String MISSING_PARAM_MSG = "Missing parameter: \"%1$s\".";

    // Unknown module: %1$d
    public final static String UNKNOWN_MODULE_MSG = "Unknown module: %1$d.";

    // An uploaded file referenced by %1$s could not be found
    public final static String UPLOAD_FILE_NOT_FOUND_MSG = "An uploaded file referenced by \"%1$s\" could not be found.";

    // Invalid action value: %1$s
    public final static String INVALID_ACTION_VALUE_MSG = "Invalid action value: \"%1$s\".";

    // Upload file with id %1$s could not be found
    public final static String FILE_NOT_FOUND_MSG = "Upload file with id \"%1$s\" could not be found.";

    // Upload file's content type \"%1$s\" does not match given file filter \"%2$s\"
    // Thrown to indicate that currently uploaded file's MIME type does not obey a certain search criterion
    public final static String INVALID_FILE_TYPE_MSG = "Upload file's content type \"%1$s\" does not match given file filter \"%2$s\".";

    // Upload file is invalid or illegal
    // Thrown to indicate that currently uploaded file is not accepted for being saved due to illegal and/or harmful content
    public final static String INVALID_FILE_MSG = "Upload file is invalid or illegal.";

    // Request rejected because its size exceeds the maximum configured size of %1$s
    public static final String MAX_UPLOAD_SIZE_EXCEEDED_UNKNOWN_MSG = "Request rejected because its size exceeds the maximum configured size of %1$s.";

    // Connection has been closed unexpectedly. Please try again.
    public static final String UNEXPECTED_EOF_MSG = "Connection has been closed unexpectedly. Please try again.";

    // Request rejected because file size (%1$s) exceeds the maximum configured file size of %2$s.
    public static final String MAX_UPLOAD_FILE_SIZE_EXCEEDED_MSG = "Request rejected because file size (%1$s) exceeds the maximum configured file size of %2$s.";

    // Request rejected because file size exceeds the maximum configured file size of %1$s.
    public static final String MAX_UPLOAD_FILE_SIZE_EXCEEDED_UNKNOWN_MSG = "Request rejected because file size exceeds the maximum configured file size of %1$s.";

    // Read timed out. Waited too long for incoming data from client.
    public static final String UNEXPECTED_TIMEOUT_MSG = "Read timed out. Waited too long for incoming data from client.";

}
