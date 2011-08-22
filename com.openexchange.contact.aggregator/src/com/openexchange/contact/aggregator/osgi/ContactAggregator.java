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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.contact.aggregator.osgi;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i2I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.contact.aggregator.osgi.ContactSource.Type;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ContactAggregator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactAggregator {
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ContactAggregator.class));
    
    private final List<ContactSourceFactory> factories = new ArrayList<ContactSourceFactory>();
    private final List<ContactSource> sources = new ArrayList<ContactSource>();
    
    
    
    public List<Contact> aggregate(final ServerSession session) throws Exception {
        final List<Contact> currentContacts = new ArrayList<Contact>();
        final List<ContactSource> allSources = new ArrayList<ContactSource>();
        allSources.addAll(sources);
        for(final ContactSourceFactory factory: factories) {
            allSources.addAll(factory.getSources(session));
        }
        
        final List<ContactSource> confirmed = new ArrayList<ContactSource>();
        final List<ContactSource> contribs = new ArrayList<ContactSource>();
        partition(allSources, confirmed, contribs);
        
        for (final ContactSource source : confirmed) {
            final List<Contact> fromSource = source.getContacts(session);
            LOG.debug("Got "+fromSource.size()+" contacts from "+source);
            for (final Contact newContact : fromSource) {
                boolean updated = false;
                for(final Contact knownContact : new ArrayList<Contact>(currentContacts)) {
                    if (calculateSimilarityScore(newContact, knownContact, session) > 9) {
                        update(knownContact, newContact);
                        updated = true;
                        break;
                    }   
                }
                if (!updated) {
                    currentContacts.add(newContact);
                }
            }
        }
        
        for (final ContactSource source : contribs) {
            final List<Contact> fromSource = source.getContacts(session);
            LOG.debug("Got "+fromSource.size()+" contacts from "+source);
            for (final Contact newContact : fromSource) {
                for(final Contact knownContact : new ArrayList<Contact>(currentContacts)) {
                    if (calculateSimilarityScore(newContact, knownContact, session) > 9) {
                        update(knownContact, newContact);
                    }
                }
            }
        }

        
        return currentContacts;
    }
    
    private void partition(final List<ContactSource> allSources, final List<ContactSource> confirmed, final List<ContactSource> contribs) {
        for (final ContactSource contactSource : allSources) {
            if (Type.CONFIRMED == contactSource.getType()) {
                confirmed.add(contactSource); 
            } else {
                contribs.add(contactSource);
            }
            
        }
    }

    private void update(final Contact aggregated, final Contact addition) {
        LOG.debug("Merging "+aggregated+" with "+addition);
        final int[] columns = Contact.CONTENT_COLUMNS;
        for (final int field : columns){
            if (aggregated.get(field) == null){
                if (addition.get(field) != null){
                    aggregated.set(field, addition.get(field));
                }
            }
        }
        final int[] mailColumns = new int[]{Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3};
        
        for(final int mailColumn : mailColumns) {
            final String mail = (String) addition.get(mailColumn);
            if (mail != null) {
                int columnCandidate = -1;
                for(final int mailColumn2 : mailColumns) {
                    final String mail2 = (String) aggregated.get(mailColumn2);
                    if (mail2 == null) {
                        if (columnCandidate == -1) {
                            columnCandidate = mailColumn2;
                        }
                    } else if (mail2.equals(mail)) {
                        columnCandidate = -1;
                        break;
                    }
                }
                if (columnCandidate != -1) {
                    aggregated.set(columnCandidate, mail);
                }
            }
        }
    }

    private static final int[] MATCH_COLUMNS = I2i(Arrays.remove(i2I(Contact.CONTENT_COLUMNS), I(Contact.USERFIELD20)));

    public static int calculateSimilarityScore(final Contact original, final Contact candidate, final Object session) {
        int score = 0;
        final int threshold = 9; // Of course
        
        if ((isset(original.getGivenName()) || isset(candidate.getGivenName())) && eq(original.getGivenName(), candidate.getGivenName())) {
            LOG.debug("givenName");
            score += 5;
        }
        if ((isset(original.getSurName()) || isset(candidate.getSurName())) && eq(original.getSurName(), candidate.getSurName())) {
            LOG.debug("surName");
            score += 5;
        }
        
        if ((isset(original.getDisplayName()) || isset(candidate.getDisplayName())) && compareDisplayNames(original.getDisplayName(), candidate.getDisplayName())) {
            LOG.debug("dispName");
            score += 10;
        }
        
        if (isset(original.getDisplayName()) && isset(candidate.getSurName()) && isset(candidate.getGivenName())) {
            final String displayName = original.getDisplayName();
            if (displayName.contains(candidate.getGivenName()) && displayName.contains(candidate.getSurName())) {
                LOG.debug("sur and given in disp");
                score += 10;
            }
        }
        
        if (isset(candidate.getDisplayName()) && isset(original.getSurName()) && isset(original.getGivenName())) {
            final String displayName = candidate.getDisplayName();
            if (displayName.contains(original.getGivenName()) && displayName.contains(original.getSurName())) {
                LOG.debug("sur and given in disp 2");
                score += 10;
            }
        }
        
        // an email-address is unique so if this is identical the contact should be the same
        final Set<String> mails = new HashSet<String>();
        final List<String> mails1 = java.util.Arrays.asList(original.getEmail1(), original.getEmail2(), original.getEmail3());
        for (final String mail : mails1) {
            if (mail != null) {
                mails.add(mail);
            }
        }
        final List<String> mails2 = java.util.Arrays.asList(candidate.getEmail1(), candidate.getEmail2(), candidate.getEmail3());
        for (final String mail : mails2) {
            if (mail != null && mails.contains(mail)) {
                LOG.debug("mail "+mail+" in "+mails);
                
                score += 10;
            }
        }
        
        final List<String> purged = getPurgedDisplayNameComponents(original.getDisplayName());
        for(final String mail : mails2) {
            if (mail == null) {
                continue;
            }
            if (isset(original.getGivenName()) && isset(original.getSurName()) && mail.contains(original.getGivenName().toLowerCase()) && mail.contains(original.getSurName().toLowerCase())) {
                score += 10;
            }
            if (purged.size() >= 2) {
                int count = 0;
                for(final String comp : purged) {
                    if (mail.contains(comp.toLowerCase())) {
                        count++;
                    }
                }
                if (count == purged.size()) {
                    score += 10;
                }
            }
        }
        
        final List<String> purged2 = getPurgedDisplayNameComponents(candidate.getDisplayName());
        for(final String mail : mails) {
            if (mail == null) {
                continue;
            }
            if (isset(candidate.getGivenName()) && isset(candidate.getSurName()) && mail.contains(candidate.getGivenName().toLowerCase()) && mail.contains(candidate.getSurName().toLowerCase())) {
                score += 10;
            }
            if (purged2.size() >= 2) {
                int count = 0;
                for(final String comp : purged2) {
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
            LOG.debug("bday");
            score += 5;
        }
        
        if( score < threshold && original.matches(candidate, MATCH_COLUMNS)) { //the score check is only to speed the process up
            score = threshold + 1;
        }
        LOG.debug("Score: "+score+" "+original+" <-> "+candidate);
        return score;
    }
    
    public static List<String> getPurgedDisplayNameComponents(final String displayName) {
        if (displayName == null) {
            return Collections.emptyList();
        }
        final List<String> d1 = java.util.Arrays.asList(displayName.split("\\s+"));
        final List<String> p1 = new ArrayList<String>();
        for (final String string : d1) {
            final String purged = purge(string);
            if (purged != null) {
                p1.add(purged);
            }
        }
        return p1;
    }
    
    public static boolean compareDisplayNames(final String displayName, final String displayName2) {
        if (eq(displayName, displayName2)) {
            return true;
        }
        if (displayName == null || displayName2 == null) {
            return false;
        }
        final List<String> p1 = getPurgedDisplayNameComponents(displayName);
        final List<String> p2 = getPurgedDisplayNameComponents(displayName2);
        
        // any two must match
        int count = 0;
        for(final String string : p2) {
            if (p1.contains(string)) {
                count++;
            }
        }
        
        return count == Math.min(p1.size(), p2.size()) && count >= 2; 
    }
    
    public static String purge(final String component) {
        // throw away non characters
        
        final StringBuilder b = new StringBuilder(component.length());
        for(final char c : component.toCharArray()) {
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

    public static boolean isset(final String s) {
        return s != null && s.length() > 0;
    }
    
    public static boolean eq(final Object o1, final Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
    public boolean add(final ContactSourceFactory arg0) {
        return factories.add(arg0);
    }

    public boolean add(final ContactSource arg0) {
        return sources.add(arg0);
    }

    
    


}
