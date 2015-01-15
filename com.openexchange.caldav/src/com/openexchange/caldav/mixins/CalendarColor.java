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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.caldav.mixins;

import java.util.Map;
import java.util.regex.Pattern;
import org.jdom2.Namespace;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.resources.CommonFolderCollection;
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
    public static final Namespace NAMESPACE = CaldavProtocol.APPLE_NS;

	private final CommonFolderCollection<?> collection;

	/**
	 * Initializes a new {@link CalendarColor}.
	 *
	 * @param collection The underlying collection
	 */
    public CalendarColor(CommonFolderCollection<?> collection) {
        super(NAMESPACE.getURI(), NAME);
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null != collection && null != collection.getFolder() && PrivateType.getInstance().equals(collection.getFolder().getType())) {
            Map<String, Object> meta = collection.getFolder().getMeta();
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
        return null;
    }

    /**
     * Parses the color value from the supplied WebDAV property.
     *
     * @param property The property to parse
     * @return The color value, or <code>null</code> if not or incorrectly set
     */
    public static String parse(WebdavProperty property) {
        if (null != property && NAMESPACE.getURI().equals(property.getNamespace()) && NAME.equals(property.getName())) {
            String value = property.getValue();
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

    private static String mapColorLabel(int colorLabel) {
        switch (colorLabel) {
            case 1:
                return "#D0EAFFFF";
            case 2:
                return "#96BBE8FF";
            case 3:
                return "#BC99DFFF";
            case 4:
                return "#F4C4F4FF";
            case 5:
                return "#EDAFB1FF";
            case 6:
                return "#F4C18CFF";
            case 7:
                return "#F7ECB8FF";
            case 8:
                return "#CEDD87FF";
            case 9:
                return "#97B857FF";
            case 10:
                return "#666666FF";
            default:
                return null;
        }
    }

}
