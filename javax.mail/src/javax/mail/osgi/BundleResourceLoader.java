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

package javax.mail.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

/**
 * {@link BundleResourceLoader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class BundleResourceLoader {

    private final BundleWiring bundleWiring;

    /**
     * Initializes a new {@link BundleResourceLoader}.
     */
    public BundleResourceLoader(Bundle bundle) {
        super();
        bundleWiring = bundle.adapt(BundleWiring.class);
        if (bundleWiring == null) {
            throw new IllegalArgumentException("The passed bundle cannot be adapted to org.osgi.framework.wiring.BundleWiring!");
        }
    }

    /**
     * Gets an input stream for reading the specified resource.
     *
     * @param name The resource name
     * @return An input stream for reading the resource, or <tt>null</tt> if the resource could not be found
     * @throws IOException If resource cannot be opened
     */
    public InputStream getResourceAsStream(String name) throws IOException {
        if (name == null || name.length() == 0) {
            return null;
        }

        String filePattern = name;
        int i = name.lastIndexOf('/');
        if (i > 0) {
            if (i < name.length() - 1) {
                filePattern = name.substring(i + 1);
            }
        }

        URL fileUrl = null;
        List<URL> entries = bundleWiring.findEntries("/", filePattern, BundleWiring.FINDENTRIES_RECURSE);
        for (URL entry : entries) {
            if (entry.toExternalForm().endsWith(name)) {
                fileUrl = entry;
                break;
            }
        }

        if (fileUrl == null) {
            return null;
        }

        return fileUrl.openStream();
    }

}
