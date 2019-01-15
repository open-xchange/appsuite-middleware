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
import com.openexchange.chronos.scheduling.SchedulingMessage;
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

    protected final SchedulingMessage message;

    protected String charset = MailProperties.getInstance().getDefaultMimeCharset();

    protected HtmlService htmlService;

    /**
     * Initializes a new {@link AbstractMimePartFactory}.
     * 
     * @param message The {@link SchedulingMessage}
     * @param serviceLookup The {@link ServiceLookup}
     * @throws OXException In case services are missing
     */
    public AbstractMimePartFactory(SchedulingMessage message, ServiceLookup serviceLookup) throws OXException {
        super();
        this.message = message;
        this.htmlService = serviceLookup.getServiceSafe(HtmlService.class);
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
            return message.getDescription().getText();
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
            return htmlService.getConformHTML(message.getDescription().getHtml(), charset);
        }, ct)));
        htmlPart.setHeader(MessageHeaders.HDR_MIME_VERSION, "1.0");
        htmlPart.setHeader(MessageHeaders.HDR_CONTENT_TYPE, ct.toString());
        return htmlPart;
    }

}
