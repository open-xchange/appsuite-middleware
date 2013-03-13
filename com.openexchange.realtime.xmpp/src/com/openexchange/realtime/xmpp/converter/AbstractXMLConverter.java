
package com.openexchange.realtime.xmpp.converter;

import com.openexchange.realtime.payload.converter.AbstractGoodQualityConverter;

/**
 * {@link AbstractXMLConverter}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractXMLConverter extends AbstractGoodQualityConverter {

    @Override
    public String getInputFormat() {
        return "xml";
    }

}
