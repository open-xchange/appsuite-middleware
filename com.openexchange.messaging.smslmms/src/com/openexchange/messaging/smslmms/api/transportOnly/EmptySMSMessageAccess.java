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

package com.openexchange.messaging.smslmms.api.transportOnly;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingPart;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SearchTerm;
import com.openexchange.messaging.smslmms.api.SMSMessage;
import com.openexchange.messaging.smslmms.api.SMSMessageAccess;
import com.openexchange.messaging.smslmms.api.SMSService;

/**
 * {@link EmptySMSMessageAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EmptySMSMessageAccess implements SMSMessageAccess {

    private final int accountId;

    private final int userId;

    private final int contextId;

    /**
     * Initializes a new {@link EmptySMSMessageAccess}.
     */
    public EmptySMSMessageAccess(final int accountId, final int userId, final int contextId) {
        super();
        this.accountId = accountId;
        this.userId = userId;
        this.contextId = contextId;
    }

    @Override
    public SMSMessage getSMSMessage(final String folder, final String id, final boolean peek) throws OXException {
        throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, folder);
    }

    @Override
    public List<SMSMessage> getSMSMessages(final String folder, final String[] messageIds, final MessagingField[] fields) throws OXException {
        for (final String id : messageIds) {
            if (null != id) {
                throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, folder);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<SMSMessage> searchSMSMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final SearchTerm<?> searchTerm, final MessagingField[] fields) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public MessagingPart getAttachment(final String folder, final String messageId, final String sectionId) throws OXException {
        throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(messageId, folder);
    }

    @Override
    public void updateSMSMessage(final SMSMessage message, final MessagingField[] fields) throws OXException {
        throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(message.getId(), message.getFolder());
    }

    @Override
    public void appendSMSMessages(final String folder, final SMSMessage[] messages) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public List<String> copyMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        for (final String id : messageIds) {
            if (null != id) {
                throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, sourceFolder);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> moveMessages(final String sourceFolder, final String destFolder, final String[] messageIds, final boolean fast) throws OXException {
        for (final String id : messageIds) {
            if (null != id) {
                throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, sourceFolder);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteMessages(final String folder, final String[] messageIds, final boolean hardDelete) throws OXException {
        // Nope
    }

    @Override
    public List<SMSMessage> getAllSMSMessages(final String folder, final IndexRange indexRange, final MessagingField sortField, final OrderDirection order, final MessagingField... fields) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public SMSMessage smsPerform(final String folder, final String id, final String action) throws OXException {
        throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, folder);
    }

    @Override
    public SMSMessage smsPerform(final String action) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(SMSService.DISPLAY_NAME);
    }

    @Override
    public SMSMessage smsPerform(final SMSMessage message, final String action) throws OXException {
        throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(message.getId(), message.getFolder());
    }

    @Override
    public MessagingContent resolveContent(final String folder, final String id, final String referenceId) throws OXException {
        throw MessagingExceptionCodes.MESSAGE_NOT_FOUND.create(id, folder);
    }

}
