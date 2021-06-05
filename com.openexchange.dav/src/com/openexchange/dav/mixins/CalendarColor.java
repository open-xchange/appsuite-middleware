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

package com.openexchange.dav.mixins;

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.optPropertyValue;
import static com.openexchange.java.Autoboxing.I;
import java.util.Map;
import java.util.regex.Pattern;
import org.jdom2.Element;
import org.jdom2.Namespace;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.folderstorage.CalendarFolderConverter;
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
        if (null != folder) {
            Object value = optPropertyValue(CalendarFolderConverter.getExtendedProperties(folder), COLOR_LITERAL);
            if (null != value && String.class.isInstance(value)) {
                return (String) value;
            }
        }
        return getValueFromMeta(folder);
    }

    private static String getValueFromMeta(UserizedFolder folder) {
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
            if (Strings.isNotEmpty(value)) {
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
                return I(2);
            case "purple":
                return I(3);
            case "red":
                return I(5);
            case "orange":
                return I(6);
            case "yellow":
                return I(7);
            case "green":
                return I(9);
            case "brown":
                return I(10);
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
            case "RED":
                return "red";
            case "#FD8208FF":
            case "ORANGE":
                return "orange";
            case "#FEC309FF":
            case "YELLOW":
                return "yellow";
            case "#56D72BFF":
            case "GREEN":
                return "green";
            case "#1D9BF6FF":
            case "BLUE":
                return "blue";
            case "#90714CFF":
            case "BROWN":
                return "brown";
            case "#BF57DAFF":
            case "PURPLE":
                return "purple";
            default:
                return "custom";
        }
    }

}
