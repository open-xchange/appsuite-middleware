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

package com.openexchange.ajax.requesthandler.converters.cover;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.datatype.DataTypes;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link Mp3CoverExtractor}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Mp3CoverExtractor implements CoverExtractor {

    /**
     * Initializes a new {@link Mp3CoverExtractor}.
     */
    public Mp3CoverExtractor() {
        super();
    }

    @Override
    public boolean handlesFile(final IFileHolder file) {
        final String fileMIMEType = file.getContentType();
        if (null != fileMIMEType && isMp3(fileMIMEType.toLowerCase(Locale.ENGLISH))) {
            return true;
        }
        final String fileName = file.getName();
        if (null != fileName && fileName.toLowerCase(Locale.ENGLISH).endsWith(".mp3")) {
            return true;
        }
        return false;
    }

    @Override
    public IFileHolder extractCover(final IFileHolder file) throws OXException {
        final ManagedFileManagement fileManagement = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
        final ManagedFile managedFile = fileManagement.createManagedFile(file.getStream());
        try {
            final File tmpFile = managedFile.getFile();
            // Create MP3 file
            final MP3File mp3 = new MP3File(tmpFile, MP3File.LOAD_IDV2TAG, true);
            // Get appropriate cover tag
            final TagField imageField = mp3.getID3v2Tag().getFirstField(FieldKey.COVER_ART);
            if (imageField instanceof AbstractID3v2Frame) {
                final FrameBodyAPIC imageFrameBody = (FrameBodyAPIC) ((AbstractID3v2Frame) imageField).getBody();
                if (!imageFrameBody.isImageUrl()) {
                    final ByteArrayFileHolder coverFile = new ByteArrayFileHolder(
                        (byte[]) imageFrameBody.getObjectValue(DataTypes.OBJ_PICTURE_DATA));
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
            }
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final TagException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final ReadOnlyFileException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final InvalidAudioFrameException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            managedFile.delete();
        }
    }

    private static final Set<String> MIME_TYPES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
        "audio/mpeg",
        "audio/x-mpeg",
        "audio/mp3",
        "audio/x-mp3",
        "audio/mpeg3",
        "audio/x-mpeg3",
        "audio/mpg",
        "audio/x-mpg",
        "audio/x-mpegaudio")));

    private static boolean isMp3(final String mimeType) {
        if (null == mimeType) {
            return false;
        }
        return MIME_TYPES.contains(mimeType);
    }

}
