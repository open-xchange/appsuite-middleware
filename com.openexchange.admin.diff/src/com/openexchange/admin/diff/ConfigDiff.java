/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.diff;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.openexchange.admin.diff.file.handler.FileHandler;
import com.openexchange.admin.diff.file.handler.IConfFileHandler;
import com.openexchange.admin.diff.file.handler.TooManyFilesException;
import com.openexchange.admin.diff.file.provider.ConfFolderFileProvider;
import com.openexchange.admin.diff.file.provider.JarFileProvider;
import com.openexchange.admin.diff.file.provider.RecursiveFileProvider;
import com.openexchange.admin.diff.result.DiffResult;

/**
 * Main class that is invoked to execute the configuration diffs.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ConfigDiff {

    /**
     * Default folder for original configuration files
     */
    protected String originalFolder = "/opt/open-xchange/bundles";

    /**
     * Default folder for installed configuration files
     */
    protected String installationFolder = "/opt/open-xchange/etc";

    /**
     * Handles processing with files
     */
    private final FileHandler fileHandler = new FileHandler();

    /**
     * Handlers that are registered for diff processing
     */
    private static Set<IConfFileHandler> handlers = new HashSet<IConfFileHandler>();

    public static void register(IConfFileHandler handler) {
        handlers.add(handler);
    }

    public DiffResult run() {
        final DiffResult diffResult = new DiffResult();

        final FileHandler fileHandler = this.fileHandler;
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<Void> orgFuture = executor.submit(new Callable<Void>() {

            @Override
            public Void call() throws TooManyFilesException {
                fileHandler.readConfFiles(diffResult, new File(originalFolder), true, new JarFileProvider(), new ConfFolderFileProvider());
                return null;
            }
        });

        Future<Void> instFuture = executor.submit(new Callable<Void>() {

            @Override
            public Void call() throws TooManyFilesException {
                fileHandler.readConfFiles(diffResult, new File(installationFolder), false, new RecursiveFileProvider());
                return null;
            }
        });

        try {
            orgFuture.get();
            instFuture.get();
        } catch (ExecutionException e) {
            Throwable rootException = e.getCause();
            if (rootException instanceof TooManyFilesException) {
                diffResult.reset();
                return diffResult;
            }
        } catch (InterruptedException e) {
            diffResult.getProcessingErrors().add(e.getLocalizedMessage());
        } finally {
            executor.shutdown();
        }

        return getDiffs(diffResult);
    }

    /**
     * Calls all registered handles to get the diffs
     *
     * @param diffResult - object to add the DiffResults to
     * @return - DiffResult object with all diffs
     */
    protected DiffResult getDiffs(DiffResult diffResult) {

        for (IConfFileHandler handler : handlers) {
            handler.getDiff(diffResult);
        }
        return diffResult;
    }
}
