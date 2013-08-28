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

package com.openexchange.subscribe.crawler.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.ho.yaml.Yaml;
import com.openexchange.config.ConfigurationService;
import com.openexchange.log.LogFactory;
import com.openexchange.subscribe.crawler.CrawlerDescription;
import com.openexchange.subscribe.crawler.osgi.Activator;

/**
 * {@link CrawlerUpdateTask}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CrawlerUpdateTask implements Runnable {

    private final ConfigurationService configurationService;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CrawlerUpdateTask.class));

    private static final String UPDATE_DIRECTORY_PATH_PROPERTY = "com.openexchange.subscribe.crawler.updatepath";

    private static final String LAST_UPDATED_FILE_PROPERTY = "com.openexchange.subscribe.crawler.updatedfile";

    private final Activator activator;

    public CrawlerUpdateTask(ConfigurationService configurationService, Activator activator) {
        this.configurationService = configurationService;
        this.activator = activator;
    }

    @Override
    public void run() {

        if (configurationService != null) {
            final String lastUpdatedFilePath = configurationService.getProperty(LAST_UPDATED_FILE_PROPERTY);
            final String updateDirectoryPath = configurationService.getProperty(UPDATE_DIRECTORY_PATH_PROPERTY);
            try {
                long now = Calendar.getInstance().getTimeInMillis();
                URL url = new URL(lastUpdatedFilePath);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setIfModifiedSince(activator.getLAST_TIME_CHECKED());
                con.connect();
                ArrayList<String> ymlFilenames = new ArrayList<String>();
                // Only if the indicator-page has been modified is any further action taken. This keeps traffic to a minimum
                if (con.getResponseCode() != 304) {
                    // Get the directory page
                    url = new URL(updateDirectoryPath);
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();
                    BufferedReader in = new java.io.BufferedReader(new InputStreamReader(con.getInputStream()));
                    String line;
                    // Get the filenames of all yml-files via regex
                    while ((line = in.readLine()) != null) {
                        String regex = "<a href=\"(.*\\.yml)\"";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            if (matcher.groupCount() > 0) {
                                ymlFilenames.add(matcher.group(1));
                            }
                        }
                    }
                    downloadAndCheckTheFiles(configurationService, updateDirectoryPath, ymlFilenames);
                    // Set the date so that we remember that we looked for changes for today and before
                    activator.setLAST_TIME_CHECKED(now);
                } else {
                    LOG.info("No updated crawlers are available.");
                }

            } catch (MalformedURLException e) {
                LOG.error(e.getMessage(), e);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void downloadAndCheckTheFiles(final ConfigurationService config, final String updateDirectoryPath, ArrayList<String> ymlFilenames) throws MalformedURLException, IOException, FileNotFoundException {
        BufferedReader in;
        String line;
        for (String ymlFilename : ymlFilenames) {
            // Create CrawlerDescriptions of the yml-files
            URL url = new URL(updateDirectoryPath + ymlFilename);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            in = new java.io.BufferedReader(new InputStreamReader(con.getInputStream()));
            String crawlerDescriptionString = "";
            while ((line = in.readLine()) != null) {
                crawlerDescriptionString += line + "\n";
            }
            HashMap<String, CrawlerDescription> currentCrawlers = new HashMap<String, CrawlerDescription>();
            ArrayList<CrawlerDescription> crawlers = activator.getCrawlersFromFilesystem(config);
            for (CrawlerDescription crawler : crawlers) {
                currentCrawlers.put(crawler.getId(), crawler);
            }

            CrawlerDescription possibleNewCrawlerDescription = Yaml.loadType(crawlerDescriptionString, CrawlerDescription.class);
            CrawlerDescription currentCrawlerDescription = currentCrawlers.get(possibleNewCrawlerDescription.getId());
            // Check each file if it is compatible and of higher priority
            if (possibleNewCrawlerDescription != null) {
                LOG.info("There is a possible new crawler description : " + ymlFilename);
                // is it compatible to the installed API?
                if (possibleNewCrawlerDescription.getCrawlerApiVersion() <= Activator.getCRAWLER_API_VERSION()) {
                    LOG.info("The API version fits");
                    final String path = config.getProperty(Activator.DIR_NAME_PROPERTY);
                    // it is an updated description for an existing crawler
                    if (currentCrawlerDescription != null) {
                        LOG.info("There is an old description that could be replaced");
                        if (possibleNewCrawlerDescription.getPriority() > currentCrawlerDescription.getPriority()) {
                            LOG.info("The priority is higher than the existing file so it will be replaced");
                            //removal needs to happen before saving in case of the filename being the same
                            activator.removeCrawlerFromFilesystem(config, possibleNewCrawlerDescription.getId());
                            Yaml.dump(possibleNewCrawlerDescription, new File(config.getDirectory(path), ymlFilename));
                            activator.restartSingleCrawler(possibleNewCrawlerDescription.getId(), config);
                        } else {
                            LOG.info("The priority is lower than that of the existing file so nothing will be done");
                        }
                        // it is a description for a completely new crawler
                    } else {
                        // only download configurations for new crawlers if this is enabled by configuration-file
                        boolean onlyUpdateInstalled = Boolean.parseBoolean(config.getProperty(Activator.ONLY_UPDATE_INSTALLED));
                        if (!onlyUpdateInstalled){
                            LOG.info("It is a completely new crawler and will be saved");
                            Yaml.dump(possibleNewCrawlerDescription, new File(config.getDirectory(path), ymlFilename));
                            activator.restartSingleCrawler(possibleNewCrawlerDescription.getId(), config);
                        } else {
                            LOG.info("Configuration forbids to install any crawlers that are not updates to existing services. Nothing will be done.");
                        }
                    }
                } else {
                    LOG.info("The API-Version does not fit");
                }
            } else {
                LOG.info("there is no new crawler description");
            }
        }
    }
}
