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

package com.openexchange.groupware.attach;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link AttachmentExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class AttachmentExceptionMessages implements LocalizableStrings {

    public static final String OVER_LIMIT_MSG = "Attachment cannot be saved. File store limit is exceeded.";

    public static final String SAVE_FAILED_MSG = "Could not save file to the file store.";

    public static final String FILE_MISSING_MSG = "Attachments must contain a file.";

    public static final String READ_FAILED_MSG = "Could not retrieve file.";

    public static final String ATTACHMENT_NOT_FOUND_MSG = "The attachment you requested no longer exists. Please refresh the view.";

    public static final String DELETE_FAILED_MSG = "Could not delete attachment.";

    public static final String INVALID_CHARACTERS_MSG = "Attachment metadata contains invalid characters.";

    public static final String FILESTORE_DOWN_MSG = "Unable to access the file store.";

    public static final String INVALID_REQUEST_PARAMETER_MSG = "Invalid parameter sent in request. Parameter '%1$s' was '%2$s' which does not look like a number.";

    private AttachmentExceptionMessages() {
        super();
    }
}
