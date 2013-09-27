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

package com.openexchange.subscribe.crawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.crawler.internal.CrawlerUpdateTask;
import com.openexchange.subscribe.crawler.osgi.Activator;


/**
 * {@link AutoUpdateTest}
 * This tests the new auto-update-functionality of the crawler-bundle
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AutoUpdateTest extends GenericSubscribeServiceTestHelpers {

    private SimConfigurationService configurationService;
    private final String allCrawlersRepositoryPath = "local_only/crawler_repository/";
    private final File allCrawlersRepositoryDirectory = new File(allCrawlersRepositoryPath);
    private String installedDirectoryPath;
    private File installedDirectory;
    private final String availableUpdatesPath = "/Users/karstenwill/Sites/crawlers/files/";
    private final File availableUpdatesDirectory = new File(availableUpdatesPath);
    private Activator activator;

    @Override
    public void setUp(){
        configurationService = new SimConfigurationService();
        //set the local crawler path
        configurationService.stringProperties.put("com.openexchange.subscribe.crawler.path", "local_only/installed_crawlers/");
        //set the path for the updatecheck-file
        configurationService.stringProperties.put("com.openexchange.subscribe.crawler.updatedfile", "http://localhost/~karstenwill/crawlers/updates.html");
        //set the path for the updated crawler configfiles
        configurationService.stringProperties.put("com.openexchange.subscribe.crawler.updatepath", "http://localhost/~karstenwill/crawlers/files/");
        //enable download of previously uninstalled crawler-configs
        configurationService.stringProperties.put("com.openexchange.subscribe.crawler.onlyupdatealreadyinstalled", "false");
        activator = new Activator();
        //set an empty ServiceRegistration as this is not the subject of this test
        activator.setServices(new ArrayList<ServiceRegistration<?>>());
        //set a simulated BundleContext as this is not the subject of this test
        activator.setBundleContext(new SimBundleContext());
        installedDirectoryPath = configurationService.getProperty("com.openexchange.subscribe.crawler.path");
        installedDirectory = new File(installedDirectoryPath);
        copyFileFromRepository("Facebook_standard.yml", installedDirectoryPath);
    }

    @Override
    public void tearDown(){
        clearWorkingDirectory(installedDirectory);
        clearWorkingDirectory(availableUpdatesDirectory);
    }

    public void testCrawlersWithAppropriateApiAndHigherPriorityWillBeDownloaded(){
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("Facebook_higherPriority_sameApi.yml", availableUpdatesPath);
        update.run();
        assertTrue("The new file should be present.", thisFileIsPresent("Facebook_higherPriority_sameApi.yml"));
        assertFalse("The old file should have been removed.", thisFileIsPresent("Facebook_standard.yml"));
    }

    public void testCrawlersWithHigherPriorityAndLowerApiWillBeDownloaded(){
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("Facebook_higherPriority_lowerApi.yml", availableUpdatesPath);
        update.run();
        assertTrue("The new file should be present.", thisFileIsPresent("Facebook_higherPriority_lowerApi.yml"));
        assertFalse("The old file should have been removed.", thisFileIsPresent("Facebook_standard.yml"));
    }

    public void testCrawlersWithLowerPriorityWillNotBeDownloaded(){
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("Facebook_lowerPriority.yml", availableUpdatesPath);
        update.run();
        assertFalse("The new file should not be present.", thisFileIsPresent("Facebook_lowerPriority.yml"));
        assertTrue("The old file should still be there", thisFileIsPresent("Facebook_standard.yml"));
    }

    public void testCrawlersWithHigherApiWillNotBeDownloaded(){
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("Facebook_higherPriority_higherApi.yml", availableUpdatesPath);
        update.run();
        assertFalse("The new file should not be present.", thisFileIsPresent("Facebook_higherPriority_higherApi.yml"));
        assertTrue("The old file should still be there", thisFileIsPresent("Facebook_standard.yml"));
    }

    public void testOnlyUpdateIfThereAreNewerUpdates(){
        activator.setLastTimeChecked(Calendar.getInstance().getTimeInMillis());
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("Facebook_higherPriority_sameApi.yml", availableUpdatesPath);
        update.run();
        assertFalse("The new file should not be present.", thisFileIsPresent("Facebook_higherPriority_sameApi.yml"));
        assertTrue("The old file should still be there", thisFileIsPresent("Facebook_standard.yml"));
    }

    public void testNewCrawlersWithRightApiWillBeDownloaded(){
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("new_service.yml", availableUpdatesPath);
        update.run();
        assertTrue("The new file should be present.", thisFileIsPresent("new_service.yml"));
    }

    public void testNewCrawlersWithRightApiWillNotBeDownloadedIfNotEnabled(){
        configurationService.stringProperties.put("com.openexchange.subscribe.crawler.onlyupdatealreadyinstalled", "true");
        final CrawlerUpdateTask update = new CrawlerUpdateTask(configurationService, activator);
        copyFileFromRepository("new_service.yml", availableUpdatesPath);
        update.run();
        // reset to default
        configurationService.stringProperties.put("com.openexchange.subscribe.crawler.onlyupdatealreadyinstalled", "false");
        assertFalse("The new file should not be present.", thisFileIsPresent("new_service.yml"));
    }

    public void testOnlyUpdateWithValidCredentials(){

    }

    private boolean thisFileIsPresent(final String filename) {
        final File[] files = installedDirectory.listFiles();
        for (final File file : files){
            if (file.getName().equals(filename)){
                return true;
            }
        }
        return false;
    }

    private void clearWorkingDirectory(final File directory){
        final File[] files = directory.listFiles();
        for (final File file : files){
            file.delete();
        }
    }

    private void copyFileFromRepository(final String filename, final String destinationDirectoryPath){
        final File source = new File(allCrawlersRepositoryPath + filename);
        final File target = new File(destinationDirectoryPath + filename);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(target);

            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
              out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            Streams.close(in, out);
        }
    }
}
