package com.openexchange.principleusecount.impl.osgi;

import com.openexchange.database.DatabaseService;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.principalusecount.PrincipalUseCountService;
import com.openexchange.principleusecount.impl.PrincipalUseCountDeleteListener;
import com.openexchange.principleusecount.impl.PrincipalUseCountServiceImpl;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { DatabaseService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        Services.setServiceLookup(this);
        registerService(DeleteListener.class, new PrincipalUseCountDeleteListener());
        registerService(PrincipalUseCountService.class, new PrincipalUseCountServiceImpl());
    }

}
