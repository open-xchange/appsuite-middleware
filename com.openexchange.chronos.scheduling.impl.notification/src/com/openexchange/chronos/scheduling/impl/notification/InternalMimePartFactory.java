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

package com.openexchange.chronos.scheduling.impl.notification;

import static com.openexchange.chronos.scheduling.common.Constants.ALTERNATIVE;
import static com.openexchange.chronos.scheduling.common.Constants.MULTIPART_ALTERNATIVE;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.common.AbstractMimePartFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.server.ServiceLookup;

/**
 * {@link InternalMimePartFactory} - Internal notification
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class InternalMimePartFactory extends AbstractMimePartFactory {

    /**
     * Initializes a new {@link InternalMimePartFactory}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param scheduleChange The change to add to the mail
     * @param recipientSettings The recipient settings
     * @throws OXException In case services are missing
     */
    public InternalMimePartFactory(ServiceLookup serviceLookup, ScheduleChange scheduleChange, RecipientSettings recipientSettings) throws OXException {
        super(serviceLookup, scheduleChange, recipientSettings);
    }

    @Override
    public MimeMultipart create() throws OXException, MessagingException {
        ContentType ct = new ContentType(MULTIPART_ALTERNATIVE);
        ct.setCharsetParameter(MailProperties.getInstance().getDefaultMimeCharset());

        /*
         * Set text and HTML
         */
        MimeMultipart part = new MimeMultipart(ALTERNATIVE);
        part.addBodyPart(generateTextPart());
        part.addBodyPart(generateHtmlPart());
        return part;
    }
}
