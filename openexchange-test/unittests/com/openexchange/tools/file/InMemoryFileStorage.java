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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.contexts.Context;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class InMemoryFileStorage extends FileStorage{

    private final Map<Context, Map<String, byte[]>>  data = new HashMap<Context, Map<String, byte[]>>();

    private final Map<Context, List<String>> deletions = new HashMap<Context, List<String>>();

    private Context ctx;

    public InMemoryFileStorage() throws FileStorageException {
        super(3,32);

    }

    @Override
	protected Set<String> delete(final String[] names) throws FileStorageException {
        for (String name : names) {
            getCtxMap().put(name,null);
            deletions.get(ctx).add(name);
        }
        return Collections.emptySet();
    }

    @Override
	protected void save(final String name, final InputStream input) throws FileStorageException {
        final List<Byte> bytes = new ArrayList<Byte>();
        final byte[] buffer = new byte[1024];
        int length = -1;
        try {
            while((length = input.read(buffer)) != -1) {
                for(int i = 0; i < length; i++) {
                    bytes.add(buffer[i]);
                }
            }
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR, e);
        }

        final byte[] data = new byte[bytes.size()];
        int i = 0;
        for(final byte b : bytes) {
            data[i++] = b;
        }
        put(name, data);
    }

    @Override
	protected InputStream load(final String name) throws FileStorageException {

        final byte[] bytes = get(name);
        if(bytes == null) {
            return null;
        }
        return new ByteArrayInputStream(bytes);
    }

    @Override
	protected long length(final String name) throws FileStorageException {
        return get(name).length;
    }

    @Override
	protected String type(final String name) throws FileStorageException {
        return "";
    }

    @Override
	protected boolean exists(final String name) throws FileStorageException {
        return getCtxMap().containsKey(name);
    }

    @Override
	protected void eliminate() throws FileStorageException {

    }

    @Override
	protected void lock(final long timeout) throws FileStorageException {

    }
    
    @Override
	protected void unlock() throws FileStorageException {

    }

    @Override
	protected void closeImpl() {

    }

    public void put(final Context ctx, final String filestoreId, final byte[] bytes) {
        getCtxMap(ctx).put(filestoreId, bytes);
    }

    public void setContext(final Context ctx) {
        this.ctx = ctx;
    }

    private Map<String, byte[]> getCtxMap(final Context ctx) {
        if(data.containsKey(ctx)) {
            return data.get(ctx);
        }
        final Map<String, byte[]> fileData = new HashMap<String, byte[]>();
        data.put(ctx, fileData);
        return fileData;
    }

    private Map<String, byte[]> getCtxMap() {
        return getCtxMap(ctx);
    }

    private byte[] get(final String name) {
        return getCtxMap().get(name);
    }

    private void put(final String name, final byte[] bytes) {
        getCtxMap().put(name, bytes);
    }

    public void forgetDeleted(final Context ctx) {
        deletions.put(ctx, new ArrayList<String>());
    }

    public List<String> getDeleted(final Context ctx) {
        return deletions.get(ctx);
    }
}
