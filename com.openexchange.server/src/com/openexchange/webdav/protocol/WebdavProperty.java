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

package com.openexchange.webdav.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;

public class WebdavProperty {

	private String namespace = "";
	private String name = "";
	private String lang = "";
	private String value = "";
	private boolean xml;
	private boolean date;
	private Map<String, String> attributes;
	private List<Element> children;

	/**
	 * Initializes a new, empty {@link WebdavProperty}.
	 */
	public WebdavProperty() {
	    this(null, null);
	}

	/**
	 * Initializes a new {@link WebdavProperty}.
	 *
	 * @param namespace The namespace
	 * @param name the property name
	 */
	public WebdavProperty(final String namespace, final String name) {
	    super();
	    this.namespace = namespace;
	    this.name = name;
	}

	public String getLanguage() {
		return lang;
	}

	public void setLanguage(final String lang) {
		this.lang = lang;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(final String namespace) {
		this.namespace = namespace;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * Adds an additional attribute to this property.
	 *
	 * @param name The attribute name
	 * @param value The value
	 */
	public void addAttribute(String name, String value) {
	    if (null == attributes) {
	        attributes = new HashMap<String, String>();
	    }
	    attributes.put(name, value);
	}

	/**
	 * Gets the additional attributes of this property.
	 *
	 * @return The attributes, or <code>null</code> if there are none
	 */
	public Map<String, String> getAttributes() {
	    return null != attributes ? Collections.unmodifiableMap(attributes) : null;
	}

	/**
	 * Gets the property's child elements
	 *
	 * @return The children, or <code>null</code> if not set
	 */
    public List<Element> getChildren() {
        return children;
    }

    /**
     * Sets the property's child elements.
     *
     * @param children The children
     */
    public void setChildren(List<Element> children) {
        this.children = children;
    }

	public boolean isXML() {
		return xml;
	}

	public void setXML(final boolean xml) {
		this.xml = xml;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + namespace.hashCode();
	}

	@Override
	public boolean equals(final Object o){
		if (o instanceof WebdavProperty) {
			final WebdavProperty prop = (WebdavProperty) o;
			return prop.lang.equals(lang) && prop.name.equals(name) && prop.namespace.equals(namespace) && prop.value.equals(value);
		}
		return false;
	}

	public boolean isDate() {
		return date;
	}

	public void setDate(final boolean b) {
		this.date = b;
	}

    @Override
    public String toString() {
        return "WebdavProperty [namespace=" + namespace + ", name=" + name + "]";
    }

}
