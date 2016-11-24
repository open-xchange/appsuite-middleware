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

package com.openexchange.dav.mixins;

import java.util.Map;
import java.util.regex.Pattern;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarColor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CalendarColor extends SingleXMLPropertyMixin {

    public static final String NAME = "calendar-color";
    public static final Namespace NAMESPACE = DAVProtocol.APPLE_NS;
    public static final String SYMBOLIC_COLOR = "symbolic-color";

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link CalendarColor}.
     *
     * @param collection The underlying collection
     */
    public CalendarColor(FolderCollection<?> collection) {
        super(NAMESPACE.getURI(), NAME);
        this.collection = collection;
    }

    @Override
    protected void configureProperty(WebdavProperty property) {
        property.setXML(true);
        String value = getValue();
        property.setValue(value);
        if (null != value) {
            property.addAttribute(SYMBOLIC_COLOR, getSymbolicColor(value));
        }
    }

    @Override
    protected String getValue() {
        return getValue(collection.getFolder());
    }

    private static String getValue(UserizedFolder folder) {
        if (null != folder && PrivateType.getInstance().equals(folder.getType())) {
            Map<String, Object> meta = folder.getMeta();
            if (null != meta) {
                if (meta.containsKey("color")) {
                    Object value = meta.get("color");
                    if (null != value && String.class.isInstance(value)) {
                        return String.valueOf(value);
                    }
                }
                if (meta.containsKey("color_label")) {
                    String value = mapColorLabel(meta.get("color_label"));
                    if (null != value) {
                        return value;
                    }
                }
            }
        }
        return "#CEE7FFFF"; // Mac OS client does not like null or empty values
    }

    /**
     * Parses the color value from the supplied WebDAV property.
     *
     * @param property The property to parse
     * @return The color value, or <code>null</code> if not or incorrectly set
     */
    public static String parse(WebdavProperty property) {
        if (null != property && NAMESPACE.getURI().equals(property.getNamespace()) && NAME.equals(property.getName())) {
            String value;
            if (DAVProperty.class.isInstance(property)) {
                Element colorElement = ((DAVProperty) property).getElement();
                value = null != colorElement ? colorElement.getValue() : null;
            } else {
                value = property.getValue();
            }
            if (false == Strings.isEmpty(value)) {
                value = value.toUpperCase().trim();
                if (Pattern.matches("^\\#([A-F0-9]{8})$", value)) {
                    return value;
                }
                if (Pattern.matches("^\\#([A-F0-9]{6})$", value)) {
                    return value + "FF";
                }
                if (Pattern.matches("^\\#([A-F0-9]{3})$", value)) {
                    char r = value.charAt(1);
                    char g = value.charAt(2);
                    char b = value.charAt(3);
                    return '#' + r + r + g + g + b + b + "FF";
                }
            }
        }
        return null;
    }

    private static String mapColorLabel(Object colorLabel) {
        if (null != colorLabel) {
            if (Integer.class.isInstance(colorLabel)) {
                return mapColorLabel(((Integer) colorLabel).intValue());
            }
            try {
                String value = String.valueOf(colorLabel);
                return mapColorLabel(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Maps the OX color label to its corresponding hex color value.
     *
     * @param colorLabel The color label
     * @return The hex color, or <code>null</code> if their is no mapping
     */
    private static String mapColorLabel(int colorLabel) {
        switch (colorLabel) {
            case 1:
                return "#CEE7FFFF";
            case 2:
                return "#96BBE8FF";
            case 3:
                return "#C4AFE3FF";
            case 4:
                return "#F0D8F0FF";
            case 5:
                return "#F2D1D2FF";
            case 6:
                return "#FFD1A3FF";
            case 7:
                return "#F7EBB6FF";
            case 8:
                return "#D4DEA7FF";
            case 9:
                return "#99AF6EFF";
            case 10:
                return "#666666FF";
            default:
                return null;
        }
    }

    /**
     * Maps the Apple Hex Code returns 'null' if no matching is available
     *
     * @param hex
     * @return
     */
    public static Integer mapColorLabel(String symbolicColor) {
        switch (symbolicColor) {
            case "custom":
                return null;
            case "blue":
                return 2;
            case "purple":
                return 3;
            case "red":
                return 5;
            case "orange":
                return 6;
            case "yellow":
                return 7;
            case "green":
                return 9;
            case "brown":
                return 10;
            default:
                return null;
        }
    }

    /**
     * Gets the symbolic color as used by the Mac OS client.
     *
     * @param hexColor The hexadecimal color value
     * @return The symbolic color name, or <code>custom</code> if no symbolic color can be matched
     */
    private static String getSymbolicColor(String hexColor) {
        switch (hexColor.toUpperCase()) {
            case "#FB0055FF":
                return "red";
            case "#FD8208FF":
                return "orange";
            case "#FEC309FF":
                return "yellow";
            case "#56D72BFF":
                return "green";
            case "#1D9BF6FF":
                return "blue";
            case "#90714CFF":
                return "brown";
            case "#BF57DAFF":
                return "purple";
            default:
                return "custom";
        }
    }

}
