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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.HaloContactDataSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailExceptionCode;
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

public abstract class AbstractMailHaloDataSource implements HaloContactDataSource {

	private ServiceLookup lookup;

	
	public AbstractMailHaloDataSource(ServiceLookup lookup){
		this.lookup = lookup;
	}
	
	
	protected abstract String getFolder(IMailFolderStorage folderStorage) throws OXException;
	
	
	@Override
	public abstract String getId();

	
	@Override
	public AJAXRequestResult investigate(final HaloContactQuery query, final AJAXRequestData req, final ServerSession session) throws OXException {
		final MailService mailService = lookup.getService(MailService.class);
		

		int[] accountIds = getAccountIds();
		List<MailMessage> messages = new LinkedList<MailMessage>();
		
		for(int accountId : accountIds){
			MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null; 
			try {
				mailAccess = mailService.getMailAccess(session, accountId);
				IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
	
	
				MailMessage[] additionalMessages = messageStorage.searchMessages(
					getFolder(mailAccess.getFolderStorage()), 
					getIndexRange(req), 
					getSortField(req), 
					getOrder(req), 
					generateSearchTerm(query), 
					getFields(req));
				mergeMessages(messages,additionalMessages, getSortField(req), session.getUser().getLocale());
			} finally {
				if (mailAccess != null ) mailAccess.close( true );
			}
		}
		return new AJAXRequestResult(messages, "mail");
	}

	
	protected MailSortField getSortField(AJAXRequestData req) throws OXException {
        int sort = req.getIntParameter(Mail.PARAMETER_SORT);
        MailSortField field = MailSortField.getField(sort);
        if(field != null)
        	return field;
		return MailSortField.RECEIVED_DATE;
	}

	
	protected OrderDirection getOrder(final AJAXRequestData req) throws OXException {
        String order = req.getParameter(Mail.PARAMETER_ORDER);

        if (order == null) return OrderDirection.ASC;
        
		if (order.equalsIgnoreCase("asc")) {
			return OrderDirection.ASC;
		} else if (order.equalsIgnoreCase("desc")) {
			return OrderDirection.DESC;
		}
		throw MailExceptionCode.INVALID_INT_VALUE.create(Mail.PARAMETER_ORDER);
	}

	
	protected IndexRange getIndexRange(AJAXRequestData req) throws OXException {
        int leftHandLimit = req.getIntParameter(Mail.LEFT_HAND_LIMIT);
        int rightHandLimit = req.getIntParameter(Mail.RIGHT_HAND_LIMIT);
        if(leftHandLimit == -1 || rightHandLimit == -1) 
        	return new IndexRange(0, 9);
		return new IndexRange(leftHandLimit, rightHandLimit); 
	}

	
	protected void mergeMessages(List<MailMessage> messages,	MailMessage[] additionalMessages, MailSortField sortField, Locale locale) {
		//TODO: [performance] since the lists are already pre-sorted, a single-pass merge might be done here
		messages.addAll(Arrays.asList(additionalMessages));
		Collections.sort(messages, new MailMessageComparator(sortField, true, locale));
	}

	
	protected MailField[] getFields(AJAXRequestData req) {
		final String[] cols = req.getParameterValues(Mail.PARAMETER_COLUMNS);
		if(cols == null) return MailField.FIELDS_LOW_COST;
		
		int[] cols2 = new int [cols.length];
		for(int i = 0; i < cols.length; i++){
			cols2[i] = Integer.parseInt(cols[i]);
		}
		return MailField.getFields(cols2);
	}

	
	//TODO: Add external accounts, too
	protected int[] getAccountIds() {
		return new int[]{MailAccount.DEFAULT_ID};
	}

	
	protected SearchTerm<?> generateSearchTerm(HaloContactQuery query) {
		Contact contact = query.getContact();

		List<String> addrs = new LinkedList<String>();
		if(contact.containsEmail1()) addrs.add(contact.getEmail1());
		if(contact.containsEmail2()) addrs.add(contact.getEmail2());
		if(contact.containsEmail3()) addrs.add(contact.getEmail3());
		
		final boolean orSearch = true;
		final int[] searchFields = new int[]{MailField.FROM.getListField().getField(), MailField.TO.getListField().getField(), MailField.CC.getListField().getField(), MailField.BCC.getListField().getField()};
		return SearchUtility.parseFields(searchFields , addrs.toArray(new String[]{}), orSearch );
	}

	
	public boolean isAvailable(ServerSession session) throws OXException {
		return true;
	}

}
