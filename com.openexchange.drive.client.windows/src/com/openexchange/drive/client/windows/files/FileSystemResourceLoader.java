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

package com.openexchange.drive.client.windows.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyEvent.Type;
import com.openexchange.config.PropertyListener;
import com.openexchange.drive.client.windows.service.UpdaterExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * {@link FileSystemResourceLoader}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class FileSystemResourceLoader extends AbstractResourceLoader implements PropertyListener {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FileSystemResourceLoader.class);

    private File parentDir = null;

    public FileSystemResourceLoader(String path) {
        this.parentDir = new File(path);
    }

    @Override
    public synchronized InputStream get(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        checkAccessible(f);
        checkAbsoluteInSubpath(f);
        return new FileInputStream(f);
    }

    @Override
    public synchronized File getFile(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        checkAccessible(f);
        checkAbsoluteInSubpath(f);
        return f;
    }

    @Override
    public synchronized long getSize(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        checkAccessible(f);
        checkAbsoluteInSubpath(f);
        return f.length();
    }

    @Override
    public synchronized boolean exists(String name) throws IOException {
        File f = new File(parentDir, name).getAbsoluteFile();
        if (f.exists()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void onPropertyChange(PropertyEvent event) {
        if (event == null) {
            return;
        }

        String value = event.getValue();
        if (event.getType() == Type.CHANGED && value != null) {
            parentDir = new File(event.getValue());
        }
    }

    @Override
    public synchronized Set<String> getAvailableFiles() throws OXException {
        String[] filenames = parentDir.list();

        if (filenames == null) {
            throw UpdaterExceptionCodes.NO_FILES_ERROR.create(parentDir.getAbsolutePath());
        }

        Set<String> retval = new HashSet<String>(Arrays.asList(filenames));
        return retval;
    }

    private synchronized void checkAbsoluteInSubpath(File f) throws FileNotFoundException {
        File current = f;

        while (current != null) {
            current = current.getParentFile();
            if (current.equals(parentDir)) {
                return;
            }
        }
        LOG.error("Trying to leave designated directory with a relative path. Denying request.");
        throw new FileNotFoundException();
    }

    private synchronized void checkAccessible(File f) throws FileNotFoundException {
        if (!f.exists()) {
            LOG.info("Trying to load {} but it doesn''t exist", f);
            throw new FileNotFoundException();
        }
        if (!f.canRead()) {
            LOG.error("Trying to load {} but the groupware can not read it", f);
            throw new FileNotFoundException();
        }
        if (!f.isFile()) {
            LOG.error("Trying to load {} but it''s not a file", f);
            throw new FileNotFoundException();
        }
    }
}
