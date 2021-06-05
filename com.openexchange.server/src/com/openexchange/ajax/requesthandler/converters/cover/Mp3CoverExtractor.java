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

package com.openexchange.ajax.requesthandler.converters.cover;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyPIC;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4TagCoverField;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link Mp3CoverExtractor} - The {@link CoverExtractor} for MP3 files.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Mp3CoverExtractor implements CoverExtractor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Mp3CoverExtractor.class);

    /**
     * Initializes a new {@link Mp3CoverExtractor}.
     */
    public Mp3CoverExtractor() {
        super();
    }

    @Override
    public boolean handlesFile(final IFileHolder file) {
        final String fileMIMEType = file.getContentType();
        if (null != fileMIMEType && isSupported(fileMIMEType.toLowerCase(Locale.ENGLISH))) {
            return true;
        }
        if (isSupportedFileExt(file.getName())) {
            return true;
        }
        return false;
    }

    @Override
    public IFileHolder extractCover(final IFileHolder file) throws OXException {
        final ManagedFileManagement fileManagement = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
        String optExtension = null;
        {
            String name = file.getName();
            if (null != name) {
                final int pos = name.indexOf('.');
                if (pos > 0) {
                    optExtension = name.substring(pos);
                }
            }
        }
        final ManagedFile managedFile = fileManagement.createManagedFile(file.getStream(), optExtension);
        try {
            final File tmpFile = managedFile.getFile();
            // Check for MP3
            if (Mp3CoverExtractor.isMp3(managedFile.getContentType()) || Mp3CoverExtractor.isMp3FileExt(managedFile.getFileName())) {
                // Create MP3 file
                final MP3File mp3 = new MP3File(tmpFile, MP3File.LOAD_IDV2TAG, true);
                // Get appropriate cover tag
                final TagField imageField = mp3.getID3v2Tag().getFirstField(FieldKey.COVER_ART);
                if (null != imageField) {
                    if (imageField instanceof AbstractID3v2Frame) {
                        final AbstractTagFrameBody body = ((AbstractID3v2Frame) imageField).getBody();
                        if (body instanceof FrameBodyAPIC) {
                            final FrameBodyAPIC imageFrameBody = (FrameBodyAPIC) body;
                            if (!imageFrameBody.isImageUrl()) {
                                final ByteArrayFileHolder coverFile = new ByteArrayFileHolder((byte[]) imageFrameBody.getObjectValue(DataTypes.OBJ_PICTURE_DATA));
                                final String mimeType = (String) imageFrameBody.getObjectValue(DataTypes.OBJ_MIME_TYPE);
                                coverFile.setContentType(mimeType);
                                if (null != mimeType) {
                                    final List<String> extensions = MimeType2ExtMap.getFileExtensions(mimeType);
                                    coverFile.setName("cover." + extensions.get(0));
                                }
                                coverFile.setDelivery(file.getDelivery());
                                coverFile.setDisposition(file.getDisposition());
                                return coverFile;
                            }
                        } else if (body instanceof FrameBodyPIC) {
                            final FrameBodyPIC imageFrameBody = (FrameBodyPIC) body;
                            if (!imageFrameBody.isImageUrl()) {
                                final ByteArrayFileHolder coverFile = new ByteArrayFileHolder((byte[]) imageFrameBody.getObjectValue(DataTypes.OBJ_PICTURE_DATA));
                                final String mimeType = (String) imageFrameBody.getObjectValue(DataTypes.OBJ_MIME_TYPE);
                                coverFile.setContentType(mimeType);
                                if (null != mimeType) {
                                    final List<String> extensions = MimeType2ExtMap.getFileExtensions(mimeType);
                                    coverFile.setName("cover." + extensions.get(0));
                                }
                                coverFile.setDelivery(file.getDelivery());
                                coverFile.setDisposition(file.getDisposition());
                                return coverFile;
                            }
                        } else {
                            LOG.warn("Extracting cover image from MP3 failed. Unknown frame body class: {}", body.getClass().getName());
                        }
                    }
                }
                return null;
            } else if (isSupported(managedFile.getContentType()) || isSupportedFileExt(managedFile.getFileName())) {
                // Grab audio file
                final AudioFile f = AudioFileIO.read(tmpFile);
                final org.jaudiotagger.tag.Tag tag = f.getTag();
                if (tag instanceof Mp4Tag) {
                    final Mp4Tag mp4tag = (Mp4Tag) tag;
                    final List<TagField> coverarts = mp4tag.get(Mp4FieldKey.ARTWORK);
                    if (null != coverarts && !coverarts.isEmpty()) {
                        final Mp4TagCoverField coverArtField = (Mp4TagCoverField) coverarts.get(0);
                        final ByteArrayFileHolder coverFile = new ByteArrayFileHolder(coverArtField.getData());
                        final String mimeType = Mp4TagCoverField.getMimeTypeForImageType(coverArtField.getFieldType());
                        coverFile.setContentType(mimeType);
                        if (null != mimeType) {
                            final List<String> extensions = MimeType2ExtMap.getFileExtensions(mimeType);
                            coverFile.setName("cover." + extensions.get(0));
                        }
                        coverFile.setDelivery(file.getDelivery());
                        coverFile.setDisposition(file.getDisposition());
                        return coverFile;
                    }
                } else {
                    final Artwork artwork = tag.getFirstArtwork();
                    if (null != artwork) {
                        final ByteArrayFileHolder coverFile = new ByteArrayFileHolder(artwork.getBinaryData());
                        final String mimeType = artwork.getMimeType();
                        coverFile.setContentType(mimeType);
                        if (null != mimeType) {
                            final List<String> extensions = MimeType2ExtMap.getFileExtensions(mimeType);
                            coverFile.setName("cover." + extensions.get(0));
                        }
                        coverFile.setDelivery(file.getDelivery());
                        coverFile.setDisposition(file.getDisposition());
                        return coverFile;
                    }
                }
            }
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (TagException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (ReadOnlyFileException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (CannotReadException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (InvalidAudioFrameException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (NoClassDefFoundError e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            managedFile.delete();
        }
    }

    private static final Set<String> EXTENSIONS = ImmutableSet.of(".mp3", ".m4a", ".flac", ".wma", ".ogg");

    /**
     * Tests for a supported file extension.
     *
     * @param fileName The file name
     * @return <code>true</code> if supported; otherwise <code>false</code>
     */
    public static boolean isSupportedFileExt(final String fileName) {
        if (null != fileName) {
            final String lowerCase = fileName.toLowerCase(Locale.ENGLISH);
            for (final String extension : EXTENSIONS) {
                if (lowerCase.endsWith(extension)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests for a MP3 file extension.
     *
     * @param fileName The file name
     * @return <code>true</code> if MP3; otherwise <code>false</code>
     */
    public static boolean isMp3FileExt(final String fileName) {
        return (null != fileName && fileName.toLowerCase(Locale.ENGLISH).endsWith(".mp3"));
    }

    private static final Set<String> MP3_MIME_TYPES = ImmutableSet.of(
        "audio/mpeg",
        "audio/x-mpeg",
        "audio/mp3",
        "audio/x-mp3",
        "audio/mpeg3",
        "audio/x-mpeg3",
        "audio/mpg",
        "audio/x-mpg",
        "audio/x-mpegaudio");

    /**
     * Tests for MP3 MIME type.
     *
     * @param mimeType The MIME type
     * @return <code>true</code> if MP3; otherwise <code>false</code>
     */
    public static boolean isMp3(final String mimeType) {
        if (null == mimeType) {
            return false;
        }
        String mt = mimeType.trim();
        for (String mp3MimeType : MP3_MIME_TYPES) {
            if (mt.startsWith(mp3MimeType)) {
                return true;
            }
        }
        return false;
    }

    private static final Set<String> MIME_TYPES = ImmutableSet.of(
        "audio/mpeg",
        "audio/x-mpeg",
        "audio/mp3",
        "audio/x-mp3",
        "audio/mpeg3",
        "audio/x-mpeg3",
        "audio/mpg",
        "audio/x-mpg",
        "audio/x-mpegaudio",
        "audio/mp4",
        "audio/mp4a-latm",
        "audio/aac",
        "audio/aacp",
        "audio/x-flac",
        "audio/flac",
        "audio/x-ms-wma",
        "application/x-ogg");

    /**
     * Tests for a supported MIME type.
     *
     * @param mimeType The MIME type
     * @return <code>true</code> if supported; otherwise <code>false</code>
     */
    public static boolean isSupported(final String mimeType) {
        if (null == mimeType) {
            return false;
        }
        String mt = mimeType.trim();
        for (String supportedMimeType : MIME_TYPES) {
            if (mt.startsWith(supportedMimeType)) {
                return true;
            }
        }
        return false;
    }

}
