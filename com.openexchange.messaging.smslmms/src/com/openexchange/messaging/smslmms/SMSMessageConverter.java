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

package com.openexchange.messaging.smslmms;

import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.messaging.ManagedFileContent;
import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingHeader;
import com.openexchange.messaging.MessagingHeader.KnownHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.smslmms.api.SMSMessage;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link SMSMessageConverter} - Converter for SMS/MMS messages.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMSMessageConverter {

    private static final SMSMessageConverter INSTANCE = new SMSMessageConverter();

    /**
     * Gets the instance.
     * 
     * @return The instance
     */
    public static SMSMessageConverter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link SMSMessageConverter}.
     */
    private SMSMessageConverter() {
        super();
    }

    /**
     * Converts specified message to a SMS/MMS message.
     * 
     * @param message The message to convert
     * @return The resulting SMS/MMS message
     * @throws OXException If conversion fails
     */
    public SMSMessage convert(final MessagingMessage message) throws OXException {
        ManagedFileManagement fileManagement = null;
        if (message instanceof SMSMessagingMessage) {
            return (SMSMessagingMessage) message;
        }
        /*
         * Needs conversion
         */
        final MessagingContent content = message.getContent();
        if (!(content instanceof MultipartContent)) {
            final StringContent stringContent = (StringContent) content;
            final MessagingHeader from = message.getHeader(KnownHeader.FROM.toString()).iterator().next();
            final MessagingHeader to = message.getHeader(KnownHeader.TO.toString()).iterator().next();
            return new SMSMessagingMessage(from, to, stringContent.getData());
        }
        /*
         * Convert multipart message
         */
        final SMSMessagingMessage smsMessage;
        final MultipartContent multipartContent = (MultipartContent) content;
        {
            final MessagingBodyPart bodyPart = multipartContent.get(0);
            final StringContent stringContent = (StringContent) bodyPart.getContent();
            final MessagingHeader from = message.getHeader(KnownHeader.FROM.toString()).iterator().next();
            final MessagingHeader to = message.getHeader(KnownHeader.TO.toString()).iterator().next();
            smsMessage = new SMSMessagingMessage(from, to, stringContent.getData());
        }
        /*
         * Iterate attached files
         */
        final int count = multipartContent.getCount();
        if (count > 1) {
            fileManagement = SMSMessagingMessage.getServiceLookup().getService(ManagedFileManagement.class);
            if (null == fileManagement) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ManagedFileManagement.class.getName());
            }
            for (int i = 1; i < count; i++) {
                final MessagingBodyPart bodyPart = multipartContent.get(i);
                final MessagingContent bodyPartContent = bodyPart.getContent();
                if (bodyPartContent instanceof ManagedFileContent) {
                    final ManagedFileContent managedFileContent = (ManagedFileContent) bodyPartContent;
                    final ManagedFile mf = fileManagement.getByID(managedFileContent.getId());
                    smsMessage.addAttachment(mf);
                }
            }
        }
        return smsMessage;
    }

}
