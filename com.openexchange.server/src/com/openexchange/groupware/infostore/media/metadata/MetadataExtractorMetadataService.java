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

package com.openexchange.groupware.infostore.media.metadata;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.metadata.MetadataExceptionCodes;
import com.openexchange.metadata.MetadataService;

/**
 * {@link MetadataExtractorMetadataService} - The metadata service implementation backed by metadata-extractor.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MetadataExtractorMetadataService implements MetadataService {

    private final Map<String, FileType> id2FileType;

    /**
     * Initializes a new {@link MetadataExtractorMetadataService}.
     */
    public MetadataExtractorMetadataService() {
        super();
        FileType[] types = FileType.values();
        ImmutableMap.Builder<String, FileType> id2FileType = ImmutableMap.builderWithExpectedSize(types.length);
        for (FileType fileType : types) {
            if (FileType.Unknown != fileType) {
                id2FileType.put(fileType.getName(), fileType);
            }
        }
        this.id2FileType = id2FileType.build();
    }

    @Override
    public com.openexchange.metadata.MetadataMap getMetadata(InputStream in, String sFileType) throws OXException {
        if (null == in) {
            throw MetadataExceptionCodes.MISSING_INPUT_STREAM.create();
        }
        if (Strings.isEmpty(sFileType)) {
            throw MetadataExceptionCodes.MISSING_FILE_TYPE.create();
        }

        BufferedInputStream bufferedStream = null;
        try {
            FileType detectedFileType = id2FileType.get(Strings.toUpperCase(sFileType));
            if (null == detectedFileType) {
                // Unknown file type
                throw MetadataExceptionCodes.UNKNOWN_FILE_TYPE.create(sFileType);
            }

            bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);

            Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, -1, detectedFileType);

            MetadataMapImpl.Builder mediaMeta = MetadataMapImpl.builder(4);

            for (Directory directory : metadata.getDirectories()) {
                if (directory.isEmpty()) {
                    continue;
                }

                // Check for a known directory
                KnownDirectory knownDirectory = KnownDirectory.knownDirectoryFor(directory);

                // Get tag list & initialize appropriate map for current metadata directory
                MetadataUtility.putMediaMeta(null == knownDirectory ? directory.getName() : knownDirectory.name(), directory, mediaMeta);
            }

            return mediaMeta.build();
        } catch (com.drew.imaging.ImageProcessingException e) {
            throw MetadataExceptionCodes.METADATA_FAILED.create(e);
        } catch (IOException e) {
            throw MetadataExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(bufferedStream, in);
        }
    }

}
