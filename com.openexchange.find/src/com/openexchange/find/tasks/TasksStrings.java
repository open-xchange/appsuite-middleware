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

package com.openexchange.find.tasks;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link TasksStrings}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class TasksStrings implements LocalizableStrings {

    // ------------------------- i18n strings for facet types -------------------------------------- //

    // Context: Searching in tasks.
    // Displayed as: [Search for] 'user input' in title.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_TASK_TITLE = "in title";

    // Search in folders.
    public static final String FACET_TASK_FOLDERS = "Task folders";

    // Search in persons.
    public static final String FACET_TASK_PARTICIPANTS = "Task participants";

    // Search in task type.
    public static final String FACET_TASK_TYPE = "Task type";

    // Search in task status
    public static final String FACET_TASK_STATUS = "Task status";

    // Context: Searching in tasks.
    // Displayed as: [Search for] 'user input' in description.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_TASK_DESCRIPTION = "in description";

    // Context: Searching in tasks.
    // Displayed as: [Search for] 'user input' in attachment name.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_TASK_ATTACHMENT_NAME = "in attachment name";

    // ------------------------- i18n strings for folder types ------------------------------------- //

    public static final String TASK_STATUS_NOT_STARTED = "Not Started";

    public static final String TASK_STATUS_IN_PROGRESS = "In Progress";

    public static final String TASK_STATUS_DONE = "Done";

    public static final String TASK_STATUS_WAITING = "Waiting";

    public static final String TASK_STATUS_DEFERRED = "Deferred";

    public static final String TASK_TYPE_SINGLE_TASK = "Single Task";

    public static final String TASK_TYPE_SERIES = "Series";

}
