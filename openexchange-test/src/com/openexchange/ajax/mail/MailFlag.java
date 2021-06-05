/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
