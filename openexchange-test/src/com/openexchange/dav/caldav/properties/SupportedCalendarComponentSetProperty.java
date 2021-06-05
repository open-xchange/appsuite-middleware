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

package com.openexchange.dav.caldav.properties;

import java.util.HashSet;
import java.util.Set;
import org.apache.jackrabbit.webdav.property.AbstractDavProperty;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.dav.PropertyNames;

/**
 * {@link SupportedCalendarComponentSetProperty}
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SupportedCalendarComponentSetProperty extends AbstractDavProperty<Set<SupportedCalendarComponentSetProperty.Comp>> {

    private final Set<SupportedCalendarComponentSetProperty.Comp> components = new HashSet<SupportedCalendarComponentSetProperty.Comp>();

    /**
     * Initializes a new {@link SupportedCalendarComponentSetProperty}.
     * 
     * @param name
     * @param isInvisibleInAllprop
     */
    public SupportedCalendarComponentSetProperty(SupportedCalendarComponentSetProperty.Comp... components) {
        super(PropertyNames.SUPPORTED_CALENDAR_COMPONENT_SET, true);
        for (Comp comp : components) {
            this.components.add(comp);
        }
    }

    @Override
    public Set<Comp> getValue() {
        return components;
    }

    public static class Comp implements XmlSerializable {

        public static final Comp VTODO = new Comp("VTODO");

        public static final Comp VEVENT = new Comp("VEVENT");

        private final String name;

        private Comp(String name) {
            super();
            this.name = name;
        }

        @Override
        public Element toXml(Document document) {
            Element element = PropertyNames.COMP.toXml(document);
            DomUtil.setAttribute(element, "name", null, name);
            return element;
        }

    }

}
