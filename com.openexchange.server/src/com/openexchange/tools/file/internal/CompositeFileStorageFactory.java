
package com.openexchange.tools.file.internal;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.exception.OXException;
import com.openexchange.server.osgi.ServerActivator;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageFactory;
import com.openexchange.tools.file.external.FileStorageFactoryCandidate;

/**
 * {@link CompositeFileStorageFactory}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeFileStorageFactory implements FileStorageFactory, ServiceTrackerCustomizer<FileStorageFactoryCandidate, FileStorageFactoryCandidate> {

    private static final List<FileStorageFactoryCandidate> facs = new CopyOnWriteArrayList<FileStorageFactoryCandidate>();

    /**
     * Initializes a new {@link CompositeFileStorageFactory}.
     */
    public CompositeFileStorageFactory() {
        super();
    }

    @Override
    public FileStorage getFileStorage(URI uri) throws OXException {
        FileStorageFactoryCandidate candidate = null;
        for (final FileStorageFactoryCandidate fac : facs) {
            if (fac.supports(uri) && (null == candidate || fac.getRanking() > candidate.getRanking())) {
                candidate = fac;
            }
        }
        if (null != candidate && candidate.getRanking() >= DEFAULT_RANKING) {
            return candidate.getFileStorage(uri);
        }

        /*
         * Fall back to default implementation
         */

        final LocalFileStorage standardFS = new LocalFileStorage(uri);
        final HashingFileStorage hashedFS = new HashingFileStorage(new File(new File(uri), "hashed"));
        final CompositingFileStorage cStorage = new CompositingFileStorage();

        cStorage.addStore(standardFS);
        cStorage.addStore("hashed", hashedFS);
        cStorage.setSavePrefix("hashed");

        return cStorage;
    }

    @Override
    public boolean supports(final URI uri) throws OXException {
        return true;
    }

    @Override
    public int getRanking() {
        return Integer.MAX_VALUE;
    }

    @Override
    public FileStorageFactoryCandidate addingService(final ServiceReference<FileStorageFactoryCandidate> reference) {
        final BundleContext context = ServerActivator.getContext();
        final FileStorageFactoryCandidate candidate = context.getService(reference);
        if (!facs.contains(candidate)) {
            facs.add(candidate);
        }
        return null;
    }

    @Override
    public void modifiedService(final ServiceReference<FileStorageFactoryCandidate> reference, final FileStorageFactoryCandidate service) {
        // Ignore
    }

    @Override
    public void removedService(final ServiceReference<FileStorageFactoryCandidate> reference, final FileStorageFactoryCandidate service) {
        facs.remove(service);
        final BundleContext context = ServerActivator.getContext();
        if (null != context) {
            context.ungetService(reference);
        }
    }
}
