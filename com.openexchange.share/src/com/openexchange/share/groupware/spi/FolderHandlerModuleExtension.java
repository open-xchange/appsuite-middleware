package com.openexchange.share.groupware.spi;

import com.openexchange.exception.OXException;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.groupware.TargetProxy;

/**
 * {@link FolderHandlerModuleExtension} - A folder handler that receives appropriate call-backs if associated methods are called for singleton {@link ModuleSupport} instance.
 * <p>
 * The call-backs allow a {@code FolderHandlerModuleExtension} instance to extends/modify the folder handling.
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.8.4
 */
public interface FolderHandlerModuleExtension {

    /**
     * Checks if this instance should be used for specified folder
     *
     * @param folder The folder name
     * @return <code>true</code> if applicable for this folder; otherwise <code>false</code>
     */
    boolean isApplicableFor(String folder);

    /**
     * The call-back for {@link ModuleSupport#isVisible(int, String, String, int, int)}
     * <p>
     * Gets a value indicating whether a share target is visible for the session's user or not, i.e. if the user has sufficient
     * permissions to read the folder or item represented by the share target.
     *
     * @param folder The folder ID; must be globally valid - not personalized in terms of the passed guest user ID
     * @param item The item ID or <code>null</code>; must be globally valid - not personalized in terms of the passed guest user ID
     * @param contextID The context ID
     * @param guestID The guest users ID
     * @return <code>true</code> if the share target is visible; otherwise <code>false</code>
     * @throws OXException If visibility check fails fatally
     */
    boolean isVisible(String folder, String item, int contextID, int guestID) throws OXException;

    /**
     * The call-back for {@link ModuleSupport#exists(int, String, String, int, int)}
     * <p>
     * Gets a value indicating whether a folder/item exists.
     *
     * @param folder The folder ID; must be globally valid - not personalized in terms of the passed guest user ID
     * @param item The item ID or <code>null</code>; must be globally valid - not personalized in terms of the passed guest user ID
     * @param contextID The context ID
     * @param guestID The guest users ID
     * @return <code>true</code> if the share target exists, <code>false</code>, otherwise
     * @throws OXException If existence test fails
     */
    boolean exists(String folder, String item, int contextID, int guestID) throws OXException;

    /**
     * The call-back for {@link ModuleSupport#resolveTarget(ShareTargetPath, int, int)}
     * <p>
     * Resolves the underlying groupware item for the given share target and returns an according {@link TargetProxy} instance. The item
     * is loaded using administrative access to the underlying module services. This method must only be used for administrative tasks
     * when no session object is available.
     *
     * @param targetPath The share target path
     * @param contextId The context identifier
     * @param guestId The identifier of the guest user to resolve the target for
     * @return The target proxy or <code>null</code> to continue
     * @throws OXException If resolving specified target fails
     */
    TargetProxy resolveTarget(ShareTargetPath targetPath, int contextId, int guestId) throws OXException;

    /**
     * <p>
     * Resolves the underlying groupware item for the given share target and returns an according {@link TargetProxy} instance. The item
     * is loaded using administrative access to the underlying module services. This method must only be used for administrative tasks
     * when no session object is available.
     *
     * @param folderTarget The share target
     * @throws OXException If resolving specified target fails
     */
    TargetProxy resolveTarget(ShareTarget folderTarget) throws OXException;
}
