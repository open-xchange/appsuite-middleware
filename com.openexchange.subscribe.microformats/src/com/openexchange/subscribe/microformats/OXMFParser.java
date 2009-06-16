
package com.openexchange.subscribe.microformats;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import com.openexchange.subscribe.SubscriptionException;

/**
 * {@link OXMFParser} - Parses a given HTML content into container elements.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OXMFParser {

    /**
     * Resets this parser for re-usage.
     */
    public void reset();

    /**
     * Adds a container element name; e.g. <code>"ox_contact"</code>.
     * 
     * @param containerElement The container element name
     */
    public void addContainerElement(final String containerElement);

    /**
     * Adds an attribute prefix; e.g. <code>"ox_"</code>.
     * 
     * @param prefix The attribute prefix
     */
    public void addAttributePrefix(final String prefix);

    /**
     * Parses passed HTML content. Each container element's name-value-pairs are backed by a map. Each map is contained in list.
     * 
     * @param html The HTML content to parse
     * @return A list of maps each map backing container element's name-value-pairs
     * @throws SubscriptionException If parsing the HTML content fails
     */
    public List<Map<String, String>> parse(final String html) throws SubscriptionException;

    public List<Map<String, String>> parse(final Reader html) throws SubscriptionException;
}
