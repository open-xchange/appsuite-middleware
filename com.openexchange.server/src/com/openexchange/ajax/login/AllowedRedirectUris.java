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

package com.openexchange.ajax.login;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import com.openexchange.java.Strings;

/**
 * {@link AllowedRedirectUris} - A white-list for allowed redirect/referrer URIs on login error.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class AllowedRedirectUris {

    /** Check if a certain referrer URI matches */
    public static interface UriMatcher {

        /**
         * Checks if specified referrer URI does match according to this matcher's consideration.
         *
         * @return  <tt>true</tt> if referrer URI matches; otherwise <code>false</code>
         */
        boolean matches(URI referrerUri);
    }

    /** A matcher which checks if referrer URI ignore-case starts with a certain prefix */
    public static class IgnoreCasePrefixUriMatcher implements UriMatcher {

        private final String prefix;

        /**
         * Initializes a new {@link IgnoreCasePrefixClientMatcher}.
         *
         * @param prefix The prefix
         */
        public IgnoreCasePrefixUriMatcher(String prefix) {
            super();
            this.prefix = Strings.asciiLowerCase(prefix);
        }

        @Override
        public boolean matches(URI referrerUri) {
            return Strings.asciiLowerCase(referrerUri.toString()).startsWith(prefix);
        }
    }

    /** A matcher which uses a regular expression to check if a referrer URI matches */
    public static class PatternUriMatcher implements UriMatcher {

        private final Pattern pattern;

        /**
         * Initializes a new {@link PatternClientMatcher}.
         *
         * @param pattern
         */
        public PatternUriMatcher(Pattern pattern) {
            super();
            this.pattern = pattern;
        }

        @Override
        public boolean matches(URI referrerUri) {
            return pattern.matcher(referrerUri.toString()).matches();
        }
    }

    /** A matcher which checks if referrer URI ignore-case equals a certain URI */
    public static class IgnoreCaseExactUriMatcher implements UriMatcher {

        private final String referrerUri;

        /**
         * Initializes a new {@link IgnoreCaseExactClientMatcher}.
         *
         * @param referrerUri referrer URI
         */
        public IgnoreCaseExactUriMatcher(String referrerUri) {
            super();
            this.referrerUri = Strings.asciiLowerCase(referrerUri);
        }

        @Override
        public boolean matches(URI referrerUri) {
            return this.referrerUri.equals(Strings.asciiLowerCase(referrerUri.toString()));
        }
    }

    private static final AllowedRedirectUris INSTANCE = new AllowedRedirectUris();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static AllowedRedirectUris getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<UriMatcher, UriMatcher> map;

    /**
     * Initializes a new {@link AllowedRedirectUris}.
     */
    private AllowedRedirectUris() {
        super();
        map = new ConcurrentHashMap<UriMatcher, UriMatcher>(4, 0.9f, 1);
    }

   /**
    * Adds specified matcher if no such matcher is already contained.
    *
    * @param matcher The matcher to add
    * @return <code>true</code> for successful insertion; otherwise <code>false</code>
    */
   public boolean add(UriMatcher matcher) {
       return (null == map.putIfAbsent(matcher, matcher));
   }

   /**
    * Gets this white-list's size.
    *
    * @return The size
    */
   public int size() {
       return map.size();
   }

   /**
    * Checks if this white-list contains specified matcher.
    *
    * @param matcher The matcher
    * @return <code>true</code> if contained; otherwise <code>false</code>
    */
   public boolean contains(UriMatcher matcher) {
       return map.containsKey(matcher);
   }

   /**
    * Removes specified matcher.
    *
    * @param matcher The matcher
    * @return <code>true</code> if specified pattern was removed; otherwise <code>false</code>
    */
   public boolean remove(UriMatcher matcher) {
       return null != map.remove(matcher);
   }

   /**
    * Clears this white-list.
    */
   public void clear() {
       map.clear();
   }

   /**
    * Checks if this white-list is empty.
    *
    * @return <code>true</code> if this white-list is empty; otherwise <code>false</code>
    */
   public boolean isEmpty() {
       return map.isEmpty();
   }

   /**
    * Checks if specified referrer URI is matched by one of contained patterns.
    *
    * @param referrerUri The referrer URI
    * @return <code>true</code> if specified referrer URI is matched by one of contained patterns; otherwise <code>false</code>
    */
   public boolean isAllowed(URI referrerUri) {
       return null == referrerUri ? false : doCheckAllowed(referrerUri);
   }

   private boolean doCheckAllowed(URI referrerUri) {
       for (UriMatcher matcher : map.keySet()) {
           if (matcher.matches(referrerUri)) {
               return true;
           }
       }
       return false;
   }

}
