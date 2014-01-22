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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicReference;
import net.thirdwing.exception.XHTMLConversionException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceLookup;

/**
 * {@link TempDirStreamProvider}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class TempDirStreamProvider {

    private final AtomicReference<File> tmpDirReference;

    /**
     * Initializes a new {@link TempDirStreamProvider}.
     */
    public TempDirStreamProvider(ServiceLookup services, String subfolder) {
        super();

        ConfigurationService cs = services.getService(ConfigurationService.class);
        String path = cs.getProperty("UPLOAD_DIRECTORY");

        File tmpDir = getTmpDirByPath(path);
        File folder = new File(tmpDir, subfolder);
        folder.mkdir();

        tmpDirReference = new AtomicReference<File>();
        tmpDirReference.set(folder);
    }

    public OutputStream createFile(String filename) throws XHTMLConversionException {
        File tmpFile;
        File directory = tmpDirReference.get();
        try {
            tmpFile = new File(directory, filename);
            tmpFile.deleteOnExit();
            return new FileOutputStream(tmpFile);
        } catch (final IOException e) {
            throw new XHTMLConversionException("Could not create OutputStream for file " + filename, e);
        }
    }

    private File getTmpDirByPath(final String path) {
        if (null == path) {
            throw new IllegalArgumentException("Path is null. Probably property \"UPLOAD_DIRECTORY\" is not set.");
        }
        final File tmpDir = new File(path);
        if (!tmpDir.exists()) {
            throw new IllegalArgumentException("Directory " + path + " does not exist.");
        }
        if (!tmpDir.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory.");
        }
        return tmpDir;
    }

    public String getDocumentContent() throws OXException {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(tmpDirReference.get(), "document.html")), "UTF-8"));
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
