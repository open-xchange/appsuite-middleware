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

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
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

    private Activator activator;
    private MailFolderDiscoverer discoverer;

    public EMailFolderContactSource(Activator activator, MailFolderDiscoverer discoverer) {
        this.activator = activator;
        this.discoverer = discoverer;
    }

    public List<Contact> getContacts(ServerSession session) {

        List<String> mailFolders = getAllVisibleEmailFolders(session);
        return getContactsFromMailFolders(session, mailFolders);
    }

    /**
     * @param session
     * @param mailFolders
     * @return
     */
    private List<Contact> getContactsFromMailFolders(ServerSession session, List<String> mailFolders) {
        List<Contact> contacts = new ArrayList<Contact>();
        for (String mailFolder : mailFolders) {
            List<Contact> tempContacts = getAllContactsFromOneFolder(mailFolder, session);
            contacts.addAll(tempContacts);
        }
        return contacts;
    }

    /**
     * @return
     */
    private List<Contact> getAllContactsFromOneFolder(String folder, ServerSession session) {
        List<Contact> contacts = new ArrayList<Contact>();
        try {
            MailServletInterface mailInterface = MailServletInterface.getInstance(session);
            int[] fields = {
                MailListField.FROM.getField(), MailListField.TO.getField(), MailListField.CC.getField(), MailListField.BCC.getField() };
            SearchIterator<MailMessage> messages = mailInterface.getAllMessages(folder, 0, 0, fields, new int[] {});
            
            while (messages.hasNext()){
                MailMessage message = messages.next();
                
                InternetAddress[] froms = message.getFrom();
                for (InternetAddress from : froms){
                    Contact contact = new Contact();
                    contact.setDisplayName(from.getPersonal());
                    contact.setEmail1(from.getAddress());
                }
                InternetAddress[] tos = message.getTo();
                for (InternetAddress to : tos){
                    Contact contact = new Contact();
                    contact.setDisplayName(to.getPersonal());
                    contact.setEmail1(to.getAddress());
                }
                InternetAddress[] ccs = message.getCc();
                for (InternetAddress cc : ccs){
                    Contact contact = new Contact();
                    contact.setDisplayName(cc.getPersonal());
                    contact.setEmail1(cc.getAddress());
                }
                InternetAddress[] bccs = message.getBcc();
                for (InternetAddress bcc : bccs){
                    Contact contact = new Contact();
                    contact.setDisplayName(bcc.getPersonal());
                    contact.setEmail1(bcc.getAddress());
                }
            }
        } catch (MailException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AbstractOXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return contacts;
    }

 
    private List<String> getAllVisibleEmailFolders(ServerSession session) {
        try {
            return discoverer.getMailFolder(session);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public Type getType() {
        return Type.CONTRIBUTOR;
    }

}
