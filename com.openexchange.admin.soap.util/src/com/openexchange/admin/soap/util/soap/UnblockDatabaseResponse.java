
package com.openexchange.admin.soap.util.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.util.dataobjects.Database;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "_return"
})
@XmlRootElement(name = "unblockDatabaseResponse")
public class UnblockDatabaseResponse {

    @XmlElement(name = "return", nillable = true)
    protected List<Database> _return;

    public List<Database> getReturn() {
        if (_return == null) {
            _return = new ArrayList<Database>();
        }
        return this._return;
    }

}
