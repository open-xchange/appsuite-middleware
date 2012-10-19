package com.openexchange.http.grizzly.filter;

import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;

/**
 * {@link FilterChainUtils} Helpers for FilterChain handling.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class FilterChainUtils {
	
    /**
     * Prettyprint a chain of filters.
     * @param filterChain The  FilterChain to print 
     * @return The chain of filters as formatted String
     */
	public static synchronized String formatFilterChainString(FilterChain filterChain) {
		StringBuilder sb = new StringBuilder();
    	sb.append("\tFilterChain contains ").append(filterChain.size()).append(" elements\n");
    	for (Filter filter : filterChain) {
    		sb.append("\t").append(filter.getClass().getName()).append("\n");
		}
    	return sb.toString();
	}
	
}
