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

package com.openexchange.mail.mime.crypto.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.crypto.PGPMailRecognizer;

/**
 * {@link PGPMimeMailRecognizer} detects whether a {@link MailMessage} is a PGP/MIME message or not.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class PGPMimeMailRecognizer implements PGPMailRecognizer {

    private static Logger LOG = LoggerFactory.getLogger(PGPMimeMailRecognizer.class);

    private static String MULTIPART_ENCRYPTED = "multipart/encrypted";
    private static String APPLICATION_PGP_ENCRYPTED = "application/pgp-encrypted";

    /**
     * Initializes a new {@link PGPMimeMailRecognizer}.
     */
    public PGPMimeMailRecognizer() {
        super();
    }

    /**
     * Internal method to check if a mail part has the given content-type
     * @param part The part
     * @param contentType The content-type to check
     * @return true, if the mail part has the given content-type, false otherwise
     */
    private boolean hasContentType(MailPart part, String contentType) {
       if(part != null && part.getContentType() != null && contentType != null) {
           return part.getContentType().toLowerCaseString().contains(contentType);
       }
       return false;
    }

    @Override
    public boolean isPGPMessage(MailMessage message) {
        boolean isEncrypted = hasContentType(message, MULTIPART_ENCRYPTED);
        boolean isPGPEncrypted = false;
        if(isEncrypted) {
            try {
                for (int i = 0; i < message.getEnclosedCount(); i++) {
                    MailPart part = message.getEnclosedMailPart(i);
                    if(hasContentType(part, APPLICATION_PGP_ENCRYPTED)) {
                       isPGPEncrypted = true;
                       break;
                    }
                }
            } catch (OXException e) {
                LOG.error("Problem parsing email to check if MIME message", e);
            }
        }
        return isPGPEncrypted;
    }

    @Override
    public boolean isPGPSignedMessage(MailMessage message) throws OXException {
        for (int i = 0; i < message.getEnclosedCount(); i++) {
            if (message.getEnclosedMailPart(i).getContentType().toString().toUpperCase().contains("PGP-SIGNATURE")) {
                return true;
            }
        }
        return false;
    }

}
