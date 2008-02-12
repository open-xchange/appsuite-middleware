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


package com.openexchange.admin.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;

/**
 * 
 * @author d7
 * 
 */
public class FilestoreDataMover implements Callable<Void> {

    private final static Log log = LogFactory.getLog(FilestoreDataMover.class);

    private String src = null;

    private String dst = null;

    private Context ctx = null;

    private Filestore dstStore = null;

    /**
     * @throws IOException
     * 
     */
    public FilestoreDataMover(final String src, final String dst, final Context ctx, final Filestore dstStore) throws IOException {
        if (!new File(src).exists()) {
            throw new IOException("Source does not exist");
        }
        this.src = src;
        this.dst = dst;
        this.ctx = ctx;
        this.dstStore = dstStore;
    }

    /**
     * 
     * get Size as long (bytes) from the source dir
     * 
     * @param source
     * @return
     */
    public long getSize(final String source) {
        return FileUtils.sizeOfDirectory(new File(source));
    }

    /**
     * 
     * get the list of files to copy from the source dir
     * 
     * @param source
     * @return
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getFileList(final String source) {
        final Collection<File> listFiles = FileUtils.listFiles(new File(source), null, true);
        final ArrayList<String> retval = new ArrayList<String>();
        for (final File file : listFiles) {
            retval.add(new StringBuilder(file.getPath()).append(File.pathSeparatorChar).append(file.getName()).toString());
        }
        return retval;
    }

    /**
     * start the copy (rsync)
     * 
     * @throws StorageException
     * @throws InterruptedException
     * @throws IOException
     * @throws ProgrammErrorException
     */
    public void copy() throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        // if context store does not yet exist, which might be possible,
        // just change the configdb
        final ArrayOutput output = new ShellExecutor().executeprocargs(new String[] { "rsync", "-a", this.src + "/", this.dst });
        if (0 != output.exitstatus) {
            throw new ProgrammErrorException("Wrong exit status. Exit status was: " + output.exitstatus + " Stderr was: \n" + output.errOutput.toString() + '\n' + "and stdout was: \n" + output.stdOutput.toString());
        }
        FileUtils.deleteDirectory(new File(this.src));

        final OXContextStorageInterface oxcox = OXContextStorageInterface.getInstance();
        ctx.setFilestoreId(dstStore.getId());
        oxcox.changeStorageData(ctx);
        oxcox.enable(ctx);
    }

    /**
     * starting the thread
     * 
     * @throws StorageException
     * @throws InterruptedException
     * @throws IOException
     * @throws ProgrammErrorException
     * 
     */
    public Void call() throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        try {
            copy();
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            // Because the client side only knows of the exceptions defined in the core we have
            // to throw the trace as string
            throw new StorageException(e.toString());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InterruptedException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final ProgrammErrorException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return null;
    }
}