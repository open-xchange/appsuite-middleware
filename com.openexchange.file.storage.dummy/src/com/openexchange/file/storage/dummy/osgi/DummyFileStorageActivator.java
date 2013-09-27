package com.openexchange.file.storage.dummy.osgi;

import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.dummy.DummyFileStorageService;
import com.openexchange.osgi.HousekeepingActivator;

public class DummyFileStorageActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(FileStorageService.class, new DummyFileStorageService(this));
    }


}
