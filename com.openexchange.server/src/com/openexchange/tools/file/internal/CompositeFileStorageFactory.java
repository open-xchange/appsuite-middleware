package com.openexchange.tools.file.internal;

import java.io.File;
import java.net.URI;
import com.openexchange.exception.OXException;
import com.openexchange.tools.file.external.FileStorage;
import com.openexchange.tools.file.external.FileStorageFactory;

/**
 * {@link CompositeFileStorageFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CompositeFileStorageFactory implements FileStorageFactory {

    @Override
    public FileStorage getFileStorage(URI uri) throws OXException {
        LocalFileStorage standardFS = new LocalFileStorage(uri);
        HashingFileStorage hashedFS = new HashingFileStorage(new File(new File(uri), "hashed"));
        CompositingFileStorage cStorage = new CompositingFileStorage();

        cStorage.addStore(standardFS);
        cStorage.addStore("hashed", hashedFS);
        cStorage.setSavePrefix("hashed");

        return cStorage;
    }
}
