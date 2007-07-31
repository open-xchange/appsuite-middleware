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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.contexts.Context;
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
	
	private final ThreadLocal<LockMode> lockMode = new ThreadLocal<LockMode>();
	
	private static final Log LOG = LogFactory.getLog(QuotaFileStorage.class);

	
    public QuotaFileStorage(final Object... initData) throws FileStorageException {
        super(initData);
        
        if (!(initData[3] instanceof Context)) {
            throw new QuotaFileStorageException(QuotaFileStorageException.Code
                .INVALID_PARAMETER, Integer.valueOf(3), initData[3].getClass().getName());
        }
        this.ctx = (Context) initData[3];
        if (!(initData[4] instanceof DBProvider)) {
            throw new QuotaFileStorageException(QuotaFileStorageException.Code
                .INVALID_PARAMETER, Integer.valueOf(4), initData[4].getClass().getName());
        }
        this.provider = (DBProvider) initData[4];
        try {
        	this.delegate = new LocalFileStorage(initData);   
        } catch (final FileStorageException x) {
        	throw addContextInfo(x, ctx);
        }
    }

	public long getQuota() {
		return ctx.getFileStorageQuota();
	}
	
	public long getUsage() throws QuotaFileStorageException {
		return getUsage(false);
	}
	
	protected long getUsage(final boolean write) throws QuotaFileStorageException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			if(write) {
				readCon = provider.getWriteConnection(ctx);
			} else {
				readCon = provider.getReadConnection(ctx);
			}
			stmt = readCon.prepareStatement(SELECT);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			if(!rs.next()){
				return 0;
			}
			return rs.getLong(1);
		} catch (final SQLException e) {
			throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQL_EXCEPTION, e);
		} catch (final TransactionException e) {
			throw new QuotaFileStorageException(e);
		} finally {
			close(readCon,stmt,rs,write);
		}
	}
	
	public void recalculateUsage() throws FileStorageException {
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
		} catch (final SQLException e) {
			throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQL_EXCEPTION, e);
		} catch (final TransactionException e) {
			throw new QuotaFileStorageException(e);
		} finally {
			close(readCon,stmt,rs,false);
		}
	}

	@Override
	public String saveNewFile(final InputStream input) throws FileStorageException  {
		String name;
		long length;
		boolean locked = false;
		try {
			name = super.saveNewFile(input);
			length = delegate.length(name);
			delegate.lock(LOCK_TIMEOUT);
			locked = true;
			if(fits(length)) {
				try {
					incUsed(length);
					return name;
				} catch (final FileStorageException x) {
					LOG.fatal("Cannot set quota. Accepting upload anyway. You'll have to run the recovery tool! ContextId: "+ctx.getContextId());
				}
			}
			super.deleteFile(name);
			quotaException(length);
			return null;
			
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		} finally {
			if(locked) {
				delegate.unlock();
			}
		}
	}
	
	@Override
	public boolean deleteFile(final String identifier) throws FileStorageException {
		try {
			delegate.lock(LOCK_TIMEOUT);
			lockMode.set(new NilLockMode());
			final long size = delegate.getFileSize(identifier);
			final boolean deleted = super.deleteFile(identifier);
			if(deleted) {
					decUsed(size);
			}
			return deleted;
		} catch (final QuotaFileStorageException x) {
			throw x;
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		}  finally {
			lockMode.set(new NormalLockMode(delegate, ctx));
			delegate.unlock();
		}
	}

	/*@Override
	public String saveNewFile(InputStream input) throws IOException {
		
		//delegate.lock(LOCK_TIMEOUT);
		boolean locked = true;
		try {
			//lockMode.set(new OnlyUnlockMode(delegate));
			//locked = false;
			String name =  super.saveNewFile(input);
			long length = delegate.length(name);
			delegate.lock(LOCK_TIMEOUT);
			locked = true;
			if(fits(length)) {
				try {
					incUsed(length);
					return name;
				} catch (IOException x) {
					LOG.fatal("Cannot set quota. Accepting upload anyway. You'll have to run the recovery tool! ContextId: "+ctx.getContextId());
				}
			}
			delegate.unlock();
			locked = false;
			
			super.deleteFile(name);
			quotaException(length);
		} finally {
			if(locked)
				delegate.unlock();
		}
		return null; // Unreachable 
	} */

	public String saveNewFile(final InputStream input, final long sizeHint) throws FileStorageException{
		if(!fits(sizeHint)) {
			quotaException(sizeHint);	
		}
		return saveNewFile(input);
	}

	@Override
	protected boolean delete(final String name) throws FileStorageException {
		try {
			return delegate.delete(name);
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		}
	}

	@Override
	protected void save(final String name, final InputStream input) throws FileStorageException {
		try {
			delegate.save(name,input);
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		}
	}

	@Override
	protected InputStream load(final String name) throws FileStorageException {
		try {
			return delegate.load(name);
		} catch (final FileStorageException x){
			throw addContextInfo(x, ctx);
		}
	}

	@Override
	protected long length(final String name) throws FileStorageException {
		try {
			return delegate.length(name);
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		}
	}

	@Override
	protected String type(final String name) throws FileStorageException {
		try {
			return delegate.type(name);
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		}
	}

	@Override
	protected boolean exists(final String name) throws FileStorageException {
		try {
			return delegate.exists(name);
		} catch (final FileStorageException x) {
			throw addContextInfo(x, ctx);
		}
	}

	@Override
	protected void lock(final long timeout) throws FileStorageException {
		if(null == lockMode.get()) {
			lockMode.set(new NormalLockMode(delegate, ctx));
		}
		lockMode.get().lock(timeout);
	}

	@Override
	protected void unlock() throws FileStorageException {
		if(null == lockMode.get()) {
			lockMode.set(new NormalLockMode(delegate, ctx));
		}
		lockMode.get().unlock();
	}

	@Override
	protected void closeImpl() {
		delegate.closeImpl();
	}
	
	private final boolean fits(final long length) throws QuotaFileStorageException {
		final long quota = getQuota();
		if(quota == -1) {
			return true;
		}
		return getUsage() + length <= quota;
	}
	
	private final void incUsed(final long length) throws QuotaFileStorageException {
		storeUsage(getUsage(true)+length);
	}
	
	private void decUsed(final long size) throws QuotaFileStorageException {
		long usage = getUsage(true);
		usage -= size;
		if(usage < 0) {
			LOG.fatal("Quota Statistics seem to be inconsistent with this FileStorage. Run the recovery tool. ContextId: "+ctx.getContextId());
			usage = 0;
		}
		storeUsage(usage);
	}


	private final void quotaException(final long length) throws QuotaFileStorageException {
		throw new QuotaFileStorageException(QuotaFileStorageException.Code.TOO_LARGE, String.valueOf(length), String.valueOf(getQuota()), String.valueOf(getUsage()));
	}
	
	
	protected void storeUsage(final long usage) throws QuotaFileStorageException {
		Connection writeCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			writeCon = provider.getWriteConnection(ctx);
			stmt = writeCon.prepareStatement(SELECT);
			stmt.setInt(1,ctx.getContextId());
			rs = stmt.executeQuery();
			String sql = UPDATE;
			if(!rs.next()) {
				sql = INSERT;
			}
			stmt.close();
			rs.close();
			rs = null;
			
			stmt = writeCon.prepareStatement(sql);
			stmt.setLong(1,usage);
			stmt.setInt(2,ctx.getContextId());
			stmt.executeUpdate();
		} catch (final SQLException e) {
			throw new QuotaFileStorageException(QuotaFileStorageException.Code.SQL_EXCEPTION, e);
		} catch (final TransactionException e) {
			throw new QuotaFileStorageException(e);
		} finally {
			close(writeCon,stmt,null,true);
		}
	}

	protected void close(final Connection readCon, final PreparedStatement stmt, final ResultSet rs, final boolean write) {
		if(stmt != null) {
			try {
				stmt.close();
			} catch (final SQLException e) {
				LOG.error("",e);
			}
		}
		if(rs != null) {
			try {
				rs.close();
			} catch (final SQLException e) {
				LOG.error("",e);
			}
		}
		
		if(readCon != null) {
			if(write) {
				provider.releaseWriteConnection(ctx,readCon);
			} else {
				provider.releaseReadConnection(ctx,readCon);
			}
		}
		
	}

	private static final QuotaFileStorageException addContextInfo(final FileStorageException x, final Context ctx) {
		return new QuotaFileStorageException(QuotaFileStorageException.Code.UNDERLYING_EXCEPTION, x, String.valueOf(ctx.getFilestoreId()), String.valueOf(ctx.getContextId()), x.getMessage());
	}
	
	protected static interface LockMode{
		public void lock(long timeout) throws FileStorageException;
		public void unlock() throws FileStorageException;
	}
	
	protected static class NormalLockMode implements LockMode {

		private FileStorage delegate;
		private Context ctx;

		public NormalLockMode(final FileStorage delegate, final Context ctx){
			this.delegate = delegate;
			this.ctx = ctx;
		}
		
		public void lock(final long timeout) throws FileStorageException {
			try {
				delegate.lock(timeout);
			} catch (final FileStorageException x) {
				throw addContextInfo(x, ctx);
			}
		}

		public void unlock() throws FileStorageException {
			try {
				delegate.unlock();
			} catch (final FileStorageException x) {
				throw addContextInfo(x, ctx);
			}
		}
		
	}
	
	protected static class NilLockMode implements LockMode {

		public void lock(final long timeout) throws FileStorageException {
		}

		public void unlock() throws FileStorageException {
		}
		
	}
	
	protected static class OnlyUnlockMode implements LockMode {

		private FileStorage delegate;
		private Context ctx;

		public OnlyUnlockMode(final FileStorage delegate, final Context ctx){
			this.delegate = delegate;
			this.ctx = ctx;
		}
		
		public void lock(final long timeout) throws FileStorageException {
		}

		public void unlock() throws FileStorageException {
			try {
				delegate.unlock();
			} catch (final FileStorageException x) {
				throw addContextInfo(x, ctx);
			}
		}
		
	}

}
