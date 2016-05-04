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

package com.openexchange.share.limit;

/**
 * {@link FileAccess} A generic class that contains information about file accesses in a defined time frame. This may contain either used or allowed values.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
public class FileAccess {

    private final int userId;
    private final int contextId;
    private long size;
    private int count;
    private long timeOfStartInMillis;
    private long timeOfEndInMillis;

    public FileAccess(int contextId, int userId, long start, long end, int counts, long size) {
        this.contextId = contextId;
        this.userId = userId;
        this.timeOfStartInMillis = start;
        this.timeOfEndInMillis = end;
        this.size = size;
        this.count = counts;
    }

    public long getSize() {
        return size;
    }

    public int getCount() {
        return count;
    }

    public int getUserId() {
        return userId;
    }

    public int getContextId() {
        return contextId;
    }

    public long getTimeOfStartInMillis() {
        return timeOfStartInMillis;
    }

    public long getTimeOfEndInMillis() {
        return timeOfEndInMillis;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTimeOfStartInMillis(long timeOfStartInMillis) {
        this.timeOfStartInMillis = timeOfStartInMillis;
    }

    public void setTimeOfEndInMillis(long timeOfEndInMillis) {
        this.timeOfEndInMillis = timeOfEndInMillis;
    }

    public static boolean isExceeded(FileAccess allowed, FileAccess used) {
        if (isSizeExceeded(allowed, used) || isCountExceeded(allowed, used)) {
            return true;
        }
        return false;
    }

    public static boolean isCountExceeded(FileAccess allowed, FileAccess used) {
        if (used.getCount() > allowed.getCount()) {
            return true;
        }
        return false;
    }

    public static boolean isSizeExceeded(FileAccess allowed, FileAccess used) {
        if (used.getSize() > allowed.getSize()) {
            return true;
        }
        return false;
    }
}
