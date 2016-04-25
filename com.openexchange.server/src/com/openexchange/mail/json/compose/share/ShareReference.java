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

package com.openexchange.mail.json.compose.share;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * {@link ShareReference} - References shared folder/items.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareReference {

    /**
     * Parses a <code>ShareReference</code> from specified reference string.
     *
     * @param referenceString The reference string to parse
     * @return The <code>ShareReference</code> instance
     */
    public static ShareReference parseFromReferenceString(String referenceString) {
        if (Strings.isEmpty(referenceString)) {
            return null;
        }

        try {
            JSONObject jReference = new JSONObject(decompress(referenceString));
            List<String> itemIds;
            {
                JSONArray jItemIds = jReference.getJSONArray("itemIds");
                int length = jItemIds.length();
                itemIds = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    itemIds.add(jItemIds.getString(i));
                }
            }
            return new ShareReference(jReference.getString("shareUrl"), itemIds, jReference.getString("folderId"), jReference.getInt("userId"), jReference.getInt("contextId"));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final String folderId;
    private final List<String> itemIds;
    private final String shareUrl;

    /**
     * Initializes a new {@link ShareReference}.
     *
     * @param shareUrl The associated share URL
     * @param itemIds The identifiers of the shared files
     * @param folderId The folder containing the files
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public ShareReference(String shareUrl, List<String> itemIds, String folderId, int userId, int contextId) {
        super();
        this.shareUrl = shareUrl;
        this.itemIds = itemIds;
        this.folderId = folderId;
        this.userId = userId;
        this.contextId = contextId;
    }

    /**
     * Generates the reference string.
     *
     * @return The reference string
     */
    public String generateReferenceString() {
        try {
            JSONObject jReference = new JSONObject(8);
            jReference.put("shareUrl", shareUrl);
            jReference.put("contextId", contextId);
            jReference.put("userId", userId);
            jReference.put("folderId", folderId);
            jReference.put("itemIds", new JSONArray(itemIds));
            return compress(jReference.toString());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private static String compress(String str) throws IOException {
        ByteArrayOutputStream byteSink = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(byteSink);
        gzip.write(str.getBytes(Charsets.UTF_8));
        gzip.flush();
        gzip.close();
        return Base64.encodeBase64String(byteSink.toByteArray());
     }

    private static String decompress(String str) throws UnsupportedEncodingException, IOException {
        byte[] data = Base64.decodeBase64(str);
        Reader reader = new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(data)), "UTF-8");
        StringBuilder outStr = new StringBuilder(str.length());
        char[] cbuf = new char[2048];
        for (int read; (read = reader.read(cbuf, 0, 2048)) > 0;) {
            outStr.append(cbuf, 0, read);
        }
        return outStr.toString();
     }

}
