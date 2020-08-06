
package com.openexchange.tools.functions;

import com.openexchange.exception.OXException;

/**
 * 
 * {@link ErrorAwareFunction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 * @param <T> The paramter type
 * @param <R> The return type
 */
@FunctionalInterface
public interface ErrorAwareFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws OXException In case the function failed
     */
    R apply(T t) throws OXException;

}
