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
