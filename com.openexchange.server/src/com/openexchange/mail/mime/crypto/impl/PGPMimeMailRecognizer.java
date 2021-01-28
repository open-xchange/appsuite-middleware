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
