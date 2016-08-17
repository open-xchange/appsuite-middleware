
package com.openexchange.admin.soap.util.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.openexchange.admin.soap.util.dataobjects.CheckedDatabases;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "_return"
})
@XmlRootElement(name = "checkDatabaseResponse")
public class CheckDatabaseResponse {

    @XmlElement(name = "return", nillable = true)
    protected CheckedDatabases _return;

    public CheckedDatabases getReturn() {
        return this._return;
    }

    public void setReturn(CheckedDatabases value) {
        this._return = value;
    }

}
