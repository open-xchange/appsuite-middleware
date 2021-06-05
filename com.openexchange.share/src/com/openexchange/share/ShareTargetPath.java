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

package com.openexchange.share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import com.google.common.io.BaseEncoding;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;

/**
 * Encapsulates the path to a shared folder or item that is used to generate
 * share links for guests. All contained IDs are valid from a global perspective,
 * i.e. are physical IDs that can be addressed without impersonating a certain user.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareTargetPath {

    private final int module;
    private final String folder;
    private final String item;
    private final Map<String, String> additionals;

    /**
     * Initializes a new {@link ShareTargetPath}.
     *
     * @param module The module ID.
     * @param folder The folder ID. Must be a full-qualified global identifier.
     * @param item The item ID or <code>null</code>, if a folder is addressed. Must be a full-qualified global identifier.
     */
    public ShareTargetPath(int module, String folder, String item) {
        this(module, folder, item, null);
    }

    /**
     * Initializes a new {@link ShareTargetPath}.
     *
     * @param module The module ID.
     * @param folder The folder ID. Must be a full-qualified global identifier.
     * @param item The item ID or <code>null</code>, if a folder is addressed. Must be a full-qualified global identifier.
     * @param additionals Additional arbitrary metadata, or <code>null</code> if not set
     */
    public ShareTargetPath(int module, String folder, String item, Map<String, String> additionals) {
        super();
        this.module = module;
        this.folder = folder;
        this.item = item;
        this.additionals = additionals;
    }

    /**
     * Gets the module ID.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Gets the folder ID.
     *
     * @return The folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Gets the item ID.
     *
     * @return The item
     */
    public String getItem() {
        return item;
    }

    /**
     * Gets the additional metadata.
     *
     * @return The metadata, or <code>null</code> if not set
     */
    public Map<String, String> getAdditionals() {
        return additionals;
    }

    /**
     * Gets whether this path denotes a folder or item.
     *
     * @return <code>true</code> for a folder
     */
    public boolean isFolder() {
        return item == null;
    }

    /**
     * Gets the path representation with a leading slash.
     * Example: /1/8/NzYxMjE.
     *
     * @return The path
     */
    public String get() {
        int version;
        List<String> segments;
        if (null == additionals) {
            version = 1;
            segments = encodeV1Segments(this);
        } else {
            version = 2;
            segments = encodeV2Segments(this);
        }
        StringBuilder stringBuilder = new StringBuilder(64).append('/').append(version);
        for (String segment : segments) {
            stringBuilder.append('/').append(segment);
        }
        return stringBuilder.toString();
    }

    /**
     * Parses the path of a share URL. The path must start at the version segment.
     * <p/>
     * Input examples:
     * <ul>
     * <li><code>/1/8/NzYxMjE</code></li>
     * <li><code>/2/m8/fOTU0Mg/iOTU0Mi8zMjExMTA/ac8O2bmRlciUvw7xAXMOqemV-LmljaGVu~Z2VodCBhdWNoIQ.cmVjaXBpZW50~amFuLmZpbnNlbEBwcmVtaXVt.d3Vyc3Q~Z3V0</code></li>
     * </ul>
     *
     * @param path The path
     * @return A {@link ShareTargetPath} instance or <code>null</code>, if the passed input is no valid path.
     */
    public static ShareTargetPath parse(String path) {
        return parse(Strings.splitAndTrim(path, "/"));
    }

    /**
     * Parses a list of path segments. The first segment must be the version segment, all further segments are version-specific.
     *
     * @param segments The segments
     * @return A {@link ShareTargetPath} instance or <code>null</code>, if the passed input is no valid path.
     */
    public static ShareTargetPath parse(List<String> segments) {
        try {
            Iterator<String> iterator = segments.iterator();
            int version = -1;
            while (iterator.hasNext()) {
                String segment = iterator.next();
                if (Strings.isEmpty(segment)) {
                    iterator.remove();
                } else if (-1 == version) {
                    version = Integer.parseInt(segment);
                    iterator.remove();
                }
            }
            switch (version) {
                case 1:
                    return decodeV1Segments(segments);
                case 2:
                    return decodeV2Segments(segments);
                default:
                    throw new IllegalArgumentException("Unknown path version: " + version);
            }
        } catch (RuntimeException e) {
            org.slf4j.LoggerFactory.getLogger(ShareTargetPath.class).debug("Error parsing share target path: {}", e.getMessage(), e);
        }
        return null;
    }

    private static List<String> encodeV1Segments(ShareTargetPath path) {
        List<String> segments = new ArrayList<String>(3);
        segments.add(String.valueOf(path.getModule()));
        segments.add(base64(path.getFolder(), true));
        if (null != path.getItem()) {
            segments.add(base64(path.getItem(), true));
        }
        return segments;
    }

    private static ShareTargetPath decodeV1Segments(List<String> segments) {
        if (null == segments || 2 != segments.size() && 3 != segments.size()) {
            throw new IllegalArgumentException("Unexpected number of path segments");
        }
        int module = Integer.parseInt(segments.get(0));
        String folder = base64(segments.get(1), false);
        String item = 3 == segments.size() ? base64(segments.get(2), false) : null;
        return new ShareTargetPath(module, folder, item);
    }

    private static List<String> encodeV2Segments(ShareTargetPath path) {
        List<String> segments = new ArrayList<String>(4);
        segments.add('m' + String.valueOf(path.getModule()));
        segments.add('f' + base64(path.getFolder(), true));
        if (null != path.getItem()) {
            segments.add('i' + base64(path.getItem(), true));
        }
        if (null != path.getAdditionals() && 0 < path.getAdditionals().size()) {
            StringBuilder stringBuilder = new StringBuilder(64).append('a');
            ArrayList<Entry<String, String>> entries = new ArrayList<Entry<String, String>>(path.getAdditionals().entrySet());
            stringBuilder.append(base64(entries.get(0).getKey(), true)).append('~').append(base64(entries.get(0).getValue(), true));
            for (int i = 1; i < entries.size(); i++) {
                stringBuilder.append('.').append(base64(entries.get(i).getKey(), true)).append('~').append(base64(entries.get(i).getValue(), true));
            }
            segments.add(stringBuilder.toString());
        }
        return segments;
    }

    private static ShareTargetPath decodeV2Segments(List<String> segments) {
        if (null == segments || 2 > segments.size()) {
            throw new IllegalArgumentException("Unexpected number of path segments");
        }
        int module = -1;
        String folder = null;
        String item = null;
        Map<String, String> meta = null;
        for (String segment : segments) {
            if (null == segment || 2 > segment.length()) {
                throw new IllegalArgumentException("Unexpected segment length");
            }
            String value = segment.substring(1);
            switch (segment.charAt(0)) {
                case 'm':
                    module = Integer.parseInt(value);
                    break;
                case 'f':
                    folder = base64(value, false);
                    break;
                case 'i':
                    item = base64(value, false);
                    break;
                case 'a':
                    String[] entries = Strings.splitByDots(value);
                    meta = new HashMap<String, String>(entries.length);
                    for (String entry : Strings.splitByDots(value)) {
                        int index = entry.indexOf('~');
                        meta.put(base64(entry.substring(0, index), false), base64(entry.substring(index + 1), false));
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected path segment: " + segment);
            }
        }
        if (-1 == module || null == folder) {
            throw new IllegalArgumentException("Incomplete share target path");
        }
        return new ShareTargetPath(module, folder, item, meta);
    }

    private static String base64(String input, boolean encode) {
        if (input == null) {
            return null;
        }

        if (encode) {
            return BaseEncoding.base64Url().omitPadding().encode(input.getBytes(Charsets.UTF_8));
        }

        return new String(BaseEncoding.base64Url().omitPadding().decode(input), Charsets.UTF_8);
    }

    /**
     * Gets a value indicating whether the targeted folder or item matches the one specified by another share target path.
     * <p/>
     * Any <i>additional</i> parameters are ignored implicitly.
     *
     * @param other The target path to match
     * @return <code>true</code> if the share targets point to the same folder or item, <code>false</code>, otherwise
     */
    public boolean matches(ShareTargetPath other) {
        return null != other && module == other.module && Objects.equals(folder, other.folder) && Objects.equals(item, other.item);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + module;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ShareTargetPath other = (ShareTargetPath) obj;
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!folder.equals(other.folder)) {
            return false;
        }
        if (item == null) {
            if (other.item != null) {
                return false;
            }
        } else if (!item.equals(other.item)) {
            return false;
        }
        if (module != other.module) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return get();
    }

}
