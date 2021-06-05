/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
