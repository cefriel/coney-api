package com.cefriel.coneyapi.model.db.entities;

import com.google.gson.JsonObject;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Id;

@NodeEntity
public class Conversation implements Comparable<Object> {


	@Id @GeneratedValue
    Long id;

    private String conv_id;
    private String conv_title;
    private String json_url;
    private String status;
    private int access_level;
    private String lang;
    private String chat_image;
    private String chat_privacy_notice;
    private String chat_intro_text;
    private String chat_primary_color;
    private String chat_secondary_color;
    private String chat_text_color;

    public Conversation() {
		super();
	}

	public String getJsonUrl() {
        return json_url;
    }

    public void setJsonUrl(String json_url) {
        this.json_url = json_url;
    }

    public String getConversationId() {
        return conv_id;
    }

    public void setConversationId(String conv_id) {
        this.conv_id = conv_id;
    }

    public String getTitle() {
        return conv_title;
    }

    public void setTitle(String conv_title) {
        this.conv_title = conv_title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAccess_level() {
        return access_level;
    }

    public void setAccess_level(int access_level) {
        this.access_level = access_level;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getChat_image() {
        return chat_image;
    }

    public void setChat_image(String chat_image) {
        this.chat_image = chat_image;
    }

    public String getChat_privacy_notice() {
        return chat_privacy_notice;
    }

    public void setChat_privacy_notice(String chat_privacy_notice) {
        this.chat_privacy_notice = chat_privacy_notice;
    }

    public String getChat_intro_text() {
        return chat_intro_text;
    }

    public void setChat_intro_text(String chat_intro_text) {
        this.chat_intro_text = chat_intro_text;
    }

    public String getChat_primary_color() {
        return chat_primary_color;
    }

    public void setChat_primary_color(String chat_primary_color) {
        this.chat_primary_color = chat_primary_color;
    }

    public String getChat_secondary_color() {
        return chat_secondary_color;
    }

    public void setChat_secondary_color(String chat_secondary_color) {
        this.chat_secondary_color = chat_secondary_color;
    }

    public String getChat_text_color() {
        return chat_text_color;
    }

    public void setChat_text_color(String chat_text_color) {
        this.chat_text_color = chat_text_color;
    }

    public int compareTo(Object o)
    {
        Conversation other = (Conversation) o;
        return conv_title.compareTo(other.conv_title);
    }

    public JsonObject toJson(){
        JsonObject conversationJson = new JsonObject();
        conversationJson.addProperty("conversationId", this.conv_id);
        conversationJson.addProperty("title", this.conv_title);
        conversationJson.addProperty("status", this.status);
        conversationJson.addProperty("accessLevel", this.access_level);
        return conversationJson;
    }

    public JsonObject toChatJson(){
        JsonObject conversationJson = new JsonObject();
        conversationJson.addProperty("conversationId", this.conv_id);
        conversationJson.addProperty("chat_privacy_notice", this.chat_privacy_notice);
        conversationJson.addProperty("chat_image", this.chat_image);
        conversationJson.addProperty("chat_intro_text", this.chat_intro_text);
        conversationJson.addProperty("chat_primary_color", this.chat_primary_color);
        conversationJson.addProperty("chat_secondary_color", this.chat_secondary_color);
        conversationJson.addProperty("chat_text_color", this.chat_text_color);
        return conversationJson;
    }
    
    
}
