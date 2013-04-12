
package com.openexchange.solr.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.solr.core.CoreContainer;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.service.QuartzService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.log.LogFactory;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.management.ManagementService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.solr.SolrAccessService;
import com.openexchange.solr.SolrCoreConfigService;
import com.openexchange.solr.SolrIndexEventProperties;
import com.openexchange.solr.SolrMBean;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.groupware.SolrCoreLoginHandler;
import com.openexchange.solr.groupware.SolrCoresCreateTableService;
import com.openexchange.solr.groupware.SolrCoresCreateTableTask;
import com.openexchange.solr.groupware.SolrIndexEventHandler;
import com.openexchange.solr.internal.DelegationSolrAccessImpl;
import com.openexchange.solr.internal.EmbeddedSolrAccessImpl;
import com.openexchange.solr.internal.RMISolrAccessImpl;
import com.openexchange.solr.internal.Services;
import com.openexchange.solr.internal.SolrConsistencyJob;
import com.openexchange.solr.internal.SolrCoreConfigServiceImpl;
import com.openexchange.solr.internal.SolrCoreTools;
import com.openexchange.solr.internal.SolrMBeanImpl;
import com.openexchange.solr.internal.SolrNodeListener;
import com.openexchange.solr.internal.SolrServlet;
import com.openexchange.solr.rmi.RMISolrAccessService;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link SolrActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrActivator extends HousekeepingActivator {

    static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrActivator.class));

    private volatile DelegationSolrAccessImpl delegationAccess;

    private static RMISolrAccessService solrRMI;

    private SolrMBean solrMBean;

    private ObjectName solrMBeanName;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, DatabaseService.class,
            ThreadPoolService.class, HazelcastInstance.class, HttpService.class };
    }

    @Override
    protected void startBundle() throws OXException {
        Services.setServiceLookup(this);
        ConfigurationService config = getService(ConfigurationService.class);
        new CheckConfigDBTables(getService(DatabaseService.class)).checkTables();
        EmbeddedSolrAccessImpl embeddedAccess = new EmbeddedSolrAccessImpl();
        embeddedAccess.startUp();
        DelegationSolrAccessImpl accessService = this.delegationAccess = new DelegationSolrAccessImpl(embeddedAccess);
        registerService(SolrAccessService.class, accessService);
        addService(SolrAccessService.class, accessService);

        /*
         * Servlet
         */
        boolean enableDebugServlet = config.getBoolProperty("com.openexchange.solr.enableDebugServlet", false);
        if (enableDebugServlet) {
            CoreContainer coreContainer = embeddedAccess.getCoreContainer();
            SolrServlet solrServlet = new SolrServlet(coreContainer, "/ox-solr");
            HttpService httpService = getService(HttpService.class);
            try {
                httpService.registerServlet("/ox-solr", solrServlet, null, null);
            } catch (ServletException e) {
                LOG.warn("Could not register SolrServlet.", e);
            } catch (NamespaceException e) {
                LOG.warn("Could not register SolrServlet.", e);
            }
        }

        SolrCoreConfigServiceImpl coreService = new SolrCoreConfigServiceImpl();
        registerService(SolrCoreConfigService.class, coreService);
        addService(SolrCoreConfigService.class, coreService);
        solrRMI = new RMISolrAccessImpl(accessService);
        registerService(Remote.class, solrRMI);
        Dictionary<String, String> eventProperties = new Hashtable<String, String>(1);
        eventProperties.put(EventConstants.EVENT_TOPIC, SolrIndexEventProperties.TOPIC_LOCK_INDEX);
        registerService(EventHandler.class, new SolrIndexEventHandler(accessService), eventProperties);

        SolrCoresCreateTableService createTableService = new SolrCoresCreateTableService();
        registerService(CreateTableService.class, createTableService);
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new SolrCoresCreateTableTask(
            createTableService)));
        registerService(LoginHandlerService.class, new SolrCoreLoginHandler(embeddedAccess));
        registerMBean(coreService);

        /*
         * Consistency stuff
         */
        HazelcastInstance hazelcast = getService(HazelcastInstance.class);
        boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
        if (isSolrNode) {
            IMap<String, Integer> solrNodes = hazelcast.getMap(SolrCoreTools.SOLR_NODE_MAP);
            String memberAddress = hazelcast.getCluster().getLocalMember().getInetSocketAddress().getAddress().getHostAddress();
            solrNodes.put(memberAddress, new Integer(0));

            track(QuartzService.class, new SimpleRegistryListener<QuartzService>() {
                @Override
                public void added(ServiceReference<QuartzService> ref, QuartzService service) {
                    try {
                        Scheduler scheduler = service.getDefaultScheduler();
                        if (scheduler.isStarted()) {
                            JobDetail jobDetail = JobBuilder
                                .newJob(SolrConsistencyJob.class)
                                .withIdentity("com.openexchange.solr", "consistencyJob")
                                .build();

                            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInMinutes(15)
                                .repeatForever()
                                .withMisfireHandlingInstructionFireNow();

                            SimpleTrigger trigger = TriggerBuilder
                                .newTrigger()
                                .withIdentity("com.openexchange.solr", "consistencyJobTrigger")
                                .forJob(jobDetail)
                                .withSchedule(scheduleBuilder)
                                .build();

                            scheduler.scheduleJob(jobDetail, trigger);
                        }
                    } catch (OXException e) {
                        LOG.error(e.getMessage(), e);
                    } catch (SchedulerException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                @Override
                public void removed(ServiceReference<QuartzService> ref, QuartzService service) {
                    // nothing to do
                }
            });
        }

        hazelcast.getCluster().addMembershipListener(new SolrNodeListener(hazelcast));
        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();

        solrRMI = null;
        ManagementService managementService = Services.optService(ManagementService.class);
        if (managementService != null && solrMBeanName != null) {
            managementService.unregisterMBean(solrMBeanName);
            solrMBean = null;
        }

        DelegationSolrAccessImpl delegationAccess = this.delegationAccess;
        if (delegationAccess != null) {
            delegationAccess.shutDown();
            this.delegationAccess = null;
        }
    }

    private void registerMBean(SolrCoreConfigServiceImpl coreService) {
        try {
            solrMBeanName = new ObjectName(SolrMBean.DOMAIN, SolrMBean.KEY, SolrMBean.VALUE);
            DelegationSolrAccessImpl delegationAccess = this.delegationAccess;
            solrMBean = new SolrMBeanImpl(delegationAccess, coreService);
            track(ManagementService.class, new SimpleRegistryListener<ManagementService>() {

                @Override
                public void added(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.registerMBean(solrMBeanName, solrMBean);
                    } catch (OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                @Override
                public void removed(ServiceReference<ManagementService> ref, ManagementService service) {
                    try {
                        service.unregisterMBean(solrMBeanName);
                    } catch (OXException e) {
                        LOG.warn(e.getMessage(), e);
                    }
                }
            });
        } catch (MalformedObjectNameException e) {
            LOG.error(e.getMessage(), e);
        } catch (NotCompliantMBeanException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
