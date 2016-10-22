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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewDocument;


/**
 * {@link PreviewConst} - Constants.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PreviewConst {

    /**
     * Initializes a new {@link PreviewConst}.
     */
    private PreviewConst() {
        super();
    }

    /** The default thumbnail preview document */
    static final PreviewDocument DEFAULT_PREVIEW_DOCUMENT;

    static {
        final Map<String, String> metadata;
        {
            Map<String, String> m = new HashMap<String, String>(4);
            m.put("content-type", "image/jpeg");
            m.put("resourcename", "thumbs.jpg");
            metadata = Collections.unmodifiableMap(m);
        }

        PreviewDocument pd = new PreviewDocument() {

            @Override
            public Boolean isMoreAvailable() {
                return Boolean.FALSE;
            }

            @Override
            public boolean hasContent() {
                return false;
            }

            @Override
            public InputStream getThumbnail() {
                return Streams.newByteArrayInputStream(DEFAULT_THUMBNAIL);
            }

            @Override
            public Map<String, String> getMetaData() {
                return metadata;
            }

            @Override
            public List<String> getContent() {
                return null;
            }
        };
        DEFAULT_PREVIEW_DOCUMENT = pd;
    }

    /** Default thumbnail image */
    public static final byte[] DEFAULT_THUMBNAIL = new byte[] {
        (byte) 255, (byte) 216, (byte) 255, (byte) 224, (byte) 0, (byte) 16, (byte) 74, (byte) 70, (byte) 73, (byte) 70, (byte) 0,
        (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 72, (byte) 0, (byte) 72, (byte) 0, (byte) 0, (byte) 255, (byte) 226, (byte) 12,
        (byte) 236, (byte) 73, (byte) 67, (byte) 67, (byte) 95, (byte) 80, (byte) 82, (byte) 79, (byte) 70, (byte) 73, (byte) 76,
        (byte) 69, (byte) 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 12, (byte) 220, (byte) 97, (byte) 112, (byte) 112, (byte) 108,
        (byte) 2, (byte) 16, (byte) 0, (byte) 0, (byte) 109, (byte) 110, (byte) 116, (byte) 114, (byte) 82, (byte) 71, (byte) 66,
        (byte) 32, (byte) 88, (byte) 89, (byte) 90, (byte) 32, (byte) 7, (byte) 221, (byte) 0, (byte) 10, (byte) 0, (byte) 23, (byte) 0,
        (byte) 11, (byte) 0, (byte) 15, (byte) 0, (byte) 59, (byte) 97, (byte) 99, (byte) 115, (byte) 112, (byte) 65, (byte) 80, (byte) 80,
        (byte) 76, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 246, (byte) 214, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 211, (byte) 45, (byte) 97,
        (byte) 112, (byte) 112, (byte) 108, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 17,
        (byte) 100, (byte) 101, (byte) 115, (byte) 99, (byte) 0, (byte) 0, (byte) 1, (byte) 80, (byte) 0, (byte) 0, (byte) 0, (byte) 98,
        (byte) 100, (byte) 115, (byte) 99, (byte) 109, (byte) 0, (byte) 0, (byte) 1, (byte) 180, (byte) 0, (byte) 0, (byte) 1, (byte) 194,
        (byte) 99, (byte) 112, (byte) 114, (byte) 116, (byte) 0, (byte) 0, (byte) 3, (byte) 120, (byte) 0, (byte) 0, (byte) 0, (byte) 35,
        (byte) 119, (byte) 116, (byte) 112, (byte) 116, (byte) 0, (byte) 0, (byte) 3, (byte) 156, (byte) 0, (byte) 0, (byte) 0, (byte) 20,
        (byte) 114, (byte) 88, (byte) 89, (byte) 90, (byte) 0, (byte) 0, (byte) 3, (byte) 176, (byte) 0, (byte) 0, (byte) 0, (byte) 20,
        (byte) 103, (byte) 88, (byte) 89, (byte) 90, (byte) 0, (byte) 0, (byte) 3, (byte) 196, (byte) 0, (byte) 0, (byte) 0, (byte) 20,
        (byte) 98, (byte) 88, (byte) 89, (byte) 90, (byte) 0, (byte) 0, (byte) 3, (byte) 216, (byte) 0, (byte) 0, (byte) 0, (byte) 20,
        (byte) 114, (byte) 84, (byte) 82, (byte) 67, (byte) 0, (byte) 0, (byte) 3, (byte) 236, (byte) 0, (byte) 0, (byte) 8, (byte) 12,
        (byte) 97, (byte) 97, (byte) 114, (byte) 103, (byte) 0, (byte) 0, (byte) 11, (byte) 248, (byte) 0, (byte) 0, (byte) 0, (byte) 32,
        (byte) 118, (byte) 99, (byte) 103, (byte) 116, (byte) 0, (byte) 0, (byte) 12, (byte) 24, (byte) 0, (byte) 0, (byte) 0, (byte) 48,
        (byte) 110, (byte) 100, (byte) 105, (byte) 110, (byte) 0, (byte) 0, (byte) 12, (byte) 72, (byte) 0, (byte) 0, (byte) 0, (byte) 62,
        (byte) 99, (byte) 104, (byte) 97, (byte) 100, (byte) 0, (byte) 0, (byte) 12, (byte) 136, (byte) 0, (byte) 0, (byte) 0, (byte) 44,
        (byte) 109, (byte) 109, (byte) 111, (byte) 100, (byte) 0, (byte) 0, (byte) 12, (byte) 180, (byte) 0, (byte) 0, (byte) 0, (byte) 40,
        (byte) 98, (byte) 84, (byte) 82, (byte) 67, (byte) 0, (byte) 0, (byte) 3, (byte) 236, (byte) 0, (byte) 0, (byte) 8, (byte) 12,
        (byte) 103, (byte) 84, (byte) 82, (byte) 67, (byte) 0, (byte) 0, (byte) 3, (byte) 236, (byte) 0, (byte) 0, (byte) 8, (byte) 12,
        (byte) 97, (byte) 97, (byte) 98, (byte) 103, (byte) 0, (byte) 0, (byte) 11, (byte) 248, (byte) 0, (byte) 0, (byte) 0, (byte) 32,
        (byte) 97, (byte) 97, (byte) 103, (byte) 103, (byte) 0, (byte) 0, (byte) 11, (byte) 248, (byte) 0, (byte) 0, (byte) 0, (byte) 32,
        (byte) 100, (byte) 101, (byte) 115, (byte) 99, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 8,
        (byte) 68, (byte) 105, (byte) 115, (byte) 112, (byte) 108, (byte) 97, (byte) 121, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 109,
        (byte) 108, (byte) 117, (byte) 99, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 33, (byte) 0,
        (byte) 0, (byte) 0, (byte) 12, (byte) 104, (byte) 114, (byte) 72, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 107, (byte) 111, (byte) 75, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 110, (byte) 98, (byte) 78, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 105, (byte) 100, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 104, (byte) 117, (byte) 72, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 99, (byte) 115, (byte) 67, (byte) 90, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 100, (byte) 97, (byte) 68, (byte) 75, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 117, (byte) 107, (byte) 85, (byte) 65, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 97, (byte) 114, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 105, (byte) 116, (byte) 73, (byte) 84, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 114, (byte) 111, (byte) 82, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 110, (byte) 108, (byte) 78, (byte) 76, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 104, (byte) 101, (byte) 73, (byte) 76, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 101, (byte) 115, (byte) 69, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 102, (byte) 105, (byte) 70, (byte) 73, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 122, (byte) 104, (byte) 84, (byte) 87, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 118, (byte) 105, (byte) 86, (byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 115, (byte) 107, (byte) 83, (byte) 75, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 122, (byte) 104, (byte) 67, (byte) 78, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 114, (byte) 117, (byte) 82, (byte) 85, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 102, (byte) 114, (byte) 70, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 109, (byte) 115, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 99, (byte) 97, (byte) 69, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 116, (byte) 104, (byte) 84, (byte) 72, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 100, (byte) 101, (byte) 68, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 101, (byte) 110, (byte) 85, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 112, (byte) 116, (byte) 66, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 112, (byte) 108, (byte) 80, (byte) 76, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 101, (byte) 108, (byte) 71, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 115, (byte) 118, (byte) 83, (byte) 69, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 116, (byte) 114, (byte) 84, (byte) 82, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 106, (byte) 97, (byte) 74, (byte) 80, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 112, (byte) 116, (byte) 80, (byte) 84, (byte) 0, (byte) 0, (byte) 0, (byte) 38, (byte) 0,
        (byte) 0, (byte) 1, (byte) 156, (byte) 0, (byte) 84, (byte) 0, (byte) 104, (byte) 0, (byte) 117, (byte) 0, (byte) 110, (byte) 0,
        (byte) 100, (byte) 0, (byte) 101, (byte) 0, (byte) 114, (byte) 0, (byte) 98, (byte) 0, (byte) 111, (byte) 0, (byte) 108, (byte) 0,
        (byte) 116, (byte) 0, (byte) 32, (byte) 0, (byte) 68, (byte) 0, (byte) 105, (byte) 0, (byte) 115, (byte) 0, (byte) 112, (byte) 0,
        (byte) 108, (byte) 0, (byte) 97, (byte) 0, (byte) 121, (byte) 0, (byte) 0, (byte) 116, (byte) 101, (byte) 120, (byte) 116,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 67, (byte) 111, (byte) 112, (byte) 121, (byte) 114, (byte) 105, (byte) 103,
        (byte) 104, (byte) 116, (byte) 32, (byte) 65, (byte) 112, (byte) 112, (byte) 108, (byte) 101, (byte) 32, (byte) 73, (byte) 110,
        (byte) 99, (byte) 46, (byte) 44, (byte) 32, (byte) 50, (byte) 48, (byte) 49, (byte) 51, (byte) 0, (byte) 0, (byte) 88, (byte) 89,
        (byte) 90, (byte) 32, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 243, (byte) 22, (byte) 0, (byte) 1,
        (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 22, (byte) 202, (byte) 88, (byte) 89, (byte) 90, (byte) 32, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 113, (byte) 192, (byte) 0, (byte) 0, (byte) 57, (byte) 138, (byte) 0, (byte) 0,
        (byte) 1, (byte) 103, (byte) 88, (byte) 89, (byte) 90, (byte) 32, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 97, (byte) 35, (byte) 0, (byte) 0, (byte) 185, (byte) 230, (byte) 0, (byte) 0, (byte) 19, (byte) 246, (byte) 88, (byte) 89,
        (byte) 90, (byte) 32, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 35, (byte) 242, (byte) 0, (byte) 0,
        (byte) 12, (byte) 144, (byte) 0, (byte) 0, (byte) 189, (byte) 208, (byte) 99, (byte) 117, (byte) 114, (byte) 118, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 4, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 5, (byte) 0, (byte) 10,
        (byte) 0, (byte) 15, (byte) 0, (byte) 20, (byte) 0, (byte) 25, (byte) 0, (byte) 30, (byte) 0, (byte) 35, (byte) 0, (byte) 40,
        (byte) 0, (byte) 45, (byte) 0, (byte) 50, (byte) 0, (byte) 54, (byte) 0, (byte) 59, (byte) 0, (byte) 64, (byte) 0, (byte) 69,
        (byte) 0, (byte) 74, (byte) 0, (byte) 79, (byte) 0, (byte) 84, (byte) 0, (byte) 89, (byte) 0, (byte) 94, (byte) 0, (byte) 99,
        (byte) 0, (byte) 104, (byte) 0, (byte) 109, (byte) 0, (byte) 114, (byte) 0, (byte) 119, (byte) 0, (byte) 124, (byte) 0, (byte) 129,
        (byte) 0, (byte) 134, (byte) 0, (byte) 139, (byte) 0, (byte) 144, (byte) 0, (byte) 149, (byte) 0, (byte) 154, (byte) 0, (byte) 159,
        (byte) 0, (byte) 163, (byte) 0, (byte) 168, (byte) 0, (byte) 173, (byte) 0, (byte) 178, (byte) 0, (byte) 183, (byte) 0, (byte) 188,
        (byte) 0, (byte) 193, (byte) 0, (byte) 198, (byte) 0, (byte) 203, (byte) 0, (byte) 208, (byte) 0, (byte) 213, (byte) 0, (byte) 219,
        (byte) 0, (byte) 224, (byte) 0, (byte) 229, (byte) 0, (byte) 235, (byte) 0, (byte) 240, (byte) 0, (byte) 246, (byte) 0, (byte) 251,
        (byte) 1, (byte) 1, (byte) 1, (byte) 7, (byte) 1, (byte) 13, (byte) 1, (byte) 19, (byte) 1, (byte) 25, (byte) 1, (byte) 31,
        (byte) 1, (byte) 37, (byte) 1, (byte) 43, (byte) 1, (byte) 50, (byte) 1, (byte) 56, (byte) 1, (byte) 62, (byte) 1, (byte) 69,
        (byte) 1, (byte) 76, (byte) 1, (byte) 82, (byte) 1, (byte) 89, (byte) 1, (byte) 96, (byte) 1, (byte) 103, (byte) 1, (byte) 110,
        (byte) 1, (byte) 117, (byte) 1, (byte) 124, (byte) 1, (byte) 131, (byte) 1, (byte) 139, (byte) 1, (byte) 146, (byte) 1, (byte) 154,
        (byte) 1, (byte) 161, (byte) 1, (byte) 169, (byte) 1, (byte) 177, (byte) 1, (byte) 185, (byte) 1, (byte) 193, (byte) 1, (byte) 201,
        (byte) 1, (byte) 209, (byte) 1, (byte) 217, (byte) 1, (byte) 225, (byte) 1, (byte) 233, (byte) 1, (byte) 242, (byte) 1, (byte) 250,
        (byte) 2, (byte) 3, (byte) 2, (byte) 12, (byte) 2, (byte) 20, (byte) 2, (byte) 29, (byte) 2, (byte) 38, (byte) 2, (byte) 47,
        (byte) 2, (byte) 56, (byte) 2, (byte) 65, (byte) 2, (byte) 75, (byte) 2, (byte) 84, (byte) 2, (byte) 93, (byte) 2, (byte) 103,
        (byte) 2, (byte) 113, (byte) 2, (byte) 122, (byte) 2, (byte) 132, (byte) 2, (byte) 142, (byte) 2, (byte) 152, (byte) 2, (byte) 162,
        (byte) 2, (byte) 172, (byte) 2, (byte) 182, (byte) 2, (byte) 193, (byte) 2, (byte) 203, (byte) 2, (byte) 213, (byte) 2, (byte) 224,
        (byte) 2, (byte) 235, (byte) 2, (byte) 245, (byte) 3, (byte) 0, (byte) 3, (byte) 11, (byte) 3, (byte) 22, (byte) 3, (byte) 33,
        (byte) 3, (byte) 45, (byte) 3, (byte) 56, (byte) 3, (byte) 67, (byte) 3, (byte) 79, (byte) 3, (byte) 90, (byte) 3, (byte) 102,
        (byte) 3, (byte) 114, (byte) 3, (byte) 126, (byte) 3, (byte) 138, (byte) 3, (byte) 150, (byte) 3, (byte) 162, (byte) 3, (byte) 174,
        (byte) 3, (byte) 186, (byte) 3, (byte) 199, (byte) 3, (byte) 211, (byte) 3, (byte) 224, (byte) 3, (byte) 236, (byte) 3, (byte) 249,
        (byte) 4, (byte) 6, (byte) 4, (byte) 19, (byte) 4, (byte) 32, (byte) 4, (byte) 45, (byte) 4, (byte) 59, (byte) 4, (byte) 72,
        (byte) 4, (byte) 85, (byte) 4, (byte) 99, (byte) 4, (byte) 113, (byte) 4, (byte) 126, (byte) 4, (byte) 140, (byte) 4, (byte) 154,
        (byte) 4, (byte) 168, (byte) 4, (byte) 182, (byte) 4, (byte) 196, (byte) 4, (byte) 211, (byte) 4, (byte) 225, (byte) 4, (byte) 240,
        (byte) 4, (byte) 254, (byte) 5, (byte) 13, (byte) 5, (byte) 28, (byte) 5, (byte) 43, (byte) 5, (byte) 58, (byte) 5, (byte) 73,
        (byte) 5, (byte) 88, (byte) 5, (byte) 103, (byte) 5, (byte) 119, (byte) 5, (byte) 134, (byte) 5, (byte) 150, (byte) 5, (byte) 166,
        (byte) 5, (byte) 181, (byte) 5, (byte) 197, (byte) 5, (byte) 213, (byte) 5, (byte) 229, (byte) 5, (byte) 246, (byte) 6, (byte) 6,
        (byte) 6, (byte) 22, (byte) 6, (byte) 39, (byte) 6, (byte) 55, (byte) 6, (byte) 72, (byte) 6, (byte) 89, (byte) 6, (byte) 106,
        (byte) 6, (byte) 123, (byte) 6, (byte) 140, (byte) 6, (byte) 157, (byte) 6, (byte) 175, (byte) 6, (byte) 192, (byte) 6, (byte) 209,
        (byte) 6, (byte) 227, (byte) 6, (byte) 245, (byte) 7, (byte) 7, (byte) 7, (byte) 25, (byte) 7, (byte) 43, (byte) 7, (byte) 61,
        (byte) 7, (byte) 79, (byte) 7, (byte) 97, (byte) 7, (byte) 116, (byte) 7, (byte) 134, (byte) 7, (byte) 153, (byte) 7, (byte) 172,
        (byte) 7, (byte) 191, (byte) 7, (byte) 210, (byte) 7, (byte) 229, (byte) 7, (byte) 248, (byte) 8, (byte) 11, (byte) 8, (byte) 31,
        (byte) 8, (byte) 50, (byte) 8, (byte) 70, (byte) 8, (byte) 90, (byte) 8, (byte) 110, (byte) 8, (byte) 130, (byte) 8, (byte) 150,
        (byte) 8, (byte) 170, (byte) 8, (byte) 190, (byte) 8, (byte) 210, (byte) 8, (byte) 231, (byte) 8, (byte) 251, (byte) 9, (byte) 16,
        (byte) 9, (byte) 37, (byte) 9, (byte) 58, (byte) 9, (byte) 79, (byte) 9, (byte) 100, (byte) 9, (byte) 121, (byte) 9, (byte) 143,
        (byte) 9, (byte) 164, (byte) 9, (byte) 186, (byte) 9, (byte) 207, (byte) 9, (byte) 229, (byte) 9, (byte) 251, (byte) 10, (byte) 17,
        (byte) 10, (byte) 39, (byte) 10, (byte) 61, (byte) 10, (byte) 84, (byte) 10, (byte) 106, (byte) 10, (byte) 129, (byte) 10,
        (byte) 152, (byte) 10, (byte) 174, (byte) 10, (byte) 197, (byte) 10, (byte) 220, (byte) 10, (byte) 243, (byte) 11, (byte) 11,
        (byte) 11, (byte) 34, (byte) 11, (byte) 57, (byte) 11, (byte) 81, (byte) 11, (byte) 105, (byte) 11, (byte) 128, (byte) 11,
        (byte) 152, (byte) 11, (byte) 176, (byte) 11, (byte) 200, (byte) 11, (byte) 225, (byte) 11, (byte) 249, (byte) 12, (byte) 18,
        (byte) 12, (byte) 42, (byte) 12, (byte) 67, (byte) 12, (byte) 92, (byte) 12, (byte) 117, (byte) 12, (byte) 142, (byte) 12,
        (byte) 167, (byte) 12, (byte) 192, (byte) 12, (byte) 217, (byte) 12, (byte) 243, (byte) 13, (byte) 13, (byte) 13, (byte) 38,
        (byte) 13, (byte) 64, (byte) 13, (byte) 90, (byte) 13, (byte) 116, (byte) 13, (byte) 142, (byte) 13, (byte) 169, (byte) 13,
        (byte) 195, (byte) 13, (byte) 222, (byte) 13, (byte) 248, (byte) 14, (byte) 19, (byte) 14, (byte) 46, (byte) 14, (byte) 73,
        (byte) 14, (byte) 100, (byte) 14, (byte) 127, (byte) 14, (byte) 155, (byte) 14, (byte) 182, (byte) 14, (byte) 210, (byte) 14,
        (byte) 238, (byte) 15, (byte) 9, (byte) 15, (byte) 37, (byte) 15, (byte) 65, (byte) 15, (byte) 94, (byte) 15, (byte) 122,
        (byte) 15, (byte) 150, (byte) 15, (byte) 179, (byte) 15, (byte) 207, (byte) 15, (byte) 236, (byte) 16, (byte) 9, (byte) 16,
        (byte) 38, (byte) 16, (byte) 67, (byte) 16, (byte) 97, (byte) 16, (byte) 126, (byte) 16, (byte) 155, (byte) 16, (byte) 185,
        (byte) 16, (byte) 215, (byte) 16, (byte) 245, (byte) 17, (byte) 19, (byte) 17, (byte) 49, (byte) 17, (byte) 79, (byte) 17,
        (byte) 109, (byte) 17, (byte) 140, (byte) 17, (byte) 170, (byte) 17, (byte) 201, (byte) 17, (byte) 232, (byte) 18, (byte) 7,
        (byte) 18, (byte) 38, (byte) 18, (byte) 69, (byte) 18, (byte) 100, (byte) 18, (byte) 132, (byte) 18, (byte) 163, (byte) 18,
        (byte) 195, (byte) 18, (byte) 227, (byte) 19, (byte) 3, (byte) 19, (byte) 35, (byte) 19, (byte) 67, (byte) 19, (byte) 99,
        (byte) 19, (byte) 131, (byte) 19, (byte) 164, (byte) 19, (byte) 197, (byte) 19, (byte) 229, (byte) 20, (byte) 6, (byte) 20,
        (byte) 39, (byte) 20, (byte) 73, (byte) 20, (byte) 106, (byte) 20, (byte) 139, (byte) 20, (byte) 173, (byte) 20, (byte) 206,
        (byte) 20, (byte) 240, (byte) 21, (byte) 18, (byte) 21, (byte) 52, (byte) 21, (byte) 86, (byte) 21, (byte) 120, (byte) 21,
        (byte) 155, (byte) 21, (byte) 189, (byte) 21, (byte) 224, (byte) 22, (byte) 3, (byte) 22, (byte) 38, (byte) 22, (byte) 73,
        (byte) 22, (byte) 108, (byte) 22, (byte) 143, (byte) 22, (byte) 178, (byte) 22, (byte) 214, (byte) 22, (byte) 250, (byte) 23,
        (byte) 29, (byte) 23, (byte) 65, (byte) 23, (byte) 101, (byte) 23, (byte) 137, (byte) 23, (byte) 174, (byte) 23, (byte) 210,
        (byte) 23, (byte) 247, (byte) 24, (byte) 27, (byte) 24, (byte) 64, (byte) 24, (byte) 101, (byte) 24, (byte) 138, (byte) 24,
        (byte) 175, (byte) 24, (byte) 213, (byte) 24, (byte) 250, (byte) 25, (byte) 32, (byte) 25, (byte) 69, (byte) 25, (byte) 107,
        (byte) 25, (byte) 145, (byte) 25, (byte) 183, (byte) 25, (byte) 221, (byte) 26, (byte) 4, (byte) 26, (byte) 42, (byte) 26,
        (byte) 81, (byte) 26, (byte) 119, (byte) 26, (byte) 158, (byte) 26, (byte) 197, (byte) 26, (byte) 236, (byte) 27, (byte) 20,
        (byte) 27, (byte) 59, (byte) 27, (byte) 99, (byte) 27, (byte) 138, (byte) 27, (byte) 178, (byte) 27, (byte) 218, (byte) 28,
        (byte) 2, (byte) 28, (byte) 42, (byte) 28, (byte) 82, (byte) 28, (byte) 123, (byte) 28, (byte) 163, (byte) 28, (byte) 204,
        (byte) 28, (byte) 245, (byte) 29, (byte) 30, (byte) 29, (byte) 71, (byte) 29, (byte) 112, (byte) 29, (byte) 153, (byte) 29,
        (byte) 195, (byte) 29, (byte) 236, (byte) 30, (byte) 22, (byte) 30, (byte) 64, (byte) 30, (byte) 106, (byte) 30, (byte) 148,
        (byte) 30, (byte) 190, (byte) 30, (byte) 233, (byte) 31, (byte) 19, (byte) 31, (byte) 62, (byte) 31, (byte) 105, (byte) 31,
        (byte) 148, (byte) 31, (byte) 191, (byte) 31, (byte) 234, (byte) 32, (byte) 21, (byte) 32, (byte) 65, (byte) 32, (byte) 108,
        (byte) 32, (byte) 152, (byte) 32, (byte) 196, (byte) 32, (byte) 240, (byte) 33, (byte) 28, (byte) 33, (byte) 72, (byte) 33,
        (byte) 117, (byte) 33, (byte) 161, (byte) 33, (byte) 206, (byte) 33, (byte) 251, (byte) 34, (byte) 39, (byte) 34, (byte) 85,
        (byte) 34, (byte) 130, (byte) 34, (byte) 175, (byte) 34, (byte) 221, (byte) 35, (byte) 10, (byte) 35, (byte) 56, (byte) 35,
        (byte) 102, (byte) 35, (byte) 148, (byte) 35, (byte) 194, (byte) 35, (byte) 240, (byte) 36, (byte) 31, (byte) 36, (byte) 77,
        (byte) 36, (byte) 124, (byte) 36, (byte) 171, (byte) 36, (byte) 218, (byte) 37, (byte) 9, (byte) 37, (byte) 56, (byte) 37,
        (byte) 104, (byte) 37, (byte) 151, (byte) 37, (byte) 199, (byte) 37, (byte) 247, (byte) 38, (byte) 39, (byte) 38, (byte) 87,
        (byte) 38, (byte) 135, (byte) 38, (byte) 183, (byte) 38, (byte) 232, (byte) 39, (byte) 24, (byte) 39, (byte) 73, (byte) 39,
        (byte) 122, (byte) 39, (byte) 171, (byte) 39, (byte) 220, (byte) 40, (byte) 13, (byte) 40, (byte) 63, (byte) 40, (byte) 113,
        (byte) 40, (byte) 162, (byte) 40, (byte) 212, (byte) 41, (byte) 6, (byte) 41, (byte) 56, (byte) 41, (byte) 107, (byte) 41,
        (byte) 157, (byte) 41, (byte) 208, (byte) 42, (byte) 2, (byte) 42, (byte) 53, (byte) 42, (byte) 104, (byte) 42, (byte) 155,
        (byte) 42, (byte) 207, (byte) 43, (byte) 2, (byte) 43, (byte) 54, (byte) 43, (byte) 105, (byte) 43, (byte) 157, (byte) 43,
        (byte) 209, (byte) 44, (byte) 5, (byte) 44, (byte) 57, (byte) 44, (byte) 110, (byte) 44, (byte) 162, (byte) 44, (byte) 215,
        (byte) 45, (byte) 12, (byte) 45, (byte) 65, (byte) 45, (byte) 118, (byte) 45, (byte) 171, (byte) 45, (byte) 225, (byte) 46,
        (byte) 22, (byte) 46, (byte) 76, (byte) 46, (byte) 130, (byte) 46, (byte) 183, (byte) 46, (byte) 238, (byte) 47, (byte) 36,
        (byte) 47, (byte) 90, (byte) 47, (byte) 145, (byte) 47, (byte) 199, (byte) 47, (byte) 254, (byte) 48, (byte) 53, (byte) 48,
        (byte) 108, (byte) 48, (byte) 164, (byte) 48, (byte) 219, (byte) 49, (byte) 18, (byte) 49, (byte) 74, (byte) 49, (byte) 130,
        (byte) 49, (byte) 186, (byte) 49, (byte) 242, (byte) 50, (byte) 42, (byte) 50, (byte) 99, (byte) 50, (byte) 155, (byte) 50,
        (byte) 212, (byte) 51, (byte) 13, (byte) 51, (byte) 70, (byte) 51, (byte) 127, (byte) 51, (byte) 184, (byte) 51, (byte) 241,
        (byte) 52, (byte) 43, (byte) 52, (byte) 101, (byte) 52, (byte) 158, (byte) 52, (byte) 216, (byte) 53, (byte) 19, (byte) 53,
        (byte) 77, (byte) 53, (byte) 135, (byte) 53, (byte) 194, (byte) 53, (byte) 253, (byte) 54, (byte) 55, (byte) 54, (byte) 114,
        (byte) 54, (byte) 174, (byte) 54, (byte) 233, (byte) 55, (byte) 36, (byte) 55, (byte) 96, (byte) 55, (byte) 156, (byte) 55,
        (byte) 215, (byte) 56, (byte) 20, (byte) 56, (byte) 80, (byte) 56, (byte) 140, (byte) 56, (byte) 200, (byte) 57, (byte) 5,
        (byte) 57, (byte) 66, (byte) 57, (byte) 127, (byte) 57, (byte) 188, (byte) 57, (byte) 249, (byte) 58, (byte) 54, (byte) 58,
        (byte) 116, (byte) 58, (byte) 178, (byte) 58, (byte) 239, (byte) 59, (byte) 45, (byte) 59, (byte) 107, (byte) 59, (byte) 170,
        (byte) 59, (byte) 232, (byte) 60, (byte) 39, (byte) 60, (byte) 101, (byte) 60, (byte) 164, (byte) 60, (byte) 227, (byte) 61,
        (byte) 34, (byte) 61, (byte) 97, (byte) 61, (byte) 161, (byte) 61, (byte) 224, (byte) 62, (byte) 32, (byte) 62, (byte) 96,
        (byte) 62, (byte) 160, (byte) 62, (byte) 224, (byte) 63, (byte) 33, (byte) 63, (byte) 97, (byte) 63, (byte) 162, (byte) 63,
        (byte) 226, (byte) 64, (byte) 35, (byte) 64, (byte) 100, (byte) 64, (byte) 166, (byte) 64, (byte) 231, (byte) 65, (byte) 41,
        (byte) 65, (byte) 106, (byte) 65, (byte) 172, (byte) 65, (byte) 238, (byte) 66, (byte) 48, (byte) 66, (byte) 114, (byte) 66,
        (byte) 181, (byte) 66, (byte) 247, (byte) 67, (byte) 58, (byte) 67, (byte) 125, (byte) 67, (byte) 192, (byte) 68, (byte) 3,
        (byte) 68, (byte) 71, (byte) 68, (byte) 138, (byte) 68, (byte) 206, (byte) 69, (byte) 18, (byte) 69, (byte) 85, (byte) 69,
        (byte) 154, (byte) 69, (byte) 222, (byte) 70, (byte) 34, (byte) 70, (byte) 103, (byte) 70, (byte) 171, (byte) 70, (byte) 240,
        (byte) 71, (byte) 53, (byte) 71, (byte) 123, (byte) 71, (byte) 192, (byte) 72, (byte) 5, (byte) 72, (byte) 75, (byte) 72,
        (byte) 145, (byte) 72, (byte) 215, (byte) 73, (byte) 29, (byte) 73, (byte) 99, (byte) 73, (byte) 169, (byte) 73, (byte) 240,
        (byte) 74, (byte) 55, (byte) 74, (byte) 125, (byte) 74, (byte) 196, (byte) 75, (byte) 12, (byte) 75, (byte) 83, (byte) 75,
        (byte) 154, (byte) 75, (byte) 226, (byte) 76, (byte) 42, (byte) 76, (byte) 114, (byte) 76, (byte) 186, (byte) 77, (byte) 2,
        (byte) 77, (byte) 74, (byte) 77, (byte) 147, (byte) 77, (byte) 220, (byte) 78, (byte) 37, (byte) 78, (byte) 110, (byte) 78,
        (byte) 183, (byte) 79, (byte) 0, (byte) 79, (byte) 73, (byte) 79, (byte) 147, (byte) 79, (byte) 221, (byte) 80, (byte) 39,
        (byte) 80, (byte) 113, (byte) 80, (byte) 187, (byte) 81, (byte) 6, (byte) 81, (byte) 80, (byte) 81, (byte) 155, (byte) 81,
        (byte) 230, (byte) 82, (byte) 49, (byte) 82, (byte) 124, (byte) 82, (byte) 199, (byte) 83, (byte) 19, (byte) 83, (byte) 95,
        (byte) 83, (byte) 170, (byte) 83, (byte) 246, (byte) 84, (byte) 66, (byte) 84, (byte) 143, (byte) 84, (byte) 219, (byte) 85,
        (byte) 40, (byte) 85, (byte) 117, (byte) 85, (byte) 194, (byte) 86, (byte) 15, (byte) 86, (byte) 92, (byte) 86, (byte) 169,
        (byte) 86, (byte) 247, (byte) 87, (byte) 68, (byte) 87, (byte) 146, (byte) 87, (byte) 224, (byte) 88, (byte) 47, (byte) 88,
        (byte) 125, (byte) 88, (byte) 203, (byte) 89, (byte) 26, (byte) 89, (byte) 105, (byte) 89, (byte) 184, (byte) 90, (byte) 7,
        (byte) 90, (byte) 86, (byte) 90, (byte) 166, (byte) 90, (byte) 245, (byte) 91, (byte) 69, (byte) 91, (byte) 149, (byte) 91,
        (byte) 229, (byte) 92, (byte) 53, (byte) 92, (byte) 134, (byte) 92, (byte) 214, (byte) 93, (byte) 39, (byte) 93, (byte) 120,
        (byte) 93, (byte) 201, (byte) 94, (byte) 26, (byte) 94, (byte) 108, (byte) 94, (byte) 189, (byte) 95, (byte) 15, (byte) 95,
        (byte) 97, (byte) 95, (byte) 179, (byte) 96, (byte) 5, (byte) 96, (byte) 87, (byte) 96, (byte) 170, (byte) 96, (byte) 252,
        (byte) 97, (byte) 79, (byte) 97, (byte) 162, (byte) 97, (byte) 245, (byte) 98, (byte) 73, (byte) 98, (byte) 156, (byte) 98,
        (byte) 240, (byte) 99, (byte) 67, (byte) 99, (byte) 151, (byte) 99, (byte) 235, (byte) 100, (byte) 64, (byte) 100, (byte) 148,
        (byte) 100, (byte) 233, (byte) 101, (byte) 61, (byte) 101, (byte) 146, (byte) 101, (byte) 231, (byte) 102, (byte) 61, (byte) 102,
        (byte) 146, (byte) 102, (byte) 232, (byte) 103, (byte) 61, (byte) 103, (byte) 147, (byte) 103, (byte) 233, (byte) 104, (byte) 63,
        (byte) 104, (byte) 150, (byte) 104, (byte) 236, (byte) 105, (byte) 67, (byte) 105, (byte) 154, (byte) 105, (byte) 241, (byte) 106,
        (byte) 72, (byte) 106, (byte) 159, (byte) 106, (byte) 247, (byte) 107, (byte) 79, (byte) 107, (byte) 167, (byte) 107, (byte) 255,
        (byte) 108, (byte) 87, (byte) 108, (byte) 175, (byte) 109, (byte) 8, (byte) 109, (byte) 96, (byte) 109, (byte) 185, (byte) 110,
        (byte) 18, (byte) 110, (byte) 107, (byte) 110, (byte) 196, (byte) 111, (byte) 30, (byte) 111, (byte) 120, (byte) 111, (byte) 209,
        (byte) 112, (byte) 43, (byte) 112, (byte) 134, (byte) 112, (byte) 224, (byte) 113, (byte) 58, (byte) 113, (byte) 149, (byte) 113,
        (byte) 240, (byte) 114, (byte) 75, (byte) 114, (byte) 166, (byte) 115, (byte) 1, (byte) 115, (byte) 93, (byte) 115, (byte) 184,
        (byte) 116, (byte) 20, (byte) 116, (byte) 112, (byte) 116, (byte) 204, (byte) 117, (byte) 40, (byte) 117, (byte) 133, (byte) 117,
        (byte) 225, (byte) 118, (byte) 62, (byte) 118, (byte) 155, (byte) 118, (byte) 248, (byte) 119, (byte) 86, (byte) 119, (byte) 179,
        (byte) 120, (byte) 17, (byte) 120, (byte) 110, (byte) 120, (byte) 204, (byte) 121, (byte) 42, (byte) 121, (byte) 137, (byte) 121,
        (byte) 231, (byte) 122, (byte) 70, (byte) 122, (byte) 165, (byte) 123, (byte) 4, (byte) 123, (byte) 99, (byte) 123, (byte) 194,
        (byte) 124, (byte) 33, (byte) 124, (byte) 129, (byte) 124, (byte) 225, (byte) 125, (byte) 65, (byte) 125, (byte) 161, (byte) 126,
        (byte) 1, (byte) 126, (byte) 98, (byte) 126, (byte) 194, (byte) 127, (byte) 35, (byte) 127, (byte) 132, (byte) 127, (byte) 229,
        (byte) 128, (byte) 71, (byte) 128, (byte) 168, (byte) 129, (byte) 10, (byte) 129, (byte) 107, (byte) 129, (byte) 205, (byte) 130,
        (byte) 48, (byte) 130, (byte) 146, (byte) 130, (byte) 244, (byte) 131, (byte) 87, (byte) 131, (byte) 186, (byte) 132, (byte) 29,
        (byte) 132, (byte) 128, (byte) 132, (byte) 227, (byte) 133, (byte) 71, (byte) 133, (byte) 171, (byte) 134, (byte) 14, (byte) 134,
        (byte) 114, (byte) 134, (byte) 215, (byte) 135, (byte) 59, (byte) 135, (byte) 159, (byte) 136, (byte) 4, (byte) 136, (byte) 105,
        (byte) 136, (byte) 206, (byte) 137, (byte) 51, (byte) 137, (byte) 153, (byte) 137, (byte) 254, (byte) 138, (byte) 100, (byte) 138,
        (byte) 202, (byte) 139, (byte) 48, (byte) 139, (byte) 150, (byte) 139, (byte) 252, (byte) 140, (byte) 99, (byte) 140, (byte) 202,
        (byte) 141, (byte) 49, (byte) 141, (byte) 152, (byte) 141, (byte) 255, (byte) 142, (byte) 102, (byte) 142, (byte) 206, (byte) 143,
        (byte) 54, (byte) 143, (byte) 158, (byte) 144, (byte) 6, (byte) 144, (byte) 110, (byte) 144, (byte) 214, (byte) 145, (byte) 63,
        (byte) 145, (byte) 168, (byte) 146, (byte) 17, (byte) 146, (byte) 122, (byte) 146, (byte) 227, (byte) 147, (byte) 77, (byte) 147,
        (byte) 182, (byte) 148, (byte) 32, (byte) 148, (byte) 138, (byte) 148, (byte) 244, (byte) 149, (byte) 95, (byte) 149, (byte) 201,
        (byte) 150, (byte) 52, (byte) 150, (byte) 159, (byte) 151, (byte) 10, (byte) 151, (byte) 117, (byte) 151, (byte) 224, (byte) 152,
        (byte) 76, (byte) 152, (byte) 184, (byte) 153, (byte) 36, (byte) 153, (byte) 144, (byte) 153, (byte) 252, (byte) 154, (byte) 104,
        (byte) 154, (byte) 213, (byte) 155, (byte) 66, (byte) 155, (byte) 175, (byte) 156, (byte) 28, (byte) 156, (byte) 137, (byte) 156,
        (byte) 247, (byte) 157, (byte) 100, (byte) 157, (byte) 210, (byte) 158, (byte) 64, (byte) 158, (byte) 174, (byte) 159, (byte) 29,
        (byte) 159, (byte) 139, (byte) 159, (byte) 250, (byte) 160, (byte) 105, (byte) 160, (byte) 216, (byte) 161, (byte) 71, (byte) 161,
        (byte) 182, (byte) 162, (byte) 38, (byte) 162, (byte) 150, (byte) 163, (byte) 6, (byte) 163, (byte) 118, (byte) 163, (byte) 230,
        (byte) 164, (byte) 86, (byte) 164, (byte) 199, (byte) 165, (byte) 56, (byte) 165, (byte) 169, (byte) 166, (byte) 26, (byte) 166,
        (byte) 139, (byte) 166, (byte) 253, (byte) 167, (byte) 110, (byte) 167, (byte) 224, (byte) 168, (byte) 82, (byte) 168, (byte) 196,
        (byte) 169, (byte) 55, (byte) 169, (byte) 169, (byte) 170, (byte) 28, (byte) 170, (byte) 143, (byte) 171, (byte) 2, (byte) 171,
        (byte) 117, (byte) 171, (byte) 233, (byte) 172, (byte) 92, (byte) 172, (byte) 208, (byte) 173, (byte) 68, (byte) 173, (byte) 184,
        (byte) 174, (byte) 45, (byte) 174, (byte) 161, (byte) 175, (byte) 22, (byte) 175, (byte) 139, (byte) 176, (byte) 0, (byte) 176,
        (byte) 117, (byte) 176, (byte) 234, (byte) 177, (byte) 96, (byte) 177, (byte) 214, (byte) 178, (byte) 75, (byte) 178, (byte) 194,
        (byte) 179, (byte) 56, (byte) 179, (byte) 174, (byte) 180, (byte) 37, (byte) 180, (byte) 156, (byte) 181, (byte) 19, (byte) 181,
        (byte) 138, (byte) 182, (byte) 1, (byte) 182, (byte) 121, (byte) 182, (byte) 240, (byte) 183, (byte) 104, (byte) 183, (byte) 224,
        (byte) 184, (byte) 89, (byte) 184, (byte) 209, (byte) 185, (byte) 74, (byte) 185, (byte) 194, (byte) 186, (byte) 59, (byte) 186,
        (byte) 181, (byte) 187, (byte) 46, (byte) 187, (byte) 167, (byte) 188, (byte) 33, (byte) 188, (byte) 155, (byte) 189, (byte) 21,
        (byte) 189, (byte) 143, (byte) 190, (byte) 10, (byte) 190, (byte) 132, (byte) 190, (byte) 255, (byte) 191, (byte) 122, (byte) 191,
        (byte) 245, (byte) 192, (byte) 112, (byte) 192, (byte) 236, (byte) 193, (byte) 103, (byte) 193, (byte) 227, (byte) 194, (byte) 95,
        (byte) 194, (byte) 219, (byte) 195, (byte) 88, (byte) 195, (byte) 212, (byte) 196, (byte) 81, (byte) 196, (byte) 206, (byte) 197,
        (byte) 75, (byte) 197, (byte) 200, (byte) 198, (byte) 70, (byte) 198, (byte) 195, (byte) 199, (byte) 65, (byte) 199, (byte) 191,
        (byte) 200, (byte) 61, (byte) 200, (byte) 188, (byte) 201, (byte) 58, (byte) 201, (byte) 185, (byte) 202, (byte) 56, (byte) 202,
        (byte) 183, (byte) 203, (byte) 54, (byte) 203, (byte) 182, (byte) 204, (byte) 53, (byte) 204, (byte) 181, (byte) 205, (byte) 53,
        (byte) 205, (byte) 181, (byte) 206, (byte) 54, (byte) 206, (byte) 182, (byte) 207, (byte) 55, (byte) 207, (byte) 184, (byte) 208,
        (byte) 57, (byte) 208, (byte) 186, (byte) 209, (byte) 60, (byte) 209, (byte) 190, (byte) 210, (byte) 63, (byte) 210, (byte) 193,
        (byte) 211, (byte) 68, (byte) 211, (byte) 198, (byte) 212, (byte) 73, (byte) 212, (byte) 203, (byte) 213, (byte) 78, (byte) 213,
        (byte) 209, (byte) 214, (byte) 85, (byte) 214, (byte) 216, (byte) 215, (byte) 92, (byte) 215, (byte) 224, (byte) 216, (byte) 100,
        (byte) 216, (byte) 232, (byte) 217, (byte) 108, (byte) 217, (byte) 241, (byte) 218, (byte) 118, (byte) 218, (byte) 251, (byte) 219,
        (byte) 128, (byte) 220, (byte) 5, (byte) 220, (byte) 138, (byte) 221, (byte) 16, (byte) 221, (byte) 150, (byte) 222, (byte) 28,
        (byte) 222, (byte) 162, (byte) 223, (byte) 41, (byte) 223, (byte) 175, (byte) 224, (byte) 54, (byte) 224, (byte) 189, (byte) 225,
        (byte) 68, (byte) 225, (byte) 204, (byte) 226, (byte) 83, (byte) 226, (byte) 219, (byte) 227, (byte) 99, (byte) 227, (byte) 235,
        (byte) 228, (byte) 115, (byte) 228, (byte) 252, (byte) 229, (byte) 132, (byte) 230, (byte) 13, (byte) 230, (byte) 150, (byte) 231,
        (byte) 31, (byte) 231, (byte) 169, (byte) 232, (byte) 50, (byte) 232, (byte) 188, (byte) 233, (byte) 70, (byte) 233, (byte) 208,
        (byte) 234, (byte) 91, (byte) 234, (byte) 229, (byte) 235, (byte) 112, (byte) 235, (byte) 251, (byte) 236, (byte) 134, (byte) 237,
        (byte) 17, (byte) 237, (byte) 156, (byte) 238, (byte) 40, (byte) 238, (byte) 180, (byte) 239, (byte) 64, (byte) 239, (byte) 204,
        (byte) 240, (byte) 88, (byte) 240, (byte) 229, (byte) 241, (byte) 114, (byte) 241, (byte) 255, (byte) 242, (byte) 140, (byte) 243,
        (byte) 25, (byte) 243, (byte) 167, (byte) 244, (byte) 52, (byte) 244, (byte) 194, (byte) 245, (byte) 80, (byte) 245, (byte) 222,
        (byte) 246, (byte) 109, (byte) 246, (byte) 251, (byte) 247, (byte) 138, (byte) 248, (byte) 25, (byte) 248, (byte) 168, (byte) 249,
        (byte) 56, (byte) 249, (byte) 199, (byte) 250, (byte) 87, (byte) 250, (byte) 231, (byte) 251, (byte) 119, (byte) 252, (byte) 7,
        (byte) 252, (byte) 152, (byte) 253, (byte) 41, (byte) 253, (byte) 186, (byte) 254, (byte) 75, (byte) 254, (byte) 220, (byte) 255,
        (byte) 109, (byte) 255, (byte) 255, (byte) 112, (byte) 97, (byte) 114, (byte) 97, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 2, (byte) 102, (byte) 102, (byte) 0, (byte) 0, (byte) 242, (byte) 167, (byte) 0,
        (byte) 0, (byte) 13, (byte) 89, (byte) 0, (byte) 0, (byte) 19, (byte) 208, (byte) 0, (byte) 0, (byte) 10, (byte) 14, (byte) 118,
        (byte) 99, (byte) 103, (byte) 116, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0,
        (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 110, (byte) 100, (byte) 105,
        (byte) 110, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 54, (byte) 0, (byte) 0, (byte) 167,
        (byte) 64, (byte) 0, (byte) 0, (byte) 85, (byte) 128, (byte) 0, (byte) 0, (byte) 76, (byte) 192, (byte) 0, (byte) 0, (byte) 158,
        (byte) 192, (byte) 0, (byte) 0, (byte) 37, (byte) 128, (byte) 0, (byte) 0, (byte) 12, (byte) 192, (byte) 0, (byte) 0, (byte) 80,
        (byte) 0, (byte) 0, (byte) 0, (byte) 84, (byte) 64, (byte) 0, (byte) 2, (byte) 51, (byte) 51, (byte) 0, (byte) 2, (byte) 51,
        (byte) 51, (byte) 0, (byte) 2, (byte) 51, (byte) 51, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 115, (byte) 102, (byte) 51, (byte) 50, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 12,
        (byte) 114, (byte) 0, (byte) 0, (byte) 5, (byte) 248, (byte) 255, (byte) 255, (byte) 243, (byte) 29, (byte) 0, (byte) 0, (byte) 7,
        (byte) 186, (byte) 0, (byte) 0, (byte) 253, (byte) 114, (byte) 255, (byte) 255, (byte) 251, (byte) 157, (byte) 255, (byte) 255,
        (byte) 253, (byte) 164, (byte) 0, (byte) 0, (byte) 3, (byte) 217, (byte) 0, (byte) 0, (byte) 192, (byte) 113, (byte) 109,
        (byte) 109, (byte) 111, (byte) 100, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 6, (byte) 16, (byte) 0,
        (byte) 0, (byte) 146, (byte) 39, (byte) 21, (byte) 49, (byte) 28, (byte) 217, (byte) 202, (byte) 255, (byte) 23, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 255, (byte) 225, (byte) 0, (byte) 128, (byte) 69, (byte) 120, (byte) 105, (byte) 102,
        (byte) 0, (byte) 0, (byte) 77, (byte) 77, (byte) 0, (byte) 42, (byte) 0, (byte) 0, (byte) 0, (byte) 8, (byte) 0, (byte) 5,
        (byte) 1, (byte) 18, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 1,
        (byte) 26, (byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 74, (byte) 1,
        (byte) 27, (byte) 0, (byte) 5, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 82, (byte) 1,
        (byte) 40, (byte) 0, (byte) 3, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 2, (byte) 0, (byte) 0, (byte) 135,
        (byte) 105, (byte) 0, (byte) 4, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 90, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 72, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0,
        (byte) 0, (byte) 72, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 2, (byte) 160, (byte) 2, (byte) 0, (byte) 4,
        (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 64, (byte) 160, (byte) 3, (byte) 0, (byte) 4,
        (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 79, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
        (byte) 255, (byte) 219, (byte) 0, (byte) 67, (byte) 0, (byte) 2, (byte) 1, (byte) 1, (byte) 2, (byte) 1, (byte) 1, (byte) 2,
        (byte) 2, (byte) 1, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 2, (byte) 3, (byte) 5, (byte) 3, (byte) 3, (byte) 3, (byte) 3,
        (byte) 3, (byte) 6, (byte) 4, (byte) 4, (byte) 3, (byte) 5, (byte) 7, (byte) 6, (byte) 7, (byte) 7, (byte) 7, (byte) 6, (byte) 6,
        (byte) 6, (byte) 7, (byte) 8, (byte) 11, (byte) 9, (byte) 7, (byte) 8, (byte) 10, (byte) 8, (byte) 6, (byte) 6, (byte) 9,
        (byte) 13, (byte) 9, (byte) 10, (byte) 11, (byte) 11, (byte) 12, (byte) 12, (byte) 12, (byte) 7, (byte) 9, (byte) 13, (byte) 14,
        (byte) 13, (byte) 12, (byte) 14, (byte) 11, (byte) 12, (byte) 12, (byte) 11, (byte) 255, (byte) 219, (byte) 0, (byte) 67, (byte) 1,
        (byte) 2, (byte) 2, (byte) 2, (byte) 3, (byte) 2, (byte) 3, (byte) 5, (byte) 3, (byte) 3, (byte) 5, (byte) 11, (byte) 8, (byte) 6,
        (byte) 8, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11,
        (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11,
        (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11,
        (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11, (byte) 11,
        (byte) 11, (byte) 11, (byte) 11, (byte) 255, (byte) 192, (byte) 0, (byte) 17, (byte) 8, (byte) 0, (byte) 79, (byte) 0, (byte) 64,
        (byte) 3, (byte) 1, (byte) 34, (byte) 0, (byte) 2, (byte) 17, (byte) 1, (byte) 3, (byte) 17, (byte) 1, (byte) 255, (byte) 196,
        (byte) 0, (byte) 31, (byte) 0, (byte) 0, (byte) 1, (byte) 5, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6,
        (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 255, (byte) 196, (byte) 0, (byte) 181, (byte) 16, (byte) 0, (byte) 2,
        (byte) 1, (byte) 3, (byte) 3, (byte) 2, (byte) 4, (byte) 3, (byte) 5, (byte) 5, (byte) 4, (byte) 4, (byte) 0, (byte) 0, (byte) 1,
        (byte) 125, (byte) 1, (byte) 2, (byte) 3, (byte) 0, (byte) 4, (byte) 17, (byte) 5, (byte) 18, (byte) 33, (byte) 49, (byte) 65,
        (byte) 6, (byte) 19, (byte) 81, (byte) 97, (byte) 7, (byte) 34, (byte) 113, (byte) 20, (byte) 50, (byte) 129, (byte) 145,
        (byte) 161, (byte) 8, (byte) 35, (byte) 66, (byte) 177, (byte) 193, (byte) 21, (byte) 82, (byte) 209, (byte) 240, (byte) 36,
        (byte) 51, (byte) 98, (byte) 114, (byte) 130, (byte) 9, (byte) 10, (byte) 22, (byte) 23, (byte) 24, (byte) 25, (byte) 26,
        (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57,
        (byte) 58, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 83, (byte) 84, (byte) 85,
        (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104,
        (byte) 105, (byte) 106, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 131,
        (byte) 132, (byte) 133, (byte) 134, (byte) 135, (byte) 136, (byte) 137, (byte) 138, (byte) 146, (byte) 147, (byte) 148, (byte) 149,
        (byte) 150, (byte) 151, (byte) 152, (byte) 153, (byte) 154, (byte) 162, (byte) 163, (byte) 164, (byte) 165, (byte) 166, (byte) 167,
        (byte) 168, (byte) 169, (byte) 170, (byte) 178, (byte) 179, (byte) 180, (byte) 181, (byte) 182, (byte) 183, (byte) 184, (byte) 185,
        (byte) 186, (byte) 194, (byte) 195, (byte) 196, (byte) 197, (byte) 198, (byte) 199, (byte) 200, (byte) 201, (byte) 202, (byte) 210,
        (byte) 211, (byte) 212, (byte) 213, (byte) 214, (byte) 215, (byte) 216, (byte) 217, (byte) 218, (byte) 225, (byte) 226, (byte) 227,
        (byte) 228, (byte) 229, (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234, (byte) 241, (byte) 242, (byte) 243, (byte) 244,
        (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249, (byte) 250, (byte) 255, (byte) 196, (byte) 0, (byte) 31, (byte) 1,
        (byte) 0, (byte) 3, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0,
        (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9,
        (byte) 10, (byte) 11, (byte) 255, (byte) 196, (byte) 0, (byte) 181, (byte) 17, (byte) 0, (byte) 2, (byte) 1, (byte) 2, (byte) 4,
        (byte) 4, (byte) 3, (byte) 4, (byte) 7, (byte) 5, (byte) 4, (byte) 4, (byte) 0, (byte) 1, (byte) 2, (byte) 119, (byte) 0, (byte) 1,
        (byte) 2, (byte) 3, (byte) 17, (byte) 4, (byte) 5, (byte) 33, (byte) 49, (byte) 6, (byte) 18, (byte) 65, (byte) 81, (byte) 7,
        (byte) 97, (byte) 113, (byte) 19, (byte) 34, (byte) 50, (byte) 129, (byte) 8, (byte) 20, (byte) 66, (byte) 145, (byte) 161,
        (byte) 177, (byte) 193, (byte) 9, (byte) 35, (byte) 51, (byte) 82, (byte) 240, (byte) 21, (byte) 98, (byte) 114, (byte) 209,
        (byte) 10, (byte) 22, (byte) 36, (byte) 52, (byte) 225, (byte) 37, (byte) 241, (byte) 23, (byte) 24, (byte) 25, (byte) 26,
        (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 67,
        (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87,
        (byte) 88, (byte) 89, (byte) 90, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106,
        (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 130, (byte) 131, (byte) 132,
        (byte) 133, (byte) 134, (byte) 135, (byte) 136, (byte) 137, (byte) 138, (byte) 146, (byte) 147, (byte) 148, (byte) 149, (byte) 150,
        (byte) 151, (byte) 152, (byte) 153, (byte) 154, (byte) 162, (byte) 163, (byte) 164, (byte) 165, (byte) 166, (byte) 167, (byte) 168,
        (byte) 169, (byte) 170, (byte) 178, (byte) 179, (byte) 180, (byte) 181, (byte) 182, (byte) 183, (byte) 184, (byte) 185, (byte) 186,
        (byte) 194, (byte) 195, (byte) 196, (byte) 197, (byte) 198, (byte) 199, (byte) 200, (byte) 201, (byte) 202, (byte) 210, (byte) 211,
        (byte) 212, (byte) 213, (byte) 214, (byte) 215, (byte) 216, (byte) 217, (byte) 218, (byte) 226, (byte) 227, (byte) 228, (byte) 229,
        (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234, (byte) 242, (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247,
        (byte) 248, (byte) 249, (byte) 250, (byte) 255, (byte) 218, (byte) 0, (byte) 12, (byte) 3, (byte) 1, (byte) 0, (byte) 2, (byte) 17,
        (byte) 3, (byte) 17, (byte) 0, (byte) 63, (byte) 0, (byte) 253, (byte) 203, (byte) 240, (byte) 7, (byte) 128, (byte) 52, (byte) 27,
        (byte) 223, (byte) 2, (byte) 104, (byte) 147, (byte) 94, (byte) 104, (byte) 154, (byte) 68, (byte) 179, (byte) 75, (byte) 97,
        (byte) 3, (byte) 187, (byte) 189, (byte) 156, (byte) 108, (byte) 206, (byte) 198, (byte) 53, (byte) 36, (byte) 146, (byte) 87,
        (byte) 36, (byte) 147, (byte) 222, (byte) 181, (byte) 255, (byte) 0, (byte) 225, (byte) 91, (byte) 248, (byte) 119, (byte) 254,
        (byte) 128, (byte) 26, (byte) 47, (byte) 254, (byte) 0, (byte) 197, (byte) 255, (byte) 0, (byte) 196, (byte) 209, (byte) 240,
        (byte) 223, (byte) 254, (byte) 73, (byte) 222, (byte) 129, (byte) 255, (byte) 0, (byte) 96, (byte) 235, (byte) 127, (byte) 253,
        (byte) 20, (byte) 181, (byte) 181, (byte) 64, (byte) 24, (byte) 191, (byte) 240, (byte) 173, (byte) 252, (byte) 59, (byte) 255,
        (byte) 0, (byte) 64, (byte) 13, (byte) 23, (byte) 255, (byte) 0, (byte) 0, (byte) 98, (byte) 255, (byte) 0, (byte) 226, (byte) 104,
        (byte) 255, (byte) 0, (byte) 133, (byte) 111, (byte) 225, (byte) 223, (byte) 250, (byte) 0, (byte) 104, (byte) 191, (byte) 248,
        (byte) 3, (byte) 23, (byte) 255, (byte) 0, (byte) 19, (byte) 91, (byte) 84, (byte) 80, (byte) 6, (byte) 47, (byte) 252, (byte) 43,
        (byte) 127, (byte) 14, (byte) 255, (byte) 0, (byte) 208, (byte) 3, (byte) 69, (byte) 255, (byte) 0, (byte) 192, (byte) 24,
        (byte) 191, (byte) 248, (byte) 154, (byte) 63, (byte) 225, (byte) 91, (byte) 248, (byte) 119, (byte) 254, (byte) 128, (byte) 26,
        (byte) 47, (byte) 254, (byte) 0, (byte) 197, (byte) 255, (byte) 0, (byte) 196, (byte) 214, (byte) 213, (byte) 20, (byte) 1,
        (byte) 139, (byte) 255, (byte) 0, (byte) 10, (byte) 223, (byte) 195, (byte) 191, (byte) 244, (byte) 0, (byte) 209, (byte) 127,
        (byte) 240, (byte) 6, (byte) 47, (byte) 254, (byte) 38, (byte) 178, (byte) 60, (byte) 127, (byte) 224, (byte) 13, (byte) 6,
        (byte) 207, (byte) 192, (byte) 154, (byte) 220, (byte) 214, (byte) 122, (byte) 38, (byte) 145, (byte) 20, (byte) 209, (byte) 88,
        (byte) 78, (byte) 232, (byte) 233, (byte) 103, (byte) 26, (byte) 178, (byte) 48, (byte) 141, (byte) 136, (byte) 32, (byte) 133,
        (byte) 200, (byte) 32, (byte) 247, (byte) 174, (byte) 198, (byte) 177, (byte) 126, (byte) 36, (byte) 127, (byte) 201, (byte) 59,
        (byte) 215, (byte) 255, (byte) 0, (byte) 236, (byte) 29, (byte) 113, (byte) 255, (byte) 0, (byte) 162, (byte) 154, (byte) 128,
        (byte) 15, (byte) 134, (byte) 255, (byte) 0, (byte) 242, (byte) 78, (byte) 244, (byte) 15, (byte) 251, (byte) 7, (byte) 91,
        (byte) 255, (byte) 0, (byte) 232, (byte) 165, (byte) 171, (byte) 30, (byte) 51, (byte) 215, (byte) 155, (byte) 194, (byte) 222,
        (byte) 15, (byte) 213, (byte) 117, (byte) 56, (byte) 208, (byte) 74, (byte) 218, (byte) 117, (byte) 156, (byte) 215, (byte) 65,
        (byte) 15, (byte) 71, (byte) 40, (byte) 133, (byte) 176, (byte) 126, (byte) 184, (byte) 170, (byte) 255, (byte) 0, (byte) 13,
        (byte) 255, (byte) 0, (byte) 228, (byte) 157, (byte) 232, (byte) 31, (byte) 246, (byte) 14, (byte) 183, (byte) 255, (byte) 0,
        (byte) 209, (byte) 75, (byte) 85, (byte) 254, (byte) 47, (byte) 127, (byte) 201, (byte) 38, (byte) 241, (byte) 71, (byte) 253,
        (byte) 130, (byte) 46, (byte) 255, (byte) 0, (byte) 244, (byte) 75, (byte) 208, (byte) 7, (byte) 204, (byte) 51, (byte) 126,
        (byte) 216, (byte) 62, (byte) 56, (byte) 150, (byte) 86, (byte) 100, (byte) 190, (byte) 179, (byte) 140, (byte) 49, (byte) 200,
        (byte) 85, (byte) 179, (byte) 143, (byte) 11, (byte) 236, (byte) 50, (byte) 9, (byte) 252, (byte) 205, (byte) 55, (byte) 254,
        (byte) 26, (byte) 247, (byte) 199, (byte) 95, (byte) 244, (byte) 17, (byte) 182, (byte) 255, (byte) 0, (byte) 192, (byte) 56,
        (byte) 191, (byte) 194, (byte) 188, (byte) 198, (byte) 138, (byte) 0, (byte) 250, (byte) 219, (byte) 227, (byte) 167, (byte) 237,
        (byte) 1, (byte) 127, (byte) 240, (byte) 115, (byte) 197, (byte) 58, (byte) 2, (byte) 199, (byte) 109, (byte) 5, (byte) 238,
        (byte) 159, (byte) 125, (byte) 110, (byte) 210, (byte) 93, (byte) 70, (byte) 70, (byte) 217, (byte) 56, (byte) 32, (byte) 101,
        (byte) 27, (byte) 160, (byte) 60, (byte) 244, (byte) 32, (byte) 131, (byte) 237, (byte) 214, (byte) 189, (byte) 31, (byte) 193,
        (byte) 254, (byte) 48, (byte) 211, (byte) 252, (byte) 119, (byte) 225, (byte) 251, (byte) 125, (byte) 79, (byte) 195, (byte) 23,
        (byte) 11, (byte) 113, (byte) 105, (byte) 112, (byte) 50, (byte) 8, (byte) 234, (byte) 167, (byte) 186, (byte) 176, (byte) 236,
        (byte) 195, (byte) 184, (byte) 175, (byte) 157, (byte) 255, (byte) 0, (byte) 110, (byte) 47, (byte) 249, (byte) 13, (byte) 120,
        (byte) 107, (byte) 254, (byte) 188, (byte) 159, (byte) 255, (byte) 0, (byte) 66, (byte) 90, (byte) 243, (byte) 255, (byte) 0,
        (byte) 130, (byte) 127, (byte) 26, (byte) 245, (byte) 15, (byte) 131, (byte) 190, (byte) 33, (byte) 19, (byte) 90, (byte) 22,
        (byte) 184, (byte) 211, (byte) 110, (byte) 88, (byte) 11, (byte) 187, (byte) 66, (byte) 120, (byte) 144, (byte) 127, (byte) 121,
        (byte) 125, (byte) 28, (byte) 118, (byte) 61, (byte) 250, (byte) 26, (byte) 0, (byte) 251, (byte) 110, (byte) 177, (byte) 126,
        (byte) 36, (byte) 127, (byte) 201, (byte) 59, (byte) 215, (byte) 255, (byte) 0, (byte) 236, (byte) 29, (byte) 113, (byte) 255,
        (byte) 0, (byte) 162, (byte) 154, (byte) 180, (byte) 180, (byte) 141, (byte) 86, (byte) 13, (byte) 119, (byte) 73, (byte) 181,
        (byte) 190, (byte) 211, (byte) 31, (byte) 204, (byte) 182, (byte) 188, (byte) 137, (byte) 103, (byte) 137, (byte) 255, (byte) 0,
        (byte) 188, (byte) 140, (byte) 1, (byte) 7, (byte) 242, (byte) 34, (byte) 179, (byte) 126, (byte) 36, (byte) 127, (byte) 201,
        (byte) 59, (byte) 215, (byte) 255, (byte) 0, (byte) 236, (byte) 29, (byte) 113, (byte) 255, (byte) 0, (byte) 162, (byte) 154,
        (byte) 128, (byte) 15, (byte) 134, (byte) 255, (byte) 0, (byte) 242, (byte) 78, (byte) 244, (byte) 15, (byte) 251, (byte) 7,
        (byte) 91, (byte) 255, (byte) 0, (byte) 232, (byte) 165, (byte) 170, (byte) 255, (byte) 0, (byte) 23, (byte) 191, (byte) 228,
        (byte) 147, (byte) 120, (byte) 163, (byte) 254, (byte) 193, (byte) 23, (byte) 127, (byte) 250, (byte) 37, (byte) 234, (byte) 199,
        (byte) 195, (byte) 127, (byte) 249, (byte) 39, (byte) 122, (byte) 7, (byte) 253, (byte) 131, (byte) 173, (byte) 255, (byte) 0,
        (byte) 244, (byte) 82, (byte) 213, (byte) 127, (byte) 139, (byte) 223, (byte) 242, (byte) 73, (byte) 188, (byte) 81, (byte) 255,
        (byte) 0, (byte) 96, (byte) 139, (byte) 191, (byte) 253, (byte) 18, (byte) 244, (byte) 1, (byte) 240, (byte) 141, (byte) 20,
        (byte) 81, (byte) 64, (byte) 30, (byte) 213, (byte) 162, (byte) 126, (byte) 219, (byte) 218, (byte) 254, (byte) 153, (byte) 164,
        (byte) 219, (byte) 219, (byte) 223, (byte) 105, (byte) 122, (byte) 109, (byte) 220, (byte) 144, (byte) 32, (byte) 140, (byte) 204,
        (byte) 75, (byte) 171, (byte) 73, (byte) 129, (byte) 140, (byte) 145, (byte) 158, (byte) 190, (byte) 184, (byte) 253, (byte) 42,
        (byte) 223, (byte) 252, (byte) 55, (byte) 102, (byte) 181, (byte) 255, (byte) 0, (byte) 64, (byte) 61, (byte) 51, (byte) 254,
        (byte) 254, (byte) 73, (byte) 254, (byte) 53, (byte) 225, (byte) 84, (byte) 80, (byte) 7, (byte) 222, (byte) 255, (byte) 0,
        (byte) 15, (byte) 60, (byte) 77, (byte) 39, (byte) 140, (byte) 252, (byte) 13, (byte) 165, (byte) 106, (byte) 215, (byte) 113,
        (byte) 164, (byte) 50, (byte) 234, (byte) 22, (byte) 201, (byte) 59, (byte) 34, (byte) 18, (byte) 85, (byte) 11, (byte) 12,
        (byte) 224, (byte) 102, (byte) 147, (byte) 226, (byte) 71, (byte) 252, (byte) 147, (byte) 189, (byte) 127, (byte) 254, (byte) 193,
        (byte) 215, (byte) 31, (byte) 250, (byte) 41, (byte) 171, (byte) 59, (byte) 224, (byte) 87, (byte) 252, (byte) 145, (byte) 207,
        (byte) 13, (byte) 127, (byte) 216, (byte) 62, (byte) 47, (byte) 253, (byte) 4, (byte) 86, (byte) 143, (byte) 196, (byte) 143,
        (byte) 249, (byte) 39, (byte) 122, (byte) 255, (byte) 0, (byte) 253, (byte) 131, (byte) 174, (byte) 63, (byte) 244, (byte) 83,
        (byte) 80, (byte) 1, (byte) 240, (byte) 223, (byte) 254, (byte) 73, (byte) 222, (byte) 129, (byte) 255, (byte) 0, (byte) 96,
        (byte) 235, (byte) 127, (byte) 253, (byte) 20, (byte) 181, (byte) 95, (byte) 226, (byte) 247, (byte) 252, (byte) 146, (byte) 111,
        (byte) 20, (byte) 127, (byte) 216, (byte) 34, (byte) 239, (byte) 255, (byte) 0, (byte) 68, (byte) 189, (byte) 88, (byte) 248,
        (byte) 111, (byte) 255, (byte) 0, (byte) 36, (byte) 239, (byte) 64, (byte) 255, (byte) 0, (byte) 176, (byte) 117, (byte) 191,
        (byte) 254, (byte) 138, (byte) 90, (byte) 209, (byte) 214, (byte) 52, (byte) 168, (byte) 53, (byte) 221, (byte) 38, (byte) 234,
        (byte) 199, (byte) 82, (byte) 93, (byte) 246, (byte) 215, (byte) 144, (byte) 188, (byte) 18, (byte) 175, (byte) 247, (byte) 145,
        (byte) 148, (byte) 169, (byte) 31, (byte) 145, (byte) 52, (byte) 1, (byte) 249, (byte) 241, (byte) 69, (byte) 125, (byte) 61,
        (byte) 55, (byte) 236, (byte) 43, (byte) 161, (byte) 180, (byte) 172, (byte) 96, (byte) 214, (byte) 181, (byte) 85, (byte) 66,
        (byte) 126, (byte) 80, (byte) 201, (byte) 27, (byte) 16, (byte) 61, (byte) 206, (byte) 6, (byte) 127, (byte) 42, (byte) 111,
        (byte) 252, (byte) 48, (byte) 158, (byte) 141, (byte) 255, (byte) 0, (byte) 65, (byte) 205, (byte) 83, (byte) 254, (byte) 253,
        (byte) 199, (byte) 254, (byte) 20, (byte) 1, (byte) 243, (byte) 29, (byte) 21, (byte) 244, (byte) 231, (byte) 252, (byte) 48,
        (byte) 158, (byte) 141, (byte) 255, (byte) 0, (byte) 65, (byte) 205, (byte) 83, (byte) 254, (byte) 253, (byte) 199, (byte) 254,
        (byte) 20, (byte) 177, (byte) 254, (byte) 194, (byte) 154, (byte) 32, (byte) 144, (byte) 25, (byte) 117, (byte) 189, (byte) 85,
        (byte) 151, (byte) 60, (byte) 128, (byte) 145, (byte) 130, (byte) 71, (byte) 177, (byte) 199, (byte) 20, (byte) 1, (byte) 233,
        (byte) 31, (byte) 2, (byte) 191, (byte) 228, (byte) 142, (byte) 120, (byte) 107, (byte) 254, (byte) 193, (byte) 241, (byte) 127,
        (byte) 232, (byte) 34, (byte) 180, (byte) 126, (byte) 36, (byte) 127, (byte) 201, (byte) 59, (byte) 215, (byte) 255, (byte) 0,
        (byte) 236, (byte) 29, (byte) 113, (byte) 255, (byte) 0, (byte) 162, (byte) 154, (byte) 175, (byte) 120, (byte) 123, (byte) 66,
        (byte) 183, (byte) 240, (byte) 198, (byte) 133, (byte) 103, (byte) 167, (byte) 105, (byte) 42, (byte) 82, (byte) 218, (byte) 198,
        (byte) 20, (byte) 130, (byte) 32, (byte) 78, (byte) 78, (byte) 213, (byte) 0, (byte) 12, (byte) 158, (byte) 231, (byte) 142,
        (byte) 181, (byte) 71, (byte) 226, (byte) 71, (byte) 252, (byte) 147, (byte) 189, (byte) 127, (byte) 254, (byte) 193, (byte) 215,
        (byte) 31, (byte) 250, (byte) 41, (byte) 168, (byte) 0, (byte) 248, (byte) 111, (byte) 255, (byte) 0, (byte) 36, (byte) 239,
        (byte) 64, (byte) 255, (byte) 0, (byte) 176, (byte) 117, (byte) 191, (byte) 254, (byte) 138, (byte) 90, (byte) 218, (byte) 175,
        (byte) 56, (byte) 240, (byte) 79, (byte) 199, (byte) 95, (byte) 10, (byte) 233, (byte) 30, (byte) 12, (byte) 210, (byte) 45,
        (byte) 53, (byte) 29, (byte) 87, (byte) 203, (byte) 184, (byte) 181, (byte) 178, (byte) 134, (byte) 25, (byte) 83, (byte) 236,
        (byte) 211, (byte) 29, (byte) 172, (byte) 177, (byte) 168, (byte) 35, (byte) 33, (byte) 48, (byte) 121, (byte) 7, (byte) 165,
        (byte) 105, (byte) 255, (byte) 0, (byte) 195, (byte) 67, (byte) 120, (byte) 63, (byte) 254, (byte) 131, (byte) 31, (byte) 249,
        (byte) 41, (byte) 63, (byte) 255, (byte) 0, (byte) 17, (byte) 64, (byte) 29, (byte) 165, (byte) 21, (byte) 197, (byte) 255,
        (byte) 0, (byte) 195, (byte) 67, (byte) 120, (byte) 63, (byte) 254, (byte) 131, (byte) 31, (byte) 249, (byte) 41, (byte) 63,
        (byte) 255, (byte) 0, (byte) 17, (byte) 71, (byte) 252, (byte) 52, (byte) 55, (byte) 131, (byte) 255, (byte) 0, (byte) 232,
        (byte) 49, (byte) 255, (byte) 0, (byte) 146, (byte) 147, (byte) 255, (byte) 0, (byte) 241, (byte) 20, (byte) 1, (byte) 218,
        (byte) 81, (byte) 92, (byte) 95, (byte) 252, (byte) 52, (byte) 55, (byte) 131, (byte) 255, (byte) 0, (byte) 232, (byte) 49,
        (byte) 255, (byte) 0, (byte) 146, (byte) 147, (byte) 255, (byte) 0, (byte) 241, (byte) 20, (byte) 127, (byte) 195, (byte) 67,
        (byte) 120, (byte) 63, (byte) 254, (byte) 131, (byte) 31, (byte) 249, (byte) 41, (byte) 63, (byte) 255, (byte) 0, (byte) 17,
        (byte) 64, (byte) 29, (byte) 165, (byte) 98, (byte) 252, (byte) 72, (byte) 255, (byte) 0, (byte) 146, (byte) 119, (byte) 175,
        (byte) 255, (byte) 0, (byte) 216, (byte) 58, (byte) 227, (byte) 255, (byte) 0, (byte) 69, (byte) 53, (byte) 98, (byte) 255,
        (byte) 0, (byte) 195, (byte) 67, (byte) 120, (byte) 63, (byte) 254, (byte) 131, (byte) 31, (byte) 249, (byte) 41, (byte) 63,
        (byte) 255, (byte) 0, (byte) 17, (byte) 89, (byte) 158, (byte) 54, (byte) 248, (byte) 235, (byte) 225, (byte) 93, (byte) 95,
        (byte) 193, (byte) 154, (byte) 189, (byte) 166, (byte) 157, (byte) 170, (byte) 249, (byte) 151, (byte) 23, (byte) 86, (byte) 83,
        (byte) 67, (byte) 18, (byte) 125, (byte) 154, (byte) 97, (byte) 185, (byte) 154, (byte) 54, (byte) 0, (byte) 100, (byte) 166,
        (byte) 7, (byte) 36, (byte) 117, (byte) 160, (byte) 15, (byte) 255, (byte) 217 };


    /** 1x1 white jpg placeholder for missing thumbnail / thumbnail being generated */
    static final byte[] MISSING_THUMBNAIL = new byte[] {
        -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 72, 0, 72, 0, 0, -1, -37, 0, 67, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -62, 0, 11, 8, 0, 1, 0, 1, 1, 1, 17, 0, -1,
        -60, 0, 20, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, -1, -38, 0, 8, 1, 1, 0, 0, 0, 1, 95, -1, -60, 0, 20, 16, 1, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 1, 5, 2, 127, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 6, 63, 2, 127, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        -1, -38, 0, 8, 1, 1, 0, 1, 63, 33, 127, -1, -38, 0, 8, 1, 1, 0, 0, 0, 16, 127, -1, -60, 0, 20, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, -1, -38, 0, 8, 1, 1, 0, 1, 63, 16, 127, -1, -39 };

}
