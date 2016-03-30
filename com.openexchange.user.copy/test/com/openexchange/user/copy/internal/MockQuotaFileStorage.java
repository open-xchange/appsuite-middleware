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

package com.openexchange.user.copy.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.tools.file.external.QuotaFileStorage;


/**
 * {@link MockQuotaFileStorage}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MockQuotaFileStorage implements QuotaFileStorage {
    
    private static final Map<String, FileHolder> STORAGE = new HashMap<String, FileHolder>();
    
    private static final MockQuotaFileStorage INSTANCE = new MockQuotaFileStorage();

    
    private MockQuotaFileStorage() {
        super();
    }
    
    public static MockQuotaFileStorage getInstance() {
        return INSTANCE;
    }

    /**
     * @see com.openexchange.tools.file.external.FileStorage#saveNewFile(java.io.InputStream)
     */
    public String saveNewFile(final InputStream file) throws OXException {
        final String uuid = UUID.randomUUID().toString();
        save(file, uuid);
        return uuid;
    }
    
    private void save(final InputStream file, final String name) throws OXException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        try {
            while ((b = file.read()) != -1) {
                baos.write(b);
            }
            baos.flush();
            baos.close();
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e);
        }
        
        STORAGE.put(name, new FileHolder(baos.toByteArray()));
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#getFile(java.lang.String)
     */
    public InputStream getFile(final String name) throws OXException {
        final FileHolder fileHolder = STORAGE.get(name);
        if (fileHolder == null) {
            final byte[] data = generateRandomFile();
            final ByteArrayInputStream is = new ByteArrayInputStream(data);
            save(is, name);
            try {
                is.close();
            } catch (final IOException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e);
            }
            
            return new ByteArrayInputStream(data);
        } else {
            return new ByteArrayInputStream(fileHolder.getData());
        }
    }
    
    private byte[] generateRandomFile() {
        final Random r = new Random(System.currentTimeMillis());
        int size;
        do {
            size = r.nextInt(2049);
        } while (size == 0);
        final byte[] randomData = new byte[size];
        r.nextBytes(randomData);
        
        return randomData;
    }

    /**
     * @see com.openexchange.tools.file.external.FileStorage#getFileList()
     */
    public SortedSet<String> getFileList() throws OXException {
        return null;
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#getFileSize(java.lang.String)
     */
    public long getFileSize(final String name) throws OXException {
        return 0;
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#getMimeType(java.lang.String)
     */
    public String getMimeType(final String name) throws OXException {
        return null;
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#deleteFile(java.lang.String)
     */
    public boolean deleteFile(final String identifier) throws OXException {
        return false;
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#deleteFiles(java.lang.String[])
     */
    public Set<String> deleteFiles(final String[] identifiers) throws OXException {
        return null;
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#remove()
     */
    public void remove() throws OXException {
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#recreateStateFile()
     */
    public void recreateStateFile() throws OXException {
    }


    /**
     * @see com.openexchange.tools.file.external.FileStorage#stateFileIsCorrect()
     */
    public boolean stateFileIsCorrect() throws OXException {
        return false;
    }


    /**
     * @see com.openexchange.tools.file.external.QuotaFileStorage#getQuota()
     */
    public long getQuota() {
        return 0;
    }


    /**
     * @see com.openexchange.tools.file.external.QuotaFileStorage#getUsage()
     */
    public long getUsage() throws OXException {
        return 0;
    }


    /**
     * @see com.openexchange.tools.file.external.QuotaFileStorage#recalculateUsage()
     */
    public void recalculateUsage() throws OXException {
    }
    
    private static final class FileHolder {
        
        private final byte[] data;
        

        public FileHolder(final byte[] data) {
            super();
            this.data = data;
        }
        
        public byte[] getData() {
            return data;
        }
    }

}
