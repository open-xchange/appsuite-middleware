
package com.openexchange.ajax.appointment.action;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UpdatesParser extends AbstractAJAXParser<UpdatesResponse> {

    private int[] columns;

    protected UpdatesParser(int[] columns) {
        super(false);
        this.columns = columns;
    }

    @Override
    protected UpdatesResponse createResponse(Response response) {
        return new UpdatesResponse(response, columns);
    }

}
