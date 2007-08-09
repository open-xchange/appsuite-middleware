package com.openexchange.ajax.framework;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class MultipleResponse extends AbstractAJAXResponse implements
    Iterable<AbstractAJAXResponse> {

    private final AbstractAJAXResponse[] responses;

    public MultipleResponse(final AbstractAJAXResponse[] responses) {
        super(null);
        this.responses = responses.clone();
    }

    public AbstractAJAXResponse getResponse(final int pos) {
        return responses[pos];
    }

    public Iterator<AbstractAJAXResponse> iterator() {
        return Collections.unmodifiableList(Arrays.asList(responses)).iterator();
    }
}
