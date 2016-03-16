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

package com.openexchange.groupware.notify;

import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.groupware.container.CalendarObject;

/**
 * This class contains all necessary information about a recipient of a
 * notification.
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EmailableParticipant implements Comparable<EmailableParticipant>, Cloneable {

    /**
     * Indicating no change compared to object's participants
     */
    public static final int STATE_NONE = 0;

    /**
     * Marks a participant as being newly added to object's participants
     */
    public static final int STATE_NEW = 1;

    /**
     * Marks a participant as being removed from object's participants
     */
    public static final int STATE_REMOVED = -1;

    private int hc;

    public String email;

    public String displayName;

    private Locale locale;

    public int type;

    public int id;

    public int[] groups;

    public TimeZone timeZone;

    public int reliability;

    public int folderId;

    public int cid;

    public int confirm = CalendarObject.NONE;

    public String confirmMessage;

    public boolean ignoreNotification;

    /**
     * The current participant's state: {@link #STATE_NONE} ,
     * {@link #STATE_REMOVED}, or {@link #STATE_NEW}
     */
    public int state = STATE_NONE;

    public EmailableParticipant(final int cid, final int type, final int id, final int[] groups, final String email,
            final String displayName, final Locale locale, final TimeZone timeZone, final int reliability,
            final int folderId, final int confirm, final String confirmMessage, final boolean ignoreNotification) {
        super();
        this.cid = cid;
        this.type = type;
        this.email = email;
        this.displayName = displayName;
        this.locale = locale;
        this.id = id;
        this.groups = groups;
        this.timeZone = timeZone;
        this.reliability = reliability;
        this.folderId = folderId;
        this.hc = getHashCode();
        this.confirm = confirm;
        this.confirmMessage = confirmMessage;
        this.ignoreNotification = ignoreNotification;
    }

    private int getHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.toLowerCase(Locale.ENGLISH).hashCode());
        return result;
    }

    public void copy(final EmailableParticipant participant) {
        this.cid = participant.cid;
        this.type = participant.type;
        this.email = participant.email;
        this.displayName = participant.displayName;
        this.locale = participant.locale;
        this.id = participant.id;
        this.groups = participant.groups;
        this.timeZone = participant.timeZone;
        this.reliability = participant.reliability;
        this.state = participant.state;
        this.confirm = participant.confirm;
        this.confirmMessage = participant.confirmMessage;
        this.ignoreNotification = participant.ignoreNotification;
        this.hc = participant.hc;
    }

    @Override
    public int hashCode() {
        return hc;
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof EmailableParticipant) {
            final EmailableParticipant other = (EmailableParticipant) o;
            return other.email.equalsIgnoreCase(email);
        }
        return false;
    }

    @Override
    public int compareTo(final EmailableParticipant other) {
        final String myCompare = displayName == null ? email : displayName;
        final String otherCompare = other.displayName == null ? other.email : other.displayName;
        final int retval;
        if (myCompare != null && otherCompare != null) {
            retval = myCompare.compareTo(otherCompare);
        } else {
            retval = 0;
        }
        return retval;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final EmailableParticipant clone = (EmailableParticipant) super.clone();
        clone.locale = (Locale) (locale == null ? null : locale.clone());
        clone.timeZone = (TimeZone) (timeZone == null ? null : timeZone.clone());
        return clone;
    }

    @Override
    public String toString() {
        return new StringBuilder("EmailableParticipant").append(" displayName=").append(displayName).append(" email=")
                .append(email).append(" locale=").append(locale).append(" timeZone=").append(timeZone.getID()).toString();
    }

    /**
     * @return the locale
     */
    public final Locale getLocale() {
        return locale;
    }
}
