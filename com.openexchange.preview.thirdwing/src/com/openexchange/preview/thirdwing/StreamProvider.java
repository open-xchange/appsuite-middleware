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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.preview.thirdwing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import net.thirdwing.exception.XHTMLConversionException;
import net.thirdwing.io.IStreamProvider;
import net.thirdwing.io.Stream;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link StreamProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class StreamProvider implements IStreamProvider {

    private String document;

    private String image;

    private final Map<String, ManagedFile> createdFiles = new HashMap<String, ManagedFile>();

    private final ServiceLookup serviceLookup;

    private final Session session;

    public StreamProvider(final ServiceLookup serviceLookup, Session session) {
        super();
        this.serviceLookup = serviceLookup;
        this.session = session;
    }

    @Override
    public Stream createFile(final String fileName) throws XHTMLConversionException {
        System.out.println("Create File: " + fileName);
        Stream stream = createInternal(fileName);
        return stream;
    }

    @Override
    public Stream createPreviewFile(String fileName) throws XHTMLConversionException {
        Stream stream = createInternal(fileName);
        this.image = fileName;
        return stream;
    }

    @Override
    public Stream createDocumentFile(String fileName) throws XHTMLConversionException {
        //System.out.println("Create document file: " + fileName);
        Stream stream = createInternal(fileName);
        this.document = fileName;
        return stream;
    }

    /**
     * @param fileName
     * @return
     * @throws XHTMLConversionException
     */
    private Stream createInternal(final String fileName) throws XHTMLConversionException {
        try {
            final String extension;
            final int lastIndex = fileName.lastIndexOf('.');
            if (lastIndex > 0) {
                extension = fileName.substring(lastIndex, fileName.length());
            } else {
                extension = fileName;
            }

            final ManagedFileManagement fileManagement = serviceLookup.getService(ManagedFileManagement.class);
            final File tempFile = fileManagement.newTempFile("open-xchange", extension);
            final FileOutputStream fos = new FileOutputStream(tempFile);
            final String mimeType = MimeType2ExtMap.getContentType(fileName);
            final ManagedFile managedFile = fileManagement.createManagedFile(tempFile);
            managedFile.setContentType(mimeType);
            managedFile.setFileName(fileName);
            managedFile.setContentDisposition("inline");
            createdFiles.put(fileName, managedFile);

            Stream retval = new Stream();
            retval.setStream(fos);
            retval.setUri(managedFile.constructURL(session));
            return retval;
        } catch (final OXException e) {
            throw new XHTMLConversionException("Could not create OutputStream for file " + fileName, e);
        } catch (final FileNotFoundException e) {
            throw new XHTMLConversionException("Could not create OutputStream for file " + fileName, e);
        }
    }

    public String getLinkForFile(final String fileName, final Session session) throws OXException {
        final ManagedFile managedFile = createdFiles.get(fileName);
        if (managedFile == null) {
            return null;
        }

        return managedFile.constructURL(session);
    }

    public InputStream getPreviewImage() throws OXException {
        try {
            return new FileInputStream(createdFiles.get(image).getFile());
        } catch (FileNotFoundException e) {
            throw PreviewExceptionCodes.ERROR.create();
        }
    }

    public String getDocumentContent() throws OXException {
        //System.out.println("Get document content: " + document);
        final ManagedFile managedFile = createdFiles.get(document);
        if (managedFile == null) {
            // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create("Missing File");
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(managedFile.getFile()), "UTF-8"));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } catch (final FileNotFoundException e) {
            // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } catch (final UnsupportedEncodingException e) {
            // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } catch (final IOException e) {
            // TODO: throw proper exception
            throw PreviewExceptionCodes.ERROR.create();
        } finally {
            Streams.close(reader);
        }
    }

}
