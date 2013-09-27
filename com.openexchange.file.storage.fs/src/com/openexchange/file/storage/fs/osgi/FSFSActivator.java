package com.openexchange.file.storage.fs.osgi;

import java.io.File;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.fs.FileSystemFileStorageService;
import com.openexchange.osgi.HousekeepingActivator;

public class FSFSActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(FileStorageService.class, new FileSystemFileStorageService(this, new File("/tmp/fs")));
    }

}
