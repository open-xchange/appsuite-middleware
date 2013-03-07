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

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.openexchange.classloader.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.osgi.framework.Bundle;

/**
 * Originally taken from <a href="from http://sling.apache.org/site/apache-sling.html">Apache Sling</a>.
 * <p>
 * &nbsp;&nbsp;<img src="http://sling.apache.org/site/media.data/logo.png">
 * <p>
 * <p>
 * &nbsp;&nbsp;<img src="http://sling.apache.org/site/media.data/logo.png">
 * <p>
 * The <code>BundleProxyClassLoader</code> is a class loader delegating to a bundle. We don't need to cache as the {@link ClassLoaderFacade}
 * is already doing this.
 */
public class BundleProxyClassLoader extends ClassLoader {

    /** The bundle. */
    private final Bundle bundle;

    public BundleProxyClassLoader(Bundle bundle) {
        this.bundle = bundle;
    }

    // Note: Both ClassLoader.getResources(...) and bundle.getResources(...) consult
    // the boot classloader. As a result, BundleProxyClassLoader.getResources(...)
    // might return duplicate results from the boot classloader. Prior to Java 5
    // Classloader.getResources was marked final. If your target environment requires
    // at least Java 5 you can prevent the occurence of duplicate boot classloader
    // resources by overriding ClassLoader.getResources(...) instead of
    // ClassLoader.findResources(...).
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<URL> findResources(String name) throws IOException {
        return this.bundle.getResources(name);
    }

    @Override
    public URL findResource(String name) {
        return this.bundle.getResource(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return this.bundle.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return this.findResource(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = this.findClass(name);
        if (resolve) {
            super.resolveClass(clazz);
        }
        return clazz;
    }
}
