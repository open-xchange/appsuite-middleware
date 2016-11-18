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

package com.openexchange.mail.json.compose;

import static com.openexchange.java.Strings.toLowerCase;
import static com.openexchange.mail.json.parser.MessageParser.parseAddressKey;
import static com.openexchange.mail.mime.filler.MimeMessageFiller.isCustomOrReplyHeader;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.parseAddressList;
import static com.openexchange.mail.mime.utils.MimeMessageUtility.shouldRetry;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.html.HtmlService;
import com.openexchange.java.HTMLDetector;
import com.openexchange.java.Strings;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DataMailPart;
import com.openexchange.mail.dataobjects.compose.ReferencedMailPart;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.ImageMatcher;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.MultipleMailPartHandler;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractComposeHandler} - The abstract compose handler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class AbstractComposeHandler<T extends ComposeContext, D extends ComposeContext> implements ComposeHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractComposeHandler.class);

    private static final String CONTENT = MailJSONField.CONTENT.getKey();
    private static final String CONTENT_TYPE = MailJSONField.CONTENT_TYPE.getKey();
    private static final String ATTACHMENTS = MailJSONField.ATTACHMENTS.getKey();
    private static final String MSGREF = MailJSONField.MSGREF.getKey();
    private static final String CID = MailJSONField.CID.getKey();
    private static final String ATTACHMENT_FILE_NAME = MailJSONField.ATTACHMENT_FILE_NAME.getKey();
    private static final String DATASOURCES = MailJSONField.DATASOURCES.getKey();
    private static final String INFOSTORE_IDS = MailJSONField.INFOSTORE_IDS.getKey();

    /**
     * Initializes a new {@link AbstractComposeHandler}.
     */
    protected AbstractComposeHandler() {
        super();
    }

    @Override
    public ComposeDraftResult createDraftResult(ComposeRequest request) throws OXException {
        D context = createDraftComposeContext(request);
        try {
            prepare(request, context);
            return doCreateDraftResult(request, context);
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } finally {
            context.dispose();
        }
    }

    @Override
    public ComposeTransportResult createTransportResult(ComposeRequest request) throws OXException {
        T context = createTransportComposeContext(request);
        try {
            prepare(request, context);
            return doCreateTransportResult(request, context);
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } finally {
            context.dispose();
        }
    }

    /**
     * Creates a new compose context suitable for this compose handler to generate a draft result.
     *
     * @param request The compose request providing needed data
     * @return The new compose context
     * @throws OXException If new compose context cannot be created
     */
    protected abstract D createDraftComposeContext(ComposeRequest request) throws OXException;

    /**
     * Creates a new compose context suitable for this compose handler to generate a draft result.
     *
     * @param request The compose request providing needed data
     * @return The new compose context
     * @throws OXException If new compose context cannot be created
     */
    protected abstract T createTransportComposeContext(ComposeRequest request) throws OXException;

    /**
     * Creates the result for saving a message into standard Drafts folder.
     *
     * @param request The compose request providing needed data
     * @param context The compose context to use
     * @return The draft result
     * @throws OXException If result cannot be created
     */
    protected abstract ComposeDraftResult doCreateDraftResult(ComposeRequest request, D context) throws OXException;

    /**
     * Creates the result for transporting one or more messages plus providing the message representation that is supposed to be saved into
     * standard Sent folder.
     *
     * @param request The compose request providing needed data
     * @param context The compose context to use
     * @return The transport result
     * @throws OXException If result cannot be created
     */
    protected abstract ComposeTransportResult doCreateTransportResult(ComposeRequest request, T context) throws OXException;

    /**
     * Creates the regular message containing all attachments as-is.
     *
     * @param context The associated compose context
     * @return The regular message
     */
    protected <C extends ComposeContext> ComposedMailMessage createRegularComposeMessage(C context) {
        ComposedMailMessage sourceMessage = context.getSourceMessage();
        sourceMessage.setBodyPart(context.getTextPart());
        for (MailPart part : context.getAllParts()) {
            sourceMessage.addEnclosedPart(part);
        }
        return sourceMessage;
    }

    /**
     * Generic preparation to fill-up specified compose context.
     *
     * @param request The compose request
     * @param context The compose context
     * @throws OXException If preparing fails
     * @throws JSONException If a JSON error occurred
     */
    protected <C extends ComposeContext> void prepare(ComposeRequest request, C context) throws OXException, JSONException {
        // Create a new compose message
        ComposedMailMessage sourceMessage = newComposedMailMessage(context);

        // Basic parsing
        parseBasics(sourceMessage, request, false);

        // Check for attachments (which also contain the text part)
        JSONArray attachmentArray = request.getJsonMail().optJSONArray(ATTACHMENTS);
        if (null != attachmentArray) {
            /*
             * Parse body text (the first array element)
             */
            String sContent;
            {
                // Grab first attachment, which is supposed to be the text part...
                JSONObject jTextBody = attachmentArray.getJSONObject(0);

                // ... and get its "content"
                sContent = jTextBody.getString(CONTENT);

                // Create a new text part instance
                TextBodyMailPart part = context.getProvider().getNewTextBodyPart(sContent);

                // Parse & set content type; be it "text/html", "multipart/alternative", whatever...
                String contentType;
                {
                    String tmp = jTextBody.optString(CONTENT_TYPE, null);
                    if (null == tmp) {
                        contentType = HTMLDetector.containsHTMLTags(sContent, true) ? "text/html" : "text/plain";
                    } else {
                        contentType = parseContentType(tmp);
                    }
                }
                part.setContentType(contentType);

                // Check for special "raw" text
                if (contentType.startsWith("text/plain") && jTextBody.optBoolean("raw", false)) {
                    part.setPlainText(sContent);
                }

                // Apply content type to compose message as well
                sourceMessage.setContentType(part.getContentType());

                // Add text part to compose context
                context.setTextPart(part);
            }

            // Remaining attachments (if any) refer to existing parts from other messages in store
            if (attachmentArray.length() > 1) {
                // Check for inline images (in case content is HTML)
                Set<String> contentIds = extractContentIds(sContent);
                parseAttachments(sourceMessage, attachmentArray, contentIds, context);
            }
        } else {
            // There are no attachments at all; yield an empty text part
            TextBodyMailPart part = context.getProvider().getNewTextBodyPart("");
            part.setContentType(MimeTypes.MIME_DEFAULT);
            sourceMessage.setContentType(part.getContentType());

            // Add text part to compose context
            context.setTextPart(part);
        }

        // Check for uploaded file attachments
        {
            UploadEvent uploadEvent = request.getUploadEvent();
            if (null != uploadEvent) {
                for (UploadFile uf : uploadEvent.getUploadFiles()) {
                    if (uf != null) {
                        context.addUploadPart(context.getProvider().getNewFilePart(uf));
                    }
                }
            }
        }

        // Parse data sources
        if (request.getJsonMail().hasAndNotNull(DATASOURCES)) {
            parseDataSources(request.getJsonMail().getJSONArray(DATASOURCES), context);
        }

        // Attach Drive documents
        if (request.getJsonMail().hasAndNotNull(INFOSTORE_IDS)) {
            parseDriveParts(request.getJsonMail().getJSONArray(INFOSTORE_IDS), context);
        }
    }

    /**
     * Parses specified Drive parts
     *
     * @param jDriveIds The Drive identifiers
     * @param context The associated compose context
     * @throws OXException If parsing fails
     * @throws JSONException If a JSON error occurred
     */
    protected void parseDriveParts(JSONArray jDriveIds, ComposeContext context) throws OXException, JSONException {
        int length = jDriveIds.length();
        if (length > 0) {
            // Check max. allowed Drive attachments
            int max = MailProperties.getInstance().getMaxDriveAttachments();
            if (max > 0 && length > max) {
                throw MailExceptionCode.MAX_DRIVE_ATTACHMENTS_EXCEEDED.create(Integer.toString(max));
            }
            for (int i = 0; i < length; i++) {
                context.addDrivePart(context.getProvider().getNewDocumentPart(jDriveIds.getString(i), context.getSession()));
            }
        }
    }

    /**
     * Parses specified data sources.
     *
     * @param jDataSources The JSON array of data sources
     * @param context The associated compose context
     * @throws OXException If parsing fails
     * @throws JSONException If a JSON error occurred
     */
    protected void parseDataSources(JSONArray jDataSources, ComposeContext context) throws OXException, JSONException {
        int length = jDataSources.length();
        if (length > 0) {
            // Check max. allowed Drive attachments
            int max = MailProperties.getInstance().getMaxDriveAttachments();
            if (max > 0 && length > max) {
                throw MailExceptionCode.MAX_DRIVE_ATTACHMENTS_EXCEEDED.create(Integer.toString(max));
            }

            // Proceed
            ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);
            if (conversionService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConversionService.class.getName());
            }

            Set<Class<?>> types = new HashSet<Class<?>>(4);
            for (int i = 0; i < length; i++) {
                JSONObject jDataSource = jDataSources.getJSONObject(i);
                if (!jDataSource.hasAndNotNull("identifier")) {
                    throw MailExceptionCode.MISSING_PARAM.create("identifier");
                }

                DataSource dataSource = conversionService.getDataSource(jDataSource.getString("identifier"));
                if (dataSource == null) {
                    throw DataExceptionCodes.UNKNOWN_DATA_SOURCE.create(jDataSource.getString("identifier"));
                }

                types.clear();
                types.addAll(Arrays.asList(dataSource.getTypes()));

                Data<?> data;
                if (types.contains(InputStream.class)) {
                    data = dataSource.getData(InputStream.class, parseDataSourceArguments(jDataSource), context.getSession());
                } else if (types.contains(byte[].class)) {
                    data = dataSource.getData(byte[].class, parseDataSourceArguments(jDataSource), context.getSession());
                } else {
                    throw MailExceptionCode.UNSUPPORTED_DATASOURCE.create();
                }

                DataMailPart dataMailPart = context.getProvider().getNewDataPart(data.getData(), data.getDataProperties().toMap(), context.getSession());
                context.addDataPart(dataMailPart);
            }
        }
    }

    /**
     * Parses the data source arguments provided by given data source's JSON representation.
     *
     * @param jDataSource The JSON representation of the data source
     * @return The parsed arguments
     * @throws JSONException If a JSON error occurred
     */
    protected DataArguments parseDataSourceArguments(JSONObject jDataSource) throws JSONException {
        if (!jDataSource.hasAndNotNull("args")) {
            return DataArguments.EMPTY_ARGS;
        }

        Object args = jDataSource.get("args");
        if (args instanceof JSONArray) {
            // Handle as JSON array
            JSONArray jsonArray = (JSONArray) args;
            int len = jsonArray.length();
            DataArguments dataArguments = new DataArguments(len);
            for (int i = 0; i < len; i++) {
                JSONObject elem = jsonArray.getJSONObject(i);
                if (elem.length() == 1) {
                    String key = elem.keys().next();
                    dataArguments.put(key, elem.getString(key));
                } else {
                    LOG.warn("Corrupt data argument in JSON object: {}", elem);
                }
            }
            return dataArguments;
        }

        // Expect JSON object
        JSONObject argsObject = (JSONObject) args;
        int len = argsObject.length();
        DataArguments dataArguments = new DataArguments(len);
        for (Entry<String, Object> entry : argsObject.entrySet()) {
            dataArguments.put(entry.getKey(), entry.getValue().toString());
        }
        return dataArguments;
    }

    /**
     * Parses the attachments provided by JSON representation of the message.
     *
     * @param composeMessage The base compose message
     * @param jAttachments The attachments to parse
     * @param contentIds The extracted content identifiers of inline images
     * @param context The compose context
     * @throws OXException If parsing fails
     * @throws JSONException If a JSON error occurred
     */
    protected void parseAttachments(ComposedMailMessage composeMessage, JSONArray jAttachments, Set<String> contentIds, ComposeContext context) throws OXException, JSONException {
        // Get the identifier of the referenced message
        MailPath parentMsgRef = composeMessage.getMsgref();

        // Load & set referenced parts
        Map<String, ReferencedMailPart> referencedParts = loadReferencedParts(jAttachments, contentIds, parentMsgRef, context);

        // Iterate attachments (once again)
        int len = jAttachments.length();
        for (int i = 1; i < len; i++) {
            JSONObject jAttachment = jAttachments.getJSONObject(i);
            String seqId = jAttachment.optString(MailListField.ID.getKey(), null);
            if (null == seqId && jAttachment.hasAndNotNull(CONTENT)) {
                handleDataPart(jAttachment, context);
            } else {
                handleReferencedPart(parentMsgRef, seqId, referencedParts, jAttachment, context);
            }
        }
    }

    /**
     * Handles specified referenced part and adds it to compose context
     *
     * @param parentMsgRef The message reference identifier
     * @param seqId The part's sequence identifier
     * @param referencedParts The already loaded referenced parts
     * @param jAttachment The attachment's JSON representation
     * @param context The compose context to store to
     * @throws OXException If handling fails
     * @throws JSONException If a JSON error occurred
     */
    protected void handleReferencedPart(MailPath parentMsgRef, String seqId, Map<String, ReferencedMailPart> referencedParts, JSONObject jAttachment, ComposeContext context) throws OXException, JSONException {
        // Prefer "msgref" from attachment if present, otherwise from superior mail
        MailPath msgref;
        boolean isMail;
        if (jAttachment.hasAndNotNull(MSGREF)) {
            msgref = new MailPath(jAttachment.get(MSGREF).toString());
            msgref = prepareMsgRef(context.getSession(), msgref);
            isMail = true;
        } else {
            msgref = parentMsgRef;
            isMail = false;
        }

        // Get part
        ReferencedMailPart referencedPart;
        if (isMail) {
            // The mail itself
            MailAccess<?, ?> access = context.getConnectedMailAccess(msgref.getAccountId());
            MailMessage referencedMail = access.getMessageStorage().getMessage(msgref.getFolder(), msgref.getMailID(), false);
            if (null == referencedMail) {
                throw MailExceptionCode.REFERENCED_MAIL_NOT_FOUND.create(msgref.getMailID(), msgref.getFolder());
            }
            referencedMail.setAccountId(access.getAccountId());
            referencedPart = context.getProvider().getNewReferencedMail(referencedMail, context.getSession());
        } else {
            referencedPart = null == seqId ? null : referencedParts.get(seqId);
        }
        if (null != referencedPart) {
            referencedPart.setMsgref(msgref);
            context.addReferencedPart(referencedPart);
        }
    }

    /**
     * Handles specified data part and adds it to compose context
     *
     * @param jAttachment The attachment's JSON representation
     * @param context The compose context to store to
     * @throws OXException If handling fails
     * @throws JSONException If a JSON error occurred
     */
    protected void handleDataPart(JSONObject jAttachment, ComposeContext context) throws OXException, JSONException {
        String contentType = parseContentType(jAttachment.getString(CONTENT_TYPE));
        String charsetName = "UTF-8";
        byte[] content;
        try {
            // Client provides HTML content in any case. Generate well-formed HTML for further processing dependent on given content type.
            HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
            if (MimeTypes.MIME_TEXT_PLAIN.equals(contentType)) {
                if (jAttachment.optBoolean("raw", false)) {
                    content = jAttachment.getString(CONTENT).getBytes(charsetName);
                } else {
                    content = htmlService.html2text(jAttachment.getString(CONTENT), true).getBytes(charsetName);
                }
            } else {
                String conformHTML = htmlService.getConformHTML(jAttachment.getString(CONTENT), "ISO-8859-1");
                content = conformHTML.getBytes(charsetName);
            }

        } catch (UnsupportedEncodingException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }

        // As data object
        DataProperties properties = new DataProperties();
        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, contentType);
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(content.length));
        properties.put(DataProperties.PROPERTY_CHARSET, charsetName);
        {
            String fileName = jAttachment.optString(ATTACHMENT_FILE_NAME, null);
            if (!Strings.isEmpty(fileName)) {
                properties.put(DataProperties.PROPERTY_NAME, fileName);
            }
        }
        Data<byte[]> data = new SimpleData<byte[]>(content, properties);
        DataMailPart dataMailPart = context.getProvider().getNewDataPart(data.getData(), data.getDataProperties().toMap(), context.getSession());
        context.addDataPart(dataMailPart);
    }

    /**
     * Loads the referenced parts.
     *
     * @param jAttachments The attachments of the JSON representation of the message
     * @param contentIds The extracted content identifiers of inline images
     * @param parentMsgRef The message reference identifier
     * @param context The compose context
     * @return The loaded parts
     * @throws OXException If loading the parts fails
     * @throws JSONException If a JSON error occurred
     */
    protected Map<String, ReferencedMailPart> loadReferencedParts(JSONArray jAttachments, Set<String> contentIds, MailPath parentMsgRef, ComposeContext context) throws OXException, JSONException {
        if (null == parentMsgRef) {
            return Collections.emptyMap();
        }

        int len = jAttachments.length();
        Map<String, String> groupedSeqIDs = new HashMap<String, String>(len);
        for (int i = 1; i < len; i++) {
            JSONObject jAttachment = jAttachments.getJSONObject(i);
            String seqId = jAttachment.hasAndNotNull(MailListField.ID.getKey()) ? jAttachment.getString(MailListField.ID.getKey()) : null;
            if (seqId != null) {
                // If "msgref" is defined in attachment itself, the referenced mail itself is meant to be attached and not an attachment
                if (!jAttachment.hasAndNotNull(MSGREF)) {
                    Object cid = jAttachment.opt(CID);
                    groupedSeqIDs.put(seqId, null == cid ? "" : cid.toString());
                }
            }
        }

        if (groupedSeqIDs.isEmpty()) {
            return Collections.emptyMap();
        }

        // Load them
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = context.getConnectedMailAccess(parentMsgRef.getAccountId());
            return loadMultipleRefs(groupedSeqIDs, parentMsgRef, contentIds, mailAccess, context);
        } catch (OXException oe) {
            if (null == mailAccess || !shouldRetry(oe)) {
                throw oe;
            }

            mailAccess = context.reconnectMailAccess(mailAccess.getAccountId());
            return loadMultipleRefs(groupedSeqIDs, parentMsgRef, contentIds, mailAccess, context);
        }
    }

    protected Map<String, ReferencedMailPart> loadMultipleRefs(Map<String, String> groupedSeqIDs, MailPath parentMsgRef, Set<String> contentIds, MailAccess<?, ?> access, ComposeContext context) throws OXException {
        MailMessage referencedMail = access.getMessageStorage().getMessage(parentMsgRef.getFolder(), parentMsgRef.getMailID(), false);
        if (null == referencedMail) {
            throw MailExceptionCode.REFERENCED_MAIL_NOT_FOUND.create(parentMsgRef.getMailID(), parentMsgRef.getFolder());
        }
        referencedMail.setAccountId(access.getAccountId());

        // Get attachments out of referenced mail
        MultipleMailPartHandler handler = new MultipleMailPartHandler(groupedSeqIDs.keySet(), false);
        new MailMessageParser().parseMailMessage(referencedMail, handler);
        Map<String, ReferencedMailPart> loadedParts = new HashMap<String, ReferencedMailPart>(groupedSeqIDs.size());
        for (Map.Entry<String, MailPart> e : handler.getMailParts().entrySet()) {
            String seqId = e.getKey();
            loadedParts.put(seqId, context.getProvider().getNewReferencedPart(e.getValue(), context.getSession()));
            groupedSeqIDs.remove(seqId);
        }

        if (!groupedSeqIDs.isEmpty()) {
            for (String seqId : groupedSeqIDs.keySet()) {
                if (!contentIds.contains(seqId)) {
                    throw MailExceptionCode.ATTACHMENT_NOT_FOUND.create(seqId, Long.valueOf(referencedMail.getMailId()), referencedMail.getFolder());
                }
            }
        }

        return loadedParts;
    }

    /**
     * Creates a new {@code ComposedMailMessage} instance using account-associated transport provider.
     *
     * @param composeContext The compose context
     * @return A new {@code ComposedMailMessage} instance
     * @throws OXException If a new {@code ComposedMailMessage} instance cannot be returned
     */
    protected ComposedMailMessage newComposedMailMessage(ComposeContext composeContext) throws OXException {
        // Create a new instance
        ServerSession session = composeContext.getSession();
        ComposedMailMessage composedMail = composeContext.getProvider().getNewComposedMailMessage(session, session.getContext());
        composedMail.setAccountId(composeContext.getAccountId());

        // Apply to context
        composeContext.setSourceMessage(composedMail);
        return composedMail;
    }

    /**
     * Parses basic information (headers) from specified compose request and fills given {@code ComposedMailMessage} instance.
     *
     * @param composedMail The composed mail message to fill
     * @param composeRequest The associated compose request
     * @param forTransport Whether parsing takes place for generating a transport result or not.
     * @throws OXException If parsing fails
     * @throws JSONException If a JSON error occurred
     */
    protected void parseBasics(ComposedMailMessage composedMail, ComposeRequest composeRequest, boolean forTransport) throws OXException, JSONException {
        try {
            JSONObject jMail = composeRequest.getJsonMail();
            /*
             * System flags
             */
            if (jMail.hasAndNotNull(MailJSONField.FLAGS.getKey())) {
                composedMail.setFlags(jMail.getInt(MailJSONField.FLAGS.getKey()));
            }
            /*
             * Thread level
             */
            if (jMail.hasAndNotNull(MailJSONField.THREAD_LEVEL.getKey())) {
                composedMail.setThreadLevel(jMail.getInt(MailJSONField.THREAD_LEVEL.getKey()));
            }
            /*
             * User flags
             */
            if (jMail.hasAndNotNull(MailJSONField.USER.getKey())) {
                JSONArray arr = jMail.getJSONArray(MailJSONField.USER.getKey());
                int length = arr.length();
                List<String> l = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    l.add(arr.getString(i));
                }
                composedMail.addUserFlags(l.toArray(new String[l.size()]));
            }
            /*
             * Parse headers
             */
            if (jMail.hasAndNotNull(MailJSONField.HEADERS.getKey())) {
                JSONObject jHeaders = jMail.getJSONObject(MailJSONField.HEADERS.getKey());
                int size = jHeaders.length();
                HeaderCollection headers = new HeaderCollection(size);
                Iterator<String> iter = jHeaders.keys();
                for (int i = size; i-- > 0;) {
                    String key = iter.next();
                    if (isCustomOrReplyHeader(key) && !key.equalsIgnoreCase("x-original-headers")) {
                        headers.setHeader(key, jHeaders.getString(key));
                    }
                }
                composedMail.addHeaders(headers);
            }
            /*
             * From Only mandatory if non-draft message
             */
            String fromKey = MailJSONField.FROM.getKey();
            if (jMail.hasAndNotNull(fromKey)) {
                try {
                    String value = jMail.getString(fromKey);
                    int endPos;
                    if ('[' == value.charAt(0) && (endPos = value.indexOf(']', 1)) < value.length()) {
                        value = new StringBuilder(32).append("\"[").append(value.substring(1, endPos)).append("]\"").append(value.substring(endPos+1)).toString();
                    }
                    composedMail.addFrom(parseAddressList(value, true, true));
                } catch (AddressException e) {
                    composedMail.addFrom(parseAddressKey(fromKey, jMail, forTransport));
                }
            } else if (forTransport) {
                throw MailExceptionCode.MISSING_FIELD.create(fromKey);
            }
            /*
             * To Only mandatory if non-draft message
             */
            composedMail.addTo(parseAddressKey(MailJSONField.RECIPIENT_TO.getKey(), jMail, forTransport));
            /*
             * Cc
             */
            composedMail.addCc(parseAddressKey(MailJSONField.RECIPIENT_CC.getKey(), jMail, forTransport));
            /*
             * Bcc
             */
            composedMail.addBcc(parseAddressKey(MailJSONField.RECIPIENT_BCC.getKey(), jMail, forTransport));
            /*
             * Optional Reply-To
             */
            {
                InternetAddress[] addrs = parseAddressKey("reply_to", jMail, false);
                if (null != addrs && addrs.length > 0) {
                    composedMail.setHeader("Reply-To", addrs[0].toString());
                }
            }
            /*
             * Disposition notification
             */
            if (jMail.hasAndNotNull(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey())) {
                /*
                 * Ok, disposition-notification-to is set. Check if its value is a valid email address
                 */
                String dispVal = jMail.getString(MailJSONField.DISPOSITION_NOTIFICATION_TO.getKey());
                if ("true".equalsIgnoreCase(dispVal)) {
                    /*
                     * Boolean value "true"
                     */
                    InternetAddress[] from = composedMail.getFrom();
                    composedMail.setDispositionNotification(from.length > 0 ? from[0] : null);
                } else {
                    InternetAddress ia = getEmailAddress(dispVal);
                    if (ia == null) {
                        /*
                         * Any other value
                         */
                        composedMail.setDispositionNotification(null);
                    } else {
                        /*
                         * Valid email address
                         */
                        composedMail.setDispositionNotification(ia);
                    }
                }
            }
            /*
             * Priority
             */
            if (jMail.hasAndNotNull(MailJSONField.PRIORITY.getKey())) {
                composedMail.setPriority(jMail.getInt(MailJSONField.PRIORITY.getKey()));
            }
            /*
             * Color Label
             */
            if (jMail.hasAndNotNull(MailJSONField.COLOR_LABEL.getKey())) {
                composedMail.setColorLabel(jMail.getInt(MailJSONField.COLOR_LABEL.getKey()));
            }
            /*
             * VCard
             */
            if (jMail.hasAndNotNull(MailJSONField.VCARD.getKey())) {
                composedMail.setAppendVCard((jMail.getInt(MailJSONField.VCARD.getKey()) > 0));
            }
            /*
             * Msg Ref
             */
            if (jMail.hasAndNotNull(MSGREF)) {
                composedMail.setMsgref(new MailPath(jMail.getString(MSGREF)));
            }
            /*
             * Subject, etc.
             */
            if (jMail.hasAndNotNull(MailJSONField.SUBJECT.getKey())) {
                composedMail.setSubject(jMail.getString(MailJSONField.SUBJECT.getKey()));
            }
            /*
             * Size
             */
            if (jMail.hasAndNotNull(MailJSONField.SIZE.getKey())) {
                composedMail.setSize(jMail.getInt(MailJSONField.SIZE.getKey()));
            }
            /*
             * Sent & received date
             */
            TimeZone timeZone = TimeZoneUtils.getTimeZone(composeRequest.getSession().getUser().getTimeZone());
            if (jMail.hasAndNotNull(MailJSONField.SENT_DATE.getKey())) {
                Date date = new Date(jMail.getLong(MailJSONField.SENT_DATE.getKey()));
                int offset = timeZone.getOffset(date.getTime());
                composedMail.setSentDate(new Date(jMail.getLong(MailJSONField.SENT_DATE.getKey()) - offset));
            } else {
                composedMail.setSentDate(new Date());
            }
            if (jMail.hasAndNotNull(MailJSONField.RECEIVED_DATE.getKey())) {
                Date date = new Date(jMail.getLong(MailJSONField.RECEIVED_DATE.getKey()));
                int offset = timeZone.getOffset(date.getTime());
                composedMail.setReceivedDate(new Date(jMail.getLong(MailJSONField.RECEIVED_DATE.getKey()) - offset));
            }
            /*
             * Security settings
             */
            {
                JSONObject jSecuritySettings = jMail.optJSONObject("security");
                if (null != jSecuritySettings) {
                    SecuritySettings settings = SecuritySettings.builder()
                        .encrypt(jSecuritySettings.optBoolean("encrypt", false))
                        .pgpInline(jSecuritySettings.optBoolean("pgpInline", false))
                        .sign(jSecuritySettings.optBoolean("sign", false))
                        .authentication(jSecuritySettings.optString("authentication", null))
                        .build();
                    if (settings.anythingSet()) {
                        composedMail.setSecuritySettings(settings);
                    }
                }
            }
            /*
             * Drop special "x-original-headers" header
             */
            composedMail.removeHeader("x-original-headers");
            /*
             * Check "msgref"
             */
            prepareMsgRef(composeRequest.getSession(), composedMail);
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        }
    }

    private static InternetAddress getEmailAddress(String addrStr) {
        if (com.openexchange.java.Strings.isEmpty(addrStr)) {
            return null;
        }
        try {
            return new QuotedInternetAddress(addrStr, false);
        } catch (AddressException e) {
            return null;
        }
    }

    private static void prepareMsgRef(Session session, MailMessage mail) throws OXException {
        MailPath msgref = mail.getMsgref();
        if (null == msgref) {
            // Nothing to do
            return;
        }
        mail.setMsgref(prepareMsgRef(session, msgref));
    }

    private static MailPath prepareMsgRef(Session session, MailPath msgref) throws OXException {
        UnifiedInboxManagement unifiedINBOXManagement = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
        if (null != unifiedINBOXManagement && msgref.getAccountId() == unifiedINBOXManagement.getUnifiedINBOXAccountID(session)) {
            // Something like: INBOX/default6/INBOX
            String nestedFullname = msgref.getFolder();
            int pos = nestedFullname.indexOf(MailFolder.DEFAULT_FOLDER_ID);
            if (-1 == pos) {
                // Return unchanged
                return msgref;
            }
            int check = pos + MailFolder.DEFAULT_FOLDER_ID.length();
            while (Character.isDigit(nestedFullname.charAt(check))) {
                check++;
            }
            if (MailProperties.getInstance().getDefaultSeparator() != nestedFullname.charAt(check)) {
                // Unexpected pattern
                return msgref;
            }
            // Create fullname argument from sub-path
            FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(nestedFullname.substring(pos));
            // Adjust msgref
            return new MailPath(arg.getAccountId(), arg.getFullname(), msgref.getMailID());
        }
        return msgref;
    }

    private static final Pattern PATTERN_ID_ATTRIBUTE = Pattern.compile("id=\"((?:\\\\\\\"|[^\"])+?)\"");

    private static Set<String> extractContentIds(String htmlContent) {
        ImageMatcher m = ImageMatcher.matcher(htmlContent);
        if (!m.find()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<String>(4);
        do {
            String imageTag = m.group();
            Matcher tmp = PATTERN_ID_ATTRIBUTE.matcher(imageTag);
            if (tmp.find()) {
                set.add(tmp.group(1));
            }
        } while (m.find());
        return set;
    }

    private static final String CT_ALTERNATIVE = "alternative";

    private static String parseContentType(String ctStrArg) {
        String ctStr = toLowerCase(ctStrArg).trim();
        if (ctStr.indexOf(CT_ALTERNATIVE) != -1) {
            return MimeTypes.MIME_MULTIPART_ALTERNATIVE;
        }
        if (MimeTypes.MIME_TEXT_PLAIN.equals(ctStr) || "text".equals(ctStr)) {
            return MimeTypes.MIME_TEXT_PLAIN;
        }
        return MimeTypes.MIME_TEXT_HTML;
    }

}
