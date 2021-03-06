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

package com.openexchange.mail.conversion;

import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.datasource.FileDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link VCardAttachMailDataHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VCardAttachMailDataHandler implements DataHandler {

    private static final String[] ARGS = {};

    private static final Class<?>[] TYPES = { InputStream.class, byte[].class };

    /**
     * Initializes a new {@link VCardAttachMailDataHandler}
     */
    public VCardAttachMailDataHandler() {
        super();
    }

    @Override
    public String[] getRequiredArguments() {
        return ARGS;
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

    @Override
    public ConversionResult processData(Data<? extends Object> data, DataArguments dataArguments, Session session) throws OXException {
        if (null == session) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create("session");
        }

        final Context ctx;
        final UserSettingMail usm;
        ctx = ContextStorage.getStorageContext(session);
        usm = UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), ctx);
        try {
            /*
             * Temporary store VCard as a file for later transport
             */
            final DataProperties vcardProperties = data.getDataProperties();
            /*
             * Get managed file from data
             */
            final ManagedFile managedFile = getBytesFromVCard(data.getData());
            String fileName = vcardProperties.get(DataProperties.PROPERTY_NAME);
            if (fileName == null) {
                fileName = "vcard.vcf";
            } else {
                fileName = MimeUtility.encodeText(fileName, "UTF-8", "Q");
            }
            managedFile.setFileName(fileName);
            /*
             * Compose content-type
             */
            final ContentType ct = new ContentType(vcardProperties.get(DataProperties.PROPERTY_CONTENT_TYPE));
            ct.setCharsetParameter(vcardProperties.get(DataProperties.PROPERTY_CHARSET));
            managedFile.setContentType(ct.toString());
            /*
             * Compose a new mail
             */
            final MimeMessage mimeMessage = new MimeMessage(MimeDefaultSession.getDefaultSession());
            /*
             * Set default subject
             */
            mimeMessage.setSubject(StringHelper.valueOf(UserStorage.getInstance().getUser(session.getUserId(), ctx).getLocale()).getString(MailStrings.DEFAULT_SUBJECT));
            /*
             * Set from
             */
            if (usm.getSendAddr() != null) {
                mimeMessage.setFrom(new QuotedInternetAddress(usm.getSendAddr(), false));
            }
            /*
             * Create multipart and its nested parts
             */
            final MimeMultipart mimeMultipart = new MimeMultipart("mixed");
            /*
             * Append empty text part
             */
            {
                final MimeBodyPart textPart = new MimeBodyPart();
                MessageUtility.setText("", "text/html; charset=UTF-8", "html", textPart);
                // textPart.setText("", "text/html; charset=UTF-8", "html");
                textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, "text/html; charset=UTF-8");
                mimeMultipart.addBodyPart(textPart);
            }
            /*
             * Append VCard data
             */
            {
                final MimeBodyPart vcardPart = new MimeBodyPart();
                /*
                 * Set appropriate JAF-DataHandler in VCard part
                 */
                vcardPart.setDataHandler(new javax.activation.DataHandler(new FileDataSource(managedFile.getFile(), ct.toString())));
                if (fileName != null) {
                    final ContentDisposition cd = new ContentDisposition(Part.ATTACHMENT);
                    cd.setFilenameParameter(fileName);
                    vcardPart.setHeader(MessageHeaders.HDR_CONTENT_DISPOSITION, MimeMessageUtility.foldContentDisposition(cd.toString()));
                }
                vcardPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
                if (fileName != null && !ct.containsNameParameter()) {
                    ct.setNameParameter(fileName);
                }
                vcardPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, MimeMessageUtility.foldContentType(ct.toString()));
                mimeMultipart.addBodyPart(vcardPart);
            }
            MessageUtility.setContent(mimeMultipart, mimeMessage);
            // mimeMessage.setContent(mimeMultipart);
            mimeMessage.saveChanges();
            // Remove generated Message-Id for template message
            mimeMessage.removeHeader(MessageHeaders.HDR_MESSAGE_ID);
            /*
             * Return mail's JSON object
             */
            final JSONObject mailObject = MessageWriter.writeMailMessage(
                MailAccount.DEFAULT_ID,
                MimeMessageConverter.convertMessage(mimeMessage),
                DisplayMode.MODIFYABLE,
                false,
                true,
                session,
                null);
            addFileInformation(mailObject, managedFile.getID());
            ConversionResult result = new ConversionResult();
            result.setData(mailObject);
            return result;
        } catch (MessagingException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    private static final String FILE_PREFIX = "file://";

    private static void addFileInformation(JSONObject mailObject, String fileId) throws JSONException, OXException {
        if (!mailObject.has(MailJSONField.ATTACHMENTS.getKey()) || mailObject.isNull(MailJSONField.ATTACHMENTS.getKey())) {
            throw DataExceptionCodes.ERROR.create(new StringBuilder(64).append("Parsed JSON mail object does not contain field '").append(
                MailJSONField.ATTACHMENTS.getKey()).append('\'').toString());
        }
        final JSONArray attachmentArray = mailObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
        final int len = attachmentArray.length();
        if (len != 2) {
            throw DataExceptionCodes.ERROR.create("Number of attachments in parsed JSON mail object is not equal to 2");
        }
        final JSONObject vcardAttachmentObject = attachmentArray.getJSONObject(1);
        vcardAttachmentObject.remove(MailListField.ID.getKey());
        vcardAttachmentObject.put(
            MailListField.ID.getKey(),
            new StringBuilder(FILE_PREFIX.length() + fileId.length()).append(FILE_PREFIX).append(fileId).toString());
    }

    private static ManagedFile getBytesFromVCard(Object vcard) throws OXException {
        try {
            final ManagedFileManagement management = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            if (null == management) {
                throw new IOException("Missing file management");
            }
            if (vcard instanceof InputStream) {
                return management.createManagedFile((InputStream) vcard);
            }
            if (vcard instanceof byte[]) {
                return management.createManagedFile((byte[]) vcard);
            }
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(vcard.getClass().getName());
        } catch (IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

}
