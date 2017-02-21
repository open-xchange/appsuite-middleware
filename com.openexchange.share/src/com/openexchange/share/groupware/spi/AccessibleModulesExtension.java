package com.openexchange.share.groupware.spi;

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.share.groupware.ModuleSupport;

/**
 * {@link AccessibleModulesExtension} - Allows to extend the accessible modules as advertised from {@link ModuleSupport#getAccessibleModules(int, int)} method.
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since v7.8.4
 */
public interface AccessibleModulesExtension {

    /**
     * Gets the identifiers of those modules a specific guest user has access to, i.e. those where at least one share target for the
     * guest exists.
     *
     * @param accessibleModules The current set of accessible modules
     * @param contextID The context identifier
     * @param guestID The identifier of the guest user
     * @return The identifiers of the modules the guest user has access to, or an empty set if there are none
     * @throws OXException If extending accessible modules fails fatally
     */
    Set<Integer> extendAccessibleModules(Set<Integer> accessibleModules, int contextID, int guestID) throws OXException;

}
