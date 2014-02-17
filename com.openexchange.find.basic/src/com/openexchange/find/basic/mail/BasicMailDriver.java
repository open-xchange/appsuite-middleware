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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.basic.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.Services;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailDocument;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.service.MailService;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.tools.session.ServerSession;


/**
 * Real mail implementation FTW!
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class BasicMailDriver extends MockMailDriver {

    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        List<Filter> filters = searchRequest.getFilters();
        if (filters == null || filters.isEmpty()) {
            // TODO: throw exception, we need at least a folder filter!
        }

        String folderName = null;
        for (Filter filter : filters) {
            Set<String> fields = filter.getFields();
            if (fields.size() == 1 && "folder".equals(fields.iterator().next())) {
                folderName = filter.getQueries().iterator().next();
                break;
            }
        }

        if (folderName == null) {
            // TODO: throw exception, we need at least a folder filter!
        }

        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folderName);
        MailService mailService = Services.getMailService();
        MailMessage[] messages = null;
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = mailService.getMailAccess(session, fullnameArgument.getAccountId());
            mailAccess.connect();
            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            IndexRange indexRange = new IndexRange(searchRequest.getStart(), searchRequest.getStart() + searchRequest.getSize());
            MailSortField sortField = MailSortField.RECEIVED_DATE;
            OrderDirection order = OrderDirection.DESC;
            messages = messageStorage.getAllMessages(fullnameArgument.getFullname(), indexRange, sortField, order, MailField.FIELDS_LOW_COST);
//            TODO: implement real search
//            SearchTerm<?> searchTerm = new SubjectTerm("*");
//            messages = messageStorage.searchMessages(fullnameArgument.getFullname(), indexRange, sortField, order, searchTerm, MailField.FIELDS_LOW_COST);
        } finally {
            if (mailAccess != null) {
                mailAccess.close(true); // caching necessary?
            }
        }

        List<Document> documents = new ArrayList<Document>(messages.length);
        for (MailMessage message : messages) {
            documents.add(new MailDocument(message));
        }

        // TODO: Does ui need the numFound value? Could become expensive to implement here
        return new SearchResult(-1, searchRequest.getStart(), documents);
    }

}
