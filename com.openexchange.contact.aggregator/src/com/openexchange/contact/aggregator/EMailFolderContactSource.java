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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.internet.IDNA;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.groupware.container.Contact;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EMailFolderContactSource}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class EMailFolderContactSource implements ContactSource {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(EMailFolderContactSource.class);
    
    private final MailFolderDiscoverer discoverer;
    private int limit = 3000;
    

    public EMailFolderContactSource(final MailFolderDiscoverer discoverer, final int limit) {
        this.discoverer = discoverer;
        this.limit = limit;
    }

    public List<Contact> getContacts(final ServerSession session) throws Exception {

        final List<String> mailFolders = getAllVisibleEmailFolders(session);
        if (mailFolders != null) {
            return getContactsFromMailFolders(session, mailFolders);
        }
        return Collections.emptyList();
    }

    /**
     * @param session
     * @param mailFolders
     * @return
     * @throws Exception
     */
    private List<Contact> getContactsFromMailFolders(final ServerSession session, final List<String> mailFolders) throws Exception {
        final List<Contact> contacts = new ArrayList<Contact>();
        for (final String mailFolder : mailFolders) {
            try {
                final List<Contact> tempContacts = getAllContactsFromOneFolder(mailFolder, session);
                contacts.addAll(tempContacts);
            } catch (final Exception x) {
                LOG.error(x.getMessage(), x);
            }
        }
        return contacts;
    }

    /**
     * @return
     * @throws Exception
     */
    private List<Contact> getAllContactsFromOneFolder(final String folder, final ServerSession session) throws Exception {
        final List<Contact> contacts = new ArrayList<Contact>();
        MailServletInterface mailInterface = null;
        try {
           mailInterface = MailServletInterface.getInstance(session);
            final int[] fields = { MailListField.FROM.getField(), MailListField.TO.getField(), MailListField.CC.getField() };
            final SearchIterator<MailMessage> messages = mailInterface.getAllMessages(folder, MailListField.RECEIVED_DATE.getField(), OrderDirection.DESC.getOrder(), fields, new int[]{0,limit});
            final Set<String> guardian = new HashSet<String>();
            while (messages.hasNext()) {
                final MailMessage message = messages.next();
                final InternetAddress[] froms = message.getFrom();
                for (final InternetAddress from : froms) {
                    if (from.getPersonal() == null) {
                        continue;
                    }
                    final Contact contact = new Contact();
                    contact.setDisplayName(from.getPersonal());
                    contact.setEmail1(IDNA.toIDN(from.getAddress()));
                    if (guardian.add(contact.getDisplayName() + contact.getEmail1())) {
                        contacts.add(contact);
                    }
                }
                final InternetAddress[] tos = message.getTo();
                for (final InternetAddress to : tos) {
                    if (to.getPersonal() == null) {
                        continue;
                    }
                    final Contact contact = new Contact();
                    contact.setDisplayName(to.getPersonal());
                    contact.setEmail1(IDNA.toIDN(to.getAddress()));
                    if (guardian.add(contact.getDisplayName() + contact.getEmail1())) {
                        contacts.add(contact);
                    }
                }
                final InternetAddress[] ccs = message.getCc();
                for (final InternetAddress cc : ccs) {
                    if (cc.getPersonal() == null) {
                        continue;
                    }
                    final Contact contact = new Contact();
                    contact.setDisplayName(cc.getPersonal());
                    contact.setEmail1(IDNA.toIDN(cc.getAddress()));
                    if (guardian.add(contact.getDisplayName() + contact.getEmail1())) {
                        contacts.add(contact);
                    }
                }

            }
        } finally {
            if (mailInterface != null) {
                mailInterface.close(true);
            }
        }
        return contacts;
    }

    private List<String> getAllVisibleEmailFolders(final ServerSession session) {
        try {
            return discoverer.getMailFolder(session);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Type getType() {
        return Type.CONTRIBUTOR;
    }
    
    public Speed getSpeed() {
        return Speed.SLOW;
    }


}
