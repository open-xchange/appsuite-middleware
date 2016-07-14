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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.mail;

import static com.openexchange.mail.json.writer.MessageWriter.getAddressesAsArray;
import static com.openexchange.mail.mime.converters.MimeMessageConverter.getAddressHeader;
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
        fromHeaders = getAddressHeader("From", message);
        toHeaders = getAddressHeader("To", message);
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
