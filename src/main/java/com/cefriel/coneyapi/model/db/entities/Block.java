package com.cefriel.coneyapi.model.db.entities;

import com.google.gson.JsonObject;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Block {

	@Id @GeneratedValue
	Long id;

	private int block_id;
	private String block_type;
	private String block_subtype;
	private String of_conversation;

	//data
	private String text;
	private String visualization;
	private String url;
	private String image_url;
	private int value;
	private int max_answers;
	private int order;
	private int points;

	private int optional;

	private String checkbox_type;


	public Block(){}

	//QUESTION
	public Block(int block_id, String block_type, String block_subtype, String text, String visualization,
				 String of_conversation){
		this.block_id = block_id;
		this.block_type = block_type;
		this.block_subtype = block_subtype;
		this.text = text;
		this.visualization = visualization;
		this.of_conversation = of_conversation;
	}

	//ANSWER
	public Block(int block_id, String block_type, String block_subtype, String text, int value,
				 int order, String of_conversation, int points, int optional) {
		this.block_id = block_id;
		this.block_type = block_type;
		this.block_subtype = block_subtype;
		this.text = text;
		this.value = value;
		this.order = order;
		this.of_conversation = of_conversation;
		this.points = points;
		this.optional = optional;
	}

	//ANSWER CHECKBOX
	public Block(int block_id, String block_type, String block_subtype, String text, int max_answers,
				 int order, String of_conversation, int points, int optional, String checkbox_type) {
		this.block_id = block_id;
		this.block_type = block_type;
		this.block_subtype = block_subtype;
		this.text = text;
		this.order = order;
		this.of_conversation = of_conversation;
		this.max_answers = max_answers;
		this.points = points;
		this.optional = optional;
		this.checkbox_type = checkbox_type;
	}

	//TALK
	public Block(int block_id, String block_type, String block_subtype, String text,
				 String url, String image_url, String of_conversation) {
		this.block_id = block_id;
		this.block_type = block_type;
		this.block_subtype = block_subtype;
		this.text = text;
		this.url = url;
		this.image_url = image_url;
		this.of_conversation = of_conversation;
	}

	public long getNeo4jId(){
		return id;
	}

	public String getBlockSubtype() {
		return block_subtype;
	}

	public void setBlockSubtype(String block_subtype) {
		this.block_subtype = block_subtype;
	}

	public String getUrl() {
		return url == null ? "" : url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImageUrl() {
		return image_url == null ? "" : image_url;
	}

	public void setImageUrl(String image_url) {
		this.image_url = image_url;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public int getBlockId() {
		return block_id;
	}

	public void setBlockId(int block_id) {
		this.block_id = block_id;
	}

	public String getBlockType() {
		return block_type;
	}

	public void setBlockType(String block_type) {
		this.block_type = block_type;
	}

	public String getOfConversation() {
		return of_conversation;
	}

	public void setOfConversation(String of_conversation) {
		this.of_conversation = of_conversation;
	}

	public String getText() {
		if (text == null) {
			return "";
		}

		String outText = text;
		if (text.length() > 4 && text.substring(0, 4).equals("----")) {
			outText = text.substring(4);
		}
		outText = outText.replaceAll("^\\s+|\\s+$", "");
		return outText;

	}

	public void setText(String text) {
		this.text = text;
	}

	public String getVisualization() {
		return visualization;
	}

	public void setVisualization(String qtype) {
		this.visualization = qtype;
	}

	public int getOptional() {
		return optional;
	}

	public void setOptional(int optional) {
		this.optional = optional;
	}

	public String getCheckbox_type() {
		return checkbox_type == null ? "" : checkbox_type;
	}

	public int getMax_answers() {
		return max_answers;
	}

	public void setMax_answers(int max_answers) {
		this.max_answers = max_answers;
	}

	public JsonObject toJson() {
		JsonObject blockJson = new JsonObject();

		blockJson.addProperty("blockId", this.block_id);
		blockJson.addProperty("type", this.block_type);
		blockJson.addProperty("subtype", this.block_subtype);
		blockJson.addProperty("ofConversation", this.of_conversation);
		blockJson.addProperty("text", this.text);
		blockJson.addProperty("visualization", this.visualization);
		blockJson.addProperty("url", this.url);
		blockJson.addProperty("imageUrl", this.image_url);
		blockJson.addProperty("value", this.value);
		blockJson.addProperty("maxAnswers", this.max_answers);
		blockJson.addProperty("order", this.order);
		blockJson.addProperty("points", this.points);
		blockJson.addProperty("optional", this.optional);

		if(this.checkbox_type!= null && !this.checkbox_type.equals("")){
			blockJson.addProperty("checkboxType", this.checkbox_type);
		}

		return blockJson;
	}
}
