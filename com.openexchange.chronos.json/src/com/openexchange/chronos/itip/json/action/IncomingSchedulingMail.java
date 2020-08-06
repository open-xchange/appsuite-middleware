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

package com.openexchange.chronos.itip.json.action;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Optional;
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.annotation.NonNull;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.scheduling.IncomingSchedulingObject;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link IncomingSchedulingMail} - Data object holding information about the mail
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class IncomingSchedulingMail implements IncomingSchedulingObject {


    private static final Logger LOGGER = LoggerFactory.getLogger(IncomingSchedulingMail.class);

    private static final String DATA_SOURCE = "com.openexchange.mail.attachment";
    private static final String CONVERSION_MAIL_ID = "com.openexchange.mail.conversion.mailid";
    private static final String CONVERSION_MAIL_FOLDER = "com.openexchange.mail.conversion.fullname";
    private static final String CONVERSION_MAIL_CID = "com.openexchange.mail.conversion.cid";
    private static final String FILE_NAME = "com.openexchange.conversion.name";
    private static final String FILE_CONTENT_TYPE = "com.openexchange.conversion.content-type";

    private final ServiceLookup services;
    private final MailMessage mail;

    private final AJAXRequestData request;
    private final String mailId;
    private final String folderId;

    /**
     * Initializes a new {@link IncomingSchedulingMail}.
     * 
     * @param services The service lookup to get the {@link ConversionService} from
     * @param request The request
     * @param session The session
     * @throws OXException If mail can't be loaded
     *
     */
    public IncomingSchedulingMail(ServiceLookup services, AJAXRequestData request, Session session) throws OXException {
        super();
        this.services = services;
        this.request = request;

        Object data = request.getData();
        if (data == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        if (false == data instanceof JSONObject) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
        JSONObject body = (JSONObject) data;

        try {
            this.folderId = body.getString(CONVERSION_MAIL_FOLDER);
            this.mailId = body.getString(CONVERSION_MAIL_ID);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
        this.mail = loadMail(session);
    }

    @Override
    @NonNull
    public CalendarUser getOriginator() throws OXException {
        InternetAddress[] addresses = mail.getFrom();
        if (null != addresses && 1 == addresses.length) {
            InternetAddress from = addresses[0];
            CalendarUser calendarUser = new CalendarUser();
            calendarUser.setEMail(from.getAddress());
            calendarUser.setUri(CalendarUtils.getURI(from.getAddress()));
            calendarUser.setCn(from.getPersonal());
            return calendarUser;
        }
        throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Can't find originator.");
    }

    @Override
    public Optional<Attachment> getAttachment(String uri) {
        if (Strings.isEmpty(uri)) {
            return Optional.empty();
        }
        /*
         * Load file from mail and convert to an attachment
         */
        boolean rollback = true;
        Attachment attachment = new Attachment();
        ThresholdFileHolder attachmentData = null;
        try {
            attachmentData = optAttachmentData(uri);
            if (null == attachmentData) {
                if (uri.equals(removeCidPrefix(uri))) {
                    LOGGER.warn("Unable to find attachment with CID {}.", uri);
                    return Optional.empty();
                }
                /*
                 * Remove CID and try again
                 */
                return getAttachment(removeCidPrefix(uri));
            }
            attachment.setData(attachmentData);
            attachment.setChecksum(attachmentData.getMD5());
            if (Strings.isNotEmpty(attachmentData.getName())) {
                attachment.setFilename(attachmentData.getName());
            }
            if (Strings.isNotEmpty(attachmentData.getContentType())) {
                attachment.setFormatType(attachmentData.getContentType());
            }
            rollback = false;
            return Optional.of(attachment);
        } catch (OXException e) {
            LOGGER.warn("Unable to receive attachment.", e);
        } finally {
            if (rollback) {
                Streams.close(attachmentData);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "IncomingSchedulingMail [folderId=" + folderId + ", mailId=" + mailId + "]";
    }

    /**
     * Loads the mail that triggered the scheduling
     *
     * @param message The message
     * @return The mail
     * @throws OXException if loading fails
     */
    protected @NonNull MailMessage loadMail(Session session) throws OXException {
        MailServletInterface instance = null;
        try {
            instance = MailServletInterface.getInstance(session);
            MailMessage mailMessage = instance.getMessage(folderId, mailId, false);
            if (null == mailMessage) {
                throw MailExceptionCode.MAIL_NOT_FOUND.create(mailId, folderId);
            }
            return mailMessage;
        } finally {
            if (null != instance) {
                instance.close();
            }
        }
    }

    /**
     * Converts a "cid" URL to its corresponding <code>Content-ID</code> message header,
     *
     * @param cidUrl The "cid" URL to convert
     * @return The corresponding contentId, or the passed value as-is if not possible
     */
    private static String getContentId(String cidUrl) {
        if (Strings.isEmpty(cidUrl)) {
            return cidUrl;
        }
        /*
         * https://tools.ietf.org/html/rfc2392#section-2:
         * A "cid" URL is converted to the corresponding Content-ID message header [MIME] by removing the "cid:" prefix, converting the
         * % encoded character to their equivalent US-ASCII characters, and enclosing the remaining parts with an angle bracket pair,
         * "<" and ">".
         */
        String contentId = cidUrl;
        if (contentId.toLowerCase().startsWith("cid:")) {
            contentId = contentId.substring(4);
        }
        try {
            contentId = URLDecoder.decode(contentId, Charsets.UTF_8_NAME);
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unexpected error decoding {}", contentId, e);
        }
        if (Strings.isEmpty(contentId)) {
            return contentId;
        }
        if ('<' != contentId.charAt(0)) {
            contentId = '<' + contentId;
        }
        if ('>' != contentId.charAt(contentId.length() - 1)) {
            contentId = contentId + '>';
        }
        return contentId;
    }

    /**
     * Attempts to retrieve data from a MIME attachment referenced by a specific content identifier and store it into a file holder.
     *
     * @param request The underlying request data providing the targeted e-mail message and session
     * @param contentId The content identifier of the attachment to retrieve
     * @return The attachment data loaded into a file holder, or <code>null</code> if not found
     */
    private ThresholdFileHolder optAttachmentData(String contentId) throws OXException {
        ConversionService conversionEngine = services.getServiceSafe(ConversionService.class);
        DataSource dataSource = conversionEngine.getDataSource(DATA_SOURCE);
        if (null == dataSource) {
            LOGGER.warn("Data source \"{}\" not available. Unable to access mail attachment data.", DATA_SOURCE);
            return null;
        }

        ThresholdFileHolder fileHolder = null;
        InputStream inputStream = null;
        try {
            DataArguments dataArguments = Utils.getDataArguments(request);
            dataArguments.put(CONVERSION_MAIL_CID, getContentId(contentId));
            Data<InputStream> data = dataSource.getData(InputStream.class, dataArguments, request.getSession());
            if (null != data) {
                inputStream = data.getData();
                fileHolder = new ThresholdFileHolder();
                fileHolder.write(inputStream);
                if (null != data.getDataProperties()) {
                    fileHolder.setContentType(data.getDataProperties().get(FILE_CONTENT_TYPE));
                    fileHolder.setName(data.getDataProperties().get(FILE_NAME));
                }
                ThresholdFileHolder retval = fileHolder;
                fileHolder = null;
                return retval;
            }
        } catch (OXException e) {
            if (e.equalsCode(49, "MSG")) {
                // Attachment not found
                return null;
            }
            throw e;
        } finally {
            Streams.close(inputStream, fileHolder);
        }

        return null;
    }

    private String removeCidPrefix(String uri) {
        if (Strings.isNotEmpty(uri) && uri.startsWith("CID:")) {
            return uri.substring(4);
        }
        return uri;
    }
}
