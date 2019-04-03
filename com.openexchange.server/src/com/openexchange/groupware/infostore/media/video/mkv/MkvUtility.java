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

package com.openexchange.groupware.infostore.media.video.mkv;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jcodec.containers.mkv.boxes.EbmlBase;
import org.jcodec.containers.mkv.boxes.EbmlBin;
import org.jcodec.containers.mkv.boxes.EbmlDate;
import org.jcodec.containers.mkv.boxes.EbmlFloat;
import org.jcodec.containers.mkv.boxes.EbmlMaster;
import org.jcodec.containers.mkv.boxes.EbmlSint;
import org.jcodec.containers.mkv.boxes.EbmlString;
import org.jcodec.containers.mkv.boxes.EbmlUint;
import org.jcodec.containers.mkv.boxes.EbmlUlong;
import org.jcodec.containers.mkv.boxes.EbmlVoid;

/**
 * {@link MkvUtility} - Utility class for MKV files.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MkvUtility {

    /**
     * Initializes a new {@link MkvUtility}.
     */
    private MkvUtility() {
        super();
    }

    /**
     * Prints specified EBML tree to given output stream.
     *
     * @param os The output stream to write to
     * @param tree The EBML tree to write
     * @throws IOException If an I/O error occurs while writing to the stream
     */
    public static void printParsedTree(OutputStream os, List<EbmlMaster> tree) throws IOException {
        for (EbmlMaster e : tree) {
            printTree(0, e, os);
        }
    }

    private static void printTree(int i, EbmlBase e, OutputStream os) throws IOException {
        os.write(printPaddedType(i, e).toString().getBytes(StandardCharsets.ISO_8859_1));
        os.write("\n".getBytes(StandardCharsets.ISO_8859_1));
        if (e instanceof EbmlMaster) {
            EbmlMaster parent = (EbmlMaster) e;
            for (EbmlBase child : parent.children) {
                printTree(i + 1, child, os);
            }
            os.write(printPaddedType(i, e).append(" CLOSED.").toString().getBytes(StandardCharsets.ISO_8859_1));
            os.write("\n".getBytes(StandardCharsets.ISO_8859_1));
        }
    }

    private static StringBuilder printPaddedType(int size, EbmlBase e) {
        StringBuilder sb = new StringBuilder();
        for (; size > 0; size--) {
            sb.append("    ");
        }
        sb.append(e.type);
        return sb;
    }

    /**
     * Converts given EBML tree to a map
     *
     * @param tree The EBML tree
     * @return The map
     */
    public static Map<String, Object> treeToMap(List<EbmlMaster> tree) {
        Map<String, Object> map = new LinkedHashMap<String, Object>(tree.size());
        for (EbmlMaster e : tree) {
            addElementToMap(e, map);
        }
        return map;
    }

    private static void addElementToMap(EbmlBase e, Map<String, Object> map) {
        String type = e.type.toString();
        if (e instanceof EbmlMaster) {
            EbmlMaster parent = (EbmlMaster) e;
            Map<String, Object> children = new LinkedHashMap<String, Object>(parent.children.size());
            map.put(type, children);
            for (EbmlBase child : parent.children) {
                addElementToMap(child, children);
            }
        } else {
            Map<String, Object> entry = new LinkedHashMap<>(4);
            String id = bytesToHex(e.type.id);
            entry.put("id", id);

            Class<? extends EbmlBase> clazz = e.type.clazz;
            if (clazz == EbmlFloat.class) {
                EbmlFloat x = (EbmlFloat) e;
                entry.put("value", String.valueOf(x.getDouble()));
            } else if (clazz == EbmlSint.class) {
                EbmlSint x = (EbmlSint) e;
                entry.put("value", String.valueOf(x.getLong()));
            } else if (clazz == EbmlDate.class) {
                EbmlDate x = (EbmlDate) e;
                entry.put("value", String.valueOf(x.getDate()));
            } else if (clazz == EbmlString.class) {
                EbmlString x = (EbmlString) e;
                entry.put("value", String.valueOf(x.getString()));
            } else if (clazz == EbmlString.class) {
                EbmlString x = (EbmlString) e;
                entry.put("value", String.valueOf(x.getString()));
            } else if (clazz == EbmlUint.class) {
                EbmlUint x = (EbmlUint) e;
                entry.put("value", String.valueOf(x.getUint()));
            } else if (clazz == EbmlUlong.class) {
                EbmlUlong x = (EbmlUlong) e;
                entry.put("value", String.valueOf(x.getUlong()));
            } else if (clazz == EbmlBin.class) {
                EbmlBin x = (EbmlBin) e;
                entry.put("value", toString(x.getData()));
            } else if (clazz == EbmlVoid.class) {
                entry.put("value", null);
            }

            entry.put("name", type);
            map.put(type, entry);
        }
    }

    private static String toString(ByteBuffer bb) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int limit = bb.limit();
        if (limit > 0) {
            sb.append(Integer.toHexString((bb.get() & 0xff))).append(' ');
            for (int i = limit - 1; i-- > 0;) {
                sb.append(' ').append(Integer.toHexString((bb.get() & 0xff)));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
