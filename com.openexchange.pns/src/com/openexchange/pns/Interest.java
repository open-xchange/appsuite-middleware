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

        if (0 == topics.length) {
            return Collections.emptyList();
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
                String prefixToAdd = topic.substring(0, topic.length() - 1);
                boolean add = true;
                for (Iterator<String> iter = prefixes.iterator(); add && iter.hasNext(); ) {
                    String existentPrefix = iter.next();
                    if (prefixToAdd.startsWith(existentPrefix)) {
                        // A more generic one already exists
                        add = false;
                    } else if (existentPrefix.startsWith(prefixToAdd)) {
                        // A more generic one is about to be added
                        iter.remove();
                    }
                }
                if (add) {
                    prefixes.add(prefixToAdd);
                }
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
                throw new IllegalArgumentException("Not a valid wild-card topic: " + prefix);
            }
            this.prefix = prefix;
        }

        @Override
        public boolean isInterestedIn(String topic) {
            return topic.startsWith(prefix);
        }
    }

}
