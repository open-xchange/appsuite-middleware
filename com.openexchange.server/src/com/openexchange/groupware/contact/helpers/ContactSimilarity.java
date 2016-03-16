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

package com.openexchange.groupware.contact.helpers;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.arrays.Arrays;


/**
 * {@link ContactSimilarity}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactSimilarity {

    public static final int THRESHHOLD = 9;

    public static boolean areSimilar(Contact original, Contact candidate) {
        return THRESHHOLD < calculateSimilarityScore(original, candidate);
    }

    private static final int[] MATCH_COLUMNS = I2i(Arrays.remove(i2I(Contact.CONTENT_COLUMNS), I(Contact.USERFIELD20)));

    public static int calculateSimilarityScore(Contact original, Contact candidate) {
        int score = 0;

        if ((isset(original.getGivenName()) || isset(candidate.getGivenName())) && eq(original.getGivenName(), candidate.getGivenName())) {
            score += 5;
        }
        if ((isset(original.getSurName()) || isset(candidate.getSurName())) && eq(original.getSurName(), candidate.getSurName())) {
            score += 5;
        }

        if ((isset(original.getDisplayName()) || isset(candidate.getDisplayName())) && compareDisplayNames(original.getDisplayName(), candidate.getDisplayName())) {
            score += 10;
        }

        if (isset(original.getDisplayName()) && isset(candidate.getSurName()) && isset(candidate.getGivenName())) {
            String displayName = original.getDisplayName();
            if (displayName.contains(candidate.getGivenName()) && displayName.contains(candidate.getSurName())) {
                score += 10;
            }
        }

        if (isset(candidate.getDisplayName()) && isset(original.getSurName()) && isset(original.getGivenName())) {
            String displayName = candidate.getDisplayName();
            if (displayName.contains(original.getGivenName()) && displayName.contains(original.getSurName())) {
                score += 10;
            }
        }

        // an email-address is unique so if this is identical the contact should be the same
        Set<String> mails = new HashSet<String>();
        List<String> mails1 = java.util.Arrays.asList(original.getEmail1(), original.getEmail2(), original.getEmail3());
        for (String mail : mails1) {
            if (mail != null) {
                mails.add(mail);
            }
        }
        List<String> mails2 = java.util.Arrays.asList(candidate.getEmail1(), candidate.getEmail2(), candidate.getEmail3());
        for (String mail : mails2) {
            if (mail != null && mails.contains(mail)) {
                score += 10;
            }
        }

        List<String> purged = getPurgedDisplayNameComponents(original.getDisplayName());
        for(String mail : mails2) {
            if (mail == null) {
                continue;
            }
            if (isset(original.getGivenName()) && isset(original.getSurName()) && mail.contains(original.getGivenName().toLowerCase()) && mail.contains(original.getSurName().toLowerCase())) {
                score += 10;
            }
            if (purged.size() >= 2) {
                int count = 0;
                for(String comp : purged) {
                    if (mail.contains(comp.toLowerCase())) {
                        count++;
                    }
                }
                if (count == purged.size()) {
                    score += 10;
                }
            }
        }

        List<String> purged2 = getPurgedDisplayNameComponents(candidate.getDisplayName());
        for(String mail : mails) {
            if (mail == null) {
                continue;
            }
            if (isset(candidate.getGivenName()) && isset(candidate.getSurName()) && mail.contains(candidate.getGivenName().toLowerCase()) && mail.contains(candidate.getSurName().toLowerCase())) {
                score += 10;
            }
            if (purged2.size() >= 2) {
                int count = 0;
                for(String comp : purged2) {
                    if (mail.contains(comp.toLowerCase())) {
                        count++;
                    }
                }
                if (count == purged2.size()) {
                    score += 10;
                }
            }
        }

        if (original.containsBirthday() && candidate.containsBirthday() && eq(original.getBirthday(), candidate.getBirthday())) {
            score += 5;
        }

        if( score < THRESHHOLD && original.matches(candidate, MATCH_COLUMNS)) { //the score check is only to speed the process up
            score = THRESHHOLD + 1;
        }
        return score;
    }

    public static List<String> getPurgedDisplayNameComponents(String displayName) {
        if (displayName == null) {
            return Collections.emptyList();
        }
        List<String> d1 = java.util.Arrays.asList(displayName.split("\\s+"));
        List<String> p1 = new ArrayList<String>();
        for (String string : d1) {
            String purged = purge(string);
            if (purged != null) {
                p1.add(purged);
            }
        }
        return p1;
    }

    public static boolean compareDisplayNames(String displayName, String displayName2) {
        if (eq(displayName, displayName2)) {
            return true;
        }
        if (displayName == null || displayName2 == null) {
            return false;
        }
        List<String> p1 = getPurgedDisplayNameComponents(displayName);
        List<String> p2 = getPurgedDisplayNameComponents(displayName2);

        // any two must match
        int count = 0;
        for(String string : p2) {
            if (p1.contains(string)) {
                count++;
            }
        }

        return count == Math.min(p1.size(), p2.size()) && count >= 2;
    }

    public static String purge(String component) {
        // throw away non characters

        int length = component.length();
        StringBuilder b = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = component.charAt(i);
            if (Character.isLetter(c)) {
                b.append(c);
            }
        }
        // sort out length 2
        if (b.length() > 2) {
            return b.toString();
        }
        return null;
    }

    public static boolean isset(String s) {
        return s != null && s.length() > 0;
    }

    public static boolean eq(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
}
