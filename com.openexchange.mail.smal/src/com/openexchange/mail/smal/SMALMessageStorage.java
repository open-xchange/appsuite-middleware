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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mail.smal;

import com.openexchange.exception.OXException;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.search.SearchTerm;
import com.openexchange.session.Session;

/**
 * {@link SMALMessageStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMALMessageStorage extends AbstractSMALStorage implements IMailMessageStorage {

    /**
     * Initializes a new {@link SMALMessageStorage}.
     */
    public SMALMessageStorage(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> realMailAccess) {
        super(session, accountId, realMailAccess);
    }

    @Override
    public String[] appendMessages(final String destFolder, final MailMessage[] msgs) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().appendMessages(destFolder, msgs);
        } finally {
            close();
        }
    }

    @Override
    public String[] copyMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().copyMessages(sourceFolder, destFolder, mailIds, fast);
        } finally {
            close();
        }
    }

    @Override
    public void deleteMessages(final String folder, final String[] mailIds, final boolean hardDelete) throws OXException {
        connect();
        try {
            delegateMailAccess.getMessageStorage().deleteMessages(folder, mailIds, hardDelete);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] getMessages(final String folder, final String[] mailIds, final MailField[] fields) throws OXException {
        connect();
        try {
            System.out.println("SMALMessageStorage.getMessages()");
            return delegateMailAccess.getMessageStorage().getMessages(folder, mailIds, fields);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] searchMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        connect();
        try {
            System.out.println("SMALMessageStorage.searchMessages()");
            return delegateMailAccess.getMessageStorage().searchMessages(folder, indexRange, sortField, order, searchTerm, fields);
        } finally {
            close();
        }
    }

    @Override
    public void updateMessageFlags(final String folder, final String[] mailIds, final int flags, final boolean set) throws OXException {
        connect();
        try {
            delegateMailAccess.getMessageStorage().updateMessageFlags(folder, mailIds, flags, set);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] getAllMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final MailField[] fields) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getAllMessages(folder, indexRange, sortField, order, fields);
        } finally {
            close();
        }
    }

    @Override
    public MailPart getAttachment(final String folder, final String mailId, final String sequenceId) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getAttachment(folder, mailId, sequenceId);
        } finally {
            close();
        }
    }

    @Override
    public MailPart getImageAttachment(final String folder, final String mailId, final String contentId) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getImageAttachment(folder, mailId, contentId);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage getMessage(final String folder, final String mailId, final boolean markSeen) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getMessage(folder, mailId, markSeen);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] getThreadSortedMessages(final String folder, final IndexRange indexRange, final MailSortField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MailField[] fields) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getThreadSortedMessages(folder, indexRange, sortField, order, searchTerm, fields);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] getUnreadMessages(final String folder, final MailSortField sortField, final OrderDirection order, final MailField[] fields, final int limit) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getUnreadMessages(folder, sortField, order, fields, limit);
        } finally {
            close();
        }
    }

    @Override
    public String[] moveMessages(final String sourceFolder, final String destFolder, final String[] mailIds, final boolean fast) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().moveMessages(sourceFolder, destFolder, mailIds, fast);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage saveDraft(final String draftFullname, final ComposedMailMessage draftMail) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().saveDraft(draftFullname, draftMail);
        } finally {
            close();
        }
    }

    @Override
    public void updateMessageColorLabel(final String folder, final String[] mailIds, final int colorLabel) throws OXException {
        connect();
        try {
            delegateMailAccess.getMessageStorage().updateMessageColorLabel(folder, mailIds, colorLabel);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] getNewAndModifiedMessages(final String folder, final MailField[] fields) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getNewAndModifiedMessages(folder, fields);
        } finally {
            close();
        }
    }

    @Override
    public MailMessage[] getDeletedMessages(final String folder, final MailField[] fields) throws OXException {
        connect();
        try {
            return delegateMailAccess.getMessageStorage().getDeletedMessages(folder, fields);
        } finally {
            close();
        }
    }

}
