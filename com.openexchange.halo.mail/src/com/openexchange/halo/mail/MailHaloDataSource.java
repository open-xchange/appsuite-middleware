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
package com.openexchange.halo.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.mail.search.SearchUtility;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailMessageComparator;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

public class MailHaloDataSource implements HaloContactDataSource {

	private ServiceLookup lookup;

	public MailHaloDataSource(ServiceLookup lookup){
		this.lookup = lookup;
	}
	
	@Override
	public AJAXRequestResult investigate(HaloContactQuery query, AJAXRequestData req, ServerSession session) throws OXException {
		MailService mailService = lookup.getService(MailService.class);
		
		int[] accountIds = getAccountIds();
		MailField[] fields = getFields(req);
		List<MailMessage> messages = new LinkedList<MailMessage>();
		MailSortField sortField = MailSortField.RECEIVED_DATE;
		
		for(int accountId : accountIds){
			MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailService.getMailAccess(session, accountId);
			IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
			
			Contact contact = query.getContact();
			SearchTerm<?> searchTerm = generateSearchTerm(contact);

			MailMessage[] additionalMessages = messageStorage.searchMessages("INBOX", getIndexRange(), sortField, OrderDirection.ASC, searchTerm, fields);
			mergeMessages(messages,additionalMessages, sortField, session.getUser().getLocale());
		}
		return new AJAXRequestResult(messages, "mail");
	}

	//TODO: currently returns the last 10, is that okay?
	private IndexRange getIndexRange() {
		return new IndexRange(0, 9); 
	}

	private void mergeMessages(List<MailMessage> messages,	MailMessage[] additionalMessages, MailSortField sortField, Locale locale) {
		//TODO: since the lists are already pre-sorted, a single-pass merge might be done here
		messages.addAll(Arrays.asList(additionalMessages));
		Collections.sort(messages, new MailMessageComparator(sortField, true, locale));
	}

	//TODO: Extract from request
	private MailField[] getFields(AJAXRequestData req) {
		return MailField.FIELDS_LOW_COST;
	}

	//TODO: Add external accounts, too
	private int[] getAccountIds() {
		return new int[]{MailAccount.DEFAULT_ID};
	}

	private SearchTerm<?> generateSearchTerm(Contact contact) {
		List<String> addrs = new LinkedList<String>();
		if(contact.containsEmail1()) addrs.add(contact.getEmail1());
		if(contact.containsEmail2()) addrs.add(contact.getEmail2());
		if(contact.containsEmail3()) addrs.add(contact.getEmail3());
		
		boolean orSearch = true;
		int[] searchFields = new int[]{MailField.FROM.ordinal(), MailField.TO.ordinal(), MailField.CC.ordinal(), MailField.BCC.ordinal()};
		return SearchUtility.parseFields(searchFields , addrs.toArray(new String[]{}), orSearch );
	}

	@Override
	public String getId() {
		return "com.openexchange.halo.mail:inbox";
	}

	@Override
	public boolean isAvailable(ServerSession session) throws OXException {
		// TODO Auto-generated method stub
		return false;
	}

}
