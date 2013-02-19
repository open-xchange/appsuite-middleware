
package com.openexchange.realtime.xmpp.converter;
import com.openexchange.realtime.payload.converter.AbstractGoodQualityConverter;


/**
 * {@link AbstractPOJOConverter}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractPOJOConverter extends AbstractGoodQualityConverter {

    @Override
    public String getOutputFormat() {
        return "xml";
    }

}
