
package com.copy.api;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "used",
    "quota",
    "saved"
})
public class Storage {

    @JsonProperty("used")
    private Integer used;
    @JsonProperty("quota")
    private Integer quota;
    @JsonProperty("saved")
    private Integer saved;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("used")
    public Integer getUsed() {
        return used;
    }

    @JsonProperty("used")
    public void setUsed(Integer used) {
        this.used = used;
    }

    @JsonProperty("quota")
    public Integer getQuota() {
        return quota;
    }

    @JsonProperty("quota")
    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    @JsonProperty("saved")
    public Integer getSaved() {
        return saved;
    }

    @JsonProperty("saved")
    public void setSaved(Integer saved) {
        this.saved = saved;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
