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


package com.openexchange.mail.json.parser;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;

/**
 * {@link IAttachmentHandler} - Tracks mail parts when parsing a mail.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IAttachmentHandler {

    /**
     * Sets the text part which may be modified.
     *
     * @param textBodyPart The text part to set
     */
    public void setTextPart(TextBodyMailPart textBodyPart);

    /**
     * Adds specified attachment.
     *
     * @param attachment The attachment to add
     * @throws OXException Depending on implementation it may indicate an exceeded quota, but other errors as well
     */
    public void addAttachment(MailPart attachment) throws OXException;

    /**
     * Generates composed mails.
     *
     * @param source The source composed mail
     * @return The resulting composed mails
     * @throws OXException If an error occurs while filling mail
     */
    public ComposedMailMessage[] generateComposedMails(ComposedMailMessage source, List<OXException> warnings) throws OXException;
}
