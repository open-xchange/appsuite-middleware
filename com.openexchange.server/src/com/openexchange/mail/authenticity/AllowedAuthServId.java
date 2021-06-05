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

package com.openexchange.mail.authenticity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link AllowedAuthServId}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class AllowedAuthServId {

    /** The logger constant */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AllowedAuthServId.class);

    /** The special instance accepting all AuthServ-Ids */
    public static final AllowedAuthServId ALL_ALLOWED = new AllowedAuthServId("*", new Checker() {

        @Override
        public boolean allows(String authServId) {
            return true;
        }
    });

    /**
     * Gets the allowed authserv-ids for given listing (may contain wild-cards).
     *
     * @param allowdAuthServIds The allowed authserv-ids
     * @return The immutable listing of allowed authserv-ids
     * @throws OXException If one of given topic identifiers is invalid
     */
    public static List<AllowedAuthServId> allowedAuthServIdsFor(String... allowdAuthServIds) throws OXException {
        if (null == allowdAuthServIds) {
            return null;
        }

        if (0 == allowdAuthServIds.length) {
            return Collections.emptyList();
        }

        return allowedAuthServIdsFor(Arrays.asList(allowdAuthServIds));
    }

    /**
     * Gets the allowed authserv-ids for given listing (may contain wild-cards).
     *
     * @param allowdAuthServIds The allowed authserv-ids
     * @return The immutable listing of allowed authserv-ids
     * @throws OXException If one of given topic identifiers is invalid
     */
    public static List<AllowedAuthServId> allowedAuthServIdsFor(Collection<String> allowdAuthServIds) throws OXException {
        if (null == allowdAuthServIds) {
            return null;
        }

        int size = allowdAuthServIds.size();
        if (size <= 0) {
            return Collections.emptyList();
        }

        Set<String> wildcards = null;
        Set<String> prefixes = null;
        Set<String> prefixesWithExpectedLength = null;
        Set<String> exacts = null;
        for (String allowdAuthServId : allowdAuthServIds) {
            allowdAuthServId = allowdAuthServId.trim();
            if (Strings.isNotEmpty(allowdAuthServId)) {
                if ("*".equals(allowdAuthServId)) {
                    return Collections.singletonList(ALL_ALLOWED);
                }
                int indexOfQuestionMark = allowdAuthServId.indexOf('?');
                int indexOfStar = allowdAuthServId.indexOf('*');
                if (indexOfQuestionMark >= 0 || indexOfStar >= 0) {
                    if (indexOfStar < 0 && indexOfQuestionMark == allowdAuthServId.length() - 1) {
                        // Ends with "?"
                        if (null == prefixesWithExpectedLength) {
                            prefixesWithExpectedLength = new LinkedHashSet<>(size);
                        }
                        String prefixToAdd = allowdAuthServId.substring(0, allowdAuthServId.length() - 1);
                        boolean add = true;
                        if (null != prefixes) {
                            for (Iterator<String> iter = prefixes.iterator(); add && iter.hasNext();) {
                                String existentPrefix = iter.next();
                                if (prefixToAdd.startsWith(existentPrefix)) {
                                    // A more generic one already exists
                                    add = false;
                                }
                            }
                        }
                        if (add) {
                            prefixesWithExpectedLength.add(prefixToAdd);
                        }
                    } else if (indexOfQuestionMark < 0 && indexOfStar == allowdAuthServId.length() - 1) {
                        // Ends with "*"
                        if (null == prefixes) {
                            prefixes = new LinkedHashSet<>(size);
                        }
                        String prefixToAdd = allowdAuthServId.substring(0, allowdAuthServId.length() - 1);
                        boolean add = true;
                        for (Iterator<String> iter = prefixes.iterator(); add && iter.hasNext();) {
                            String existentPrefix = iter.next();
                            if (prefixToAdd.startsWith(existentPrefix)) {
                                // A more generic one already exists
                                add = false;
                            } else if (existentPrefix.startsWith(prefixToAdd)) {
                                // A more generic one is about to be added
                                iter.remove();
                            }
                        }
                        if (null != prefixesWithExpectedLength) {
                            for (Iterator<String> iter = prefixesWithExpectedLength.iterator(); add && iter.hasNext();) {
                                String existentPrefix = iter.next();
                                if (existentPrefix.startsWith(prefixToAdd)) {
                                    // A more generic one is about to be added
                                    iter.remove();
                                }
                            }
                        }
                        if (add) {
                            prefixes.add(prefixToAdd);
                        }
                    } else {
                        // A regex
                        if (null == wildcards) {
                            wildcards = new LinkedHashSet<>(size);
                        }
                        wildcards.add(allowdAuthServId);
                    }
                } else {
                    if (null == exacts) {
                        exacts = new LinkedHashSet<>(size);
                    }
                    exacts.add(allowdAuthServId);
                }
            }
        }

        ImmutableList.Builder<AllowedAuthServId> builder = null;
        if (null != exacts) {
            for (String exact : exacts) {
                if (null == builder) {
                    builder = ImmutableList.builder();
                }
                builder.add(new AllowedAuthServId(exact, new ExactChecker(exact)));
            }
        }
        if (null != prefixes) {
            for (String prefix : prefixes) {
                if (null == builder) {
                    builder = ImmutableList.builder();
                }
                builder.add(new AllowedAuthServId(prefix + "*", new StartsWithChecker(prefix)));
            }
        }
        if (null != prefixesWithExpectedLength) {
            for (String prefix : prefixesWithExpectedLength) {
                if (null == builder) {
                    builder = ImmutableList.builder();
                }
                builder.add(new AllowedAuthServId(prefix + "?", new StartsWithExpectedLengthChecker(prefix)));
            }
        }
        if (null != wildcards) {
            for (String wildcard : wildcards) {
                if (null == builder) {
                    builder = ImmutableList.builder();
                }
                RegExChecker regExChecker = RegExChecker.instanceFor(wildcard);
                if (null != regExChecker) {
                    builder.add(new AllowedAuthServId(wildcard, regExChecker));
                } else {
                    LOGGER.error("Invalid wild-card pattern: {}", wildcard);
                    throw MailAuthenticityExceptionCodes.INVALID_PROPERTY.create(MailAuthenticityProperty.AUTHSERV_ID.getFQPropertyName());
                }
            }
        }
        return null == builder ? Collections.<AllowedAuthServId> emptyList() : builder.build();
    }

    // --------------------------------------------------------------------------

    private final String authServId;
    private final Checker checker;

    /**
     * Initializes a new {@link AllowedAuthServId}.
     */
    private AllowedAuthServId(String authServId, Checker checker) {
        super();
        this.authServId = authServId;
        this.checker = checker;
    }

    /**
     * Checks if this instance allows given authserv-id.
     *
     * @param authServId The authserv-id to check
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    public boolean allows(String authServId) {
        return null != authServId && checker.allows(authServId);
    }

    @Override
    public String toString() {
        return authServId;
    }

    // ------------------------------------------------------------------------------

    private static interface Checker {

        /**
         * Checks if this instance allows given AuthServ-Id.
         *
         * @param authServId The AuthServ-Id
         * @return <code>true</code> if allowed; otherwise <code>false</code>
         */
        boolean allows(String authServId);
    }

    private static final class ExactChecker implements Checker {

        private final String authServId;

        ExactChecker(String authServId) {
            super();
            this.authServId = authServId;
        }

        @Override
        public boolean allows(String authServId) {
            return this.authServId.equals(authServId);
        }
    }

    private static final class StartsWithChecker implements Checker {

        private final String prefix;

        StartsWithChecker(String prefix) {
            super();
            this.prefix = prefix;
        }

        @Override
        public boolean allows(String authServId) {
            return authServId.startsWith(prefix);
        }
    }

    private static final class StartsWithExpectedLengthChecker implements Checker {

        private final String prefix;
        private final int expectedLength;

        StartsWithExpectedLengthChecker(String prefix) {
            super();
            this.prefix = prefix;
            expectedLength = prefix.length() + 1;
        }

        @Override
        public boolean allows(String authServId) {
            return authServId.length() == expectedLength && authServId.startsWith(prefix);
        }
    }

    private static final class RegExChecker implements Checker {

        private static final ConcurrentMap<String, RegExChecker> instances = new ConcurrentHashMap<>();

        static RegExChecker instanceFor(String wildcardExpression) {
            RegExChecker checker = instances.get(wildcardExpression);
            if (null == checker) {
                try {
                    RegExChecker newchecker = new RegExChecker(wildcardExpression);
                    checker = instances.putIfAbsent(wildcardExpression, newchecker);
                    if (null == checker) {
                        checker = newchecker;
                    }
                } catch (PatternSyntaxException e) {
                    // Invalid regex pattern
                    return null;
                }
            }
            return checker;
        }

        // ----------------------------------------------------------------------

        /** The time-and-size-based eviction cache (24h, 10.000 elements) for regex results */
        private final LoadingCache<String, Boolean> cache;

        /**
         * Initializes a new {@link RegExChecker}.
         *
         * @param wildcardExpression The wild-card expression
         * @throws PatternSyntaxException If wild-card expression is invalid
         */
        private RegExChecker(String wildcardExpression) {
            super();
            final Pattern pattern = Pattern.compile(Strings.wildcardToRegex(wildcardExpression));
            cache = CacheBuilder.newBuilder().expireAfterAccess(24, TimeUnit.HOURS).maximumSize(10000).build(new CacheLoader<String, Boolean>() {

                @Override
                public Boolean load(String authServId) {
                    try {
                        return Boolean.valueOf(pattern.matcher(authServId).matches());
                    } catch (StackOverflowError x) {
                        LOGGER.warn("Failed to match authserv-id \"{}\" against pattern \"{}\"", authServId, wildcardExpression, x);
                        return Boolean.FALSE;
                    }
                }
            });
        }

        @Override
        public boolean allows(String authServId) {
            return cache.getUnchecked(authServId).booleanValue();
        }
    }
}
