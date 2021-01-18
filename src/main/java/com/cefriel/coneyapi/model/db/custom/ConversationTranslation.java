package com.cefriel.coneyapi.model.db.custom;

import com.google.gson.JsonObject;
import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class ConversationTranslation {

    private String language;
    private String title;
    private String privacyLink;
    private String introText;

    public ConversationTranslation(String language, String title, String privacyLink, String introText){
        this.language = language;
        this.title = title;
        this.privacyLink = privacyLink;
        this.introText = introText;
    }

    public JsonObject toJson(){
        JsonObject translationJson = new JsonObject();

        translationJson.addProperty("language", this.language);
        translationJson.addProperty("title", title == null ? "" : title);
        translationJson.addProperty("privacyLink", privacyLink == null ? "" : privacyLink);
        translationJson.addProperty("introText", introText == null ? "" : introText);

        return translationJson;
    }

    public String getLanguage() {
        return language;
    }
}
