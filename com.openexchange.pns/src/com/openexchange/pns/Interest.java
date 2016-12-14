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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;

/**
 * {@link Interest} - Represents an interest for either all topics, a topic namespace or an exact topic.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class Interest {

    /** The special instance having interest for all topics */
    public static final Interest ALL_INTEREST = new Interest(KnownTopic.ALL.getName(), new Checker() {

        @Override
        public boolean isInterestedIn(String topic) {
            return true;
        }
    });

    /**
     * Gets the interests for given topics (may contain wild-card topics).
     *
     * @param topics The topics for which to return the interests; e.g. <code>"ox:mail:new"</code> or <code>"ox:mail:*"</code>
     * @return The immutable listing of interests
     * @throws IllegalArgumentException If one of given topic identifiers is invalid
     */
    public static List<Interest> interestsFor(String... topics) {
        if (null == topics) {
            return null;
        }

        return interestsFor(Arrays.asList(topics));
    }

    /**
     * Gets the interests for given topics (may contain wild-card topics).
     *
     * @param topics The topics for which to return the interests; e.g. <code>"ox:mail:new"</code> or <code>"ox:mail:*"</code>
     * @return The immutable listing of interests
     * @throws IllegalArgumentException If one of given topic identifiers is invalid
     */
    public static List<Interest> interestsFor(Collection<String> topics) {
        if (null == topics) {
            return null;
        }

        int size = topics.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        Set<String> prefixes = null;
        Set<String> exacts = null;
        for (String topic : topics) {
            PushNotifications.validateTopicName(topic);
            if (KnownTopic.ALL.getName().equals(topic)) {
                return Collections.singletonList(ALL_INTEREST);
            }
            if (topic.endsWith(":*")) {
                if (null == prefixes) {
                    prefixes = new LinkedHashSet<>(size);
                }
                prefixes.add(topic.substring(0, topic.length() - 1));
            } else {
                if (null == exacts) {
                    exacts = new LinkedHashSet<>(size);
                }
                exacts.add(topic);
            }
        }

        ImmutableList.Builder<Interest> builder = ImmutableList.builder();
        if (null != exacts) {
            for (String exact : exacts) {
                if (null == prefixes) {
                    builder.add(new Interest(exact, new ExactChecker(exact)));
                } else {
                    // Maybe the exact topic is already covered by a prefix
                    boolean added = false;
                    for (Iterator<String> it = prefixes.iterator(); !added && it.hasNext();) {
                        String prefix = it.next();
                        if (exact.startsWith(prefix)) {
                            builder.add(new Interest(prefix + "*", new StartsWithChecker(prefix)));
                            it.remove();
                            added = true;
                        }
                    }
                    if (false == added) {
                        builder.add(new Interest(exact, new ExactChecker(exact)));
                    }
                }
            }
        }
        if (null != prefixes) {
            for (String prefix : prefixes) {
                builder.add(new Interest(prefix + "*", new StartsWithChecker(prefix)));
            }
        }
        return builder.build();
    }

    // --------------------------------------------------------------------------

    private final String interest;
    private final Checker checker;

    /**
     * Initializes a new {@link Interest}.
     */
    private Interest(String interest, Checker checker) {
        super();
        this.interest = interest;
        this.checker = checker;
    }

    /**
     * Checks if this instance signals interest for given topic.
     *
     * @param topic The topic identifier; e.g <code>"ox:mail:new"</code>
     * @return <code>true</code> if interested; otherwise <code>false</code>
     */
    public boolean isInterestedIn(String topic) {
        return null != topic && checker.isInterestedIn(topic);
    }

    @Override
    public String toString() {
        return interest;
    }

    // ------------------------------------------------------------------------------

    private static interface Checker {

        /**
         * Checks if this instance signals interest for given topic.
         *
         * @param topic The topic identifier; e.g <code>"ox:mail:new"</code>
         * @return <code>true</code> if interested; otherwise <code>false</code>
         */
        boolean isInterestedIn(String topic);
    }

    private static final class ExactChecker implements Checker {

        private final String exactTopic;

        ExactChecker(String exactTopic) {
            super();
            this.exactTopic = exactTopic;
        }

        @Override
        public boolean isInterestedIn(String topic) {
            return exactTopic.equals(topic);
        }
    }

    private static final class StartsWithChecker implements Checker {

        private final String prefix;

        StartsWithChecker(String prefix) {
            super();
            if (false == prefix.endsWith(":")) {
                throw new IllegalArgumentException("No a valid wild-card topic: " + prefix);
            }
            this.prefix = prefix;
        }

        @Override
        public boolean isInterestedIn(String topic) {
            return topic.startsWith(prefix);
        }
    }

}
