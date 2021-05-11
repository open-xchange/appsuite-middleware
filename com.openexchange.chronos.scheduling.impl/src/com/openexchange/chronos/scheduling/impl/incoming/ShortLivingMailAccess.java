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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.chronos.scheduling.impl.incoming;

import static com.openexchange.chronos.scheduling.impl.incoming.MailUtils.getAttachmentPart;
import static com.openexchange.chronos.scheduling.impl.incoming.MailUtils.getMailAccess;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.crypto.CryptographicAwareMailAccessFactory;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ShortLivingMailAccess}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class ShortLivingMailAccess {

    private final ServiceLookup services;
    private final CalendarSession session;
    private final String folderId;
    private final String mailId;
    private final int accountId;

    /**
     * Initializes a new {@link ShortLivingMailAccess}.
     * 
     * @param services The service lookup to get additional services from
     * @param session The user session
     * @param folderId The folder identifier of the mail folder
     * @param mailId The identifier of the mail
     * @param accountId The account identifier
     */
    protected ShortLivingMailAccess(ServiceLookup services, CalendarSession session, String folderId, String mailId, int accountId) {
        super();
        this.services = services;
        this.session = session;
        this.folderId = folderId;
        this.mailId = mailId;
        this.accountId = accountId;
    }

    /**
     * Loads a mail part by creating a mail access, loading the part and closing the access
     *
     * @param contentId The content identifier of the part
     * @return The loaded part
     * @throws OXException In case loading fails
     */
    public MailPart getMailPart(String contentId) throws OXException {
        MailAccess<?, ?> mailAccess = null;
        try {
            mailAccess = getMailAccess(services.getOptionalService(CryptographicAwareMailAccessFactory.class), session.getSession(), accountId);
            MailMessage message = mailAccess.getMessageStorage().getMessage(folderId, mailId, false);
            MailPart attachmentPart = getAttachmentPart(message, contentId);
            if (null != attachmentPart) {
                attachmentPart.loadContent();
            }
            return attachmentPart;
        } finally {
            if (null != mailAccess) {
                mailAccess.close(true);
            }
        }
    }
}
