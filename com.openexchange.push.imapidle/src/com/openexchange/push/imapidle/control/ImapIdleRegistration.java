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

package com.openexchange.push.imapidle.control;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.push.imapidle.ImapIdlePushListener;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link ImapIdleRegistration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
class ImapIdleRegistration implements Delayed {

    private final long stamp;
    private final ImapIdlePushListener pushListener;
    private final IMAPFolder imapFolder;
    private final int hash;

    /**
     * Initializes a new {@link ImapIdleRegistration}.
     */
    ImapIdleRegistration(ImapIdlePushListener pushListener,  IMAPFolder imapFolder, long elapseMillis) {
        super();
        stamp = System.currentTimeMillis() + elapseMillis;
        this.imapFolder = imapFolder;
        this.pushListener = pushListener;
        hash = 31 * 1 + ((pushListener == null) ? 0 : pushListener.hashCode());
    }

    /**
     * Initializes a new {@link ImapIdleRegistration}.
     */
    ImapIdleRegistration(ImapIdlePushListener pushListener) {
        super();
        stamp = 0L;
        imapFolder = null;
        this.pushListener = pushListener;
        hash = 31 * 1 + ((pushListener == null) ? 0 : pushListener.hashCode());
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((ImapIdleRegistration) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the push listener.
     *
     * @return The push listener
     */
    public ImapIdlePushListener getPushListener() {
        return pushListener;
    }

    /**
     * Gets the IMAP folder currently idle on.
     *
     * @return The IMAP folder
     */
    public IMAPFolder getImapFolder() {
        return imapFolder;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof ImapIdleRegistration)) {
            return obj.equals(pushListener);
        }
        ImapIdleRegistration other = (ImapIdleRegistration) obj;
        if (pushListener == null) {
            if (other.pushListener != null) {
                return false;
            }
        } else if (!pushListener.equals(other.pushListener)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ImapIdleRegistration [stamp=" + stamp + ", pushListener=" + pushListener + "]";
    }

}
