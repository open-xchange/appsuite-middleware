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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.lock.impl;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import junit.framework.TestCase;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.internal.JCSCacheService;
import com.openexchange.caching.internal.JCSCacheServiceInit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.lock.LockService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link fBug41681Test}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug41681Test extends TestCase {

    private LockService lockService;

    private static final int THREAD_COUNT = 10;
    private static final int ACQUIRIES_PER_THREAD = 10;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        final File propertyFile = File.createTempFile("cache", "ccf");
        String regionName = "GenLocks";
        PrintWriter out = new PrintWriter(propertyFile.getAbsolutePath());
        out.write(
            "jcs.default=LTCP\n" +
            "jcs.region." + regionName + "=\n" +
            "jcs.region." + regionName + ".cacheattributes=org.apache.jcs.engine.CompositeCacheAttributes\n" +
            "jcs.region." + regionName + ".cacheattributes.MaxObjects=20000000\n" +
            "jcs.region." + regionName + ".cacheattributes.MemoryCacheName=org.apache.jcs.engine.memory.lru.LRUMemoryCache\n" +
            "jcs.region." + regionName + ".cacheattributes.UseMemoryShrinker=true\n" +
            "jcs.region." + regionName + ".cacheattributes.MaxMemoryIdleTimeSeconds=150\n" +
            "jcs.region." + regionName + ".cacheattributes.ShrinkerIntervalSeconds=30\n" +
            "jcs.region." + regionName + ".elementattributes=org.apache.jcs.engine.ElementAttributes\n" +
            "jcs.region." + regionName + ".elementattributes.IsEternal=false\n" +
            "jcs.region." + regionName + ".elementattributes.MaxLifeSeconds=-1\n" +
            "jcs.region." + regionName + ".elementattributes.IdleTime=150\n" +
            "jcs.region." + regionName + ".elementattributes.IsSpool=false\n" +
            "jcs.region." + regionName + ".elementattributes.IsRemote=false\n" +
            "jcs.region." + regionName + ".elementattributes.IsLateral=false\n")
        ;
        out.flush();
        out.close();
        ServerServiceRegistry services = ServerServiceRegistry.getInstance();
        SimConfigurationService configService = new SimConfigurationService() {

            @Override
            public File getFileByName(String fileName) {
                return propertyFile;
            }

        };
        configService.stringProperties.put("com.openexchange.caching.configfile", propertyFile.getAbsolutePath());
        configService.stringProperties.put("com.openexchange.caching.jcs.eventInvalidation", "false");
        services.addService(ConfigurationService.class, configService);
        JCSCacheServiceInit.initInstance();
        JCSCacheServiceInit.getInstance().start(configService);
        services.addService(CacheService.class, JCSCacheService.getInstance());
        services.addService(CacheKeyService.class, JCSCacheService.getInstance());
        lockService = new LockServiceImpl();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLockService() throws Exception {
        Thread[] threads = new Thread[THREAD_COUNT];
        final AtomicReference<OXException> exceptionHolder = new AtomicReference<OXException>();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < ACQUIRIES_PER_THREAD; i++) {
                    String key = "OXFolderCache-48470-1";
                    try {
                        Lock lock = lockService.getSelfCleaningLockFor(key);
                        try {
                            lock.lock();
                        } finally {
                            lock.unlock();
                        }
                    } catch (OXException e) {
                        exceptionHolder.set(e);
                    }
                }
            }
        };
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(runnable);
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i].join();
        }
        OXException e = exceptionHolder.get();
        if (null != e) {
            fail(e.getMessage());
        }
    }

}
