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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.filestore.sproxyd;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.sproxyd.chunkstorage.ChunkStorage;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link S3FileStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SproxydFileStorage implements FileStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydFileStorage.class);

    private final SproxydClient client;
    private final ServiceLookup services;

    public SproxydFileStorage(ServiceLookup services) {
        super();
        this.services = services;
        this.client = new SproxydClient("http://sproxyd.ox.io/proxy/57462_ctx_store/");
    }

    private ChunkStorage getStorage() throws OXException {
        ChunkStorage service = services.getOptionalService(ChunkStorage.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(ChunkStorage.class);
        }
        return service;
    }

    private void storeChunk(String uuid, String scalityId, long offset, long length) throws OXException {

    }

    private void deleteChunks(String uuid) throws OXException {

    }

    private List<SproxydChunk> getChunks(String uuid) throws OXException {
        return null;
    }

    @Override
    public String saveNewFile(InputStream file) throws OXException {
        boolean success = false;
        String uuid = UUIDs.getUnformattedString(UUID.randomUUID());
        ChunkedUpload chunkedUpload = null;
        List<String> scalityIds = new ArrayList<String>();
        try {
            chunkedUpload = new ChunkedUpload(file);
            long offset = 0;
            long length = 0;
            while (chunkedUpload.hasNext()) {
                UploadChunk chunk = null;
                try {
                    chunk = chunkedUpload.next();
                    length = chunk.getSize();
                    String scalityId = client.put(chunk.getData(), length);
                    scalityIds.add(scalityId);
                    storeChunk(uuid, scalityId, offset, length);
                    offset += length;
                } finally {
                    Streams.close(chunk);
                }
            }
            success = true;
        } finally {
            Streams.close(chunkedUpload);
            if (false == success) {
                client.delete(scalityIds);
                deleteChunks(uuid);
            }
        }
        return uuid;
    }

    @Override
    public InputStream getFile(String name) throws OXException {
        List<SproxydChunk> chunks = getChunks(name);
        if (null == chunks || 0 == chunks.size()) {
            throw FileStorageCodes.FILE_NOT_FOUND.create(name);
        }
        if (1 == chunks.size()) {
            return client.get(chunks.get(0).getId());
        }
        List<InputStream> streams = new ArrayList<InputStream>(chunks.size());
        for (SproxydChunk chunk : chunks) {
            streams.add(client.get(chunk.getId()));
        }
        return new SequenceInputStream(Collections.enumeration(streams));
    }

    @Override
    public SortedSet<String> getFileList() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getFileSize(String name) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getMimeType(String name) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean deleteFile(String identifier) throws OXException {
        List<SproxydChunk> chunks = getChunks(identifier);
        if (null == chunks || 0 == chunks.size()) {
            return false;
        }
        for (SproxydChunk chunk : chunks) {
            client.delete(chunk.getId());
        }
        return true;
    }

    @Override
    public Set<String> deleteFiles(String[] identifiers) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove() throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void recreateStateFile() throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean stateFileIsCorrect() throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long appendToFile(InputStream file, String name, long offset) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFileLength(long length, String name) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public InputStream getFile(String name, long offset, long length) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
