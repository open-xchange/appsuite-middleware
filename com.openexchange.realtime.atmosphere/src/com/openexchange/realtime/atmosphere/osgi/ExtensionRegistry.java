
package com.openexchange.realtime.atmosphere.osgi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.realtime.atmosphere.payload.transformer.AtmospherePayloadElementTransformer;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;

public class ExtensionRegistry extends ServiceRegistry {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ExtensionRegistry.class);

    private static final ExtensionRegistry INSTANCE = new ExtensionRegistry();

    private final Map<Class<? extends Stanza>, StanzaHandler> stanzaClassToHandler;

    private final ConcurrentHashMap<Class<? extends Stanza>, StanzaBuilder<? extends Stanza>> builders;

    private final Map<ElementPath, AtmospherePayloadElementTransformer> elementPathToTransformer;

    /**
     * Encapsulated constructor.
     */
    private ExtensionRegistry() {
        super();
        stanzaClassToHandler = new ConcurrentHashMap<Class<? extends Stanza>, StanzaHandler>();
        builders = new ConcurrentHashMap<Class<? extends Stanza>, StanzaBuilder<? extends Stanza>>();
        elementPathToTransformer = new ConcurrentHashMap<ElementPath, AtmospherePayloadElementTransformer>();
    }

    /**
     * Get the Registry singleton.
     *
     * @return the Registry singleton
     */
    public static ExtensionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Gets the appropriate handler for the specified Stanz class.
     *
     * @param stanzaClass The Stanza subclass we want to handle.
     * @return The appropriate handler or <code>null</code> if none is applicable.
     */
    public StanzaHandler getHandlerFor(Stanza stanza) {
        return stanzaClassToHandler.get(stanza.getClass());
    }

    /**
     * Adds specified handler to this library.
     *
     * @param transformer The handler to add
     */
    public void addStanzaHandler(StanzaHandler handler) {
        stanzaClassToHandler.put(handler.getStanzaClass(), handler);
    }

    /**
     * Removes specified handler from this library.
     *
     * @param transformer The handler to remove
     */
    public void removeStanzaHandler(StanzaHandler handler) {
        stanzaClassToHandler.remove(handler.getStanzaClass());
    }

    /**
     * Gets the appropriate transformer for the specified Stanz class.
     *
     * @param stanzaClass The Stanza subclass we want to transform.
     * @return The appropriate transformer or <code>null</code> if none is applicable.
     */
    public AtmospherePayloadElementTransformer getTransformerFor(ElementPath elementPath) {
        return elementPathToTransformer.get(elementPath);
    }

    /**
     * Adds specified transformer to this library.
     *
     * @param transformer The transformer to add
     */
    public void addPayloadElementTransFormer(AtmospherePayloadElementTransformer transformer) {
        elementPathToTransformer.put(transformer.getElementPath(), transformer);
    }

    /**
     * Removes specified transformer from this library.
     *
     * @param transformer The transformer to remove
     */
    public void removePayloadElementTransformer(AtmospherePayloadElementTransformer transformer) {
        elementPathToTransformer.remove(transformer.getElementPath());
    }

    /**
     * Get the collected ElementPaths the registered PayloadElementTransformers are able to transform.
     *
     * @return the collected ElementPaths the registered PayloadElementTransformers are able to transform.
     */
    public Set<ElementPath> getTransformableableElementPaths() {
        return new HashSet<ElementPath>(elementPathToTransformer.keySet());
    }

    @Override
    public void clearRegistry() {
        elementPathToTransformer.clear();
        builders.clear();
        stanzaClassToHandler.clear();
    }

}
