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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.quota;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link QuotaExceptionMessages} - Exception messages for quota module that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuotaExceptionMessages implements LocalizableStrings {

    public static final String QUOTA_EXCEEDED_MSG = "Quota exceeded. Please delete some items in order to create new ones. Please note: The quota refers to all items of all users in this context.";

    public static final String QUOTA_EXCEEDED_CALENDAR_MSG = "Quota exceeded for calendar. Quota limit: %2$s. Quota used: %1$s. Please delete some appointments in order to create new ones. Please note: The quota refers to all appointments of all users in this context.";

    public static final String QUOTA_EXCEEDED_CONTACTS_MSG = "Quota exceeded for contacts. Quota limit: %2$s. Quota used: %1$s. Please delete some contacts in order to create new ones. Please note: The quota refers to all contacts of all users in this context.";

    public static final String QUOTA_EXCEEDED_TASKS_MSG = "Quota exceeded for tasks. Quota limit: %2$s. Quota used: %1$s. Please delete some tasks in order to create new ones. Please note: The quota refers to all tasks of all users in this context.";

    public static final String QUOTA_EXCEEDED_FILES_MSG = "Quota exceeded for files. Quota limit: %2$s. Quota used: %1$s. Please delete some files in order to create new ones. Please note: The quota refers to all files of all users in this context.";

    public static final String QUOTA_EXCEEDED_ATTACHMENTS_MSG = "Quota exceeded for attachments. Quota limit: %2$s. Quota used: %1$s. Please delete some attachments in order to create new ones. Please note: The quota refers to all attachments of all users in this context.";

    /**
     * Initializes a new {@link QuotaExceptionMessages}.
     */
    private QuotaExceptionMessages() {
        super();
    }

}
