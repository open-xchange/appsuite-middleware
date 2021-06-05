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
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
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
    public IMAPMailPart(ByteArray byteArray, BODYSTRUCTURE body, String fullName) throws IOException {
        super();
        ThresholdInputStreamProvider inProvider = new ThresholdInputStreamProvider();
        try {
            inProvider.write(byteArray.getBytes(), byteArray.getStart(), byteArray.getCount());
            this.inProvider = inProvider;
            inProvider = null;
            this.body = body;
            this.fullName = fullName;
        } finally {
            Streams.close(inProvider);
        }
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
    public IMAPMailPart(IMAPMessage msg, String sectionId, boolean peek, BODYSTRUCTURE body, String fullName, boolean loadContent) throws IOException {
        super();
        if (loadContent) {
            ThresholdInputStreamProvider tisp = new ThresholdInputStreamProvider();
            try {
                tisp.write(new IMAPInputStream(msg, sectionId, body.size, peek));
                this.inProvider = tisp;
                tisp = null;
            } finally {
                Streams.close(tisp);
            }
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
        InputStreamProvider inp = this.inProvider;
        if (!(inp instanceof ThresholdInputStreamProvider)) {
            ThresholdInputStreamProvider tisp = new ThresholdInputStreamProvider();
            try {
                tisp.write(inp.getInputStream());
                this.inProvider = tisp;
                tisp = null;
            } catch (com.sun.mail.util.MessageRemovedIOException e) {
                throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(e, e.getMessage());
            } catch (IOException e) {
                throw MailExceptionCode.UNREADBALE_PART_CONTENT_SIMPLE.create(e, e.getMessage());
            } finally {
                Streams.close(tisp);
            }
        }
    }

    @Override
    public InputStream getInputStream() throws OXException {
        String encoding = getFirstHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC);
        if (null != encoding) {
            try {
                return MimeUtility.decode(inProvider.getInputStream(), encoding);
            } catch (MessagingException e) {
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
    public void applyBodyStructure(BODYSTRUCTURE bodystructure) {
        if (null == bodystructure) {
            return;
        }
        {
            String disposition = bodystructure.disposition;
            ParameterList dParams = bodystructure.dParams;
            if (null != disposition || null != dParams) {
                ContentDisposition contentDisposition = new ContentDisposition();
                if (null != disposition) {
                    contentDisposition.setDisposition(disposition);
                }
                if (null != dParams) {
                    for (Enumeration<String> names = dParams.getNames(); names.hasMoreElements();) {
                        String name = names.nextElement();
                        String value = dParams.get(name);
                        contentDisposition.setParameter(name, (Strings.isEmpty(value) || (value.indexOf("=?") < 0)) ? value : MimeMessageUtility.decodeEnvelopeHeader(value));
                    }
                }
                setContentDisposition(contentDisposition);
            }
        }
        {
            ParameterList cParams = bodystructure.cParams;
            if (null != bodystructure.type || null != bodystructure.subtype || null != cParams) {
                ContentType contentType = new ContentType();
                if (null != bodystructure.type) {
                    contentType.setPrimaryType(Strings.asciiLowerCase(bodystructure.type));
                }
                if (null != bodystructure.subtype) {
                    contentType.setSubType(Strings.asciiLowerCase(bodystructure.subtype));
                }
                if (null != cParams) {
                    for (Enumeration<String> names = cParams.getNames(); names.hasMoreElements();) {
                        String name = names.nextElement();
                        String value = cParams.get(name);
                        contentType.setParameter(name, (Strings.isEmpty(value) || (value.indexOf("=?") < 0)) ? value : MimeMessageUtility.decodeEnvelopeHeader(value));
                    }
                }
                setContentType(contentType);
            }
        }
        {
            String encoding = bodystructure.encoding;
            if (null != encoding) {
                setHeader(MessageHeaders.HDR_CONTENT_TRANSFER_ENC, encoding);
            }
        }
        {
            String fileName = bodystructure.attachment;
            if (null != fileName) {
                getContentDisposition().setFilenameParameter(fileName);
            }
        }
        {
            int size = bodystructure.size;
            if (size > 0) {
                setSize(size);
            }
        }
    }

    @Override
    public void opened(ConnectionEvent e) {
        // Ignore
    }

    @Override
    public void disconnected(ConnectionEvent e) {
        // Ignore
    }

    @Override
    public void closed(ConnectionEvent e) {
        if (ConnectionEvent.CLOSED == e.getType()) {
            Object source = e.getSource();
            if (source instanceof IMAPFolder) {
                @SuppressWarnings("resource") IMAPFolder imapFolder = (IMAPFolder) source;
                if (fullName.equals(imapFolder.getFullName())) {
                    try {
                        loadContent();
                    } catch (@SuppressWarnings("unused") OXException x) {
                        // Ignore
                    }
                }

            }
        }
    }

}
