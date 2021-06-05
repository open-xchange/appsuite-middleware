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

package com.openexchange.chronos.scheduling.common;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import com.openexchange.chronos.scheduling.RecipientSettings;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractMimePartFactory}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public abstract class AbstractMimePartFactory implements MimePartFactory {

    protected static final String TEXT = "text";
    protected static final String HTML = "html";

    protected String charset = MailProperties.getInstance().getDefaultMimeCharset();

    protected final ServiceLookup services;

    protected final HtmlService htmlService;
    protected final ScheduleChange scheduleChange;
    protected final RecipientSettings recipientSettings;

    /**
     * Initializes a new {@link AbstractMimePartFactory}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     * @param scheduleChange The change to add to the mail
     * @param recipientSettings The recipient settings
     * @throws OXException In case services are missing
     */
    public AbstractMimePartFactory(ServiceLookup serviceLookup, ScheduleChange scheduleChange, RecipientSettings recipientSettings) throws OXException {
        super();
        this.services = serviceLookup;
        this.htmlService = serviceLookup.getServiceSafe(HtmlService.class);
        this.scheduleChange = scheduleChange;
        this.recipientSettings = recipientSettings;
    }

    /*
     * ----------------------------- HELPERS -----------------------------
     */

    protected MimeBodyPart generateTextPart() throws MessagingException {
        MimeBodyPart textPart = new MimeBodyPart();
        ContentType ct = new ContentType();
        ct.setPrimaryType(TEXT);
        ct.setSubType("plain");
        ct.setCharsetParameter(charset);
        textPart.setDataHandler(new DataHandler(new OnDemandStringDataSource(() -> {
            return scheduleChange.getText(recipientSettings);
        }, ct)));
        textPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        textPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
        return textPart;
    }

    protected MimeBodyPart generateHtmlPart() throws MessagingException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        ContentType ct = new ContentType();
        ct.setPrimaryType(TEXT);
        ct.setSubType(HTML);
        ct.setCharsetParameter(charset);
        htmlPart.setDataHandler(new DataHandler(new OnDemandStringDataSource(() -> {
            return htmlService.getConformHTML(scheduleChange.getHtml(recipientSettings), charset);
        }, ct)));
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
        return htmlPart;
    }

}
