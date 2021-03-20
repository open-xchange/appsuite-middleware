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
