/*
 * OPEN-XCHANGE - "the communication and information enviroment"
 *
 * All intellectual property rights in the Software are protected by
 * international copyright laws.
 *
 * OPEN-XCHANGE is a trademark of Netline Internet Service GmbH and all other
 * brand and product names are or may be trademarks of, and are used to identify
 * products or services of, their respective owners.
 *
 * Please make sure that third-party modules and libraries are used according to
 * their respective licenses.
 *
 * Any modifications to this package must retain all copyright notices of the
 * original copyright holder(s) for the original code used.
 *
 * After any such modifications, the original code will still remain copyrighted
 * by the copyright holder(s) or original author(s).
 *
 * Copyright (C) 1998 - 2005 Netline Internet Service GmbH
 * mail:                    info@netline-is.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.openexchange.tools.file;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Test for the file storage.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FileStorageTest extends TestCase {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(FileStorageTest.class);

    /**
     * Test method for
     * 'com.openexchange.tools.file.FileStorage.getInstance(Object...)'.
     * @throws Throwable if an error occurs.
     */
    public final void testGetInstance() throws Throwable {
        final File tempFile = File.createTempFile("filestorage", ".tmp");
        tempFile.delete();
        LOG.trace(tempFile.getAbsolutePath());
        final FileStorage storage = FileStorage.getInstance(tempFile);
        tempFile.delete();
        assertNotNull("Can't create file storage.", storage);
    }

}
