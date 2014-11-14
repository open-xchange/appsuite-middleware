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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import com.openexchange.ajax.container.IFileHolder.InputStreamClosure;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;


/**
 * {@link DocumentAndMetadataImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DocumentAndMetadataImpl implements DocumentAndMetadata {

    private final DocumentMetadata metadata;
    private final InputStreamClosure isClosure;
    private final String eTag;

    /**
     * Initializes a new {@link DocumentAndMetadataImpl}.
     *
     * @param metadata The metadata
     * @param isClosure The input stream closure, or <code>null</code> if not set
     * @param eTag The E-Tag
     */
    public DocumentAndMetadataImpl(DocumentMetadata metadata, InputStreamClosure isClosure, String eTag) {
        super();
        this.metadata = metadata;
        this.isClosure = isClosure;
        this.eTag = eTag;
    }

    @Override
    public Date getLastModified() {
        return metadata.getLastModified();
    }

    @Override
    public long getFolderId() {
        return metadata.getFolderId();
    }

    @Override
    public String getTitle() {
        return metadata.getTitle();
    }

    @Override
    public int getVersion() {
        return metadata.getVersion();
    }

    @Override
    public long getFileSize() {
        return metadata.getFileSize();
    }

    @Override
    public String getFileMIMEType() {
        return metadata.getFileMIMEType();
    }

    @Override
    public String getFileName() {
        return metadata.getFileName();
    }

    @Override
    public int getId() {
        return metadata.getId();
    }

    @Override
    public int getCreatedBy() {
        return metadata.getCreatedBy();
    }

    @Override
    public long getSequenceNumber() {
        return metadata.getSequenceNumber();
    }

    @Override
    public String getETag() {
        return eTag;
    }

    @Override
    public InputStream getData() throws OXException {
        try {
            return isClosure.newStream();
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

}
