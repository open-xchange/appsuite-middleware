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
    public SupportedCalendarComponentSetProperty(SupportedCalendarComponentSetProperty.Comp...components) {
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
