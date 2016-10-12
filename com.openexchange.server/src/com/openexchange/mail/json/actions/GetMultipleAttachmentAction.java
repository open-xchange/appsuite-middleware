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

package com.openexchange.mail.json.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.mail.MessageRemovedException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link GetMultipleAttachmentAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GetMultipleAttachmentAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetMultipleAttachmentAction}.
     *
     * @param services
     */
    public GetMultipleAttachmentAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            // final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            String[] sequenceIds = req.optStringArray(Mail.PARAMETER_MAILATTCHMENT);

            /*
             * Remove duplicate attachment ids
             */
            Set<String> attachmentIds = new LinkedHashSet<>(Arrays.asList(sequenceIds));
            sequenceIds = attachmentIds.toArray(new String[attachmentIds.size()]);

            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            ManagedFile mf = null;
            try {
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fullFileName = getFileName(req.getSession().getUser().getLocale(), mailInterface.getMessage(folderPath, uid));
                /*
                 * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment disposition.
                 */
                {
                    final AJAXRequestData ajaxRequestData = req.getRequest();
                    if (null != ajaxRequestData) {
                        if (ajaxRequestData.setResponseHeader("Content-Type", "application/zip")) {
                            try {
                                final StringBuilder sb = new StringBuilder(512);
                                sb.append("attachment");
                                DownloadUtility.appendFilenameParameter(fullFileName, "application/zip", ajaxRequestData.getUserAgent(), sb);
                                ajaxRequestData.setResponseHeader("Content-Disposition", sb.toString());
                                createZipArchive(folderPath, uid, sequenceIds, mailInterface, ajaxRequestData.optOutputStream());
                                // Streamed
                                return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                            } catch (final IOException e) {
                                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                            }
                        }
                    }
                }
                // The regular way
                mf = mailInterface.getMessageAttachments(folderPath, uid, sequenceIds);
                final ThresholdFileHolder fileHolder = new ThresholdFileHolder();
                /*
                 * Write from content's input stream to response output stream
                 */
                {
                    final InputStream zipInputStream = mf.getInputStream();
                    try {
                        fileHolder.write(zipInputStream);
                    } finally {
                        Streams.close(zipInputStream);
                    }
                }
                /*
                 * Parameterize file holder
                 */
                req.getRequest().setFormat("file");
                fileHolder.setName(fullFileName);
                // fileHolder.setContentType("application/octet-stream");
                fileHolder.setContentType("application/zip");
                return new AJAXRequestResult(fileHolder, "file");
            } finally {
                if (null != mf) {
                    mf.delete();
                    mf = null;
                }
            }
        } catch (final OXException e) {
            if (e.getCause() instanceof IOException) {
                final IOException ioe = (IOException) e.getCause();
                if ("com.sun.mail.util.MessageRemovedIOException".equals(ioe.getClass().getName()) || (e.getCause() instanceof MessageRemovedException)) {
                    throw MailExceptionCode.MAIL_NOT_FOUND_SIMPLE.create(ioe);
                }
            }
            throw e;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Returns the filename for the attachment
     *
     * @param req - Mailrequest to g
     * @param userLocale - Locale of the user to get correct subject translation in case subject is not set
     * @return String - file name
     */
    protected String getFileName(final Locale userLocale, final MailMessage message) {
        String fileName = message.getSubject();
        if (fileName == null) { // in case no subject was set
            fileName = StringHelper.valueOf(userLocale).getString(MailStrings.DEFAULT_SUBJECT);
        }

        return new StringBuilder(fileName).append(".zip").toString();
    }

    private void createZipArchive(final String folderPath, final String uid, final String[] sequenceIds, final MailServletInterface mailInterface, OutputStream out) throws OXException {
        final ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(out);
        zipOutput.setEncoding("UTF-8");
        zipOutput.setUseLanguageEncodingFlag(true);
        try {
            final int buflen = 8192;
            final byte[] buf = new byte[buflen];

            Map<String, Integer> fileNamesInArchive = new HashMap<String, Integer>();
            if (null == sequenceIds) {
                for (final MailPart mailPart : mailInterface.getAllMessageAttachments(folderPath, uid)) {
                    addPart2Archive(mailPart, zipOutput, buflen, buf, fileNamesInArchive);
                }
            } else {
                for (final String sequenceId : sequenceIds) {
                    final MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, false);
                    addPart2Archive(mailPart, zipOutput, buflen, buf, fileNamesInArchive);
                }
            }
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

    private void addPart2Archive(final MailPart mailPart, final ZipArchiveOutputStream zipOutput, final int buflen, final byte[] buf, Map<String, Integer> fileNamesInArchive) throws OXException {
        final InputStream in = mailPart.getInputStream();
        try {
            /*
             * Add ZIP entry to output stream
             */
            String name = mailPart.getFileName();
            if (null == name) {
                final List<String> extensions = MimeType2ExtMap.getFileExtensions(mailPart.getContentType().getBaseType());
                name = extensions == null || extensions.isEmpty() ? "part.dat" : "part." + extensions.get(0);
            }
            String entryName = name;
            Integer count = fileNamesInArchive.get(name);
            if (null != count) {
                entryName = FileStorageUtility.enhance(name, count++);
                fileNamesInArchive.put(name, count);
            } else {
                fileNamesInArchive.put(name, 1);
            }
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            zipOutput.putArchiveEntry(entry);
            /*
             * Transfer bytes from the file to the ZIP file
             */
            long size = 0;
            for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                zipOutput.write(buf, 0, read);
                size += read;
            }
            entry.setSize(size);
            /*
             * Complete the entry
             */
            zipOutput.closeArchiveEntry();
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(in);
        }
    }
}
