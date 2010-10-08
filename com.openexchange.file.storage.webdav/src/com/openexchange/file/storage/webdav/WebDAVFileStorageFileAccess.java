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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.file.storage.webdav;

import org.apache.commons.httpclient.HttpClient;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionException;

/**
 * {@link WebDAVFileStorageFileAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageFileAccess extends AbstractWebDAVAccess implements FileStorageFileAccess {

    private final String rootUri;

    /**
     * Initializes a new {@link WebDAVFileStorageFileAccess}.
     */
    public WebDAVFileStorageFileAccess(final HttpClient client, final FileStorageAccount account, final Session session) {
        super(client, account, session);
        rootUri = (String) account.getConfiguration().get(WebDAVConstants.WEBDAV_URL);
    }

    public boolean exists(final String id, final int version) {
        
        
        // TODO Auto-generated method stub
        return false;
    }

    public void startTransaction() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    public void commit() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    public void rollback() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    public void finish() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    public void setTransactional(boolean transactional) {
        // TODO Auto-generated method stub
        
    }

    public void setRequestTransactional(boolean transactional) {
        // TODO Auto-generated method stub
        
    }

    public void setCommitsTransaction(boolean commits) {
        // TODO Auto-generated method stub
        
    }

}
