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

package com.openexchange.mail.watcher;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;


/**
 * {@link MailAccessDelayElement} - A simple wrapper for {@link MailAccess} that implements {@link Delayed} interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessDelayElement implements Delayed {

    private static final int WATCHER_TIME = MailProperties.getInstance().getWatcherTime();

    /** The associated {@link MailAccess} instance */
    public final MailAccess<?, ?> mailAccess;

    /** The time stamp considered when to rate this element as expired/elapsed */
    public final long stamp;

    /**
     * Initializes a new {@link MailAccessDelayElement}.
     *
     * @param mailAccess The associated {@link MailAccess} instance
     * @param stamp The time stamp
     */
    public MailAccessDelayElement(MailAccess<?, ?> mailAccess, long stamp) {
        super();
        this.mailAccess = mailAccess;
        this.stamp = stamp;
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = stamp;
        long otherStamp = ((MailAccessDelayElement) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return (unit.convert(WATCHER_TIME - (System.currentTimeMillis() - stamp), TimeUnit.MILLISECONDS));
    }

    @Override
    public int hashCode() {
        return mailAccess.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MailAccessDelayElement)) {
            return false;
        }
        final MailAccessDelayElement other = (MailAccessDelayElement) obj;
        if (mailAccess == null) {
            if (other.mailAccess != null) {
                return false;
            }
        } else if (!mailAccess.equals(other.mailAccess)) {
            return false;
        }
        return true;
    }

}
