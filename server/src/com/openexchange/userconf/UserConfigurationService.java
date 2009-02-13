
package com.openexchange.userconf;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;

/**
 * {@link UserConfigurationService} - The user configuration service.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface UserConfigurationService {

    /**
     * A convenience method that invokes {@link #getUserConfigurationSafe(int, int[], Context)} with the group parameter set to
     * <code>null</code>
     * 
     * @param userId The user ID
     * @param ctx The context
     * @return The corresponding instance of {@link UserConfiguration} or <code>null</code> on exception
     * @see #getUserConfigurationSafe(int, int[], Context)
     */
    public UserConfiguration getUserConfigurationSafe(final int userId, final Context ctx);

    /**
     * A convenience method that invokes {@link #getUserConfiguration(int, int[], Context)}. If an exception occurs <code>null</code> is
     * returned
     * 
     * @param userId The user ID
     * @param groups The user's groups
     * @param ctx The contexts
     * @return The corresponding instance of {@link UserConfiguration} or <code>null</code> on exception
     */
    public UserConfiguration getUserConfigurationSafe(final int userId, final int[] groups, final Context ctx);

    /**
     * Determines the instance of <code>UserConfiguration</code> that corresponds to given user ID.
     * 
     * @param userId - the user ID
     * @param ctx - the context
     * @return the instance of <code>UserConfiguration</code>
     * @throws UserConfigurationException If user's configuration could not be determined
     * @see #getUserConfiguration(int, int[], Context)
     */
    public UserConfiguration getUserConfiguration(final int userId, final Context ctx) throws UserConfigurationException;

    /**
     * Determines the instance of <code>UserConfiguration</code> that corresponds to given user ID. If <code>groups</code> argument is set,
     * user's groups need not to be loaded from user storage
     * 
     * @param userId - the user ID
     * @param groups - user's groups
     * @param ctx - the context
     * @return the instance of <code>UserConfiguration</code>
     * @throws UserConfigurationException If user's configuration could not be determined
     */
    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws UserConfigurationException;

    /**
     * <p>
     * Clears the whole storage. All kept instances of <code>UserConfiguration</code> are going to be removed from storage.
     * <p>
     * <b>NOTE:</b> Only the instances are going to be removed from storage; underlying database is not affected
     * 
     * @throws UserConfigurationException If clearing fails
     */
    public void clearStorage() throws UserConfigurationException;

    /**
     * <p>
     * Removes the instance of <code>UserConfiguration</code> that corresponds to given user ID from storage.
     * <p>
     * <b>NOTE:</b> Only the instance is going to be removed from storage; underlying database is not affected
     * 
     * @param userId - the user ID
     * @param ctx - the context
     * @throws UserConfigurationException If removal fails
     */
    public void removeUserConfiguration(int userId, Context ctx) throws UserConfigurationException;

    /**
     * Saves specified user configuration.
     * 
     * @param userConfiguration The user configuration to save.
     * @throws UserConfigurationException If saving user configuration fails.
     */
    public void saveUserConfiguration(final UserConfiguration userConfiguration) throws UserConfigurationException;

    /**
     * Saves specified user configuration.
     * 
     * @param permissionBits The permission bits.
     * @param userId The user ID.
     * @param ctx The context the user belongs to.
     * @throws UserConfigurationException If saving user configuration fails.
     */
    public void saveUserConfiguration(int permissionBits, int userId, Context ctx) throws UserConfigurationException;

}
