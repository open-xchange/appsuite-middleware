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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.imagetransformation.java.exif;

import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.twelvemonkeys.imageio.metadata.CompoundDirectory;
import com.twelvemonkeys.imageio.metadata.Entry;
import com.twelvemonkeys.imageio.metadata.exif.EXIFReader;
import com.twelvemonkeys.imageio.metadata.exif.TIFF;
import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;

/**
 * {@link ExifTool}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class ExifTool  {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExifTool.class);
    private static final byte[] EXIF_MARKER = "Exif".getBytes(Charsets.US_ASCII);

    /**
     * Tries to read out the orientation tag from the Exif data found in metadata provided by the supplied image reader.
     *
     * @param reader The image reader to use
     * @param imageIndex The image index to get the metadata for
     * @return The orientation value, or <code>null</code> if not found
     */
    public static Orientation readOrientation(ImageReader reader, int imageIndex) throws IOException {
        IIOMetadata metadata = reader.getImageMetadata(imageIndex);
        if (null != metadata) {
            byte[] exifData = getExifData(metadata);
            if (null != exifData && 6 < exifData.length) {
                ImageInputStream input = null;
                try {
                    input = new ByteArrayImageInputStream(exifData, 6, exifData.length - 6); // skip Exif\0_
                    CompoundDirectory exifDirectory = (CompoundDirectory) new EXIFReader().read(input);
                    Entry entry = exifDirectory.getEntryById(TIFF.TAG_ORIENTATION);
                    if (null != entry && Integer.class.isInstance(entry.getValue())) {
                        return Orientation.valueOf(((Integer) entry.getValue()).intValue());
                    }
                } finally {
                    Streams.close(input);
                }
            }
        }
        return null;
    }

    private static byte[] getExifData(IIOMetadata metadata) {
        IIOMetadataNode jpegRootNode;
        try {
            Node tree = metadata.getAsTree("javax_imageio_jpeg_image_1.0");
            if (null != tree && IIOMetadataNode.class.isInstance(tree)) {
                jpegRootNode = (IIOMetadataNode) tree;
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            LOG.debug("Error getting metadata", e);
            return null;
        }
        NodeList elements = jpegRootNode.getElementsByTagName("markerSequence");
        if (null != elements && 0 < elements.getLength()) {
            for (int i = 0; i < elements.getLength(); i++) {
                Node markerSequenceNode = elements.item(i);
                if (null != markerSequenceNode && null != markerSequenceNode.getChildNodes()) {
                    NodeList childNodes = markerSequenceNode.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node item = childNodes.item(j);
                        if (null != item && IIOMetadataNode.class.isInstance(item)) {
                            Object userObject = ((IIOMetadataNode) item).getUserObject();
                            if (null != userObject && byte[].class.isInstance(userObject)) {
                                byte[] data = (byte[]) userObject;
                                if (3 < data.length && EXIF_MARKER[0] == data[0] && EXIF_MARKER[1] == data[1] && EXIF_MARKER[2] == data[2] && EXIF_MARKER[3] == data[3]) {
                                    return data;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Initializes a new {@link ExifTool}.
     */
    private ExifTool() {
        super();
    }

}
