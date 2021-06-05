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
    public TextBodyMailPart(String mailBody) {
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
    protected final void fillInstance(TextBodyMailPart newInstance) {
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
    public void setText(String mailBody) {
        if (null == mailBody) {
            this.mailBody = null;
            return;
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
    public void append(String html) {
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
    public void setPlainText(String text) {
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
    public void appendPlainText(String text) {
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

    @Override
    public DataHandler getDataHandler() throws OXException {
        return new DataHandler(getDataSource());
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return NO_ENCLOSED_PARTS;
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return null;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        try {
            return getDataSource().getInputStream();
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void loadContent() {
    }

    @Override
    public void prepareForCaching() {
    }

    @Override
    public ComposedPartType getType() {
        return ComposedMailPart.ComposedPartType.BODY;
    }
}
