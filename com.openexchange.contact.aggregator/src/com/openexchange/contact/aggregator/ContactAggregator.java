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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.aggregator;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.aggregator.ContactSource.Type;
import com.openexchange.groupware.contact.helpers.ContactSimilarity;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactAggregator}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactAggregator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ContactAggregator.class);

    private final List<ContactSourceFactory> factories = new ArrayList<ContactSourceFactory>();

    private final List<ContactSource> sources = new ArrayList<ContactSource>();
    
    private ConfigViewFactory configViews = null;

    public List<Contact> aggregate(ServerSession session, boolean fastOnly, List<Contact> originalContacts) throws Exception {
        List<Contact> currentContacts = new ArrayList<Contact>();
        List<ContactSource> allSources = new ArrayList<ContactSource>();
        allSources.addAll(sources);
        for (ContactSourceFactory factory : factories) {
            if (!fastOnly || factory.getSpeed() == ContactSource.Speed.FAST) {
                allSources.addAll(factory.getSources(session));
            }
        }

        List<ContactSource> confirmed = new ArrayList<ContactSource>();
        List<ContactSource> contribs = new ArrayList<ContactSource>();
        partition(allSources, confirmed, contribs);
        
        ConfigView view = configViews.getView(session.getUserId(), session.getContextId());
        
        int degradeStatusThreshhold = view.opt("com.openexchange.contact.aggregator.degradeFolderStatusThreshhold", int.class, Integer.MAX_VALUE);
        int maxContacts = view.opt("com.openexchange.contact.aggregator.preferredNumberOfContacts", int.class, Integer.MAX_VALUE);

        for (ContactSource source : confirmed) {
            if (fastOnly && source.getSpeed() != ContactSource.Speed.FAST) {
                continue;
            }
            List<Contact> fromSource = source.getContacts(session);
            LOG.debug("Got " + fromSource.size() + " contacts from " + source);
            if (fromSource.size() >= degradeStatusThreshhold || (currentContacts.size() >= maxContacts && !source.getType().equals(Type.IMPORTANT))) {
                contribs.add(new StaticContactSource(fromSource, ContactSource.Speed.FAST, ContactSource.Type.CONTRIBUTOR));
                continue;
            }
            for (Contact newContact : fromSource) {
                boolean updated = false;
                for (Contact knownContact : new ArrayList<Contact>(currentContacts)) {
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

        for (ContactSource source : contribs) {
            if (fastOnly && source.getSpeed() != ContactSource.Speed.FAST) {
                continue;
            }
            List<Contact> fromSource = source.getContacts(session);
            LOG.debug("Got " + fromSource.size() + " contacts from " + source);
            for (Contact newContact : fromSource) {
                for (Contact knownContact : new ArrayList<Contact>(currentContacts)) {
                    if (calculateSimilarityScore(newContact, knownContact, session) > ContactSimilarity.THRESHHOLD) {
                        update(knownContact, newContact);
                    }
                }
            }
        }
        
        List<Contact> purgedForIrrelevantUpdates = new ArrayList<Contact>(currentContacts.size());
        for(Contact c : currentContacts) {
            Contact matching = null;
            int similarity = 0;
            for (Contact knownContact : originalContacts) {
                int score = calculateSimilarityScore(c, knownContact, session);
                if (score > 9 && similarity < score) {
                    similarity = score;
                    matching = knownContact;
                } 
            }
            if (matching == null || matching.getLastModified().getTime() - c.getLastModified().getTime() < 0) {
                purgedForIrrelevantUpdates.add(c);
            }
            
        }

        return purgedForIrrelevantUpdates;
    }

    private void partition(List<ContactSource> allSources, List<ContactSource> confirmed, List<ContactSource> contribs) {
        for (ContactSource contactSource : allSources) {
            if (Type.CONFIRMED == contactSource.getType() || Type.IMPORTANT == contactSource.getType()) {
                confirmed.add(contactSource);
            } else {
                contribs.add(contactSource);
            }

        }
    }

    private void update(Contact aggregated, Contact addition) {
        boolean overwrite = aggregated.getLastModified().getTime() - addition.getLastModified().getTime() < 0;
        LOG.debug("Merging " + aggregated + " with " + addition);
        int[] columns = Contact.CONTENT_COLUMNS;
        for (int field : columns) {
            if (aggregated.get(field) == null || overwrite) {
                if (addition.get(field) != null ) {
                    aggregated.set(field, addition.get(field));
                }
            }
        }
        if (overwrite) {
            aggregated.setLastModified(addition.getLastModified());
        }
        int[] mailColumns = new int[] { Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3 };

        for (int mailColumn : mailColumns) {
            String mail = (String) addition.get(mailColumn);
            if (mail != null) {
                int columnCandidate = -1;
                for (int mailColumn2 : mailColumns) {
                    String mail2 = (String) aggregated.get(mailColumn2);
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

    public static int calculateSimilarityScore(Contact original, Contact candidate, Object session) {
        return ContactSimilarity.calculateSimilarityScore(original, candidate);
    }

    public boolean add(ContactSourceFactory arg0) {
        return factories.add(arg0);
    }

    public boolean add(ContactSource arg0) {
        return sources.add(arg0);
    }
    
    public void setConfigViews(ConfigViewFactory configViews) {
        this.configViews = configViews;
    }

}
