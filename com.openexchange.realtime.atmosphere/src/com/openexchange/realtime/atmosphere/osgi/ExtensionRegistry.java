
package com.openexchange.realtime.atmosphere.osgi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ServiceRegistry;
import com.openexchange.realtime.atmosphere.AtmosphereExceptionCode;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.atmosphere.stanza.StanzaInitializer;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.transformer.PayloadElementTransformer;
import com.openexchange.realtime.util.ElementPath;

public class ExtensionRegistry extends ServiceRegistry {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ExtensionRegistry.class);

    private static final ExtensionRegistry INSTANCE = new ExtensionRegistry();

    private final Map<Class<? extends Stanza>, StanzaHandler> handlers;

    private final ConcurrentHashMap<Class<? extends Stanza>, StanzaBuilder<? extends Stanza>> builders;

    private final Map<Class<?>, PayloadElementTransformer> classToTransformer;

    private final Map<ElementPath, PayloadElementTransformer> elementPathToTransformer;

    /**
     * Encapsulated constructor.
     */
    private ExtensionRegistry() {
        super();
        handlers = new ConcurrentHashMap<Class<? extends Stanza>, StanzaHandler>();
        builders = new ConcurrentHashMap<Class<? extends Stanza>, StanzaBuilder<? extends Stanza>>();
        classToTransformer = new ConcurrentHashMap<Class<?>, PayloadElementTransformer>();
        elementPathToTransformer = new ConcurrentHashMap<ElementPath, PayloadElementTransformer>();
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
        return handlers.get(stanza.getClass());
    }

    /**
     * Adds specified handler to this library.
     * 
     * @param transformer The handler to add
     */
    public void addStanzaHandler(StanzaHandler handler) {
        handlers.put(handler.getStanzaClass(), handler);
    }

    /**
     * Removes specified handler from this library.
     * 
     * @param transformer The handler to remove
     */
    public void removeStanzaHandler(StanzaHandler handler) {
        handlers.remove(handler.getStanzaClass());
    }

    /**
     * Gets the appropriate transformer for the specified Stanz class.
     * 
     * @param stanzaClass The Stanza subclass we want to transform.
     * @return The appropriate transformer or <code>null</code> if none is applicable.
     */
    public PayloadElementTransformer getTransformerFor(ElementPath elementPath) {
        return elementPathToTransformer.get(elementPath);
    }

    /**
     * Adds specified transformer to this library.
     * 
     * @param transformer The transformer to add
     */
    public void addPayloadElementTransFormer(PayloadElementTransformer transformer) {
        classToTransformer.put(transformer.getElementClass(), transformer);
    }

    /**
     * Removes specified transformer from this library.
     * 
     * @param transformer The transformer to remove
     */
    public void removePayloadElementTransformer(PayloadElementTransformer transformer) {
        classToTransformer.remove(transformer.getElementClass());
    }

    /**
     * Add an mapping ElementPath <-> Class<?> to the registry
     * 
     * @param elementPath The ElementPath of payloadElement
     * @param mappingClass The mapping Class used for the PayloadElement during transformation
     * @throws OXException 
     */
    public void addElementPathMapping(ElementPath elementPath, Class<?> mappingClass) throws OXException {
        PayloadElementTransformer payloadElementTransformer = classToTransformer.get(mappingClass);
        if (payloadElementTransformer == null) {
            throw AtmosphereExceptionCode.MISSING_TRANSFORMER_FOR_PAYLOADELEMENT.create(elementPath);
        }
        elementPathToTransformer.put(elementPath, payloadElementTransformer);
    }

    /**
     * Remove a mapping ElementPath <-> Class
     * 
     * @param elementPath The ElementPath that identifies the mapping to remove
     */
    public void removeElementpathMapping(ElementPath elementPath) {
        elementPathToTransformer.remove(elementPath);
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
        classToTransformer.clear();
        builders.clear();
        handlers.clear();
    }

}
