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

package com.openexchange.imagetransformation.java.exif;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.PrintStream;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.NamedNodeMap;
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
                    Entry entry = exifDirectory.getEntryById(I(TIFF.TAG_ORIENTATION));
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
        // dumpMetadata(metadata, System.out);
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

    private static void indent(int level, PrintStream out) {
        for (int i = 0; i < level; i++) {
            out.print("    ");
        }
    }

    private static void displayAttributes(NamedNodeMap attributes, PrintStream out) {
        if (attributes != null) {
            int count = attributes.getLength();
            for (int i = 0; i < count; i++) {
                Node attribute = attributes.item(i);

                out.print(" ");
                out.print(attribute.getNodeName());
                out.print("='");
                out.print(attribute.getNodeValue());
                out.print("'");
            }
        }
    }

    private static void displayMetadataNode(Node node, int level, PrintStream out) {
        indent(level, out);
        out.print("<");
        out.print(node.getNodeName());

        NamedNodeMap attributes = node.getAttributes();
        displayAttributes(attributes, out);

        Node child = node.getFirstChild();
        if (child == null) {
            String value = node.getNodeValue();
            if (value == null || value.length() == 0) {
                out.println("/>");
            } else {
                out.print(">");
                out.print(value);
                out.print("<");
                out.print(node.getNodeName());
                out.println(">");
            }
            return;
        }

        out.println(">");
        while (child != null) {
            displayMetadataNode(child, level + 1, out);
            child = child.getNextSibling();
        }

        indent(level, out);
        out.print("</");
        out.print(node.getNodeName());
        out.println(">");
    }

    /**
     * Dumps given meta-data to given output.
     *
     * @param metadata The meta-data to dump
     * @param out The print writer to write to
     */
    public static void dumpMetadata(IIOMetadata metadata, PrintStream out) {
        String[] names = metadata.getMetadataFormatNames();
        if (names != null) {
            int length = names.length;
            for (int i = 0; i < length; i++) {
                indent(2, out);
                out.println("Format name: " + names[i]);
                displayMetadataNode(metadata.getAsTree(names[i]), 3, out);
            }
        }
    }

    /**
     * Initializes a new {@link ExifTool}.
     */
    private ExifTool() {
        super();
    }

}
