package com.luxoft.consumer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "track")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String user;
    private String method;
    private String resource;
    private String timestamp;

    private Track() {
    }

    public Track(String user, String method, String resource) {
        setUser(user);
        setMethod(method);
        setResource(resource);
    }

    public Track(String user, String method, String resource, String timestamp) {
        setUser(user);
        setMethod(method);
        setResource(resource);
        setTimestamp(timestamp);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}