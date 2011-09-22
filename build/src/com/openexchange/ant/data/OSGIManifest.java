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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ant.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Represents the possible values in a MANIFEST.MF file.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class OSGIManifest {

    public static final String MANIFEST_VERSION = "Manifest-Version";
    public static final String BUNDLE_MANIFEST_VERSION = "Bundle-ManifestVersion";
    public static final String BUNDLE_SYMBOLIC_NAME = "Bundle-SymbolicName";
    public static final String BUNDLE_LOCALIZATION = "Bundle-Localization";
    public static final String BUNDLE_NAME = "Bundle-Name";
    public static final String BUNDLE_VERSION = "Bundle-Version";
    public static final String BUNDLE_ACTIVATOR = "Bundle-Activator";
    public static final String BUNDLE_DESCRIPTION = "Bundle-Description";
    public static final String REQUIRE_BUNDLE = "Require-Bundle";
    public static final String IMPORT_PACKAGE = "Import-Package";
    public static final String EXPORT_PACKAGE = "Export-Package";
    public static final String EXPORT_SERVICE = "Export-Service";
    public static final String BUNDLE_NATIVE_CODE = "Bundle-NativeCode";
    public static final String FRAGMENT_HOST = "Fragment-Host";
    public static final String BUNDLE_CLASSPATH = "Bundle-ClassPath";

    protected final Attributes fEntries;

    public OSGIManifest(final File file) throws IOException {
        final FileInputStream stream = new FileInputStream(file);
        final Manifest manifest = new Manifest(stream);
        fEntries = manifest.getMainAttributes();
        stream.close();
    }

    public OSGIManifest(final Manifest manifest) {
        this.fEntries = manifest.getMainAttributes();
    }

    public String getEntry(final String key) {
        String value = fEntries.getValue(key);
        if (value != null && value.indexOf(';') != -1) {
            value = value.substring(0, value.indexOf(';'));
        }
        return value;
    }

    public Set<String> getListEntry(final String key) {
        final String value = fEntries.getValue(key);
        final Set<String> tokens = new HashSet<String>();
        if (value != null) {
            final StringTokenizer tokenizer = new StringTokenizer(value, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token.indexOf(';') != -1) {
                    // in case of version specification
                    token = token.substring(0, token.indexOf(';'));
                }
                tokens.add(token.trim());
            }
        }
        return tokens;
    }
}
