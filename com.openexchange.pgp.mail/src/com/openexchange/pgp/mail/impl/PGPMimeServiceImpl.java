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

package com.openexchange.pgp.mail.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import com.openexchange.exception.OXException;
import com.openexchange.pgp.core.PGPEncrypter;
import com.openexchange.pgp.core.PGPSignatureCreator;
import com.openexchange.pgp.mail.PGPMimeService;
import com.openexchange.pgp.mail.exceptions.PGPMailExceptionCodes;
import com.openexchange.pgp.mail.tools.PGPMimeMailCreator;

/**
 * {@link PGPMimeEncryptor} Encrypts a given MimeMessage using PGP/MIME
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.4
 */
public class PGPMimeServiceImpl implements PGPMimeService {

    /**
     * Internal method to create a proper PGP/MIME message
     *
     * @param encryptedMimeData Already encrypted PGP/MIME data
     * @param headers Headers to add to the PGP/MIME message
     * @param additionalClearTextParts Additional clear text BodyParts to add to the encrypted MimeMessage
     * @return The constructed PGP/MIME message
     * @throws IOException
     * @throws MessagingException
     */
    private MimeMessage createPGPMimeWrapper(InputStream encryptedMimeData, HashMap<String, String> headers, List<BodyPart> additionalClearTextParts) throws MessagingException, IOException {
        //Create a PGP/MIME message based on the given encrypted data
        return new PGPMimeMailCreator().createPGPMimeMessage(encryptedMimeData, headers, additionalClearTextParts);
    }

    /**
     * Internal method to extract all headers from the given MimeMessage.
     *
     * @param mimeMessage The MimeMessage to extract the headers from.
     * @return The headers for the given MimeMessage.
     * @throws MessagingException
     */
    private HashMap<String, String> getHeadersFrom(MimeMessage mimeMessage) throws MessagingException {
        HashMap<String, String> ret = new HashMap<String, String>();
        Enumeration headers = mimeMessage.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header h = (Header) headers.nextElement();
            ret.put(h.getName(), h.getValue());
        }
        return ret;
    }

    private String normalize(String content) {
        StringBuilder sb = new StringBuilder();
        String[] lines = content.split("\n");
        for (String line : lines) {
            sb.append(StringUtils.stripEnd(line, null) + "\r\n");
        }
        return sb.toString();
    }

    private String getMessageContentForSigning(MimeMessage message) throws IOException, MessagingException {
        MimeMessage msg = new MimeMessage(message);
        Enumeration<Header> headers = msg.getAllHeaders();
        while (headers.hasMoreElements()) {
            Header h = headers.nextElement();
            String name = h.getName();
            if (name != null && !name.contains("Content")) {
                msg.removeHeader(h.getName());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        msg.writeTo(out, new String[] { "Message-ID", "MIME-Version" });
        out.close();
        return out.toString();
    }

    private String getPartContent(MimeMultipart mp) throws IOException, MessagingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // we need to wrap in message to get full writeout of the mimepart contents witht he content-type header
        MimeMessage msg = new MimeMessage(Session.getInstance(new Properties()));
        msg.setContent(mp);
        msg.writeTo(out, new String[] { "Message-ID", "MIME-Version" });
        out.close();
        return new String(out.toByteArray(), "UTF-8");
    }

    private MimeMultipart getFirstMultipart(MimeMessage message) throws IOException, MessagingException {
        final Object content = message.getContent();
        if (content instanceof Multipart || content instanceof MimeMultipart) {
            final MimeMultipart mp = (MimeMultipart) content;
            return (mp);
        }
        return null;
    }

    @Override
    public MimeMessage encrypt(MimeMessage mimeMessage, List<PGPPublicKey> recipientsKeys) throws OXException {
        return encrypt(mimeMessage, recipientsKeys, null);
    }

    @Override
    public MimeMessage encrypt(MimeMessage mimeMessage, List<PGPPublicKey> recipientsKeys, List<BodyPart> additionalClearTextParts) throws OXException {
        return encryptSigned(mimeMessage, null /* no signing key */, null /* no signing password */, recipientsKeys, additionalClearTextParts);
    }

    @Override
    public MimeMessage encryptSigned(MimeMessage mimeMessage, PGPSecretKey signingKey, char[] password, List<PGPPublicKey> recipientsKeys) throws OXException {
        return encryptSigned(mimeMessage, signingKey, password, recipientsKeys, null);
    }

    @Override
    public MimeMessage encryptSigned(MimeMessage mimeMessage, PGPSecretKey signingKey, char[] password, List<PGPPublicKey> recipientsKeys, List<BodyPart> additionalClearTextParts) throws OXException {
        try {
            final boolean armored = true;
            final boolean doSigning = signingKey != null;
            final ByteArrayOutputStream encryptedMimeMessage = new ByteArrayOutputStream();
            final PGPEncrypter pgpEncrypter = new PGPEncrypter();
            final HashMap<String, String> originalHeaders = getHeadersFrom(mimeMessage);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            mimeMessage.writeTo(os);
            ByteArrayInputStream mimeMessageData = new ByteArrayInputStream(os.toByteArray());

            if (doSigning) {
                //Encrypt and sign the mime message
                pgpEncrypter.encryptSigned(mimeMessageData, encryptedMimeMessage, armored, signingKey, password, recipientsKeys.toArray(new PGPPublicKey[recipientsKeys.size()]));
            } else {
                //Only encrypt the mime message
                pgpEncrypter.encrypt(mimeMessageData, encryptedMimeMessage, armored, recipientsKeys.toArray(new PGPPublicKey[recipientsKeys.size()]));
            }

            //Wrapping the encrypted message in a proper PGP/MIME part
            return createPGPMimeWrapper(new ByteArrayInputStream(encryptedMimeMessage.toByteArray()), originalHeaders, additionalClearTextParts);
        } catch (MessagingException e) {
            throw PGPMailExceptionCodes.MESSAGE_EXCEPTION.create(e, e.getMessage());
        } catch (IOException e) {
            throw PGPMailExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } catch (PGPException e) {
            throw PGPMailExceptionCodes.PGP_EXCEPTION.create(e, e.getMessage());
        }
    }

    @Override
    public MimeMessage sign(MimeMessage message, PGPSecretKey signingKey, char[] password) throws OXException {

        try {
            //Get the content and normalize; We need to normalize the text, remove whitespaces, etc
            MimeMultipart firstMultiPart = getFirstMultipart(message);
            String signingContent;
            if (firstMultiPart != null) {
                signingContent = normalize(getPartContent(firstMultiPart));
            } else { // If not multipart email, get the content
                signingContent = normalize(getMessageContentForSigning(message));
            }

            //Sign
            ByteArrayInputStream signingContentStream = new ByteArrayInputStream(signingContent.getBytes("UTF-8"));
            ByteArrayOutputStream signedContentStream = new ByteArrayOutputStream();
            PGPSignatureCreator signatureCreator = new PGPSignatureCreator();
            final boolean armored = true;
            signatureCreator.createSignature(signingContentStream, signedContentStream, armored, signingKey, password);

            MimeMultipart newcontent = new MimeMultipart("signed; micalg=pgp-sha1; protocol=\"application/pgp-signature\"");
            // Add our normalized text back as a body part
            MimeBodyPart bodyPart = new MimeBodyPart(new ByteArrayInputStream(signingContent.getBytes("UTF-8")));
            newcontent.addBodyPart(bodyPart);
            // Create signature attachment
            String signature = new String(signedContentStream.toByteArray());
            BodyPart signaturePart = new MimeBodyPart();
            signaturePart.setContent(signature, "application/pgp-signature");
            signaturePart.setDisposition("attachment");
            signaturePart.setFileName("signature.asc");
            signaturePart.setHeader("Content-Transfer-Encoding", "7bit");
            signaturePart.setHeader("Content-Type", "application/pgp-signature");
            newcontent.addBodyPart(signaturePart);

            MimeMessage ret = new MimeMessage(message);
            ret.setContent(newcontent);
            ret.saveChanges();

            return ret;
        } catch (IOException e) {
            throw PGPMailExceptionCodes.IO_EXCEPTION.create(e, e.getMessage());
        } catch (MessagingException e) {
            throw PGPMailExceptionCodes.MESSAGE_EXCEPTION.create(e, e.getMessage());
        }
    }
}
