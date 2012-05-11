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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.thirdwing.common.ConversionJobfactory;
import net.thirdwing.common.IConversionJob;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewPolicy;
import com.openexchange.preview.Quality;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link ThirdwingPreviewService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ThirdwingPreviewService implements InternalPreviewService {

    private final ServiceLookup serviceLookup;

    private static final List<PreviewPolicy> POLICIES = new ArrayList<PreviewPolicy>();

    static {
        POLICIES.add(new PreviewPolicy("application/msword", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/msword", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.wordprocessingml.document", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.wordprocessingml.document", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.wordprocessingml.template", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.wordprocessingml.template", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-word.document.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-word.document.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-word.template.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-word.template.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.spreadsheetml.template", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.spreadsheetml.template", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.sheet.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.sheet.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.template.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.template.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.addin.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.addin.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.sheet.binary.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-excel.sheet.binary.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.presentationml.presentation", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.presentationml.presentation", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.presentationml.template", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.presentationml.template", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.presentationml.slideshow", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.openxmlformats-officedocument.presentationml.slideshow", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.addin.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.addin.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.presentation.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.presentation.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.template.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.template.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", PreviewOutput.IMAGE, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/pdf", PreviewOutput.HTML, Quality.GOOD));
        POLICIES.add(new PreviewPolicy("application/pdf", PreviewOutput.IMAGE, Quality.GOOD));
    }

    public ThirdwingPreviewService(final ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public String detectDocumentType(final InputStream inputStream) throws OXException {
        return null;
    }

    @Override
    public PreviewDocument getPreviewFor(final String arg, final PreviewOutput output, final Session session, int pages) throws OXException {
        File file = new File(arg);
        if (file.isFile()) {
            return generatePreview(file, session, pages);
        }
        file = null;
        try {
            final URL url = new URL(arg);
            final URLConnection connection = url.openConnection();
            final String path = url.getPath();
            final int slash = path.lastIndexOf('/');
            String name = ".tmp";
            if (slash + 1 < path.length()) { // works even with -1!
                name = path.substring(slash + 1);
            }
            file = streamToFile(new BufferedInputStream(connection.getInputStream()), name);
            return generatePreview(file, session, pages);
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Override
    public PreviewDocument getPreviewFor(final Data<InputStream> documentData, final PreviewOutput output, final Session session, int pages) throws OXException {
        File file = null;
        try {
            file = streamToFile(documentData.getData(), documentData.getDataProperties().get(DataProperties.PROPERTY_NAME));
            return generatePreview(file, session, pages);
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            if (file != null) {
                file.delete();
            }
        }
    }

    @Override
    public List<PreviewPolicy> getPreviewPolicies() {
        return POLICIES;
    }

    @Override
    public boolean canDetectContentType() {
        return false;
    }

    private PreviewDocument generatePreview(final File file, final Session session, int pages) throws OXException {
        final IConversionJob transformer = ConversionJobfactory.getTransformer(file);
        final StreamProvider streamProvider = new StreamProvider(serviceLookup, session);
        final TransformationObservationTask observationTask = new TransformationObservationTask(streamProvider, session, pages, transformer, file);

        final Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("content-type", "text/html");
        metaData.put("resourcename", "document.html");
        ThirdwingPreviewDocument previewDocument = new ThirdwingPreviewDocument(metaData, observationTask.call(), streamProvider.getPreviewImage(), observationTask.hasMoreContent());

        return previewDocument;
        
    }

    private File streamToFile(final InputStream is, final String name) throws OXException, IOException {
        FileOutputStream fos = null;
        try {
            final String extension;
            final int lastIndex = name.lastIndexOf('.');
            if (lastIndex > 0) {
                extension = name.substring(lastIndex, name.length());
            } else {
                extension = name;
            }

            final ManagedFileManagement fileManagement = serviceLookup.getService(ManagedFileManagement.class);
            final File file = fileManagement.newTempFile("open-xchange", extension);
            fos = new FileOutputStream(file);
            final byte[] buf = new byte[2048];
            for (int len; (len = is.read(buf, 0, 2048)) > 0;) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            return file;
        } finally {
            Streams.close(is);
            Streams.close(fos);
        }
    }

}
