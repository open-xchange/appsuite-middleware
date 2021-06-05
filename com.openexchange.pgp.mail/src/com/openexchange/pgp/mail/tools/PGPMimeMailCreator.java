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

package com.openexchange.pgp.mail.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.openexchange.tools.io.IOUtils;

/**
 * {@link PGPMimeMailCreator} creates a PGP/MIME email
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPMimeMailCreator {

    /**
     * Internal method to get the content of the given InputStream stream
     *
     * @param inputStream The inputStream
     * @return The content of the InputStream s UTF-8 encoded String
     * @throws IOException
     */
    private String getContent(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.transfer(inputStream, outputStream);
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Internal method to add a set of headers to the given MimeMessage
     *
     * @param message The message to add the set of headers to
     * @param headers A set of headers (name, value)
     * @return The MimeMessage with the headers added
     * @throws MessagingException
     */
    private MimeMessage addHeaders(MimeMessage message, HashMap<String, String> headers) throws MessagingException {
        if (headers == null) {
            return message;
        }
        Iterator<Entry<String, String>> it = headers.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> h = it.next();
            if (!h.getKey().equals("Content-Type")) {
                message.addHeader(h.getKey(), h.getValue());
            }
        }
        return message;
    }

    /**
     * Creates a full PGP/MIME message from the given, already encrypted, data.
     *
     * @param encryMimeMessage InputStream of the already encrypted mime data.
     * @param additionalClearTextParts A list of additional BodyParts to add to the MimeMessage
     * @return A PGP/MIME message with the given encrypted data and additional BodyParts
     * @throws MessagingException
     * @throws IOException
     */
    public MimeMessage createPGPMimeMessage(InputStream encryMimeMessage, List<BodyPart> additionalClearTextParts) throws MessagingException, IOException {
        return createPGPMimeMessage(encryMimeMessage, null, additionalClearTextParts);
    }

    /**
     * Creates a full PGP/MIME message from the given, already encrypted, data.
     *
     * @param encryMimeMessage InputStream of the already encrypted mime data.
     * @param headers A set of headers to add to the PGP/MIME message
     * @param additionalClearTextParts A list of additional BodyParts to add to the MimeMessage
     * @return A PGP/MIME message with the given encrypted data and additional BodyParts
     * @throws MessagingException
     * @throws IOException
     */
    public MimeMessage createPGPMimeMessage(InputStream encryMimeMessage, HashMap<String, String> headers, List<BodyPart> additionalClearTextParts) throws MessagingException, IOException {

        //Create the PGP/MIME part
        final MimeMultipart pgpMimePart = createPGPMimePart(encryMimeMessage, additionalClearTextParts);

        //Create the PGP/MIME message based on the part
        MimeMessage newMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        if (headers != null) {
            addHeaders(newMessage, headers);
        }
        newMessage.setContent(pgpMimePart);
        newMessage.saveChanges();
        return newMessage;
    }

    /**
     * Creates a MimeMultipart from the given, already encrypted, data.
     *
     * @param encryptedMimeDataStream InputStream of the already encrypted mime data.
     * @param additionalClearTextParts A list of additional BodyParts to add to the MimeMultipart
     * @return A PGP/MIME MimeMultipart with the given encrypted data and additional BodyParts
     * @throws MessagingException
     * @throws IOException
     */
    public MimeMultipart createPGPMimePart(InputStream encryptedMimeDataStream, List<BodyPart> additionalClearTextParts) throws MessagingException, IOException {

        //New multipart
        final MimeMultipart pgp = new MimeMultipart("encrypted; protocol=\"application/pgp-encrypted\"");

        //PGP/MIME identification & Version part
        final BodyPart ver = new MimeBodyPart();
        ver.setDescription("PGP/MIME version identification");
        ver.setContent("Version: 1\r\n", "application/pgp-encrypted");
        ver.setHeader("Content-Transfer-Encoding", "7bit");
        pgp.addBodyPart(ver);

        //PGP encrypted content part
        final BodyPart pt = new MimeBodyPart();
        pt.setFileName("encrypted.asc");
        pt.setDescription("OpenPGP encrypted message");
        pt.setDisposition("inline");
        pt.setContent(getContent(encryptedMimeDataStream), "application/octet-stream");
        pt.setHeader("Content-Transfer-Encoding", "7bit");
        pgp.addBodyPart(pt);

        //Adding additional clear text parts to the mail if present
        if (additionalClearTextParts != null) {
            for (BodyPart additionalClearTextPart : additionalClearTextParts) {
                pgp.addBodyPart(additionalClearTextPart);
            }
        }

        return pgp;
    }
}
