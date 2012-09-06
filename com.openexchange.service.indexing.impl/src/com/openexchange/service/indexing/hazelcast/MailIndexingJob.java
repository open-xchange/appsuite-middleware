package com.openexchange.service.indexing.hazelcast;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hazelcast.core.DistributedTask;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.MailJobInfo;
import com.openexchange.solr.SolrCoreIdentifier;

public class MailIndexingJob implements Job {
    
    public MailIndexingJob() {
        super();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {        
        try {
            JobDataMap jobData = context.getMergedJobDataMap();
            Object object = jobData.get(HazelcastIndexingService.MAIL_JOB_INFO);
            if (!(object instanceof MailJobInfo)) {
                throw new JobExecutionException("JobDataMap did not contain valid MailJobInfo instance.", false);
            }
            
            String folder = jobData.getString("folder");
            if (folder == null) {
                throw new JobExecutionException("Folder was null.", false);
            }
            
            MailJobInfo jobInfo = (MailJobInfo) object;
            IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
            IndexAccess<MailMessage> indexAccess = indexFacade.acquireIndexAccess(Types.EMAIL, jobInfo.userId, jobInfo.contextId);
            QueryParameters queryParameters = new QueryParameters.Builder("Something").setLength(0).setHandler(SearchHandler.SIMPLE).build();
            indexAccess.query(queryParameters, Collections.singleton(MailIndexField.ID));
            indexFacade.releaseIndexAccess(indexAccess);           
            
            SolrCoreIdentifier identifier = new SolrCoreIdentifier(jobInfo.contextId, jobInfo.userId, Types.EMAIL);
            HazelcastInstance hazelcast = Services.getService(HazelcastInstance.class);
            IMap<String, String> solrCores = hazelcast.getMap("solrCoreMap");            
            String owner = solrCores.get(identifier.toString());
            if (owner == null) {
                // TODO:
//                throw new OXException("Index " + identifier.toString() + " was not active.");
            }
            
            Member executor = null;            
            for (Member member : hazelcast.getCluster().getMembers()) {
                if (owner.equals(resolveSocketAddress(member.getInetSocketAddress()))) {
                    executor = member;
                    break;
                }
            }
            
            Callable<Object> mailFolderCallable = new MailFolderCallable(folder, jobInfo);
            if (executor == null) {
                //TODO:exception
            } else if (executor.equals(hazelcast.getCluster().getLocalMember())) {
                mailFolderCallable.call();
            } else {
                FutureTask<Object> task = new DistributedTask<Object>(mailFolderCallable, executor);
                ExecutorService executorService = hazelcast.getExecutorService();
                executorService.submit(task);
                task.get();
            }
        } catch (Exception e) {
            throw new JobExecutionException(e, false);
        }
    }
    
    private String resolveSocketAddress(InetSocketAddress addr) {
        if (addr.isUnresolved()) {
            return addr.getHostName();
        } else {
            return addr.getAddress().getHostAddress();
        }
    }
}
