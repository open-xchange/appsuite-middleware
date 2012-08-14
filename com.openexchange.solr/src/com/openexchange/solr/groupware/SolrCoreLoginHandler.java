package com.openexchange.solr.groupware;

import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.log.LogFactory;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.solr.SolrCoreIdentifier;
import com.openexchange.solr.SolrProperties;
import com.openexchange.solr.internal.EmbeddedSolrAccessImpl;
import com.openexchange.solr.internal.Services;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.DiscardBehavior;

public class SolrCoreLoginHandler implements LoginHandlerService {
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SolrCoreLoginHandler.class));
	
	private final EmbeddedSolrAccessImpl embeddedAccess;
	
	
	public SolrCoreLoginHandler(final EmbeddedSolrAccessImpl embeddedAccess) {
		super();
		this.embeddedAccess = embeddedAccess;
	}

	@Override
	public void handleLogin(final LoginResult login) throws OXException {
		final int contextId = login.getContext().getContextId();
		final int userId = login.getUser().getId();
		final ThreadPoolService threadPoolService = Services.getService(ThreadPoolService.class);
		final Callable<Object> task = new Callable<Object>() {
			
			@Override
			public Object call() throws Exception {
				try {
                    // TODO: extend with other modules
                    final SolrCoreIdentifier mailIdentifier = new SolrCoreIdentifier(contextId, userId, Types.EMAIL);
                    final SolrCoreIdentifier infostoreIdentifier = new SolrCoreIdentifier(contextId, userId, Types.INFOSTORE);
                    final SolrCoreIdentifier attachmentIdentifier = new SolrCoreIdentifier(contextId, userId, Types.ATTACHMENT);
                    final ConfigurationService config = Services.getService(ConfigurationService.class);
                    final boolean isSolrNode = config.getBoolProperty(SolrProperties.IS_NODE, false);
                    if (isSolrNode && !embeddedAccess.hasActiveCore(mailIdentifier)) {
                        embeddedAccess.startCore(mailIdentifier);
                    }
                    if (isSolrNode && !embeddedAccess.hasActiveCore(infostoreIdentifier)) {
                        embeddedAccess.startCore(infostoreIdentifier);
                    }
                    if (isSolrNode && !embeddedAccess.hasActiveCore(attachmentIdentifier)) {
                        embeddedAccess.startCore(attachmentIdentifier);
                    }
                } catch (OXException e) {
                    LOG.warn("Error durig core start up.", e);
                }
				
                return null;
			}
			
		};
		
		threadPoolService.submit(ThreadPools.task(task), DiscardBehavior.getInstance());
	}

	@Override
	public void handleLogout(final LoginResult logout) throws OXException {
		return;
	}

}
