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

package com.openexchange.server.osgi;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.osgi.framework.Bundle;

/**
 * {@link BundleProxyClassLoader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class BundleProxyClassLoader extends ClassLoader {

	private final Bundle bundle;

	private final ClassLoader parent;

	public BundleProxyClassLoader(final Bundle bundle) {
		if (null == bundle) {
			throw new IllegalArgumentException("bundle");
		}
		this.bundle = bundle;
		this.parent = null;
	}

	public BundleProxyClassLoader(final Bundle bundle, final ClassLoader parent) {
		super(parent);
		if (null == bundle) {
			throw new IllegalArgumentException("bundle");
		}
		this.parent = parent;
		this.bundle = bundle;
	}

	/*
	 * Note: Both ClassLoader.getResources(...) and bundle.getResources(...)
	 * consult the boot classloader. As a result,
	 * BundleProxyClassLoader.getResources(...) might return duplicate results
	 * from the boot classloader. Prior to Java 5 Classloader.getResources was
	 * marked final. If your target environment requires at least Java 5 you can
	 * prevent the occurence of duplicate boot classloader resources by
	 * overriding ClassLoader.getResources(...) instead of
	 * ClassLoader.findResources(...).
	 */

	@Override
	public Class<?> findClass(final String name) throws ClassNotFoundException {
		return bundle.loadClass(name);
	}

	@Override
	public URL findResource(final String name) {
		return bundle.getResource(name);
	}

	@Override
	public Enumeration<URL> findResources(final String name) throws IOException {
		return bundle.getResources(name);
	}

	@Override
	public URL getResource(final String name) {
		return (parent == null) ? findResource(name) : super.getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(final String name) throws IOException {
		return bundle.getResources(name);
	}

	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		final Class<?> clazz = (parent == null) ? findClass(name) : super.loadClass(name, false);
		if (resolve) {
			super.resolveClass(clazz);
		}
		return clazz;
	}

}
