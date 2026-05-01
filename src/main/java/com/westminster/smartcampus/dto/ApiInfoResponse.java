package com.westminster.smartcampus.dto;

import java.util.Map;

public class ApiInfoResponse {

    private String name;        
    private String version;     
    private String description; 
    private String contact;     
    private long timestamp;     
    private Map<String, String> links; 

    public ApiInfoResponse() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, String> getLinks() { return links; }
    public void setLinks(Map<String, String> links) { this.links = links; }
}
