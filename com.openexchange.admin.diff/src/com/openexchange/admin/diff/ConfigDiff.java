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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
    private FileHandler fileHandler = new FileHandler();

    /**
     * Handlers that are registered for diff processing
     */
    private static Set<IConfFileHandler> handlers = new HashSet<IConfFileHandler>();

    public static void register(IConfFileHandler handler) {
        handlers.add(handler);
    }

    public DiffResult run() {
        final DiffResult diffResult = new DiffResult();

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
