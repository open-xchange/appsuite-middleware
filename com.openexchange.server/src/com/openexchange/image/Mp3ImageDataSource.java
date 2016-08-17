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

package com.openexchange.image;

import static com.openexchange.ajax.requesthandler.converters.cover.Mp3CoverExtractor.isMp3;
import static com.openexchange.ajax.requesthandler.converters.cover.Mp3CoverExtractor.isMp3FileExt;
import static com.openexchange.ajax.requesthandler.converters.cover.Mp3CoverExtractor.isSupported;
import static com.openexchange.ajax.requesthandler.converters.cover.Mp3CoverExtractor.isSupportedFileExt;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.AbstractDataType;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyPIC;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagCoverField;
import com.openexchange.ajax.container.TmpFileFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link Mp3ImageDataSource}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Mp3ImageDataSource implements ImageDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Mp3ImageDataSource.class);

    private static final Mp3ImageDataSource INSTANCE = new Mp3ImageDataSource();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static Mp3ImageDataSource getInstance() {
        return INSTANCE;
    }

    private static final String[] ARGS = { "com.openexchange.file.storage.folder", "com.openexchange.file.storage.id" };

    /**
     * Initializes a new {@link Mp3ImageDataSource}.
     */
    private Mp3ImageDataSource() {
        super();
    }

    @Override
    public <D> Data<D> getData(final Class<? extends D> type, final DataArguments dataArguments, final Session session) throws OXException {
        if (!InputStream.class.equals(type)) {
            throw DataExceptionCodes.TYPE_NOT_SUPPORTED.create(type.getName());
        }
        /*
         * Get arguments
         */
        final String folderId;
        {
            final String val = dataArguments.get(ARGS[0]);
            if (val == null) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[0]);
            }
            folderId = val.toString();
        }
        final String fileId;
        {
            final String val = dataArguments.get(ARGS[1]);
            if (val == null) {
                throw DataExceptionCodes.MISSING_ARGUMENT.create(ARGS[1]);
            }
            fileId = val.toString();
        }
        /*
         * Get MP3 image
         */
        String mimeType = null;
        byte[] imageBytes = null;
        {
            TmpFileFileHolder tfh = optData(fileId, folderId, ServerSessionAdapter.valueOf(session));
            try {
                //final File tmpFile = managedFile.getFile();
                // Check for MP3
                if (isMp3(tfh.getContentType()) || isMp3FileExt(tfh.getName())) {
                    // Create MP3 file
                    MP3File mp3 = new MP3File(tfh.getTmpFile(), MP3File.LOAD_IDV2TAG, true);

                    // Get appropriate cover tag
                    AbstractID3v2Tag id3v2Tag = mp3.getID3v2Tag();
                    if (null == id3v2Tag) {
                        LOG.warn("Extracting cover image from MP3 failed. Missing ID3v2 tag.");
                    } else {
                        TagField imageField = id3v2Tag.getFirstField(FieldKey.COVER_ART);
                        if (imageField instanceof AbstractID3v2Frame) {
                            AbstractTagFrameBody body = ((AbstractID3v2Frame) imageField).getBody();
                            if (body instanceof FrameBodyAPIC) {
                                FrameBodyAPIC imageFrameBody = (FrameBodyAPIC) body;
                                if (!imageFrameBody.isImageUrl()) {
                                    imageBytes = (byte[]) getObjectValue(DataTypes.OBJ_PICTURE_DATA, imageFrameBody);
                                    mimeType = (String) getObjectValue(DataTypes.OBJ_MIME_TYPE, imageFrameBody);
                                }
                            } else if (body instanceof FrameBodyPIC) {
                                FrameBodyPIC imageFrameBody = (FrameBodyPIC) body;
                                if (!imageFrameBody.isImageUrl()) {
                                    imageBytes = (byte[]) getObjectValue(DataTypes.OBJ_PICTURE_DATA, imageFrameBody);
                                    mimeType = (String) getObjectValue(DataTypes.OBJ_MIME_TYPE, imageFrameBody);
                                }
                            } else {
                                LOG.warn("Extracting cover image from MP3 failed. Unknown frame body class: {}", body.getClass().getName());
                            }
                        }
                    }
                } else if (isSupported(tfh.getContentType()) || isSupportedFileExt(tfh.getName())) {
                    // Grab audio file
                    AudioFile f = AudioFileIO.read(tfh.getTmpFile());
                    org.jaudiotagger.tag.Tag tag = f.getTag();
                    if (tag instanceof Mp4Tag) {
                        Mp4Tag mp4tag = (Mp4Tag) tag;
                        List<TagField> coverarts = mp4tag.get(Mp4FieldKey.ARTWORK);
                        if (null != coverarts && !coverarts.isEmpty()) {
                            final Mp4TagCoverField coverArtField = (Mp4TagCoverField) coverarts.get(0);
                            imageBytes = coverArtField.getData();
                            mimeType = Mp4TagCoverField.getMimeTypeForImageType(coverArtField.getFieldType());
                        }
                    } else {
                        Artwork artwork = tag.getFirstArtwork();
                        if (null != artwork) {
                            imageBytes = artwork.getBinaryData();
                            mimeType = artwork.getMimeType();
                        }
                    }
                }
            } catch (IOException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (TagException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ReadOnlyFileException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (CannotReadException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (InvalidAudioFrameException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(tfh);
            }
        }

        // Return
        if (imageBytes == null) {
            LOG.debug("Requested a non-existing image in MP3 file: file-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", fileId, folderId, session.getContextId(), session.getUserId());
            DataProperties properties = new DataProperties();
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        if (com.openexchange.ajax.helper.ImageUtils.isSvg(imageBytes)) {
            LOG.debug("Detected a possibly harmful SVG image in MP3 file: file-id={} folder={} context={} session-user={}. Returning an empty image as fallback.", fileId, folderId, session.getContextId(), session.getUserId());
            DataProperties properties = new DataProperties();
            properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
            properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(0));
            return new SimpleData<D>((D) (new UnsynchronizedByteArrayInputStream(new byte[0])), properties);
        }

        DataProperties properties = new DataProperties();
        properties.put(DataProperties.PROPERTY_CONTENT_TYPE, mimeType);
        properties.put(DataProperties.PROPERTY_SIZE, String.valueOf(imageBytes.length));
        if (null != mimeType) {
            final List<String> extensions = MimeType2ExtMap.getFileExtensions(mimeType);
            properties.put(DataProperties.PROPERTY_NAME, "image." + extensions.get(0));
        }
        return new SimpleData<D>((D) (Streams.newByteArrayInputStream(imageBytes)), properties);
    }

    private Object getObjectValue(final String identifier, final AbstractTagFrameBody imageFrameBody) {
        final AbstractDataType dataType = imageFrameBody.getObject(identifier);
        return null == dataType ? null : dataType.getValue();
    }

    @Override
    public String[] getRequiredArguments() {
        final String[] args = new String[ARGS.length];
        System.arraycopy(ARGS, 0, args, 0, ARGS.length);
        return args;
    }

    @Override
    public Class<?>[] getTypes() {
        return new Class<?>[] { InputStream.class };
    }

    private static final String REGISTRATION_NAME = "com.openexchange.file.storage.mp3Cover";

    @Override
    public String getRegistrationName() {
        return REGISTRATION_NAME;
    }

    private static final String ALIAS = "/file/mp3Cover";

    @Override
    public String getAlias() {
        return ALIAS;
    }

    @Override
    public ImageLocation parseUrl(final String url) {
        return ImageUtility.parseImageLocationFrom(url);
    }

    @Override
    public DataArguments generateDataArgumentsFrom(final ImageLocation imageLocation) {
        final DataArguments dataArguments = new DataArguments(2);
        dataArguments.put(ARGS[0], imageLocation.getFolder());
        dataArguments.put(ARGS[1], imageLocation.getId());
        return dataArguments;
    }

    @Override
    public String generateUrl(final ImageLocation imageLocation, final Session session) throws OXException {
        final StringBuilder sb = new StringBuilder(64);
        ImageUtility.startImageUrl(imageLocation, session, this, true, sb);
        final com.openexchange.file.storage.File file = optFile(imageLocation, ServerSessionAdapter.valueOf(session));
        if (null != file) {
            sb.append('&').append("timestamp=").append(file.getLastModified().getTime());
        }
        return sb.toString();
    }

    @Override
    public long getExpires() {
        return -1L;
    }

    @Override
    public String getETag(final ImageLocation imageLocation, final Session session) throws OXException {
        final char delim = '#';
        final StringBuilder builder = new StringBuilder(128);
        builder.append(delim).append(imageLocation.getFolder());
        final com.openexchange.file.storage.File file = optFile(imageLocation, ServerSessionAdapter.valueOf(session));
        if (null != file) {
            builder.append(delim).append(file.getLastModified().getTime());
        }
        builder.append(delim);
        return ImageUtility.getMD5(builder.toString(), "hex");
    }

    @Override
    public ImageLocation parseRequest(final AJAXRequestData requestData) {
        return ImageUtility.parseImageLocationFrom(requestData);
    }

    private static com.openexchange.file.storage.File optFile(final ImageLocation imageLocation, final ServerSession session) throws OXException {
        return optFile(imageLocation.getId(), imageLocation.getFolder(), session);
    }

    private static com.openexchange.file.storage.File optFile(final String fileId, final String folderId, final ServerSession session) throws OXException {
        if (!session.getUserPermissionBits().hasInfostore()) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(fileId, folderId);
        }
        final ServerServiceRegistry serviceRegistry = ServerServiceRegistry.getInstance();
        IDBasedFileAccess fileAccess = serviceRegistry.getService(IDBasedFileAccessFactory.class).createAccess(session);
        try {
            final com.openexchange.file.storage.File mp3File = fileAccess.getFileMetadata(fileId, FileStorageFileAccess.CURRENT_VERSION);
            // Check MIME type
            String fileMIMEType = Strings.asciiLowerCase(mp3File.getFileMIMEType());
            if (null != fileMIMEType) {
                if (!isSupported(fileMIMEType)) {
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("File \"" + mp3File.getFileName() + "\" [context " + session.getContextId() + ", id " + mp3File.getId() + "] is not a supported audio MIME type: " + fileMIMEType);
                }
            } else {
                String fileName = Strings.asciiLowerCase(mp3File.getFileName());
                if (null != fileName && !isSupportedFileExt(fileName)) {
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("File \"" + mp3File.getFileName() + "\" [context " + session.getContextId() + ", id " + mp3File.getId() + "] has no supported audio file extension.");
                }
            }
            return mp3File;
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            fileAccess.finish();
        }
    }

    private static TmpFileFileHolder optData(String fileId, String folderId, ServerSession session) throws OXException {
        if (!session.getUserPermissionBits().hasInfostore()) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(fileId, folderId);
        }
        IDBasedFileAccessFactory fileAccessFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class);
        IDBasedFileAccess fileAccess = fileAccessFactory.createAccess(session);
        TmpFileFileHolder tfh = null;
        boolean error = true;
        try {
            com.openexchange.file.storage.File audioFile = fileAccess.getFileMetadata(fileId, FileStorageFileAccess.CURRENT_VERSION);
            tfh = new TmpFileFileHolder();
            tfh.write(fileAccess.getDocument(fileId, FileStorageFileAccess.CURRENT_VERSION)); // Stream is closed in write()
            tfh.setContentType(audioFile.getFileMIMEType());
            tfh.setName(audioFile.getFileName());
            error = false;
            return tfh;
        } finally {
            fileAccess.finish();
            if (error) {
                Streams.close(tfh);
            }
        }
    }

}
