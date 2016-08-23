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

import static com.openexchange.mail.json.compose.share.ShareComposeConstants.HEADER_SHARE_REFERENCE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.utils.MailPasswordUtil;

/**
 * {@link ShareReference} - References shared folder/items.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareReference {

    /** The key. */
    private static final Key KEY = MailPasswordUtil.generateSecretKey(Charsets.toAsciiBytes("jsonkeyp"));

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
            JSONObject jReference = new JSONObject(MailPasswordUtil.decrypt(referenceString, KEY));
            List<Item> items;
            {
                JSONArray jItems = jReference.getJSONArray("items");
                int length = jItems.length();
                items = new ArrayList<Item>(length);
                for (int i = 0; i < length; i++) {
                    items.add(parseItemFrom(jItems.getJSONObject(i)));
                }
            }
            Date expiration = null;
            if (jReference.hasAndNotNull("expiration")) {
                expiration = new Date(jReference.getLong("expiration"));
            }
            return new ShareReference(jReference.getString("shareToken"), items, parseItemFrom(jReference.getJSONObject("folder")), expiration, jReference.optString("password", null), jReference.getInt("userId"), jReference.getInt("contextId"));
        } catch (java.security.GeneralSecurityException e) {
            // A GZIP format error has occurred or the compression method used is unsupported
            throw new IllegalArgumentException("Invalid reference string", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates a string from specified share reference suitable to be set as header value for a MIME message.
     *
     * @param shareReference The share reference
     * @return The pure folded header value
     */
    public static String generateStringForMime(ShareReference shareReference) {
        if (null == shareReference) {
            return null;
        }

        String str = shareReference.generateReferenceString();
        String splitted = split4Mime(str);
        String folded = MimeMessageUtility.fold(USED, splitted);
        return folded;
    }

    /**
     * Parses the share reference taken as header value from a MIME message.
     *
     * @param fromMime The pure folded header value
     * @return The parsed share reference
     */
    public static ShareReference parseFromMime(String fromMime) {
        if (null == fromMime) {
            return null;
        }

        String unfolded = MimeMessageUtility.unfold(fromMime);
        String unsplitted = unsplit(unfolded);
        return ShareReference.parseFromReferenceString(unsplitted);
    }

    private static Item parseItemFrom(JSONObject jItem) throws JSONException {
        if (null == jItem) {
            return null;
        }
        return new Item(jItem.getString("id"), jItem.getString("name"));
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    /**
     * A builder for a share reference.
     */
    public static class Builder {

        private final int contextId;
        private final int userId;
        private Item folder;
        private List<Item> items;
        private String shareToken;
        private Date expiration;
        private String password;

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
         * @param folder The folder
         * @return This builder instance
         */
        public Builder folder(Item folder) {
            this.folder = folder;
            return this;
        }

        /**
         * Sets the items
         *
         * @param items The items
         * @return This builder instance
         */
        public Builder items(List<Item> items) {
            this.items = items;
            return this;
        }

        /**
         * Sets the share token
         *
         * @param shareToken The share token
         * @return This builder instance
         */
        public Builder shareToken(String shareToken) {
            this.shareToken = shareToken;
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
         * Sets the password
         *
         * @param password The password
         * @return This builder instance
         */
        public Builder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Creates the appropriate {@code ShareReference} instance according to this builder's arguments.
         *
         * @return The {@code ShareReference} instance
         */
        public ShareReference build() {
            return new ShareReference(shareToken, items, folder, expiration, password, userId, contextId);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final Item folder;
    private final List<Item> items;
    private final String shareToken;
    private final Date expiration;
    private final String password;

    /**
     * Initializes a new {@link ShareReference}.
     *
     * @param shareToken The associated share token
     * @param items The shared files
     * @param folder The folder containing the files
     * @param expiration The optional expiration date
     * @param password The optional password
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    ShareReference(String shareToken, List<Item> items, Item folder, Date expiration, String password, int userId, int contextId) {
        super();
        this.shareToken = shareToken;
        this.items = items;
        this.folder = folder;
        this.expiration = expiration;
        this.password = password;
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
     * Gets the folder
     *
     * @return The folder
     */
    public Item getFolder() {
        return folder;
    }

    /**
     * Gets the items
     *
     * @return The items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Gets the share token
     *
     * @return The share token
     */
    public String getShareToken() {
        return shareToken;
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
     * Gets the optional password
     *
     * @return The password or <code>null</code>
     */
    public String getPassword() {
        return password;
    }

    /**
     * Generates the reference string.
     *
     * @return The reference string
     */
    public String generateReferenceString() {
        try {
            JSONObject jReference = new JSONObject(8);
            jReference.put("shareToken", shareToken);
            jReference.put("contextId", contextId);
            jReference.put("userId", userId);
            jReference.put("folder", new JSONObject(2).put("id", folder.getId()).put("name", folder.getName()));
            {
                JSONArray jItems = new JSONArray(items.size());
                for (Item item : items) {
                    jItems.put(new JSONObject(2).put("id", item.getId()).put("name", item.getName()));
                }
                jReference.put("items", jItems);
            }
            if (null != expiration) {
                jReference.put("expiration", expiration.getTime());
            }
            if (null != password) {
                jReference.put("password", password);
            }
            return MailPasswordUtil.encrypt(jReference.toString(), KEY);
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
        sb.append("[contextId=").append(contextId).append(", userId=").append(userId);
        if (folder != null) {
            sb.append(", folder=").append(folder);
        }
        if (items != null) {
            sb.append(", items=").append(items);
        }
        if (shareToken != null) {
            sb.append(", shareToken=").append(shareToken);
        }
        if (expiration != null) {
            sb.append(", expiration=").append(expiration);
        }
        if (password != null) {
            sb.append(", password=").append(password);
        }
        sb.append(']');
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

    private static String unsplit(String s) {
        int length = s.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0, k = length; k-- > 0; i++) {
            char c = s.charAt(i);
            if (' ' != c) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static final int USED = HEADER_SHARE_REFERENCE.length() + 2;

    private static String split4Mime(String s) {
        int chunkSize = 76;
        int length = s.length();
        if (length <= chunkSize) {
            return s;
        }

        StringBuilder sb = new StringBuilder(length + 16);
        boolean first = true;
        for (int i = 0; i < length;) {
            if (first) {
                first = false;
                int cs = chunkSize - USED;
                sb.append(s.substring(i, Math.min(length, i + cs)));
                i += cs;
            } else {
                sb.append(' ');
                sb.append(s.substring(i, Math.min(length, i + chunkSize)));
                i += chunkSize;
            }
        }
        return sb.toString();
    }

}
