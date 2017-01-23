
package com.openexchange.ajax.framework;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class MultipleResponse<T extends AbstractAJAXResponse> extends AbstractAJAXResponse implements Iterable<T> {

    private final T[] responses;

    public MultipleResponse(final T[] responses) {
        super(null);
        this.responses = responses.clone();
    }

    public T getResponse(final int pos) {
        return responses[pos];
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(Arrays.asList(responses)).iterator();
    }
}
