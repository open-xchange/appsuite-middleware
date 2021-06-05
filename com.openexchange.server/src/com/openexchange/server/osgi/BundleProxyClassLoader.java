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
