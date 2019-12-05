package com.openexchange.tools.functions;

import com.openexchange.exception.OXException;

public interface ErrorAwareFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws OXException;

}
