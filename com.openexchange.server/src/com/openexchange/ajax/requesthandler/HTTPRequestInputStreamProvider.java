package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

public class HTTPRequestInputStreamProvider  implements AJAXRequestData.InputStreamProvider {

    private final HttpServletRequest req;

    protected HTTPRequestInputStreamProvider(final HttpServletRequest req) {
        this.req = req;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return req.getInputStream();
    }
}

