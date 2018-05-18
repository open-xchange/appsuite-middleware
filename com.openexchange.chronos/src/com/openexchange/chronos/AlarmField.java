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

package com.openexchange.chronos;

/**
 * {@link AlarmField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public enum AlarmField {

    /**
     * The internal identifier of the alarm.
     */
    ID,
    /**
     * The universal identifier of the alarm.
     */
    UID,
    /**
     * The relationship between this and other alarms.
     */
    RELATED_TO,
    /**
     * The time when the alarm was last sent or acknowledged.
     */
    ACKNOWLEDGED,
    /**
     * The type of action invoked when the alarm is triggered.
     */
    ACTION,
    /**
     * The additional repetitions of the alarm's trigger.
     */
    REPEAT,
    /**
     * The moment the alarm will trigger.
     */
    TRIGGER,
    /**
     * Extended properties of the alarm.
     */
    EXTENDED_PROPERTIES,
    /**
     * A list of attachments. Can be used as the sound source for the audio action or as attachments for the mail action.
     */
    ATTACHMENTS,
    /**
     * A summary which can be used as the mail subject for the mail action.
     */
    SUMMARY,
    /**
     * A description which can either be used to be display for the display action or used as a text for the mail action.
     */
    DESCRIPTION,

    /**
     * A list of mail addresses for the mail action.
     */
    ATTENDEES

    ;
}
