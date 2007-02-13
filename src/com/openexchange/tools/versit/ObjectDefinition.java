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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

/**
 * @author Viktor Pracht
 */
public class ObjectDefinition implements VersitDefinition {

	private HashMap Properties = new HashMap();

	private HashMap Children = new HashMap();

	public static final ObjectDefinition Default = new ObjectDefinition();

	public ObjectDefinition() {
	}

	public ObjectDefinition(String[] propertyNames,
			PropertyDefinition[] properties, String[] childNames,
			ObjectDefinition[] children) {
		for (int i = 0; i < properties.length; i++)
			addProperty(propertyNames[i], properties[i]);
		for (int i = 0; i < childNames.length; i++)
			addChild(childNames[i], children[i]);
	}

	private ObjectDefinition(HashMap properties, HashMap children) {
		Properties.putAll(properties);
		Children.putAll(children);
	}

	public PropertyDefinition getProperty(String name) {
		return (PropertyDefinition) Properties.get(name.toUpperCase());
	}

	public void addProperty(String name, PropertyDefinition property) {
		Properties.put(name.toUpperCase(), property);
	}

	public void addChild(String name, ObjectDefinition child) {
		Children.put(name.toUpperCase(), child);
	}

	protected Property parseProperty(Scanner s) throws IOException {
		while (s.peek == -2)
			s.read();
		String name = s.parseName();
		if (name.length() == 0)
			return null;
		if (s.peek == '.') {
			s.read();
			name = s.parseName();
			if (name.length() == 0)
				return null;
		}
		PropertyDefinition propdef = getProperty(name);
		if (propdef == null)
			propdef = PropertyDefinition.Default;
		return propdef.parse(s, name);
	}

	public Reader getReader(InputStream stream, String charset)
			throws IOException {
		return new ReaderScanner(new InputStreamReader(stream, charset));
	}

	public VersitObject parse(VersitDefinition.Reader reader)
			throws IOException {
		VersitObject child, object = parseBegin(reader);
		if (object != null)
			while ((child = parseChild(reader, object)) != null)
				object.addChild(child);
		return object;
	}

	public VersitObject parseBegin(VersitDefinition.Reader reader)
			throws IOException {
		Scanner s = (Scanner) reader;
		Property begin;
		do {
			while (s.peek == -2)
				s.read();
			if (s.peek == -1)
				return null;
			begin = ObjectDefinition.Default.parseProperty(s);
			if (begin == null)
				while (s.peek != -1 && s.peek != -2)
					s.read();
		} while (begin == null || !begin.name.equalsIgnoreCase("BEGIN"));
		VersitObject object = new VersitObject((String) begin.getValue());
		return object;
	}

	public VersitObject parseChild(VersitDefinition.Reader reader,
			VersitObject object) throws IOException {
		Scanner s = (Scanner) reader;
		Property property = parseProperty(s);
		while (property != null && !property.name.equalsIgnoreCase("END")) {
			if (property.name.equalsIgnoreCase("BEGIN")) {
				String childName = ((String) property.getValue()).toUpperCase();
				VersitDefinition def = getChildDef(childName);
				VersitObject grandchild, child = new VersitObject(childName);
				while ((grandchild = def.parseChild(s, child)) != null)
					child.addChild(grandchild);
				return child;
			}
			object.addProperty(property);
			property = parseProperty(s);
		}
		if (property == null)
			throw new VersitException(s, "Incomplete object");
		return null;
	}

	public Writer getWriter(OutputStream stream, String charset)
			throws IOException {
		return new FoldingWriter(new OutputStreamWriter(stream, charset));
	}

	public void write(VersitDefinition.Writer writer, VersitObject object)
			throws IOException {
		writeProperties(writer, object);
		writeEnd(writer, object);
	}

	public void writeProperties(VersitDefinition.Writer writer,
			VersitObject object) throws IOException {
		FoldingWriter fw = (FoldingWriter) writer;
		fw.write("BEGIN:");
		fw.writeln(object.name);
		int count = object.getPropertyCount();
		for (int i = 0; i < count; i++) {
			Property property = object.getProperty(i);
			PropertyDefinition definition = getProperty(property.name);
			if (definition != null)
				definition.write(fw, property);
		}
		count = object.getChildCount();
		for (int i = 0; i < count; i++) {
			VersitObject child = object.getChild(i);
			VersitDefinition definition = getChildDef(child.name);
			definition.write(fw, child);
		}
	}

	public void writeEnd(VersitDefinition.Writer writer, VersitObject object)
			throws IOException {
		FoldingWriter fw = (FoldingWriter) writer;
		fw.write("END:");
		fw.writeln(object.name);
	}

	public VersitDefinition getChildDef(String name) {
		VersitDefinition child = (ObjectDefinition) Children.get(name
				.toUpperCase());
		if (child == null)
			return ObjectDefinition.Default;
		return child;
	}

	public Iterator iterator() {
		return Properties.values().iterator();
	}

	public VersitDefinition copy() {
		return new ObjectDefinition(Properties, Children);
	}

}
