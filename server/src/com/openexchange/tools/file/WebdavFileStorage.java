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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.webdav.lib.WebdavResource;

import com.openexchange.tools.file.FileStorageException.Code;

public class WebdavFileStorage extends FileStorage {
	
	private static final Log LOG = LogFactory.getLog(WebdavFileStorage.class);
	
	private String host;
	private String path;
	private String userName;
	private String password;
	
	private final ThreadLocal<WebdavResource> lock = new ThreadLocal<WebdavResource>();
	
	private static WebdavLockManager lockMgr;
	
	private final List<WebdavResource> locks = new ArrayList<WebdavResource>();

	public static void setVMID(final String vmID) {
		if(null == lockMgr) {
			lockMgr = new WebdavLockManager(vmID);
		} else {
			throw new IllegalStateException("Please call #setVMID(String) only once during the lifetime of this classloader.");
		}
	}
	
	public WebdavFileStorage(final Object... initArgs) throws FileStorageException{
		super(initArgs);
		if(lockMgr == null) {
			throw new IllegalStateException("Please call #setVMID(String) once before creating a WebdavFileStorage");
		}
		
		this.host = (String) initArgs[2];
		if(host == null) {
			throw new IllegalArgumentException("Please specify a host");
		}
		this.path = (String) initArgs[3];
		if(path == null) {
			path = "/";
		}
		this.userName = (String) initArgs[4];
		this.password = (String) initArgs[5];
	}
	
	

	protected WebdavResource getResource(final String subPath) throws IOException{
		if(!host.endsWith("/")) {
			host += "/";
		}
		
		if(path == null) { 
			path = "";
		} else if(!path.endsWith("/")) {
			path += "/";
		}
		
		final HttpURL url = new HttpURL("http://" + host + path + subPath) {

			private static final long serialVersionUID = 1L;

			@Override
			public char[] getRawPassword() {
				// Fix for bug in http-client
				char[] pw = super.getRawPassword();
				if (null == pw) {
					return new char[0];
				}
				return pw;
			}
		};
		
		if(null != userName && null != password) {
			url.setUser(userName);
			url.setPassword(password);
		}
		return new WebdavResource(url, 0, WebdavResource.NOACTION);
	}
	
	/*private void releaseId(String identifier) throws IOException{
		WebdavResource lock;
		try {
			lock = lockInternal();
		} catch (Exception x){
			throw new IOException("Cannot get lock: "+x);
		}
		
		try {
			Enumeration e = lock.propfindMethod("OX:freeIds");
			if(!e.hasMoreElements()){
				lock.proppatchMethod(propName("freeIds"), identifier, true);	
			} else {
				String ids = (String) e.nextElement();
				lock.proppatchMethod(propName("freeIds"), ids+" "+identifier, true);	
			}
		} finally {
			unlock();
		}
	}*/
	
	@Override
	protected boolean delete(final String name) throws FileStorageException {
        try {
    		final WebdavResource res = getResource(name);
    		final boolean del = res.deleteMethod();
    		return del;
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e, e.getMessage());
        }
    }

	@Override
	protected void save(final String name, final InputStream input) throws FileStorageException {
        try {
    		final WebdavResource res = getResource(name);
    		if(name.contains("/")) {
    			mkParentDirs(name.substring(0,name.lastIndexOf('/')));
    		}
    		if(!res.putMethod(input)){
    			throw new FileStorageException(FileStorageException.Code
                    .CREATE_FAILED, res);
    		}
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e);
        }
	}

	private void mkParentDirs(final String path) throws FileStorageException {
		if(!exists(path)){
			if(path.contains("/")){
				mkParentDirs(path.substring(0,path.lastIndexOf('/')));
			}
            try {
                final WebdavResource res = getResource(path);
                res.mkcolMethod();
            } catch (final IOException e) {
                throw new FileStorageException(FileStorageException.Code
                    .CREATE_DIR_FAILED, path);
            }
		}
	}

	@Override
	protected InputStream load(final String name) throws FileStorageException {
        try {
            final WebdavResource res = getResource(name);
            return res.getMethodData();
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e);
        }
	}

	@Override
	protected long length(final String name) throws FileStorageException {
        try {
    		final WebdavResource res = getResource(name);
    		final Enumeration e = res.propfindMethod(WebdavResource.GETCONTENTLENGTH);
    		if(!e.hasMoreElements()) {
				throw new IOException("Cannot get content length.");
			}
    		return Long.parseLong((String)e.nextElement());
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e, e.getMessage());
        }
	}

	@Override
	protected String type(final String name) throws FileStorageException {
        try {
    		final WebdavResource res = getResource(name);
    		final Enumeration e = res.propfindMethod(WebdavResource.GETCONTENTTYPE);
    		if(!e.hasMoreElements()) {
				throw new IOException("Cannot get content type.");
			}
    		return (String)e.nextElement();
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e);
        }
	}

	@Override
	protected boolean exists(final String name) throws FileStorageException {
        try {
    		final WebdavResource res = getResource(name);
    		try {
    			res.setProperties(1);
    		} catch (final Exception e) {
    			LOG.debug(e.getMessage(), e);
    		}
    		return res.exists();
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e, e.getMessage());
        }
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void eliminate() throws FileStorageException {
        // FIXME completely untested code. Maybe lock must be removed.
        try {
            final WebdavResource res = getResource("");
            if (!res.deleteMethod()) {
                throw new FileStorageException(Code.NOT_ELIMINATED);
            }
        } catch (IOException e) {
            throw new FileStorageException(Code.IOERROR, e, e.getMessage());
        }
    }

    @Override
	protected void lock(final long timeout) throws FileStorageException {
        try {
    		if(lock.get()!=null) {
				return; // Reentrant lock
			}
    		final WebdavResource l = getResource("lock");
    		try {
    			lockMgr.lock(l,timeout);
    		} catch (final InterruptedException e) {
    			return;
    		}
    		lock.set(l);
    		locks.add(l);
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e);
        }
	}

	@Override
	protected void unlock() throws FileStorageException {
        try {
    		final WebdavResource lock = this.lock.get();
    		if(lock == null) {
				throw new IllegalStateException("The Thread "+Thread.currentThread().getName()+" doesn't hold the lock for this FileStorage");
			}
    		lockMgr.unlock(lock);
    		this.lock.set(null);
    		locks.remove(lock);
        } catch (final IOException e) {
            throw new FileStorageException(FileStorageException.Code.IOERROR,
                e);
        }
	}

	@Override
	protected void closeImpl() {
		for (final WebdavResource res : locks) {
			try {
				lockMgr.unlock(res);
			} catch (final IOException e) {
				LOG.debug("",e);
			}
		}
	}

}
