
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
    "path",
    "name",
    "token",
    "permissions",
    "syncing",
    "public",
    "type",
    "size",
    "date_last_synced",
    "stub",
    "recipient_confirmed",
    "url",
    "revision_id",
    "thumb",
    "share",
    "counts",
    "links"
})
public class Revision {

    @JsonProperty("id")
    private String id;
    @JsonProperty("path")
    private String path;
    @JsonProperty("name")
    private String name;
    @JsonProperty("token")
    private Object token;
    @JsonProperty("permissions")
    private Object permissions;
    @JsonProperty("syncing")
    private Boolean syncing;
    @JsonProperty("public")
    private Boolean _public;
    @JsonProperty("type")
    private String type;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("date_last_synced")
    private Integer dateLastSynced;
    @JsonProperty("stub")
    private Boolean stub;
    @JsonProperty("recipient_confirmed")
    private Boolean recipientConfirmed;
    @JsonProperty("url")
    private String url;
    @JsonProperty("revision_id")
    private Integer revisionId;
    @JsonProperty("thumb")
    private Object thumb;
    @JsonProperty("share")
    private Object share;
    @JsonProperty("counts")
    private List<Object> counts = new ArrayList<Object>();
    @JsonProperty("links")
    private List<Object> links = new ArrayList<Object>();
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

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("token")
    public Object getToken() {
        return token;
    }

    @JsonProperty("token")
    public void setToken(Object token) {
        this.token = token;
    }

    @JsonProperty("permissions")
    public Object getPermissions() {
        return permissions;
    }

    @JsonProperty("permissions")
    public void setPermissions(Object permissions) {
        this.permissions = permissions;
    }

    @JsonProperty("syncing")
    public Boolean getSyncing() {
        return syncing;
    }

    @JsonProperty("syncing")
    public void setSyncing(Boolean syncing) {
        this.syncing = syncing;
    }

    @JsonProperty("public")
    public Boolean getPublic() {
        return _public;
    }

    @JsonProperty("public")
    public void setPublic(Boolean _public) {
        this._public = _public;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    @JsonProperty("date_last_synced")
    public Integer getDateLastSynced() {
        return dateLastSynced;
    }

    @JsonProperty("date_last_synced")
    public void setDateLastSynced(Integer dateLastSynced) {
        this.dateLastSynced = dateLastSynced;
    }

    @JsonProperty("stub")
    public Boolean getStub() {
        return stub;
    }

    @JsonProperty("stub")
    public void setStub(Boolean stub) {
        this.stub = stub;
    }

    @JsonProperty("recipient_confirmed")
    public Boolean getRecipientConfirmed() {
        return recipientConfirmed;
    }

    @JsonProperty("recipient_confirmed")
    public void setRecipientConfirmed(Boolean recipientConfirmed) {
        this.recipientConfirmed = recipientConfirmed;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("revision_id")
    public Integer getRevisionId() {
        return revisionId;
    }

    @JsonProperty("revision_id")
    public void setRevisionId(Integer revisionId) {
        this.revisionId = revisionId;
    }

    @JsonProperty("thumb")
    public Object getThumb() {
        return thumb;
    }

    @JsonProperty("thumb")
    public void setThumb(Object thumb) {
        this.thumb = thumb;
    }

    @JsonProperty("share")
    public Object getShare() {
        return share;
    }

    @JsonProperty("share")
    public void setShare(Object share) {
        this.share = share;
    }

    @JsonProperty("counts")
    public List<Object> getCounts() {
        return counts;
    }

    @JsonProperty("counts")
    public void setCounts(List<Object> counts) {
        this.counts = counts;
    }

    @JsonProperty("links")
    public List<Object> getLinks() {
        return links;
    }

    @JsonProperty("links")
    public void setLinks(List<Object> links) {
        this.links = links;
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
