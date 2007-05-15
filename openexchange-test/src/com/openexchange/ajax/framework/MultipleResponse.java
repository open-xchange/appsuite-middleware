package com.openexchange.ajax.framework;

public class MultipleResponse extends AJAXResponse {

    private final AJAXResponse[] responses;

    public MultipleResponse(final AJAXResponse[] responses) {
        super(null);
        this.responses = responses.clone();
    }

    public AJAXResponse getResponse(final int pos) {
        return responses[pos];
    }
}
