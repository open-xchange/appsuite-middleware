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

package com.openexchange.gmail.send.filler;

import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import com.openexchange.exception.OXException;
import com.openexchange.gmail.send.dataobjects.GmailSendMailMessage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.mime.filler.CompositionParameters;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.mime.filler.SessionCompositionParameters;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Session;

/**
 * {@link GmailSendMessageFiller} - Fills an instance of {@link MimeMessage} with headers/contents given through an instance of
 * {@link GmailSendMailMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GmailSendMessageFiller extends MimeMessageFiller {

    /**
     * Constructor
     * 
     * @param session The session
     * @param ctx The context
     * @param usm The user's mail settings
     */
    public GmailSendMessageFiller(Session session, Context ctx, UserSettingMail usm) {
        super(new SessionCompositionParameters(session, ctx, usm));
    }

    /**
     * Constructor
     * 
     * @param compositionParameters
     */
    public GmailSendMessageFiller(CompositionParameters compositionParameters) {
        super(compositionParameters);
    }

    /**
     * Fills given instance of {@link MimeMessage}
     *
     * @param mail The source mail
     * @param mimeMessage The MIME message to fill
     * @param type The compose type
     * @param accountId The identifier of the associated account
     * @param session The session
     * @throws MessagingException If a messaging error occurs
     * @throws OXException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    public void fillMail(ComposedMailMessage mail, MimeMessage mimeMessage, ComposeType type) throws MessagingException, OXException, IOException {
        if (null != type) {
            mail.setSendType(type);
        }

        // Set headers
        setMessageHeaders(mail, mimeMessage);

        // Set common headers
        setCommonHeaders(mimeMessage);

        // Fill body
        fillMailBody(mail, mimeMessage, type);
    }

}
