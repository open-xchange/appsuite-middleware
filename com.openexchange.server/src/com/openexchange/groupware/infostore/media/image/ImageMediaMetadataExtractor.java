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

package com.openexchange.groupware.infostore.media.image;

import static com.eaio.util.text.HumanTime.exactly;
import static com.openexchange.java.Autoboxing.I;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.bmp.BmpHeaderDirectory;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.gif.GifImageDirectory;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.media.Effort;
import com.openexchange.groupware.infostore.media.ExtractorResult;
import com.openexchange.groupware.infostore.media.InputStreamProvider;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractor;
import com.openexchange.groupware.infostore.media.MediaMetadataExtractors;
import com.openexchange.imagetransformation.ImageMetadataService;
import com.openexchange.java.GeoLocation;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ImageMediaMetadataExtractor} - The extractor for image files.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ImageMediaMetadataExtractor implements MediaMetadataExtractor {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImageMediaMetadataExtractor.class);

    private static enum KnownArgument {

        FILE_TYPE("image.fileType"),
        ;

        private final String id;

        private KnownArgument(String id) {
            this.id = id;
        }

        /**
         * Gets the argument's identifier
         *
         * @return The identifier
         */
        String getId() {
            return id;
        }
    }

    private static final ImageMediaMetadataExtractor INSTANCE = new ImageMediaMetadataExtractor();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ImageMediaMetadataExtractor getInstance() {
        return INSTANCE;
    }

    private static enum KnownDirectory {
        EXIF("exif", null), GPS("gps", GpsDirectory.class), IPTC("iptc", IptcDirectory.class);

        final String id;
        final Class<? extends Directory> optConcretetDirectoryType;

        private KnownDirectory(String id, Class<? extends Directory> directoryType) {
            this.id = id;
            this.optConcretetDirectoryType = directoryType;
        }

        String getId() {
            return id;
        }

        static KnownDirectory knownDirectoryFor(Directory directory) {
            if (null != directory) {
                String directoryName = Strings.asciiLowerCase(directory.getName());
                for (KnownDirectory knownDirectory : KnownDirectory.values()) {
                    Class<? extends Directory> concretetDirectoryType = knownDirectory.optConcretetDirectoryType;
                    if (null != concretetDirectoryType) {
                        // Check by type
                        if (concretetDirectoryType.equals(directory.getClass())) {
                            return knownDirectory;
                        }
                    } else {
                        // Check by name
                        if (directoryName.indexOf(knownDirectory.id) >= 0) {
                            return knownDirectory;
                        }
                    }
                }
            }
            return null;
        }
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final Set<FileType> fastTypes;
    private final boolean writeMediaMetadata;

    /**
     * Initializes a new {@link ImageMediaMetadataExtractor}.
     */
    private ImageMediaMetadataExtractor() {
        super();
        writeMediaMetadata = false;
        this.fastTypes = EnumSet.of(FileType.Jpeg, FileType.Psd, FileType.Eps, FileType.Bmp, FileType.Ico, FileType.Pcx, FileType.Heif);
    }

    private boolean indicatesImage(DocumentMetadata document) {
        // Only read images
        return (mimeTypeIndicatesImage(document.getFileMIMEType()) || mimeTypeIndicatesImage(getMimeTypeByFileName(document.getFileName())));
    }

    private boolean mimeTypeIndicatesImage(String mimeType) {
        // Starts with "image/"
        return (null != mimeType && Strings.asciiLowerCase(mimeType).startsWith("image/"));
    }

    private String getMimeTypeByFileName(String fileName) {
        return MimeType2ExtMap.getContentType(fileName, null);
    }

    @Override
    public boolean isApplicable(DocumentMetadata document) throws OXException {
        return indicatesImage(document);
    }

    @Override
    public Effort estimateEffort(InputStream in, DocumentMetadata document, Map<String, Object> optArguments) throws OXException {
        if (false == indicatesImage(document)) {
            return Effort.NOT_APPLICABLE;
        }

        BufferedInputStream bufferedInputStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 64);

        FileType detectedFileType;
        try {
            detectedFileType = FileTypeDetector.detectFileType(bufferedInputStream);
        } catch (IOException e) {
            throw InfostoreExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (@SuppressWarnings("unused") AssertionError e) {
            detectedFileType = FileType.Unknown;
        }

        if (FileType.Unknown == detectedFileType) {
            LOGGER.warn("Failed to estimate effort for extraction of image metadata from document {} with version {}. File type is unknown.", I(document.getId()), I(document.getVersion()));
            return Effort.NOT_APPLICABLE;
        }

        if (null != optArguments) {
            optArguments.put(KnownArgument.FILE_TYPE.getId(), detectedFileType);
        }

        Effort effort = fastTypes.contains(detectedFileType) ? Effort.LOW_EFFORT : Effort.HIGH_EFFORT;
        LOGGER.debug("Estimated {} effort for extracting {} image metadata from document {} ({}) with version {}", effort.getName(), detectedFileType.getName(), I(document.getId()), document.getFileName(), I(document.getVersion()));
        return effort;
    }

    @Override
    public ExtractorResult extractAndApplyMediaMetadata(InputStream optStream, InputStreamProvider provider, DocumentMetadata document, Map<String, Object> arguments) throws OXException {
        if (null == provider) {
            throw OXException.general("Stream must not be null.");
        }
        if (null == document) {
            throw OXException.general("Document must not be null.");
        }

        if (false == indicatesImage(document)) {
            return ExtractorResult.NONE;
        }

        InputStream in = optStream;
        BufferedInputStream bufferedStream = null;
        try {
            boolean debugEnabled = LOGGER.isDebugEnabled();
            long startNanos = debugEnabled ? System.nanoTime() : 0L;

            FileType detectedFileType = null == arguments ? null : (FileType) arguments.get(KnownArgument.FILE_TYPE.getId());
            if (null == detectedFileType) {
                in = null == in ? provider.getInputStream() : in;
                bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);

                try {
                    bufferedStream.mark(65536);
                    detectedFileType = FileTypeDetector.detectFileType(bufferedStream);
                } catch (IOException e) {
                    LOGGER.warn("Failed to extract image metadata from document {} with version {}", I(document.getId()), I(document.getVersion()), e);
                    return ExtractorResult.NONE;
                } catch (@SuppressWarnings("unused") AssertionError e) {
                    detectedFileType = FileType.Unknown;
                }
            }

            if (FileType.Unknown == detectedFileType) {
                LOGGER.warn("Failed to extract image metadata from document {} with version {}. File type is unknown.", I(document.getId()), I(document.getVersion()));
                return ExtractorResult.NONE;
            }

            if (debugEnabled) {
                LOGGER.debug("Going to extract {} image metadata from document {} ({}) with version {}", detectedFileType.getName(), I(document.getId()), document.getFileName(), I(document.getVersion()));
            }

            try {
                if (null == bufferedStream) {
                    in = null == in ? provider.getInputStream() : in;
                    bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);
                } else {
                    try {
                        bufferedStream.reset();
                    } catch (Exception e) {
                        // Reset failed
                        if (debugEnabled) {
                            LOGGER.debug("Failed to reset stream for extracting image metadata from document {} with version {}", I(document.getId()), I(document.getVersion()), e);
                        }
                        Streams.close(bufferedStream, in);
                        in = provider.getInputStream();
                        bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);
                    }
                }

                Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, -1, detectedFileType);

                String model = null;
                String make = null;

                Map<String, Object> mediaMeta = writeMediaMetadata ? new LinkedHashMap<String, Object>(4) : null;

                Thread currentThread = Thread.currentThread();
                for (Directory directory : metadata.getDirectories()) {
                    if (currentThread.isInterrupted()) {
                        return ExtractorResult.INTERRUPTED;
                    }

                    if (directory.isEmpty()) {
                        continue;
                    }

                    // Check for a known directory
                    KnownDirectory knownDirectory = KnownDirectory.knownDirectoryFor(directory);

                    // Check if media meta-data are supposed to be written
                    if (writeMediaMetadata) {
                        // Get tag list & initialize appropriate map for current metadata directory
                        boolean discardDirectory = putMediaMeta(null == knownDirectory ? directory.getName() : knownDirectory.name(), directory, mediaMeta);
                        if (discardDirectory) {
                            // Nothing put into media metadata for current metadata directory
                            continue;
                        }
                    }

                    if (null != knownDirectory) {
                        switch (knownDirectory) {
                            case EXIF:
                                if (document.getWidth() == null) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_EXIF_IMAGE_WIDTH);
                                    Long longObject = MediaMetadataExtractors.getLongValue(value);
                                    if (null != longObject) {
                                        document.setWidth(longObject.longValue());
                                    }
                                }
                                if (document.getHeight() == null) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_EXIF_IMAGE_HEIGHT);
                                    Long longObject = MediaMetadataExtractors.getLongValue(value);
                                    if (null != longObject) {
                                        document.setHeight(longObject.longValue());
                                    }
                                }
                                if (document.getWidth() == null) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_IMAGE_WIDTH);
                                    Long longObject = MediaMetadataExtractors.getLongValue(value);
                                    if (null != longObject) {
                                        document.setWidth(longObject.longValue());
                                    }
                                }
                                if (document.getHeight() == null) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_IMAGE_HEIGHT);
                                    Long longObject = MediaMetadataExtractors.getLongValue(value);
                                    if (null != longObject) {
                                        document.setHeight(longObject.longValue());
                                    }
                                }
                                if (document.getCameraIsoSpeed() == null) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_ISO_EQUIVALENT);
                                    Long longObject = MediaMetadataExtractors.getLongValue(value);
                                    if (null != longObject) {
                                        document.setCameraIsoSpeed(longObject.longValue());
                                    }
                                }
                                if (document.getCameraAperture() == null) {
                                    Double aperture = directory.getDoubleObject(ExifDirectoryBase.TAG_APERTURE);
                                    if (aperture != null) {
                                        document.setCameraAperture(aperture.doubleValue());
                                    }
                                }
                                if (document.getCameraExposureTime() == null) {
                                    Double exposureTime = directory.getDoubleObject(ExifDirectoryBase.TAG_SHUTTER_SPEED);
                                    if (exposureTime != null) {
                                        document.setCameraExposureTime(exposureTime.doubleValue());
                                    }
                                }
                                if (document.getCameraFocalLength() == null) {
                                    Double focalLength = directory.getDoubleObject(ExifDirectoryBase.TAG_FOCAL_LENGTH);
                                    if (focalLength != null) {
                                        document.setCameraFocalLength(focalLength.doubleValue());
                                    }
                                }
                                if (null == make) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_MAKE);
                                    if (null != value) {
                                        make = value.toString();
                                    }
                                }
                                if (null == model) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_MODEL);
                                    if (null != value) {
                                        model = value.toString();
                                    }
                                }
                                if (null == document.getCaptureDate()) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_DATETIME);
                                    if (null != value) {
                                        document.setCaptureDate(MediaMetadataExtractors.parseDateStringToDate(value.toString(), null));
                                    }
                                }
                                if (null == document.getCaptureDate()) {
                                    Object value = directory.getObject(ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
                                    if (null != value) {
                                        document.setCaptureDate(MediaMetadataExtractors.parseDateStringToDate(value.toString(), null));
                                    }
                                }
                                break;
                            case GPS:
                                {
                                    Double latitude = null;
                                    Double longitude = null;

                                    // Latitude
                                    String dms = directory.getDescription(GpsDirectory.TAG_LATITUDE);
                                    if (null != dms) {
                                        latitude = GeoLocation.parseDMSStringToDouble(dms);
                                    }

                                    // Longitude
                                    dms = directory.getDescription(GpsDirectory.TAG_LONGITUDE);
                                    if (null != dms) {
                                        longitude = GeoLocation.parseDMSStringToDouble(dms);
                                    }

                                    if (null != latitude && null != longitude) {
                                        document.setGeoLocation(new GeoLocation(latitude.doubleValue(), longitude.doubleValue()));
                                    }
                                }

                                break;
                            case IPTC:
                                if (null == document.getCaptureDate()) {
                                    String dateString = (String) directory.getObject(IptcDirectory.TAG_DATE_CREATED);
                                    String timeString = (String) directory.getObject(IptcDirectory.TAG_TIME_CREATED);
                                    document.setCaptureDate(MediaMetadataExtractors.parseDateStringToDate(dateString, timeString));
                                }
                                break;
                            default:
                                break;
                        }
                    } else {
                        switch (detectedFileType) {
                            case Png:
                                if (directory instanceof PngDirectory) {
                                    PngDirectory pngDirectory = (PngDirectory) directory;

                                    if (document.getWidth() == null) {
                                        Long longObject = pngDirectory.getLongObject(PngDirectory.TAG_IMAGE_WIDTH);
                                        if (null != longObject) {
                                            document.setWidth(longObject.longValue());
                                        }
                                    }
                                    if (document.getHeight() == null) {
                                        Long longObject = pngDirectory.getLongObject(PngDirectory.TAG_IMAGE_HEIGHT);
                                        if (null != longObject) {
                                            document.setHeight(longObject.longValue());
                                        }
                                    }
                                    if (null == document.getCaptureDate()) {
                                        Object object = pngDirectory.getObject(PngDirectory.TAG_LAST_MODIFICATION_TIME);
                                        if (null != object) {
                                            document.setCaptureDate(MediaMetadataExtractors.parseDateStringToDate(object.toString(), null));
                                        }
                                    }
                                }
                                break;
                            case Jpeg:
                                if (directory instanceof JpegDirectory) {
                                    JpegDirectory jpegDirectory = (JpegDirectory) directory;

                                    if (document.getWidth() == null) {
                                        Long longObject = jpegDirectory.getLongObject(JpegDirectory.TAG_IMAGE_WIDTH);
                                        if (null != longObject) {
                                            document.setWidth(longObject.longValue());
                                        }
                                    }
                                    if (document.getHeight() == null) {
                                        Long longObject = jpegDirectory.getLongObject(JpegDirectory.TAG_IMAGE_HEIGHT);
                                        if (null != longObject) {
                                            document.setHeight(longObject.longValue());
                                        }
                                    }
                                }
                                break;
                            case Heif:
                                if (directory instanceof HeifDirectory) {
                                    HeifDirectory heifDirectory = (HeifDirectory) directory;

                                    if (document.getWidth() == null) {
                                        Long longObject = heifDirectory.getLongObject(HeifDirectory.TAG_IMAGE_WIDTH);
                                        if (null != longObject) {
                                            document.setWidth(longObject.longValue());
                                        }
                                    }
                                    if (document.getHeight() == null) {
                                        Long longObject = heifDirectory.getLongObject(HeifDirectory.TAG_IMAGE_HEIGHT);
                                        if (null != longObject) {
                                            document.setHeight(longObject.longValue());
                                        }
                                    }
                                }
                                break;
                            case Bmp:
                                if (directory instanceof BmpHeaderDirectory) {
                                    BmpHeaderDirectory bmpDirectory = (BmpHeaderDirectory) directory;

                                    if (document.getWidth() == null) {
                                        Long longObject = bmpDirectory.getLongObject(BmpHeaderDirectory.TAG_IMAGE_WIDTH);
                                        if (null != longObject) {
                                            document.setWidth(longObject.longValue());
                                        }
                                    }
                                    if (document.getHeight() == null) {
                                        Long longObject = bmpDirectory.getLongObject(BmpHeaderDirectory.TAG_IMAGE_HEIGHT);
                                        if (null != longObject) {
                                            document.setHeight(longObject.longValue());
                                        }
                                    }
                                }
                                break;
                            case Gif:
                                if (directory instanceof GifImageDirectory) {
                                    GifImageDirectory gifDirectory = (GifImageDirectory) directory;

                                    if (document.getWidth() == null) {
                                        Long longObject = gifDirectory.getLongObject(GifImageDirectory.TAG_WIDTH);
                                        if (null != longObject) {
                                            document.setWidth(longObject.longValue());
                                        }
                                    }
                                    if (document.getHeight() == null) {
                                        Long longObject = gifDirectory.getLongObject(GifImageDirectory.TAG_HEIGHT);
                                        if (null != longObject) {
                                            document.setHeight(longObject.longValue());
                                        }
                                    }
                                }
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                }

                if (document.getWidth() == null || document.getHeight() == null) {
                    ImageMetadataService imageMetadataService = ServerServiceRegistry.getInstance().getService(ImageMetadataService.class);
                    if (null != imageMetadataService) {
                        // Reset buffered stream for re-use
                        try {
                            bufferedStream.reset();
                        } catch (Exception e) {
                            // Reset failed
                            if (debugEnabled) {
                                LOGGER.debug("Failed to reset stream for extracting image metadata from document {} with version {}", I(document.getId()), I(document.getVersion()), e);
                            }
                            Streams.close(bufferedStream, in);
                            in = provider.getInputStream();
                            bufferedStream = in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in, 65536);
                        }
                        try {
                            Dimension dimension = imageMetadataService.getDimensionFor(bufferedStream, null, null);
                            document.setWidth(dimension.width);
                            document.setHeight(dimension.height);
                        } catch (Exception e) {
                            // Failed to determine image height/width via ImageMetadataService
                            LOGGER.warn("Failed to determine image height/width via {} for document {} with version {}", ImageMetadataService.class.getName(), I(document.getId()), I(document.getVersion()), e);
                        }
                    }
                }

                if (null != make || null != model) {
                    String cameraModel;
                    if (null == make) {
                        cameraModel = null == model ? null : model.trim();
                    } else {
                        cameraModel = null == model ? make.trim() : new StringBuilder(make.trim()).append(' ').append(model.trim()).toString();
                    }

                    document.setCameraModel(cameraModel);
                }

                document.setMediaMeta(mediaMeta);

                if (debugEnabled) {
                    long durationMillis = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
                    String formattedDuration = exactly(durationMillis, true);
                    if (Strings.isEmpty(formattedDuration)) {
                        formattedDuration = durationMillis + "ms";
                    }
                    LOGGER.debug("Successfully extracted {} image metadata in {} from document {} ({}) with version {}", detectedFileType.getName(), formattedDuration, I(document.getId()), document.getFileName(), I(document.getVersion()));
                }
                return ExtractorResult.SUCCESSFUL;
            } catch (com.drew.imaging.ImageProcessingException e) {
                LOGGER.warn("Failed to extract {} image metadata from document {} ({}) with version {}", detectedFileType.getName(), I(document.getId()), document.getFileName(), I(document.getVersion()), e);
                return ExtractorResult.ACCEPTED_BUT_FAILED;
            } catch (Exception e) {
                LOGGER.warn("Failed to extract {} image metadata from document {} ({}) with version {}", detectedFileType.getName(), I(document.getId()), document.getFileName(), I(document.getVersion()), e);
                throw e instanceof OXException ? (OXException) e : OXException.general("Failed to extract image metadata from document " + document.getId() + " with version " + document.getVersion(), e);
            }
        } finally {
            Streams.close(bufferedStream, in);
        }
    }

    /**
     * Puts directory's tags into media metadata and signals if given directory can be discarded.
     *
     * @param mapIdentifier The map identifier to use
     * @param directory The directory to extract from
     * @param mediaMeta The media metadata to put the directory map to
     * @return <code>true</code> if given directory can be discarded; otherwise <code>false</code>
     */
    private boolean putMediaMeta(String mapIdentifier, Directory directory, Map<String, Object> mediaMeta) {
        Collection<Tag> tags = directory.getTags();

        boolean mapAlreadyExists = true;
        @SuppressWarnings("unchecked") Map<String, Map<String, Object>> directoryMap = (Map<String, Map<String, Object>>) mediaMeta.get(mapIdentifier);
        if (null == directoryMap) {
            // No such map, yet
            directoryMap = new LinkedHashMap<>(tags.size());
            mapAlreadyExists = false;
            mediaMeta.put(mapIdentifier, directoryMap);
        }

        Iterator<Tag> tagsIterator = tags.iterator();
        boolean somethingAdded = false;
        while (!somethingAdded && tagsIterator.hasNext()) {
            Tag tag = tagsIterator.next();
            somethingAdded |= putTagIntoDirectoryMapIfAbsent(tag, directory, directoryMap, mapAlreadyExists);
        }
        if (somethingAdded) {
            // Something put into media metadata for current directory
            while (tagsIterator.hasNext()) {
                Tag tag = tagsIterator.next();
                somethingAdded |= putTagIntoDirectoryMapIfAbsent(tag, directory, directoryMap, mapAlreadyExists);
            }
            return false;
        }

        // Nothing put into media metadata for current directory
        if (!mapAlreadyExists) {
            // Directory map was newly added, thus remove it as it is empty
            mediaMeta.remove(mapIdentifier);
            return true;
        }

        // Do not discard it as such a map already exists in media metadata
        return false;
    }

    private boolean putTagIntoDirectoryMapIfAbsent(Tag tag, Directory directory, Map<String, Map<String, Object>> directoryMap, boolean checkExistence) {
        int tagType = tag.getTagType();
        String name = tag.getTagName();

        String key = name == null ? Integer.toString(tagType) : name;
        if (key.startsWith("Unknown tag")) {
            // Discard unknown tags
            return false;
        }

        String description = tag.getDescription();
        if (null != description && description.startsWith("[") && description.endsWith(" values]")) {
            // Any non-categorized binary data; e.g. thumbnail or image areas
            return false;
        }

        if (false == checkExistence || false == directoryMap.containsKey(key)) {
            String value = getStringValue(tagType, directory);
            if (Strings.isNotEmpty(value)) {
                Map<String, Object> entry = new LinkedHashMap<>(4);

                entry.put("id", Integer.valueOf(tagType));

                if (null != name) {
                    entry.put("name", name);
                }

                entry.put("value", value);

                if (null != description) {
                    entry.put("description", description.trim());
                }

                directoryMap.put(key, entry);
                return true;
            }
        }

        // Nothing put into directory map
        return false;
    }

    private String getStringValue(int tagType, Directory directory) {
        Object value = directory.getObject(tagType);
        if (null == value) {
            return null;
        }
        return isArray(value) ? array2String(value) : value.toString().trim();
    }

    private int getMaxAllowByteArraysLength() {
        return 100;
    }

    private String array2String(Object array) {
        int length = Array.getLength(array);
        int iMax = length - 1;
        if (iMax < 0) {
            return "[]";
        }

        StringBuilder b = new StringBuilder(length << 2);
        b.append('[');
        for (int i = 0; ; i++) {
            Object obj = Array.get(array, i);
            if (isArray(obj)) {
                String sArray = array2String(obj);
                if (null != sArray) {
                    b.append(sArray);
                } else {
                    if (i == iMax) {
                        return b.append(']').toString();
                    }
                    continue;
                }
            } else {
                if (length > getMaxAllowByteArraysLength() && (obj instanceof Byte)) {
                    // Byte array is too big
                    return null;
                }
                b.append(String.valueOf(obj));
            }

            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }

    /**
     * Checks if specified object is an array.
     *
     * @param object The object to check
     * @return <code>true</code> if specified object is an array; otherwise <code>false</code>
     */
    private boolean isArray(final Object object) {
        /*-
         * getClass().isArray() is significantly slower on Sun Java 5 or 6 JRE than on IBM.
         * So much that using clazz.getName().charAt(0) == '[' is faster on Sun JVM.
         */
        // return (null != object && object.getClass().isArray());
        return (null != object && '[' == object.getClass().getName().charAt(0));
    }

}
