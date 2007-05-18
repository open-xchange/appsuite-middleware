package com.openexchange.ajax.framework;

public class MultipleResponse extends AbstractAJAXResponse {

    private final AbstractAJAXResponse[] responses;

    public MultipleResponse(final AbstractAJAXResponse[] responses) {
        super(null);
        this.responses = responses.clone();
    }

    public AbstractAJAXResponse getResponse(final int pos) {
        return responses[pos];
    }
}
