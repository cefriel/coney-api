package com.cefriel.coneyapi.model.db.custom;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class UserSession {

    String session;
    String userId;
    String startTimestamp;
    String endTimestamp;
    String meta1;
    String meta2;
    String language;

    public UserSession(){}

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserId(boolean anonymize){
        if(anonymize){
            return userId == null ? "" : "u"+(userId.hashCode() & 0xfffffff);
        }
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getEndTimestamp() {
        return endTimestamp == null ? "unfinished" : endTimestamp;
    }

    public void setEndTimestamp(String endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getMeta1() {
        return meta1 == null ? "" : meta1;
    }

    public void setMeta1(String meta1) {
        this.meta1 = meta1;
    }

    public String getMeta2() {
        return meta2 == null ? "" : meta2;
    }

    public void setMeta2(String meta2) {
        this.meta2 = meta2;
    }

    public String getLanguage() {
        return language == null ? "" : language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
