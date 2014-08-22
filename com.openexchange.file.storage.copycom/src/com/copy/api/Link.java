
package com.copy.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "id",
    "public",
    "expires",
    "expired",
    "url",
    "url_short",
    "recipients",
    "creator_id",
    "confirmation_required"
})
public class Link {

    @JsonProperty("id")
    private String id;
    @JsonProperty("public")
    private Boolean _public;
    @JsonProperty("expires")
    private Boolean expires;
    @JsonProperty("expired")
    private Boolean expired;
    @JsonProperty("url")
    private String url;
    @JsonProperty("url_short")
    private String urlShort;
    @JsonProperty("recipients")
    private List<Object> recipients = new ArrayList<Object>();
    @JsonProperty("creator_id")
    private String creatorId;
    @JsonProperty("confirmation_required")
    private Boolean confirmationRequired;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("public")
    public Boolean getPublic() {
        return _public;
    }

    @JsonProperty("public")
    public void setPublic(Boolean _public) {
        this._public = _public;
    }

    @JsonProperty("expires")
    public Boolean getExpires() {
        return expires;
    }

    @JsonProperty("expires")
    public void setExpires(Boolean expires) {
        this.expires = expires;
    }

    @JsonProperty("expired")
    public Boolean getExpired() {
        return expired;
    }

    @JsonProperty("expired")
    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("url_short")
    public String getUrlShort() {
        return urlShort;
    }

    @JsonProperty("url_short")
    public void setUrlShort(String urlShort) {
        this.urlShort = urlShort;
    }

    @JsonProperty("recipients")
    public List<Object> getRecipients() {
        return recipients;
    }

    @JsonProperty("recipients")
    public void setRecipients(List<Object> recipients) {
        this.recipients = recipients;
    }

    @JsonProperty("creator_id")
    public String getCreatorId() {
        return creatorId;
    }

    @JsonProperty("creator_id")
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @JsonProperty("confirmation_required")
    public Boolean getConfirmationRequired() {
        return confirmationRequired;
    }

    @JsonProperty("confirmation_required")
    public void setConfirmationRequired(Boolean confirmationRequired) {
        this.confirmationRequired = confirmationRequired;
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
