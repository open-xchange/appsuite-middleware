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

package com.openexchange.imap.dataobjects;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;
import com.openexchange.exception.OXException;
import com.openexchange.imap.util.InputStreamProvider;
import com.openexchange.imap.util.ThresholdInputStreamProvider;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.dataobjects.MimeRawSource;
import com.openexchange.mail.mime.datasource.MessageDataSource;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.sun.mail.iap.ByteArray;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link IMAPMailPart} - A mail part that references an IMAP message part.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPMailPart extends MailPart implements MimeRawSource, ConnectionListener {

    private static final long serialVersionUID = 6469037985453175593L;

    private InputStreamProvider inProvider;
    private final BODYSTRUCTURE body;
    private final String fullName;

    /**
     * Initializes a new {@link IMAPMailPart}.
     *
     * @param byteArray The associated part's binary data
     * @param body The body structure information
     * @throws IOException If an I/O error occurs
     */
    public IMAPMailPart(final ByteArray byteArray, final BODYSTRUCTURE body, final String fullName) throws IOException {
        super();
        final ThresholdInputStreamProvider inProvider = new ThresholdInputStreamProvider();
        inProvider.write(byteArray.getBytes(), byteArray.getStart(), byteArray.getCount());
        this.inProvider = inProvider;
        this.body = body;
        this.fullName = fullName;
    }

    /**
     * Initializes a new {@link IMAPMailPart}.
     *
     * @param msg The associated IMAP message
     * @param sectionId The referenced section identifier
     * @param peek Whether to peek (<code>\Seen</code> flag not set) or not
     * @param body The body structure information
     * @throws IOException If an I/O error occurs while loading content
     */
    public IMAPMailPart(final IMAPMessage msg, final String sectionId, final boolean peek, final BODYSTRUCTURE body, final String fullName, final boolean loadContent) throws IOException {
        super();
        if (loadContent) {
            ThresholdInputStreamProvider tisp = new ThresholdInputStreamProvider();
            tisp.write(new IMAPInputStream(msg, sectionId, body.size, peek));
            this.inProvider = tisp;
        } else {
            inProvider = new InputStreamProvider() {

                @Override
                public InputStream getInputStream() throws OXException {
                    return new IMAPInputStream(msg, sectionId, body.size, peek);
                }
            };
        }
        this.body = body;
        this.fullName = fullName;
    }

    @Override
    public void prepareForCaching() {
        // Nope
    }

    @Override
    public void loadContent() throws OXException {
        final InputStreamProvider inp = this.inProvider;
        if (!(inp instanceof ThresholdInputStreamProvider)) {
            try {
                final ThresholdInputStreamProvider tisp = new ThresholdInputStreamProvider();
                tisp.write(inp.getInputStream());
                this.inProvider = tisp;
            } catch (final IOException e) {
                throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    @Override
    public InputStream getInputStream() throws OXException {
        final String encoding = getFirstHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC);
        if (null != encoding) {
            try {
                return MimeUtility.decode(inProvider.getInputStream(), encoding);
            } catch (final MessagingException e) {
                throw MimeMailException.handleMessagingException(e);
            }
        }
        return inProvider.getInputStream();
    }

    @Override
    public InputStream getRawInputStream() throws OXException {
        return inProvider.getInputStream();
    }

    @Override
    public Part getPart() {
        return null;
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return null;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return MailPart.NO_ENCLOSED_PARTS;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        try {
            return new DataHandler(new MessageDataSource(getInputStream(), body.type + "/" + body.subtype));
        } catch (IOException e) {
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Object getContent() throws OXException {
        try {
            Object obj = getDataHandler().getContent();
            if (obj instanceof MimeMessage) {
                return MimeMessageConverter.convertMessage((MimeMessage) obj);
            } else if (obj instanceof Part) {
                return MimeMessageConverter.convertPart((Part) obj, false);
            } else {
                return obj;
            }
        } catch (UnsupportedEncodingException e) {
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            if ("com.sun.mail.util.MessageRemovedIOException".equals(e.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e);
            }
            throw MailExceptionCode.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Applies specified body structure information to this IMAP mail part.
     *
     * @param bodystructure The body structure information
     */
    public void applyBodyStructure(final BODYSTRUCTURE bodystructure) {
        if (null == bodystructure) {
            return;
        }
        {
            final String disposition = bodystructure.disposition;
            final ParameterList dParams = bodystructure.dParams;
            if (null != disposition || null != dParams) {
                final ContentDisposition contentDisposition = new ContentDisposition();
                if (null != disposition) {
                    contentDisposition.setDisposition(disposition);
                }
                if (null != dParams) {
                    for (final Enumeration<?> names = dParams.getNames(); names.hasMoreElements();) {
                        final String name = names.nextElement().toString();
                        contentDisposition.setParameter(name, MimeMessageUtility.decodeEnvelopeHeader(dParams.get(name)));
                    }
                }
                setContentDisposition(contentDisposition);
            }
        }
        {
            final ParameterList cParams = bodystructure.cParams;
            if (null != bodystructure.type || null != bodystructure.subtype || null != cParams) {
                final ContentType contentType = new ContentType();
                if (null != bodystructure.type) {
                    contentType.setPrimaryType(com.openexchange.java.Strings.toLowerCase(bodystructure.type));
                }
                if (null != bodystructure.subtype) {
                    contentType.setSubType(com.openexchange.java.Strings.toLowerCase(bodystructure.subtype));
                }
                if (null != cParams) {
                    for (final Enumeration<?> names = cParams.getNames(); names.hasMoreElements();) {
                        final String name = names.nextElement().toString();
                        contentType.setParameter(name, MimeMessageUtility.decodeEnvelopeHeader(cParams.get(name)));
                    }
                }
                setContentType(contentType);
            }
        }
        {
            final String encoding = bodystructure.encoding;
            if (null != encoding) {
                setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, encoding);
            }
        }
        {
            final String fileName = bodystructure.attachment;
            if (null != fileName) {
                getContentDisposition().setFilenameParameter(fileName);
            }
        }
        {
            final int size = bodystructure.size;
            if (size > 0) {
                setSize(size);
            }
        }
    }

    @Override
    public void opened(final ConnectionEvent e) {
        // Ignore
    }

    @Override
    public void disconnected(final ConnectionEvent e) {
        // Ignore
    }

    @Override
    public void closed(final ConnectionEvent e) {
        if (ConnectionEvent.CLOSED == e.getType()) {
            final Object source = e.getSource();
            if (source instanceof IMAPFolder) {
                final IMAPFolder imapFolder = (IMAPFolder) source;
                if (fullName.equals(imapFolder.getFullName())) {
                    try {
                        loadContent();
                    } catch (final OXException x) {
                        // Ignore
                    }
                }

            }
        }
    }

}
