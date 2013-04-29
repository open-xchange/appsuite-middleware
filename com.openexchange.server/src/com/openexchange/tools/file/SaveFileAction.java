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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.file;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jonelo.jacksum.util.Service;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;

public class SaveFileAction extends AbstractUndoable implements UndoableAction {

    protected FileStorage storage;

    private InputStream in;

    private String id;

    private String md5;

    public SaveFileAction() {
        super();
    }

    @Override
    protected void undoAction() throws OXException {
        storage.deleteFile(id);
    }

    @Override
    public void perform() throws OXException {
        DigestInputStream digestStream = null;
        try {
            digestStream = new DigestInputStream(in, MessageDigest.getInstance("MD5"));
        } catch (NoSuchAlgorithmException e) {
            // okay, save without checksum instead
        }
        if (null != digestStream) {
            try {
                id = saveFile(digestStream);
                md5 = Service.format(digestStream.getMessageDigest().digest());
            } finally {
                Streams.close(digestStream);
            }
        } else {
            id = saveFile(in);
        }
    }

    protected String saveFile(InputStream stream) throws OXException {
        return storage.saveNewFile(in);
    }

    public String getId() {
        return id;
    }

    public String getMd5() {
        return md5;
    }

    public void setIn(final InputStream in) {
        this.in = in;
    }

    public void setStorage(final FileStorage storage) {
        this.storage = storage;
    }

}
