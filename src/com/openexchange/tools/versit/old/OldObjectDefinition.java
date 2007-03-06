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



package com.openexchange.tools.versit.old;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;

public class OldObjectDefinition implements VersitDefinition {

	protected static final String[] NoNames = new String[] {};

	protected static final OldParamDefinition[] NoParams = new OldParamDefinition[] {};

	protected static final OldParamDefinition Encoding = new OldParamDefinition(
			new String[] { "7BIT", "8BIT", "QUOTED-PRINTABLE", "BASE64" });

	protected static final OldParamDefinition TextParam = new OldParamDefinition(
			NoNames);

	protected static final OldParamDefinition ValueParam = new OldParamDefinition(
			new String[] { "INLINE", "CONTENT-ID", "CID", "URL" });

	protected static final String[] DefaultParamNames = { "ENCODING",
			"CHARSET", "LANGUAGE", "VALUE" };

	protected static final OldParamDefinition[] DefaultParams = { Encoding,
			TextParam, TextParam, ValueParam };

	protected static final OldPropertyDefinition DefaultProperty = new OldPropertyDefinition(
			DefaultParamNames, DefaultParams);

	protected static final OldShortPropertyDefinition DateTimeProperty = new OldDateTimePropertyDefinition(
			DefaultParamNames, DefaultParams);

	protected String Name = null;

	protected HashMap<String, OldPropertyDefinition> Properties = new HashMap<String, OldPropertyDefinition>();

	protected HashMap<String, OldObjectDefinition> Children = new HashMap<String, OldObjectDefinition>();

	public OldObjectDefinition(String[] propertyNames,
			OldPropertyDefinition[] properties) {
		this(propertyNames, properties, new String[] {},
				new OldObjectDefinition[] {});
	}

	public OldObjectDefinition(String[] propertyNames,
			OldPropertyDefinition[] properties, String[] childNames,
			OldObjectDefinition[] children) {
		for (int i = 0; i < propertyNames.length; i++) {
			Properties.put(propertyNames[i].toUpperCase(), properties[i]);
		}
		for (int i = 0; i < childNames.length; i++) {
			Children.put(childNames[i].toUpperCase(), children[i]);
		}
	}

	private OldObjectDefinition(String name, HashMap<String, OldPropertyDefinition> properties,
			HashMap<String, OldObjectDefinition> children) {
		Name = name;
		Properties.putAll(properties);
		Children.putAll(children);
	}

	private String getType(final OldScanner s) throws IOException {
		s.skipWS();
		if (s.peek != ':') {
			return null;
		}
		s.read();
		s.skipWS();
		final String type = s.parseName();
		s.skipWS();
		if (s.peek == -2) {
			return type;
		}
		throw new VersitException(s, "Invalid BEGIN type");
	}

	public void write(final OldFoldingWriter fw, final VersitObject object)
			throws IOException {
		fw.write("BEGIN");
		fw.write(":");
		fw.writeln(object.name.getBytes(fw.charset));
		for (int i = 0; i < object.getPropertyCount(); i++) {
			final Property property = object.getProperty(i);
			final OldPropertyDefinition propdef = Properties.get(property.name);
			if (propdef != null) {
				propdef.write(fw, property);
			}
		}
		for (int i = 0; i < object.getChildCount(); i++) {
			final VersitObject child = object.getChild(i);
			final OldObjectDefinition objdef = Children.get(child.name);
			if (objdef != null) {
				objdef.write(fw, child);
			}
		}
		fw.write("END");
		fw.write(":");
		fw.writeln(object.name.getBytes(fw.charset));
	}

	public Reader getReader(final InputStream stream, final String charset)
			throws IOException {
		final OldScanner s = new OldScanner(stream);
		s.DefaultCharset = charset;
		s.DefaultEncoding = OldXBitEncoding.Default;
		return s;
	}

	public VersitObject parse(final Reader reader) throws IOException {
		VersitObject child, object = parseBegin(reader);
		if (object != null) {
			while ((child = parseChild(reader, object)) != null) {
				object.addChild(child);
			}
		}
		return object;
	}

	public VersitObject parseBegin(final Reader reader) throws IOException {
		final OldScanner s = (OldScanner) reader;
		final VersitObject object = new VersitObject(Name);
		while (s.peek != -1) {
			if ("BEGIN".equalsIgnoreCase(s.parseName())
					&& object.name.equalsIgnoreCase(getType(s))) {
				return object;
			}
			while (s.peek != -2 && s.peek != -1) {
				s.read();
			}
			while (s.peek == -2) {
				s.read();
			}
		}
		return null;
	}

	public VersitObject parseChild(final Reader reader, final VersitObject object)
			throws IOException {
		final OldScanner s = (OldScanner) reader;
		while (s.peek != -1) {
			while (s.peek == -2) {
				s.read();
			}
			String name = s.parseName().toUpperCase();
			if ("END".equalsIgnoreCase(name) && s.peek != '.' && s.peek != ';') {
				s.unfold = false;
				if (object.name.equalsIgnoreCase(getType(s))) {
					return null;
				}
				throw new VersitException(s, "Invalid end of " + object.name);
			}
			if ("BEGIN".equalsIgnoreCase(name) && s.peek != '.'
					&& s.peek != ';') {
				final String type = getType(s);
				final OldObjectDefinition objdef = Children.get(type);
				if (objdef == null) {
					throw new VersitException(s, "Invalid element: " + type);
				}
				VersitObject grandchild, child = new VersitObject(type);
				while ((grandchild = objdef.parseChild(s, child)) != null) {
					child.addChild(grandchild);
				}
				return child;
			}
			s.unfold = true;
			while (s.peek == '.') {
				s.read();
				name = s.parseName();
			}
			OldPropertyDefinition propdef = Properties.get(name);
			if (propdef == null) {
				propdef = DefaultProperty;
			}
			final Property property = new Property(name);
			propdef.parse(s, property);
			object.addProperty(property);
			s.unfold = false;
			if (s.peek != -1 && s.peek != -2) {
				throw new VersitException(s, "Error at the end of property");
			}
		}
		return null;
	}

	public Writer getWriter(final OutputStream stream, final String charset)
			throws IOException {
		return new OldFoldingWriter(stream, charset);
	}

	public void write(final Writer writer, final VersitObject object) throws IOException {
		writeProperties(writer, object);
		writeEnd(writer, object);
	}

	public void writeProperties(final Writer writer, final VersitObject object)
			throws IOException {
		final OldFoldingWriter fw = (OldFoldingWriter) writer;
		fw.write("BEGIN");
		fw.write(":");
		fw.writeln(object.name.getBytes(fw.charset));
		for (int i = 0; i < object.getPropertyCount(); i++) {
			final Property property = object.getProperty(i);
			final OldPropertyDefinition propdef = Properties.get(property.name);
			if (propdef != null) {
				propdef.write(fw, property);
			}
		}
		for (int i = 0; i < object.getChildCount(); i++) {
			final VersitObject child = object.getChild(i);
			final OldObjectDefinition objdef = Children.get(child.name);
			if (objdef != null) {
				objdef.write(fw, child);
			}
		}
	}

	public void writeEnd(final Writer writer, final VersitObject object) throws IOException {
		final OldFoldingWriter fw = (OldFoldingWriter) writer;
		fw.write("END");
		fw.write(":");
		fw.writeln(object.name.getBytes(fw.charset));
	}

	public VersitDefinition getChildDef(final String name) {
		return Children.get(name);
	}

	public VersitDefinition copy() {
		return new OldObjectDefinition(Name, Properties, Children);
	}

	public Iterator iterator() {
		return Properties.values().iterator();
	}

}
