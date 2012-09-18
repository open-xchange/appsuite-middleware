
package com.openexchange.realtime.presence.subscribe.osgi;

import java.util.Arrays;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;
import com.openexchange.realtime.presence.subscribe.database.CreatePresenceSubscriptionDB;
import com.openexchange.realtime.presence.subscribe.database.SubscriptionsSQL;
import com.openexchange.realtime.presence.subscribe.impl.SubscriptionServiceImpl;
import com.openexchange.user.UserService;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends HousekeepingActivator {

//    private BundleActivator testFragment;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { DatabaseService.class, ContextService.class, UserService.class, MessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
       System.out.println("Here i am");
        final DatabaseService dbService = getService(DatabaseService.class);
        SubscriptionsSQL.db = dbService;
        registerService(PresenceSubscriptionService.class, new SubscriptionServiceImpl(this));
        registerService(UpdateTaskProviderService.class, new UpdateTaskProviderService() {

            @Override
            public Collection<? extends UpdateTask> getUpdateTasks() {
                return Arrays.asList(new CreatePresenceSubscriptionDB(dbService));
            }
        });

//        try {
//            Class<? extends BundleActivator> clazz = (Class<? extends BundleActivator>) Class.forName("com.openexchange.realtime.presence.subscribe.test.osgi.Activator");
//            testFragment = clazz.newInstance();
//            testFragment.start(context);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.osgi.DeferredActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
//        try {
//            if (testFragment != null) {
//                testFragment.stop(context);
//            }
//        } catch (Exception e) {
//
//        }
        super.stop(context);
    }

}
