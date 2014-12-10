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

package com.openexchange.admin.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.io.FileUtils;
import com.openexchange.admin.osgi.FilestoreLocationUpdaterRegistry;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.filestore.FilestoreLocationUpdater;
import com.openexchange.tools.file.FileStorages;

/**
 * @author d7
 */
public class FilestoreDataMover implements Callable<Void> {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FilestoreDataMover.class);

    /**
     * {@link PostProcessTask} - A task that gets executed after successful completion.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @since v7.8.0
     */
    public static interface PostProcessTask {

        /**
         * Performs the post-process task.
         *
         * @throws StorageException If task fails
         */
        void perform() throws StorageException;
    }

    // -------------------------------------------------------------------------------------------------------


    public static FilestoreDataMover newContextFilestoreDataMover(String src, String dst, Context ctx, Filestore dstStore, boolean rsyncEnabled) {
        return new FilestoreDataMover(src, dst, null, ctx, dstStore, rsyncEnabled);
    }

    public static FilestoreDataMover newUserFilestoreDataMover(String src, String dst, User user, Context ctx, Filestore dstStore, boolean rsyncEnabled) {
        return new FilestoreDataMover(src, dst, user, ctx, dstStore, rsyncEnabled);
    }

    // -------------------------------------------------------------------------------------------------------

    private final String src;
    private final String dst;
    private final Context ctx;
    private final User user;
    private final Filestore dstStore;
    private final boolean rsyncEnabled;
    private final Queue<PostProcessTask> postProcessTasks;

    private FilestoreDataMover(String src, String dst, User user, Context ctx, Filestore dstStore, boolean rsyncEnabled) {
        super();
        this.src = src;
        this.dst = dst;
        this.ctx = ctx;
        this.user = user;
        this.dstStore = dstStore;
        this.rsyncEnabled = rsyncEnabled;
        postProcessTasks = new ConcurrentLinkedQueue<PostProcessTask>();
    }

    /**
     * Adds given post-process task.
     *
     * @param task The task to execute when job is done successfully
     * @return This instance
     */
    public FilestoreDataMover addPostProcessTask(PostProcessTask task) {
        if (null != task) {
            postProcessTasks.add(task);
        }
        return this;
    }

    /**
     * get Size as long (bytes) from the source dir
     *
     * @param source
     * @return
     */
    public long getSize(final String source) {
        return FileUtils.sizeOfDirectory(new File(source));
    }

    /**
     * get the list of files to copy from the source dir
     *
     * @param source
     * @return
     */
    public List<String> getFileList(final String source) {
        final Collection<File> listFiles = FileUtils.listFiles(new File(source), null, true);
        final ArrayList<String> retval = new ArrayList<String>();
        for (final File file : listFiles) {
            retval.add(new StringBuilder(file.getPath()).append(File.pathSeparatorChar).append(file.getName()).toString());
        }
        return retval;
    }

    /**
     * Start the copy (rsync)
     *
     * @throws StorageException
     * @throws InterruptedException
     * @throws IOException
     * @throws ProgrammErrorException
     */
    public void copy() throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        if (rsyncEnabled) {
            if (new File(this.src).exists()) {
                final ArrayOutput output = new ShellExecutor().executeprocargs(new String[] { "rsync", "-a", this.src, this.dst + '/' });
                if (0 != output.exitstatus) {
                    throw new ProgrammErrorException(
                        "Wrong exit status. Exit status was: " + output.exitstatus + " Stderr was: \n" + output.errOutput.toString() + '\n' + "and stdout was: \n" + output.stdOutput.toString());
                }
                FileUtils.deleteDirectory(new File(this.src));
            }

            ctx.setFilestoreId(dstStore.getId());

            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.changeFilestoreDataFor(ctx);

            for (PostProcessTask task : postProcessTasks) {
                task.perform();
            }
        } else {
            FileStorage srcStorage;
            FileStorage dstStorage;
            Set<String> srcFiles;
            Map<String, String> fileMapping;
            try {
                String src = this.src;
                if (!src.endsWith("/")) {
                    src = src + "/";
                }
                StringBuilder dstUri = new StringBuilder();
                dstUri.append(dst).append("/").append(ctx.getId()).append("_ctx_store/");
                srcStorage = FileStorages.getFileStorageService().getFileStorage(new URI(src));
                dstStorage = FileStorages.getFileStorageService().getFileStorage(new URI(dstUri.toString()));
                srcFiles = srcStorage.getFileList();
                fileMapping = new HashMap<String, String>(srcFiles.size());
                for (String file : srcFiles) {
                    InputStream is = srcStorage.getFile(file);
                    String newFile = dstStorage.saveNewFile(is);
                    if (null != newFile) {
                        fileMapping.put(file, newFile);
                        log.info("Copied file " + file + " to " + newFile);
                    }
                }
            } catch (OXException e) {
                throw new StorageException(e);
            } catch (URISyntaxException e) {
                throw new StorageException(e);
            }

            Connection con = null;
            try {
                con = Database.getNoTimeout(ctx.getId().intValue(), true);
                List<FilestoreLocationUpdater> services = FilestoreLocationUpdaterRegistry.getInstance().getServices();
                for (FilestoreLocationUpdater updater : services) {
                    updater.updateFilestoreLocation(fileMapping, ctx.getId().intValue(), con);
                }
            } catch (OXException e) {
                throw new StorageException(e);
            } catch (SQLException e) {
                throw new StorageException(e);
            } finally {
                Database.backNoTimeout(ctx.getId().intValue(), true, con);
            }

            try {
                srcStorage.deleteFiles(srcFiles.toArray(new String[srcFiles.size()]));

                ctx.setFilestoreId(dstStore.getId());

                OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
                oxcox.changeFilestoreDataFor(ctx);

                final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                Cache cache = cacheService.getCache("Filestore");
                cache.clear();
                Cache contextCache = cacheService.getCache("Context");
                contextCache.remove(ctx.getId());

                for (PostProcessTask task : postProcessTasks) {
                    task.perform();
                }
            } catch (OXException e) {
                throw new StorageException(e);
            }
        }
    }

    /**
     * starting the thread
     *
     * @throws StorageException
     * @throws InterruptedException
     * @throws IOException
     * @throws ProgrammErrorException
     */
    @Override
    public Void call() throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        try {
            copy();
        } catch (final StorageException e) {
            log.error("", e);
            // Because the client side only knows of the exceptions defined in the core we have
            // to throw the trace as string
            throw new StorageException(e.toString());
        } catch (final IOException e) {
            log.error("", e);
            throw e;
        } catch (final InterruptedException e) {
            log.error("", e);
            throw e;
        } catch (final ProgrammErrorException e) {
            log.error("", e);
            throw e;
        }
        return null;
    }

}
