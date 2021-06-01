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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.crypto.PGPMailRecognizer;

/**
 * {@link PGPInlineMailRecognizer} detects whether a {@link MailMessage} is a PGP/INLINE message or not.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class PGPInlineMailRecognizer implements PGPMailRecognizer {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PGPMailRecognizer.class);

    private static final String BEGIN_PGP_MESSAGE_MARKER = "-----BEGIN PGP MESSAGE-----";
    private static final String BEGIN_PGP_SIGNATURE_MARKER = "-----BEGIN PGP SIGNED MESSAGE-----";

    /**
     * Initializes a new {@link PGPInlineMailRecognizer}.
     */
    public PGPInlineMailRecognizer() {
        super();
    }

    private boolean isPGPInlineObject(InputStream stream) throws IOException {
        if (stream == null) {
            return false;
        }
        byte[] peekedData = new byte[1024];
        int read = stream.read(peekedData);
        if(read > 0) {
            String peekedContent = new String(peekedData, StandardCharsets.UTF_8);
            return peekedContent.contains(BEGIN_PGP_MESSAGE_MARKER);
        }
        return false;
    }

    private boolean isPGPSignedObject(InputStream stream) throws IOException {
        if (stream == null) {
            return false;
        }
        byte[] peekedData = new byte[1024];
        int read = stream.read(peekedData);
        if(read > 0) {
            String peekedContent = new String(peekedData, StandardCharsets.UTF_8);
            return peekedContent.contains(BEGIN_PGP_SIGNATURE_MARKER);
        }
        return false;
    }

    /**
     * Return inputStream for either entire Text message, or the first Multipart
     * @param message
     * @return
     * @throws OXException
     */
    private InputStream getMimeMessageStream (MailMessage message) throws OXException {
        MailMessage msg = message;
        if (msg.getContentType() != null && msg.getContentType().contains("multipart") && msg.getEnclosedCount() > 0) {
            MailPart part = msg.getEnclosedMailPart(0);
            if (part.getContentType() != null && part.getContentType().contains("alternative")) {
                if (part.getEnclosedCount() > 0) {
                    return part.getEnclosedMailPart(0).getInputStream();
                }
            }
            return msg.getEnclosedMailPart(0).getInputStream();  // we only need to pull the first part, as we are just taking a "peek"
        }
        return msg.getInputStream();
    }

    @Override
    public boolean isPGPMessage(MailMessage message) throws OXException {
        try (InputStream in = getMimeMessageStream(message)) {
            return isPGPInlineObject(in);
        } catch (IOException e) {
            logger.error("Problem checking if message is PGP Message ", e);
        }
        return false;
    }

    @Override
    public boolean isPGPSignedMessage(MailMessage message) throws OXException {
        try (InputStream in = getMimeMessageStream(message)) {
            return isPGPSignedObject(in);
        } catch (IOException e) {
            logger.error("Problem checking if message has PGP Signature ", e);
        }
        return false;
    }

}
