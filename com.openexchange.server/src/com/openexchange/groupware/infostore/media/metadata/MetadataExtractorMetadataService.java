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

package com.openexchange.groupware.infostore.media.metadata;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.metadata.MetadataExceptionCodes;
import com.openexchange.metadata.MetadataMap;
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
    public MetadataMap getMetadata(InputStream in, String optFileType) throws OXException {
        if (null == in) {
            throw MetadataExceptionCodes.MISSING_INPUT_STREAM.create();
        }

        BufferedInputStream bufferedStream = null;
        try {
            FileType detectedFileType;
            if (Strings.isEmpty(optFileType)) {
                // Ensure stream is not empty
                bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);
                bufferedStream.mark(65536);
                {
                    int read = bufferedStream.read();
                    if (read < 0) {
                        throw MetadataExceptionCodes.MISSING_INPUT_STREAM.create();
                    }
                    bufferedStream.reset();
                }

                // Auto-detect & check file type
                try {
                    bufferedStream.mark(65536);
                    detectedFileType = FileTypeDetector.detectFileType(bufferedStream);
                } catch (@SuppressWarnings("unused") AssertionError e) {
                    detectedFileType = FileType.Unknown;
                }
                if (FileType.Unknown == detectedFileType) {
                    return MetadataMap.EMPTY;
                }

                // Reset stream for re-use
                bufferedStream.reset();
            } else {
                // Assume proper file type already given. Look-up bxy identifier
                detectedFileType = id2FileType.get(Strings.toUpperCase(optFileType));
                if (null == detectedFileType) {
                    // Unknown file type
                    throw MetadataExceptionCodes.UNKNOWN_FILE_TYPE.create(optFileType);
                }

                // Initialize BufferedInputStream
                bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);
            }

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
