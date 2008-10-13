package com.openexchange.exceptions;

import com.openexchange.groupware.Component;

import java.util.List;

/**
 * The component registry makes sure that the component shorthand for messages is uniquely assigned to individual
 * applications (bundles). It keeps track of all error messages registered in the server.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ComponentRegistry {

    /**
     * Called during bundle startup in the Activator of a given bundle to claim a component String. Upon success the given exceptions class
     * is registered in the server and initialized with the given component and applicationId. If the component has already
     * been taken a ComponentAlreadyRegisteredException is thrown.
     * @param component  The component String the client wants to claim.
     * @param applicationId The application identifier of the client. Usually the bundle identifier, in all cases this should be in java package (reverse dns) notation.
     * @param exceptions The Exceptions subclass the bundle will use to create its exceptions.
     * @throws ComponentAlreadyRegisteredException Thrown when the component has already been claimed by another application.
     */
    public void registerComponent(Component component, String applicationId, Exceptions exceptions) throws ComponentAlreadyRegisteredException;

    /**
     * Called during bundle shutdown to remove the registration of a component.
     * @param component The component to deregister
     */
    public void deregisterComponent(Component component);

    /**
     * Looks up the exceptions subclass responsible for the error messages of a given component.
     * @param component The component.
     * @return The Exceptions subclass registered for the given component. Returns null, if the component was not registered.
     */
    public Exceptions getExceptionsForComponent(Component component);

    /**
     * Looks up the exceptions subclasses responsible for the error messages of a given application.
     * @param applicationId The application identifier. Usually the bundle identifier, in all cases this should be in java package (reverse dns) notation.
     * @return The Exceptions subclass registered for the given application. Returns null, if the application was not registered.
     */
    public List<Exceptions> getExceptionsForApplication(String applicationId);

    /**
     * Returns a list of all registered components
     * @return A list of all components registered with this component registry.
     */
    public List<Component> getComponents();

    /**
     * Returns a list of all application ids.
     * @return A list of all application ids registered with this component registry.
     */
    public List<String> getApplicationIds();

    /**
     * Returns a list of all Exceptions subclasses registered with this component registry.
     * @return A list of all Exceptions subclasses registered with this component registry.
     */
    public List<Exceptions> getExceptions();

}
