
package com.openexchange.ajax.kata;

import com.openexchange.groupware.container.FolderObject;

/**
 * {@link ContactRunner}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class FolderRunner extends AbstractDirectoryRunner {

    public FolderRunner(String name) {
        super(name, "folderKatas", FolderObject.class);
    }
}
