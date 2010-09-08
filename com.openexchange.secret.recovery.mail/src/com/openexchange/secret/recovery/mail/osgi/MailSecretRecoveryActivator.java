
package com.openexchange.secret.recovery.mail.osgi;

import org.osgi.framework.ServiceRegistration;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.secret.recovery.SecretConsistencyCheck;
import com.openexchange.secret.recovery.SecretMigrator;
import com.openexchange.server.osgiservice.DeferredActivator;
import com.openexchange.tools.session.ServerSession;

public class MailSecretRecoveryActivator extends DeferredActivator {

    private static final Class<?>[] NEEDED_SERVICES = { MailAccountStorageService.class };
    private ServiceRegistration consistencyReg;
    private ServiceRegistration migratorReg;

    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED_SERVICES;
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        // Ignore
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        // Ignore
    }

    @Override
    protected void startBundle() throws Exception {
        final MailAccountStorageService mailAccountStorage = getService(MailAccountStorageService.class);

        consistencyReg = context.registerService(SecretConsistencyCheck.class.getName(), new SecretConsistencyCheck() {

            public boolean checkSecretCanDecryptStrings(ServerSession session, String secret) throws AbstractOXException {
                return mailAccountStorage.checkCanDecryptPasswords(session.getUserId(), session.getContextId(), secret);
            }

        }, null);
        
        migratorReg = context.registerService(SecretMigrator.class.getName(), new SecretMigrator() {

            public void migrate(String oldSecret, String newSecret, ServerSession session) throws AbstractOXException {
                mailAccountStorage.migratePasswords(session.getUserId(), session.getContextId(), oldSecret, newSecret);
            }
            
        }, null);
    }

    @Override
    protected void stopBundle() throws Exception {
        if(consistencyReg != null) {
            consistencyReg.unregister();
        }
        
        if(migratorReg != null) {
            migratorReg.unregister();
        }
    }

}
