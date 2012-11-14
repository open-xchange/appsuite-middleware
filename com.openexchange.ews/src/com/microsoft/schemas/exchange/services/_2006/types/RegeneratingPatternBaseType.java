
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegeneratingPatternBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegeneratingPatternBaseType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://schemas.microsoft.com/exchange/services/2006/types}IntervalRecurrencePatternBaseType">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegeneratingPatternBaseType")
@XmlSeeAlso({
    YearlyRegeneratingPatternType.class,
    DailyRegeneratingPatternType.class,
    MonthlyRegeneratingPatternType.class,
    WeeklyRegeneratingPatternType.class
})
public abstract class RegeneratingPatternBaseType
    extends IntervalRecurrencePatternBaseType
{


}
