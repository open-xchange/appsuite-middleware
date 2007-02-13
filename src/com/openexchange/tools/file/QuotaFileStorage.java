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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;


/**
 * This class is a decorator for integrating the FileStorage into the quota system
 *
 */
public class QuotaFileStorage extends FileStorage {
	
	private static final String SELECT = "SELECT used FROM filestore_usage WHERE cid = ?";
	private static final String INSERT = "INSERT INTO filestore_usage (used, cid) VALUES (?, ?)";
	private static final String UPDATE = "UPDATE filestore_usage SET used = ? WHERE cid = ?";
	
	private FileStorage delegate;
	private DBProvider provider;
	private Context ctx;
	
	private ThreadLocal<LockMode> lockMode = new ThreadLocal<LockMode>();
	
	private static final Log LOG = LogFactory.getLog(FileStorage.class);
	
	public static final QuotaFileStorage getQuotaInstance(Context ctx, DBProvider provider) throws IOException, ContextException {
        final File f;
        try {
            f = new File(ctx.getFileStorageLocation());
        } catch (IllegalArgumentException e) {
            LOG.error("Problem with URL: \"" + ctx.getFileStorageLocation()
                + '\"', e);
            throw new IOException("Problem with URL: \""
                + ctx.getFileStorageLocation() + '\"');
        }
    	return new QuotaFileStorage(DEFAULT_DEPTH, DEFAULT_FILES, getInstance(f), ctx, provider);
    }
	
	public static final QuotaFileStorage getQuotaInstance(Context ctx, DBProvider provider, final Object... initData) throws IOException {
    	return new QuotaFileStorage(DEFAULT_DEPTH, DEFAULT_FILES, getInstance(initData), ctx, provider);
    }
    
    public static final QuotaFileStorage getQuotaInstance(Context ctx, DBProvider provider, int depth, int files, Object...initData) throws IOException {
    	return new QuotaFileStorage(depth,files,getInstance(depth,files,initData),ctx,provider);
    }

	
	public QuotaFileStorage(int depth, int entries, FileStorage delegate, Context ctx, DBProvider provider) throws IOException {
		super(depth,entries);
		this.delegate = delegate;
		this.ctx = ctx;
		this.provider = provider;
	}
	
	public long getQuota() {
		return ctx.getFileStorageQuota();
	}
	
	public long getUsage() throws IOException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			readCon = provider.getReadConnection(ctx);
			stmt = readCon.prepareStatement(SELECT);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			if(!rs.next()){
				return 0;
			}
			return rs.getLong(1);
		} catch (SQLException e) {
			LOG.debug(e);
			throw new IOException(e.toString());
		} catch (TransactionException e) {
			LOG.debug(e);
			throw new IOException(e.toString());
		} finally {
			close(readCon,stmt,rs,false);
		}
	}
	
	public void recalculateUsage() throws IOException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		/*
		 * This is done in two parts: 
		 * First sum up the usages from del_infostore_document, 
		 * infostore_document and prg_attachment
		 * Second set the new calculated sum in infostore_usage;
		 */ 
		final String infostore_document = "SELECT sum(file_size) FROM " + 
			"infostore_document WHERE cid=?";
		final String del_infostore_document = "SELECT sum(file_size) FROM " +
			"del_infostore_document WHERE cid=?";
		final String prg_attachment = "SELECT sum(file_size) FROM " +
			"prg_attachment WHERE cid=?";
		long usage;
		try {
			readCon = provider.getReadConnection(ctx);
			stmt = readCon.prepareStatement(infostore_document);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			if(rs.next()){
				usage = rs.getInt(1);
			} else {
				usage = 0;
			}

			stmt = readCon.prepareStatement(del_infostore_document);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			if(rs.next()){
				usage = usage + rs.getInt(1);
			}

			stmt = readCon.prepareStatement(prg_attachment);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			if(rs.next()){
				usage = usage + rs.getInt(1);
			}

			storeUsage(usage);
		} catch (SQLException e) {
			LOG.debug(e);
			throw new IOException(e.toString());
		} catch (TransactionException e) {
			LOG.debug(e);
			throw new IOException(e.toString());
		} finally {
			close(readCon,stmt,rs,false);
		}
	}

	
	
	@Override
	public boolean deleteFile(String identifier) throws IOException {
		delegate.lock(LOCK_TIMEOUT);
		lockMode.set(new NilLockMode());
		try {
			long size = delegate.getFileSize(identifier);
			boolean deleted = super.deleteFile(identifier);
			if(deleted) {
					decUsed(size);
			}
			return deleted;
		} finally {
			lockMode.set(new NormalLockMode(delegate));
			delegate.unlock();
		}
	}

	@Override
	public String saveNewFile(InputStream input) throws IOException {
		delegate.lock(LOCK_TIMEOUT);
		try {
			lockMode.set(new NilLockMode());
			String name =  super.saveNewFile(input);
			long length = delegate.length(name);
			if(fits(length)) {
				try {
					incUsed(length);
					return name;
				} catch (IOException x) {
					LOG.fatal("Can't set quota. Accepting upload anyway. You'll have to run the recover tool! ContextId: "+ctx.getContextId());
				}
			}
			super.deleteFile(name);
			quotaException(length);
		} finally {
			lockMode.set(new NormalLockMode(delegate));
			delegate.unlock();
		}
		return null; // Unreachable 
	}

	public String saveNewFile(InputStream input, long sizeHint) throws IOException{
		if(!fits(sizeHint)) {
			quotaException(sizeHint);
			
		}
		return saveNewFile(input);
	}

	@Override
	protected boolean delete(String name) throws IOException {
		return delegate.delete(name);
	}

	@Override
	protected void save(String name, InputStream input) throws IOException {
		delegate.save(name,input);
	}

	@Override
	protected InputStream load(String name) throws IOException {
		return delegate.load(name);
	}

	@Override
	protected long length(String name) throws IOException {
		return delegate.length(name);
	}

	@Override
	protected String type(String name) throws IOException {
		return delegate.type(name);
	}

	@Override
	protected boolean exists(String name) throws IOException {
		return delegate.exists(name);
	}

	@Override
	protected void lock(long timeout) throws IOException {
		if(null == lockMode.get()) {
			lockMode.set(new NormalLockMode(delegate));
		}
		lockMode.get().lock(timeout);
	}

	@Override
	protected void unlock() throws IOException {
		if(null == lockMode.get()) {
			lockMode.set(new NormalLockMode(delegate));
		}
		lockMode.get().unlock();
	}

	@Override
	protected void closeImpl() {
		delegate.closeImpl();
	}
	
	private final boolean fits(long length) throws IOException {
		long quota = getQuota();
		if(quota == -1)
			return true;
		return getUsage() + length <= quota;
	}
	
	private final void incUsed(long length) throws IOException {
		storeUsage(getUsage()+length);
	}
	
	private void decUsed(long size) throws IOException {
		long usage = getUsage();
		usage -= size;
		if(usage < 0) {
			LOG.error("Quota Statistics seem to be inconsistent with this FileStorage. Run the recovery tool. ContextId: "+ctx.getContextId());
			usage = 0;
		}
		storeUsage(usage);
	}


	private final void quotaException(long length) throws IOException {
		throw new IOException("This file of size "+length+" doesn't fit in the filestorage. Quota: "+getQuota()+" Used: "+getUsage());
	}
	
	
	protected void storeUsage(long usage) throws IOException {
		Connection writeCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			writeCon = provider.getWriteConnection(ctx);
			stmt = writeCon.prepareStatement(SELECT);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			String sql = UPDATE;
			if(!rs.next())
				sql = INSERT;
			stmt.close();
			rs.close();
			rs = null;
			
			stmt = writeCon.prepareStatement(sql);
			stmt.setLong(1,usage);
			stmt.setInt(2,ctx.getContextId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOG.debug(e);
			throw new IOException(e.toString());
		} catch (TransactionException e) {
			LOG.debug(e);
			throw new IOException(e.toString());
		} finally {
			close(writeCon,stmt,null,true);
		}
	}

	protected void close(Connection readCon, PreparedStatement stmt, ResultSet rs, boolean write) {
		if(stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOG.debug("",e);
			}
		}
		if(rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				LOG.debug("",e);
			}
		
		if(readCon != null)
			if(write)
				provider.releaseWriteConnection(ctx,readCon);
			else
				provider.releaseReadConnection(ctx,readCon);
		
	}

	
	protected static interface LockMode{
		public void lock(long timeout) throws IOException;
		public void unlock() throws IOException;
	}
	
	protected static class NormalLockMode implements LockMode {

		private FileStorage delegate;

		public NormalLockMode(FileStorage delegate){
			this.delegate = delegate;
		}
		
		public void lock(long timeout) throws IOException {
			delegate.lock(timeout);
		}

		public void unlock() throws IOException {
			delegate.unlock();
		}
		
	}
	
	protected static class NilLockMode implements LockMode {

		public void lock(long timeout) throws IOException {
		}

		public void unlock() throws IOException {
		}
		
	}

}
