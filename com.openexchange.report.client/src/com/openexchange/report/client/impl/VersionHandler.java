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

package com.openexchange.report.client.impl;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class VersionHandler {

    public static String[] getServerVersion() throws IOException {
        String[] retval = new String[2];

        // For debugging in eclipse
        // String path = new File("../com.openexchange.version/META-INF/MANIFEST.MF").getAbsolutePath();
        // URL manifestURL = new URL("file:" + path);
        URL manifestURL = new URL("jar:file:/opt/open-xchange/bundles/com.openexchange.version.jar!/META-INF/MANIFEST.MF");
        Attributes attrs = new Manifest(manifestURL.openStream()).getMainAttributes();

        retval[0] = attrs.getValue("OXVersion") + " Rev" + attrs.getValue("OXRevision");
        retval[1] = String.valueOf(attrs.getValue("OXBuildDate"));

        return retval;
    }
}
