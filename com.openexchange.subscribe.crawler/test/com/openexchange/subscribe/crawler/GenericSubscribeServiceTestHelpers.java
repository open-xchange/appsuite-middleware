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

package com.openexchange.subscribe.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import com.openexchange.config.SimConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.subscribe.crawler.internal.GenericSubscribeService;
import com.openexchange.subscribe.crawler.osgi.CrawlersActivator;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public abstract class GenericSubscribeServiceTestHelpers extends TestCase {

    public static final String VALID_EMAIL_REGEX = "([a-z@A-Z0-9\\.\\-\\{\\}\\#\\|\\^\\$\\*\\+\\?\\'\\/!%&=_`~]*)";
    public static final String VALID_NAME = "([a-zA-Z\\s\u00e4\u00f6\u00fc\u00df-\u00e9\u00e8]*)";
    public static final String VALID_PHONE_REGEX = "([0-9\\s\\+\\-\\/\\(\\)]*)";
    public static final String VALID_ADDRESS_PART = "[a-zA-Z0-9\\.\\s\u00e4\u00f6\u00fc\u00df]*";

    private HashMap<String, String> map;
    List<CrawlerDescription> crawlers;
    private CrawlersActivator activator;

    public GenericSubscribeServiceTestHelpers() {
        super();
    }

    public GenericSubscribeServiceTestHelpers(final String name) {
        super(name);
    }

    /**
     * Get all yml-files in the config directory and create crawlers out of them.
     */
    @Override
    public void setUp() {
        try {
            // insert path to credentials-file here (switch for automated tests (Hudson) / local tests)
            map = (HashMap<String, String>) new Yaml().load(new FileReader(getSecretsFile()));
            // map = (HashMap<String, String>) Yaml.load(new File("/Users/karstenwill/Documents/open-xchange/crawler/crawlerCredentials.yml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SimConfigurationService config = new SimConfigurationService(System.getProperty("openexchange.propdir"));
        //test with the real crawlers (switch for automated tests (Hudson) / local tests)
        config.stringProperties.put("com.openexchange.subscribe.crawler.path", System.getProperty("crawlersConf"));
        config.stringProperties.put("com.openexchange.subscribe.crawler.gmx.de", Boolean.TRUE.toString());
        config.stringProperties.put("com.openexchange.subscribe.crawler.googlemail", Boolean.TRUE.toString());
        config.stringProperties.put("com.openexchange.subscribe.crawler.web.de", Boolean.TRUE.toString());
        config.stringProperties.put("com.openexchange.subscribe.crawler.t-online.de", Boolean.TRUE.toString());
        // config.stringProperties.put("com.openexchange.subscribe.crawler.path", "conf/crawlers/");
        activator = new CrawlersActivator();
        crawlers = activator.getCrawlersFromFilesystem(config);

        // insert ical-Parser class (switch for automated tests (Hudson) / local tests)
        //activator.setICalParser(new com.openexchange.data.conversion.ical.ical4j.ICal4JParser());
        activator.setICalParser(null);
    }

    protected void findOutIfThereAreContactsForThisConfiguration(final String username, final String password, final CrawlerDescription crawler) {
        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, false, false);
    }

    protected void findOutIfThereAreContactsForThisConfiguration(final String username, final String password, final CrawlerDescription crawler, final boolean verbose) {
        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, verbose, false);
    }

    protected void findOutIfThereAreContactsForThisConfiguration(final String username, final String password, final CrawlerDescription crawler, final boolean verbose, final boolean enableJavascript) {
        Calendar rightNow = Calendar.getInstance();
        final long before = rightNow.getTime().getTime();
        // create a GenericSubscribeService that uses this CrawlerDescription
        final GenericSubscribeService service = new GenericSubscribeService(
            crawler.getDisplayName(),
            crawler.getId(),
            crawler.getModule(),
            crawler.getWorkflowString(),
            crawler.getPriority(),
            activator,
            enableJavascript);

        final Workflow testWorkflow = service.getWorkflow();

        Contact[] contacts = new Contact[0];
        try {
            contacts = (Contact[]) testWorkflow.execute(username, password);
        } catch (final OXException e) {
            e.printStackTrace();
        }
        assertTrue("There are no contacts for crawler : " + crawler.getDisplayName(), contacts.length != 0);
        if (verbose){
            for (final Contact contact : contacts) {
                System.out.println("contact retrieved is : " + contact.getDisplayName());
                System.out.println("contacts first name : " + contact.getGivenName());
                System.out.println("contacts last name : " + contact.getSurName());
                System.out.println("contacts title : " + contact.getTitle());
                System.out.println("contacts position : " + contact.getPosition());
                System.out.println("contacts business email address : " + contact.getEmail1());
                System.out.println("contacts private email address : " + contact.getEmail2());
                System.out.println("contacts business mobile phone number : " + contact.getCellularTelephone1());
                System.out.println("contacts private mobile phone number : " + contact.getCellularTelephone2());
                System.out.println("contacts work phone number : " + contact.getTelephoneBusiness1());
                System.out.println("contacts home phone number : " + contact.getTelephoneHome1());
                System.out.println("contacts instant messenger : " + contact.getInstantMessenger1());
                System.out.println("contacts birthday : " + contact.getBirthday());
                System.out.println("contacts picture type : " + contact.getImageContentType());
                System.out.println("contacts street of work : " + contact.getStreetBusiness());
                System.out.println("contacts postal code of work : " + contact.getPostalCodeBusiness());
                System.out.println("contacts city of work : " + contact.getCityBusiness());
                System.out.println("contacts country of work : " + contact.getCountryBusiness());
                System.out.println("contacts street of private address : " + contact.getStreetHome());
                System.out.println("contacts postal code of private address : " + contact.getPostalCodeHome());
                System.out.println("contacts city of private address : " + contact.getCityHome());
                System.out.println("contacts country of private address : " + contact.getCountryHome());
                System.out.println("contacts company : " + contact.getCompany());
                System.out.println("contacts note : " + contact.getNote());

                System.out.println("----------");
            }
        }
        System.out.println("Number of contacts retrieved : " + Integer.toString(contacts.length));
        rightNow = Calendar.getInstance();
        final long after = rightNow.getTime().getTime();
        System.out.println("Time : " + Long.toString((after - before) / 1000) + " seconds");
    }

    protected void findOutIfThereAreEventsForThisConfiguration(final String username, final String password, final CrawlerDescription crawler, final boolean verbose, final boolean enableJavascript) {
        Calendar rightNow = Calendar.getInstance();
        final long before = rightNow.getTime().getTime();
        // create a GenericSubscribeService that uses this CrawlerDescription
        final GenericSubscribeService service = new GenericSubscribeService(
            crawler.getDisplayName(),
            crawler.getId(),
            crawler.getModule(),
            crawler.getWorkflowString(),
            crawler.getPriority(),
            activator,
            enableJavascript);

        final Workflow testWorkflow = service.getWorkflow();
        testWorkflow.setDebuggingEnabled(true);
        Appointment[] events = new Appointment[0];
        try {
            events = (Appointment[])testWorkflow.execute(username, password);
        } catch (final OXException e) {
            e.printStackTrace();
        }
        assertTrue("There are no events for crawler : " + crawler.getDisplayName(), events.length != 0);
        if (verbose){
            for (final Appointment event : events) {
                System.out.println("event retrieved is : " + event.getTitle());
                System.out.println("Timezone is : " + event.getTimezone());
                System.out.println("Start Date is : " + event.getStartDate());
                System.out.println("End Date is : " + event.getEndDate());
                System.out.println("Description is : " + event.getNote());
                System.out.println("----------");
            }
        }
        System.out.println("Number of events retrieved : " + Integer.toString(events.length));
        rightNow = Calendar.getInstance();
        final long after = rightNow.getTime().getTime();
        System.out.println("Time : " + Long.toString((after - before) / 1000) + " seconds");
    }

    protected void findOutIfThereAreTasksForThisConfiguration(final String username, final String password, final CrawlerDescription crawler, final boolean verbose, final boolean enableJavascript) {
        Calendar rightNow = Calendar.getInstance();
        final long before = rightNow.getTime().getTime();
        // create a GenericSubscribeService that uses this CrawlerDescription
        final GenericSubscribeService service = new GenericSubscribeService(
            crawler.getDisplayName(),
            crawler.getId(),
            crawler.getModule(),
            crawler.getWorkflowString(),
            crawler.getPriority(),
            activator,
            enableJavascript);

        final Workflow testWorkflow = service.getWorkflow();
        testWorkflow.setDebuggingEnabled(true);
        Task[] tasks = new Task[0];
        try {
            tasks = (Task[])testWorkflow.execute(username, password);
        } catch (final OXException e) {
            e.printStackTrace();
        }
        assertTrue("There are no tasks for crawler : " + crawler.getDisplayName(), tasks.length != 0);
        if (verbose){
            for (final Task task : tasks) {
                System.out.println("task retrieved is : " + task.getTitle());
                System.out.println("Start Date is : " + task.getStartDate());
                System.out.println("End Date is : " + task.getEndDate());
                System.out.println("Description is : " + task.getNote());
                System.out.println("----------");
            }
        }
        System.out.println("Number of events retrieved : " + Integer.toString(tasks.length));
        rightNow = Calendar.getInstance();
        final long after = rightNow.getTime().getTime();
        System.out.println("Time : " + Long.toString((after - before) / 1000) + " seconds");
    }

    /**
     * Create files for this CrawlerDescription that will be used by the live system
     *
     * @param crawler
     */
    protected void dumpThis(final CrawlerDescription crawler, final String filename) {
        try {
            new Yaml().dump(crawler, new FileWriter(new File("../open-xchange-development/crawlers/" + filename + ".yml")));
            new Yaml().dump(crawler, new FileWriter(new File("conf/crawlers/" + filename + ".yml")));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected void checkSingleCrawler(String nameOfCrawlerToCheck) {
        boolean crawlerFound = false;
        boolean credentialsFound = false;
        for (CrawlerDescription crawler : crawlers) {
            String crawlerName = crawler.getDisplayName();
            if (crawlerName.equals(nameOfCrawlerToCheck)) {
                crawlerFound = true;
                String [] domains = {".de",".com",".uk",".fr",".es",".nl"};
                for (String domain : domains){
                    if (map.containsKey(crawlerName+"_user" + domain) && map.containsKey(crawlerName+"_password" + domain)){
                        credentialsFound = true;
                        String username = map.get(crawlerName+"_user" + domain);
                        String password = map.get(crawlerName+"_password" + domain);
                        System.out.println("***** Testing crawler : " + crawlerName + " for domain : " + domain);
                        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
                    }
                }
            }
        }
        if (! crawlerFound){fail("No description found for crawler : " + nameOfCrawlerToCheck);}
        if (! credentialsFound) {fail("No credentials found for crawler : " + nameOfCrawlerToCheck);}

    }

    private File getSecretsFile() {
        String value = System.getProperty("secretFile");
        if (null == value) {
            fail("File for crawler credentials is not defined.");
        }
        File secrets = new File(value);
        if (!secrets.exists()) {
            fail("File for crawler credentials does not exist.");
        }
        if (!secrets.isFile()) {
            fail("File for crawler credentials is not a file.");
        }
        if (!secrets.canRead()) {
            fail("File for crawler credentials can not be read.");
        }
        return secrets;
    }
}
