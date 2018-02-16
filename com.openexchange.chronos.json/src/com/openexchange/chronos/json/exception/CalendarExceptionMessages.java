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

package com.openexchange.chronos.json.exception;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link CalendarExceptionMessages}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class CalendarExceptionMessages implements LocalizableStrings {

    /**
     * The event %s couldn't be deleted: %s
     */
    public static final String ERROR_DELETING_EVENT_MSG = "The event %s couldn't be deleted: %s";
    /**
     * Multiple events couldn't be deleted.
     */
    public static final String ERROR_DELETING_EVENTS_MSG = "Multiple events couldn't be deleted.";
    /**
     * Unable to add alarms: %s
     */
    public static final String UNABLE_TO_ADD_ALARMS_MSG = "Unable to add alarms: %s";
    /**
     * The content-id '%1$s' refers to a non-existing attachment in the body part.
     */
    public static final String MISSING_BODY_PART_ATTACHMENT_REFERENCE_MSG = "The content-id '%1$s' refers to a non-existing attachment in the body part.";
    /**
     * The metadata of the attachment '%1$s' does not have a content-id.
     */
    public static final String MISSING_METADATA_ATTACHMENT_REFERENCE_MSG = "The metadata of the attachment '%1$s' does not have a content-id.";
}
