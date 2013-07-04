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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import com.openexchange.java.Charsets;

/**
 * @author Viktor Pracht
 * @author Tobias Prinz (handling of invalid values, bug 8527)
 */
public class ObjectDefinition implements VersitDefinition {

    private final Map<String, PropertyDefinition> Properties = new HashMap<String, PropertyDefinition>();

    private final Map<String, ObjectDefinition> Children = new HashMap<String, ObjectDefinition>();

    public static final ObjectDefinition Default = new ObjectDefinition();

    public ObjectDefinition() {
        super();
    }

    public ObjectDefinition(final String[] propertyNames, final PropertyDefinition[] properties, final String[] childNames, final ObjectDefinition[] children) {
        for (int i = 0; i < properties.length; i++) {
            addProperty(propertyNames[i], properties[i]);
        }
        for (int i = 0; i < childNames.length; i++) {
            addChild(childNames[i], children[i]);
        }
    }

    private ObjectDefinition(final Map<String, PropertyDefinition> properties, final Map<String, ObjectDefinition> children) {
        Properties.putAll(properties);
        Children.putAll(children);
    }

    public PropertyDefinition getProperty(final String name) {
        return Properties.get(name.toUpperCase(Locale.ENGLISH));
    }

    public final void addProperty(final String name, final PropertyDefinition property) {
        Properties.put(name.toUpperCase(Locale.ENGLISH), property);
    }

    public final void addChild(final String name, final ObjectDefinition child) {
        Children.put(name.toUpperCase(Locale.ENGLISH), child);
    }

    protected Property parseProperty(final Scanner s) throws IOException {
        while (s.peek == -2) {
            s.read();
        }
        String name = s.parseName();
        if (name.length() == 0) {
            return null;
        }
        if (s.peek == '.') {
            s.read();
            name = s.parseName();
            if (name.length() == 0) {
                return null;
            }
        }
        PropertyDefinition propdef = getProperty(name);
        if (propdef == null) {
            propdef = PropertyDefinition.Default;
        }
        return propdef.parse(s, name);
    }

    @Override
    public Reader getReader(final InputStream stream, final String charset) throws IOException {
        return new ReaderScanner(new InputStreamReader(stream, Charsets.forName(charset)));
    }

    @Override
    public VersitObject parse(final VersitDefinition.Reader reader) throws IOException {
        VersitObject child;
        final VersitObject object = parseBegin(reader);
        if (object != null) {
            while ((child = parseChild(reader, object)) != null) {
                object.addChild(child);
            }
        }
        return object;
    }

    @Override
    public VersitObject parseBegin(final VersitDefinition.Reader reader) throws IOException {
        final Scanner s = (Scanner) reader;
        Property begin;
        do {
            while (s.peek == -2) {
                s.read();
            }
            if (s.peek == -1) {
                return null;
            }
            begin = ObjectDefinition.Default.parseProperty(s);
            if (begin == null) {
                while (s.peek != -1 && s.peek != -2) {
                    s.read();
                }
            }
        } while (begin == null || !begin.name.equalsIgnoreCase("BEGIN"));
        return new VersitObject((String) begin.getValue());
    }

    @Override
    public VersitObject parseChild(final VersitDefinition.Reader reader, final VersitObject object) throws IOException {
        final Scanner s = (Scanner) reader;
        Property property = parseProperty(s);
        while (property != null) {
            if (property.name.equalsIgnoreCase("END") && (((String) property.getValue()).equalsIgnoreCase(object.name))) {
                // if (((String) property.getValue()).equalsIgnoreCase(object.name)) {
                break;
                // }
            }
            if (property.name.equalsIgnoreCase("BEGIN")) {
                final String childName = ((String) property.getValue()).toUpperCase();
                final VersitDefinition def = getChildDef(childName);
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
            property = parseProperty(s);
        }
        if (property == null) {
            throw new VersitException(s, "Incomplete object");
        }
        return null;
    }

    @Override
    public Writer getWriter(final OutputStream stream, final String charset) throws IOException {
        return new FoldingWriter(new OutputStreamWriter(stream, charset));
    }

    @Override
    public void write(final VersitDefinition.Writer writer, final VersitObject object) throws IOException {
        writeProperties(writer, object);
        writeEnd(writer, object);
    }

    @Override
    public void writeProperties(final VersitDefinition.Writer writer, final VersitObject object) throws IOException {
        final FoldingWriter fw = (FoldingWriter) writer;
        fw.write("BEGIN:");
        fw.writeln(object.name);
        int count = object.getPropertyCount();
        for (int i = 0; i < count; i++) {
            final Property property = object.getProperty(i);
            final PropertyDefinition definition = getProperty(property.name);
            if (definition != null) {
                definition.write(fw, property);
            }
        }
        count = object.getChildCount();
        for (int i = 0; i < count; i++) {
            final VersitObject child = object.getChild(i);
            final VersitDefinition definition = getChildDef(child.name);
            definition.write(fw, child);
        }
    }

    @Override
    public void writeEnd(final VersitDefinition.Writer writer, final VersitObject object) throws IOException {
        final FoldingWriter fw = (FoldingWriter) writer;
        fw.write("END:");
        fw.writeln(object.name);
    }

    @Override
    public VersitDefinition getChildDef(final String name) {
        final VersitDefinition child = Children.get(name.toUpperCase(Locale.ENGLISH));
        if (child == null) {
            return ObjectDefinition.Default;
        }
        return child;
    }

    @Override
    public Iterator<PropertyDefinition> iterator() {
        return Properties.values().iterator();
    }

    @Override
    public VersitDefinition copy() {
        return new ObjectDefinition(Properties, Children);
    }

}
