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
import java.util.Date;
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
     * @throws IllegalArgumentException If specified reference string is invalid
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
            Date expiration = null;
            if (jReference.hasAndNotNull("expiration")) {
                expiration = new Date(jReference.getLong("expiration"));
            }
            return new ShareReference(jReference.getString("shareUrl"), itemIds, jReference.getString("folderId"), expiration, jReference.getInt("userId"), jReference.getInt("contextId"));
        } catch (java.util.zip.ZipException e) {
            // A GZIP format error has occurred or the compression method used is unsupported
            throw new IllegalArgumentException("Invalid reference string", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * A builder for a share reference.
     */
    public static class Builder {

        private final int contextId;
        private final int userId;
        private String folderId;
        private List<String> itemIds;
        private String shareUrl;
        private Date expiration;

        /**
         * Initializes a new {@link Builder}.
         *
         * @param userId The user identifier
         * @param contextId The context identifier
         */
        public Builder(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
        }

        /**
         * Sets the folder identifier
         *
         * @param folderId The folder identifier
         * @return This builder instance
         */
        public Builder folderId(String folderId) {
            this.folderId = folderId;
            return this;
        }

        /**
         * Sets the item identifiers
         *
         * @param itemIds The item identifiers
         * @return This builder instance
         */
        public Builder itemIds(List<String> itemIds) {
            this.itemIds = itemIds;
            return this;
        }

        /**
         * Sets the share URL
         *
         * @param shareUrl The share URL
         * @return This builder instance
         */
        public Builder shareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
            return this;
        }

        /**
         * Sets the expiration date
         *
         * @param expiration The expiration date
         * @return This builder instance
         */
        public Builder expiration(Date expiration) {
            this.expiration = expiration;
            return this;
        }

        /**
         * Creates the appropriate {@code ShareReference} instance according to this builder's arguments.
         *
         * @return The {@code ShareReference} instance
         */
        public ShareReference build() {
            return new ShareReference(shareUrl, itemIds, folderId, expiration, userId, contextId);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final String folderId;
    private final List<String> itemIds;
    private final String shareUrl;
    private final Date expiration;

    /**
     * Initializes a new {@link ShareReference}.
     *
     * @param shareUrl The associated share URL
     * @param itemIds The identifiers of the shared files
     * @param folderId The folder containing the files
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    private ShareReference(String shareUrl, List<String> itemIds, String folderId, Date expiration, int userId, int contextId) {
        super();
        this.shareUrl = shareUrl;
        this.itemIds = itemIds;
        this.folderId = folderId;
        this.expiration = expiration;
        this.userId = userId;
        this.contextId = contextId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the folder identifier
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Gets the item identifiers
     *
     * @return The item identifiers
     */
    public List<String> getItemIds() {
        return itemIds;
    }

    /**
     * Gets the share URL
     *
     * @return The share URL
     */
    public String getShareUrl() {
        return shareUrl;
    }

    /**
     * Gets the optional expiration date
     *
     * @return The expiration date or <code>null</code>
     */
    public Date getExpiration() {
        return expiration;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("[contextId=").append(contextId).append(", userId=").append(userId).append(", ");
        if (folderId != null) {
            sb.append("folderId=").append(folderId).append(", ");
        }
        if (itemIds != null) {
            sb.append("itemIds=").append(itemIds).append(", ");
        }
        if (shareUrl != null) {
            sb.append("shareUrl=").append(shareUrl).append(", ");
        }
        if (expiration != null) {
            sb.append("expiration=").append(expiration);
        }
        sb.append("]");
        return sb.toString();
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
