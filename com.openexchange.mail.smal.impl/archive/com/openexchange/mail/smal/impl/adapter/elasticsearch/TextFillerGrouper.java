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

package com.openexchange.mail.smal.impl.adapter.elasticsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link TextFillerGrouper}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TextFillerGrouper {

    /**
     * Initializes a new {@link TextFillerGrouper}.
     */
    private TextFillerGrouper() {
        super();
    }

    /**
     * Groups passed text fillers by user accounts.
     *
     * @param textFillers The text fillers to group
     * @return The grouped text fillers
     */
    public static List<List<TextFiller>> groupTextFillersByAccount(final Collection<TextFiller> textFillers) {
        final Map<Key, List<TextFiller>> map = new HashMap<TextFillerGrouper.Key, List<TextFiller>>(textFillers.size());
        for (final TextFiller textFiller : textFillers) {
            final Key key = accountKeyFor(textFiller);
            List<TextFiller> list = map.get(key);
            if (null == list) {
                list = new LinkedList<TextFiller>();
                map.put(key, list);
            }
            list.add(textFiller);
        }
        return new ArrayList<List<TextFiller>>(map.values());
    }

    /**
     * Groups passed text fillers by user account's folders.
     *
     * @param textFillers The text fillers to group
     * @return The grouped text fillers
     */
    public static List<List<TextFiller>> groupTextFillersByFullName(final Collection<TextFiller> textFillers) {
        final Map<Key, List<TextFiller>> map = new HashMap<TextFillerGrouper.Key, List<TextFiller>>(textFillers.size());
        for (final TextFiller textFiller : textFillers) {
            final Key key = folderKeyFor(textFiller);
            List<TextFiller> list = map.get(key);
            if (null == list) {
                list = new LinkedList<TextFiller>();
                map.put(key, list);
            }
            list.add(textFiller);
        }
        return new ArrayList<List<TextFiller>>(map.values());
    }

    private static Key accountKeyFor(final TextFiller textFiller) {
        return new Key(null, textFiller.getAccountId(), textFiller.getUserId(), textFiller.getContextId());
    }

    private static Key folderKeyFor(final TextFiller textFiller) {
        return new Key(textFiller.getFullName(), textFiller.getAccountId(), textFiller.getUserId(), textFiller.getContextId());
    }

    private static final class Key {

        private final String fullName;

        private final int accountId;

        private final int cid;

        private final int user;

        private final int hash;

        public Key(final String fullName, final int accountId, final int user, final int cid) {
            super();
            this.fullName = fullName;
            this.accountId = accountId;
            this.user = user;
            this.cid = cid;
            final int prime = 31;
            int result = 1;
            result = prime * result + accountId;
            result = prime * result + cid;
            result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (accountId != other.accountId) {
                return false;
            }
            if (cid != other.cid) {
                return false;
            }
            if (fullName == null) {
                if (other.fullName != null) {
                    return false;
                }
            } else if (!fullName.equals(other.fullName)) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

    } // End of class Key
}
