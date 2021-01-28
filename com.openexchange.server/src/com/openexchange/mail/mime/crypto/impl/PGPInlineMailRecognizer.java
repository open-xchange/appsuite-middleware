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
 *    trademarks of the OX Software GmbH group of companies.
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
