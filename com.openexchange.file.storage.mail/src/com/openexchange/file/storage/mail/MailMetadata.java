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

package com.openexchange.file.storage.mail;

import static com.openexchange.mail.json.writer.MessageWriter.getAddressesAsArray;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MailFolderUtility;
import com.sun.mail.imap.IMAPMessage;

/**
 * {@link MailMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class MailMetadata extends DefaultFile {

    private final String originalSubject;
    private final Long origUid;
    private final String origFolder;
    private final InternetAddress[] fromHeaders;
    private final InternetAddress[] toHeaders;

    /**
     * Initializes a new {@link MailMetadata}.
     *
     * @param message The IMAP message to construct the metadata for
     */
    public MailMetadata(IMAPMessage message) throws MessagingException {
        super();
        originalSubject = MimeMessageUtility.getHeader("X-Original-Subject", null, message);
        origUid = (Long) message.getItem("X-REAL-UID");
        origFolder = (String) message.getItem("X-MAILBOX");
        fromHeaders = MimeMessageUtility.getAddressHeader("From", message);
        toHeaders = MimeMessageUtility.getAddressHeader("To", message);
    }

    /**
     * Serializes the mail metadata to JSON.
     *
     * @return The mail metadata as JSON object, or <code>null</code> if an error occurs during serialization
     */
    public JSONObject renderJSON() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("subject", null == originalSubject ? JSONObject.NULL : MimeMessageUtility.decodeMultiEncodedHeader(originalSubject));
            jsonObject.put("id", null == origUid ? JSONObject.NULL : origUid.toString());
            jsonObject.put("folder", null == origFolder ? JSONObject.NULL : MailFolderUtility.prepareFullname(0, origFolder));
            jsonObject.put("from", fromHeaders == null || fromHeaders.length == 0 ? JSONObject.NULL : getAddressesAsArray(fromHeaders));
            jsonObject.put("to", toHeaders == null || toHeaders.length == 0 ? JSONObject.NULL : getAddressesAsArray(toHeaders));
            return jsonObject;
        } catch (JSONException e) {
            org.slf4j.LoggerFactory.getLogger(MailMetadata.class).warn("Error seriliazing mail metadata to JSON", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(renderJSON());
    }

}
