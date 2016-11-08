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

package com.openexchange.mail.dataobjects.compose;

import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessageRemovedException;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link TextBodyMailPart} - Designed to keep a mail's (text) body while offering a suitable implementation of {@link MailPart}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class TextBodyMailPart extends MailPart implements ComposedMailPart {

    private static final long serialVersionUID = 5748081743621854608L;

    private StringBuilder mailBody;

    private StringBuilder plainText;

    private transient DataSource dataSource;

    /**
     * Constructs a new {@link TextBodyMailPart}
     * <p>
     * The body part is supposed to be HTML content which is ought to be converted to appropriate MIME type on transport.
     *
     * @param mailBody The mail body as HTML content
     */
    public TextBodyMailPart(final String mailBody) {
        super();
        this.mailBody = null == mailBody ? null : new StringBuilder(mailBody);
    }

    /**
     * Gets a copy of this {@link TextBodyMailPart}.
     *
     * @return A copy of this {@link TextBodyMailPart
     * @throws OXException If creating a copy fails
     */
    public abstract TextBodyMailPart copy() throws OXException;

    /**
     * Fills specified instance with this body part's content.
     * <p>
     * This method is supposed to be invoked in subclass' {@link #copy()} method.
     *
     * @param newInstance The new instance to fill
     */
    protected final void fillInstance(final TextBodyMailPart newInstance) {
        newInstance.mailBody = null == mailBody ? null : new StringBuilder(mailBody.toString());
        newInstance.plainText = null == plainText ? null : new StringBuilder(plainText.toString());
        newInstance.dataSource = null;
        if (containsContentType()) {
            newInstance.setContentType(getContentType());
        }
        if (containsContentDisposition()) {
            newInstance.setContentDisposition(getContentDisposition());
        }
    }

    /**
     * Sets this part's text.
     * <p>
     * The body part is supposed to be HTML content which is ought to be converted to appropriate MIME type on transport.
     *
     * @param mailBody The mail body as HTML content
     */
    public void setText(final String mailBody) {
        if (null == mailBody) {
            this.mailBody = null;
        }
        if (null == this.mailBody) {
            this.mailBody = new StringBuilder(mailBody);
            return;
        }
        this.mailBody.setLength(0);
        this.mailBody.append(mailBody);
    }

    /**
     * Appends specified HTML text to this part.
     *
     * @param html The HTML text to append
     */
    public void append(final String html) {
        if (null == html) {
            return;
        }
        if (null == this.mailBody) {
            this.mailBody = new StringBuilder(html);
            return;
        }
        mailBody.append(html);
    }

    /**
     * Sets this part's optional plain text.
     *
     * @param text The mail body as plain text
     */
    public void setPlainText(final String text) {
        if (null == text) {
            plainText = null;
        } else if (null == plainText) {
            plainText = new StringBuilder(text);
        } else {
            plainText.setLength(0);
            plainText.append(text);
        }
    }

    /**
     * Gets this part's optional plain text.
     *
     * @return The mail body as plain text or <code>null</code>
     */
    public String getPlainText() {
        return null == plainText ? null : plainText.toString();
    }

    /**
     * Gets this part's HTML text.
     *
     * @return The mail body as HTML text or <code>null</code>
     */
    public String getHTML() {
        return null == mailBody ? null : mailBody.toString();
    }

    /**
     * Appends specified plain text to this part.
     *
     * @param text The plain text to append
     */
    public void appendPlainText(final String text) {
        if (null == plainText) {
            plainText = new StringBuilder(text);
        } else {
            plainText.append(text);
        }
    }

    private DataSource getDataSource() throws OXException {
        /*
         * Lazy creation
         */
        if (null == dataSource) {
            dataSource = new MessageDataSource(getHTMLContent(), getContentType());
        }
        return dataSource;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#getContent()
     */
    @Override
    public Object getContent() throws OXException {
        return getHTMLContent();
    }

    private String getHTMLContent() throws OXException {
        if (null != mailBody) {
            return mailBody.toString();
        }
        if (null != plainText) {
            final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
            return htmlService.htmlFormat(plainText.toString());
        }
        throw MailExceptionCode.UNEXPECTED_ERROR.create("Missing text.");
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#getDataHandler()
     */
    @Override
    public DataHandler getDataHandler() throws OXException {
        return new DataHandler(getDataSource());
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#getEnclosedCount()
     */
    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#getEnclosedMailPart(int)
     */
    @Override
    public MailPart getEnclosedMailPart(final int index) throws OXException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws OXException {
        try {
            return getDataSource().getInputStream();
        } catch (final IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#loadContent()
     */
    @Override
    public void loadContent() {
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.dataobjects.MailPart#prepareForCaching()
     */
    @Override
    public void prepareForCaching() {
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.mail.transport.smtp.dataobjects.SMTPMailPart#getType()
     */
    @Override
    public ComposedPartType getType() {
        return ComposedMailPart.ComposedPartType.BODY;
    }
}
