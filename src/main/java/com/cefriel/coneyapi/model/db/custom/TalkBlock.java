package com.cefriel.coneyapi.model.db.custom;
import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class TalkBlock implements Comparable<Object> {

    private int reteId;
    private int neo4jId;
    private Integer depth;
    private String type;
    private String subtype;
    private String ofConversation;
    private String text;
    private String url;
    private String image_url;

    private int orderInConversation;


    public TalkBlock(){}

    public int getReteId() {
        return reteId;
    }

    public void setReteId(int reteId) {
        this.reteId = reteId;
    }

    public int getNeo4jId() {
        return neo4jId;
    }

    public void getNeo4jId(int reteId) {
        this.neo4jId = reteId;
    }


    public Integer getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int compareTo(Object o) {
        TalkBlock qb = (TalkBlock) o;
        return depth.compareTo(qb.getDepth());
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getOfConversation() {
        return ofConversation;
    }

    public void setOfConversation(String ofConversation) {
        this.ofConversation = ofConversation;
    }

    public String getText() {
        return text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getOrderInConversation() {
        return orderInConversation;
    }

    public void setOrderInConversation(int orderInConversation) {
        this.orderInConversation = orderInConversation;
    }

    public void setNeo4jId(int neo4jId) {
        this.neo4jId = neo4jId;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }
}
