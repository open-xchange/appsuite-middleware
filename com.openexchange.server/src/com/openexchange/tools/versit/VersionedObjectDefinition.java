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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.versit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.openexchange.java.Charsets;

/**
 * @author Viktor Pracht
 */
public class VersionedObjectDefinition extends ObjectDefinition {

    private final Map<String, ObjectDefinition> Definitions = new HashMap<String, ObjectDefinition>();

    private ObjectDefinition Definition = ObjectDefinition.Default;

    public VersionedObjectDefinition() {
        super();
    }

    private VersionedObjectDefinition(final Map<String, ObjectDefinition> definitions, final ObjectDefinition definition) {
        final int size = definitions.size();
        final Iterator<Map.Entry<String, ObjectDefinition>> iter = definitions.entrySet().iterator();
        for (int k = 0; k < size; k++) {
            final Map.Entry<String, ObjectDefinition> entry = iter.next();
            final ObjectDefinition od = entry.getValue();
            final ObjectDefinition copy = (ObjectDefinition) od.copy();
            addDefinition(entry.getKey(), copy);
            if (od.equals(definition)) {
                Definition = copy;
            }
        }
        Definition = definition;
    }

    public VersionedObjectDefinition(final String[] versions, final ObjectDefinition[] definitions) {
        for (int i = 0; i < versions.length; i++) {
            addDefinition(versions[i], definitions[i]);
        }
    }

    public final void addDefinition(final String version, final ObjectDefinition definition) {
        if (Definitions.isEmpty()) {
            Definition = definition;
        }
        Definitions.put(version, definition);
    }

    public void setVersion(final String version) {
        Definition = Definitions.get(version);
        if (Definition == null) {
            Definition = ObjectDefinition.Default;
        }
    }

    @Override
    public Reader getReader(final InputStream stream, final String charset) throws IOException {
        return new ReaderScanner(new InputStreamReader(stream, Charsets.forName(charset)));
    }

    @Override
    public VersitObject parseChild(final Reader reader, final VersitObject object) throws IOException {
        final Scanner s = (Scanner) reader;
        Property property = Definition.parseProperty(s);
        while (property != null) {
            if (property.name.equalsIgnoreCase("END") && (((String) property.getValue()).equalsIgnoreCase(object.name))) {
                // if (((String) property.getValue()).equalsIgnoreCase(object.name)) {
                break;
                // }
            }
            if (property.name.equalsIgnoreCase("BEGIN")) {
                final String childName = ((String) property.getValue()).toUpperCase();
                final VersitDefinition def = Definition.getChildDef(childName);
                VersitObject grandchild;
                final VersitObject child = new VersitObject(childName);
                while ((grandchild = def.parseChild(s, child)) != null) {
                    child.addChild(grandchild);
                }
                return child;
            }
            if (!property.isInvalid()) {
                object.addProperty(property);
            }
            if (property.name.equalsIgnoreCase("VERSION")) {
                setVersion(property.getValue().toString());
            }
            property = Definition.parseProperty(s);
        }
        if (property == null) {
            throw new VersitException(s, "Incomplete object");
        }
        return null;
    }

    @Override
    public void write(final Writer writer, final VersitObject object) throws IOException {
        final Property VersionProperty = object.getProperty("VERSION");
        if (VersionProperty != null) {
            setVersion((String) VersionProperty.getValue());
        }
        Definition.write(writer, object);
    }

    @Override
    public void writeProperties(final Writer writer, final VersitObject object) throws IOException {
        final Property VersionProperty = object.getProperty("VERSION");
        if (VersionProperty != null) {
            setVersion((String) VersionProperty.getValue());
        }
        Definition.writeProperties(writer, object);
    }

    @Override
    public void writeEnd(final Writer writer, final VersitObject object) throws IOException {
        Definition.writeEnd(writer, object);
    }

    @Override
    public VersitDefinition getChildDef(final String name) {
        return Definition.getChildDef(name);
    }

    @Override
    public Iterator<PropertyDefinition> iterator() {
        return Definition.iterator();
    }

    @Override
    public VersitDefinition copy() {
        return new VersionedObjectDefinition(Definitions, Definition);
    }

}
