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

package com.openexchange.ajax.mail;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link MailFlag}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public enum MailFlag {
    ANSWERED(1, "answered"),
    DELETED(2, "deleted"),
    DRAFT(4, "draft"),
    FLAGGED(8, "flagged"),
    RECENT(16, "recent"),
    SEEN(32, "seen"),
    USER(64, "user"),
    SPAM(128, "spam"),
    FORWARDED(256, "forwarded"),
    READ_ACK(512, "read_ack");

    private int value;

    private String name;

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    private MailFlag(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public boolean isContainedIn(int bitmask) {
        return (bitmask & value) > 0;
    }

    public static MailFlag getByValue(int value) {
        MailFlag[] values = values();
        for (MailFlag temp : values) {
            if (temp.value == value) {
                return temp;
            }
        }
        return null;
    }

    public static MailFlag getByName(String searched) {
        MailFlag[] values = values();
        for (MailFlag temp : values) {
            if (searched.trim().equalsIgnoreCase(temp.name)) {
                return temp;
            }
        }
        return null;
    }

    public static Set<MailFlag> transform(int bitmask) {
        HashSet<MailFlag> set = new HashSet<MailFlag>();
        for (int i = 1; i <= bitmask;) {
            if ((i & bitmask) > 0 && getByValue(i) != null) {
                set.add(getByValue(i));
            }
            i = i << 1;
        }
        return set;
    }
}
