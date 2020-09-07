package com.cefriel.coneyapi.model.db.custom;

import com.google.gson.JsonObject;
import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class ConversationTranslation {

    private String language;
    private String title;

    public ConversationTranslation(String language, String title){
        this.language = language;
        this.title = title;
    }

    public JsonObject toJson(){
        JsonObject translationJson = new JsonObject();

        translationJson.addProperty("language", this.language);
        translationJson.addProperty("title", this.title);

        return translationJson;
    }
}
