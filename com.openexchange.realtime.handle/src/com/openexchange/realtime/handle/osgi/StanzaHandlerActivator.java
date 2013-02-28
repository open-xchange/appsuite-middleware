package com.openexchange.realtime.handle.osgi;

import java.util.concurrent.Future;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.handle.impl.StanzaQueueServiceImpl;
import com.openexchange.realtime.handle.impl.iq.IQHandler;
import com.openexchange.realtime.handle.impl.message.MessageHandler;
import com.openexchange.realtime.handle.impl.presence.PresenceHandler;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

public class StanzaHandlerActivator extends HousekeepingActivator {
    
    private Future<Object> presenceFuture;
    
    private Future<Object> messageFuture;
    
    private Future<Object> iqFuture;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ResourceDirectory.class, MessageDispatcher.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        StanzaQueueServiceImpl queueService = new StanzaQueueServiceImpl();
        ThreadPoolService threadPoolService = getService(ThreadPoolService.class);
        presenceFuture = threadPoolService.submit(ThreadPools.task(new PresenceHandler(queueService.getPresenceQueue())));
        messageFuture = threadPoolService.submit(ThreadPools.task(new MessageHandler(queueService.getMessageQueue())));
        iqFuture = threadPoolService.submit(ThreadPools.task(new IQHandler(queueService.getIqQueue())));
        registerService(StanzaQueueService.class, queueService);
    }
    
    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        if (presenceFuture != null) {
            presenceFuture.cancel(true);
        }
        
        if (messageFuture != null) {
            messageFuture.cancel(true);
        }
        
        if (iqFuture != null) {
            iqFuture.cancel(true);
        }
    }

}
