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

package com.openexchange.groupware.filestore;

import static com.openexchange.groupware.filestore.RdbFilestoreStorage.findMatchOrElse;
import static com.openexchange.groupware.filestore.RdbFilestoreStorage.setUriAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

/**
 * {@link MWB1009Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class MWB1009Test {

    /**
     * Initializes a new {@link MWB1009Test}.
     */
    public MWB1009Test() {
        super();
    }

    @Test
    public void testFindMatch() {
        try {
            Map<String, FilestoreImpl> filestores = new LinkedHashMap<>(4);
            {
                FilestoreImpl filestore = new FilestoreImpl();
                filestore.setId(1);
                String sUri = "s3://ionos";
                setUriAsString(sUri, filestore);
                filestore.setSize(1024000);
                filestore.setMaxContext(500);
                filestores.put(sUri, filestore);
            }
            {
                FilestoreImpl filestore = new FilestoreImpl();
                filestore.setId(2);
                String sUri = "s3://ionos2";
                setUriAsString(sUri, filestore);
                filestore.setSize(1024000);
                filestore.setMaxContext(500);
                filestores.put(sUri, filestore);
            }
            {
                FilestoreImpl filestore = new FilestoreImpl();
                filestore.setId(3);
                String sUri = "/var/opt/filestore";
                setUriAsString(sUri, filestore);
                filestore.setSize(1024000);
                filestore.setMaxContext(500);
                filestores.put(sUri, filestore);
            }
            {
                FilestoreImpl filestore = new FilestoreImpl();
                filestore.setId(3);
                String sUri = "/var/opt/data_export/";
                setUriAsString(sUri, filestore);
                filestore.setSize(1024000);
                filestore.setMaxContext(500);
                filestores.put(sUri, filestore);
            }

            String sUriToLookUp = "/var/opt/data_export/export12";
            Filestore fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(3, fs.getId());

            // -------------------------------------------------------

            sUriToLookUp = "/var/opt/data_export";
            fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(3, fs.getId());

            // -------------------------------------------------------

            sUriToLookUp = "/var/opt/data_export/";
            fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(3, fs.getId());

            // -------------------------------------------------------

            sUriToLookUp = "s3://ionos2";
            fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(2, fs.getId());

            // -------------------------------------------------------

            sUriToLookUp = "s3://ionos2/peter";
            fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(2, fs.getId());

            // -------------------------------------------------------

            sUriToLookUp = "s3://ionos";
            fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(1, fs.getId());

            // -------------------------------------------------------

            sUriToLookUp = "s3://ionos/hans";
            fs = findMatchOrElse(sUriToLookUp, filestores);
            assertEquals(1, fs.getId());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
