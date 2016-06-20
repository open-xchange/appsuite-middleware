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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.compose.share;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareComposeStrings} - The i18n string literals for share compose module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link ShareComposeStrings}.
     */
    private ShareComposeStrings() {
        super();
    }

    // The default name for a folder
    public static final String DEFAULT_NAME_FOLDER = "Folder";

    // The default name for a file
    public static final String DEFAULT_NAME_FILE = "File";

    // -----------------------------------------------------------------------------------------------------------------------------------

    // The name of the folder holding the attachments, which were shared to other recipients.
    public static final String FOLDER_NAME_SHARED_MAIL_ATTACHMENTS = "My shared mail attachments";

    public static final String SHARED_ATTACHMENTS_INTRO_SINGLE = "%1$s has shared the following file with you:";

    public static final String SHARED_ATTACHMENTS_INTRO_MULTI = "%1$s has shared the following files with you:";

    public static final String VIEW_FILE = "View file";

    public static final String VIEW_FILES = "View files";

    // The internationalized text put into text body of an email of which attachments exceed user's quota limitation
    // Indicates the elapsed date for affected message's attachments
    public static final String SHARED_ATTACHMENTS_EXPIRATION = "The link will expire on %1$s";

    // The internationalized text put into text body of an email of which attachments exceed user's quota limitation
    // Indicates the password for affected message's attachments
    public static final String SHARED_ATTACHMENTS_PASSWORD = "Please use the following password: %1$s";

    // Fall-back name if a file has no valid name
    public static final String DEFAULT_FILE_NAME = "Unnamed";

}
