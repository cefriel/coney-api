package com.cefriel.coneyapi.controller;

import com.cefriel.coneyapi.exception.MethodNotAllowedException;
import com.cefriel.coneyapi.exception.ParsingException;
import com.cefriel.coneyapi.exception.ResourceNotFoundException;
import com.cefriel.coneyapi.service.ChatService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat/")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    public ChatController(ChatService chatService){
        this.chatService = chatService;
    }


    @ApiOperation(value = "Returns conversation data, user ID, session and all the blocks up until the first question")
    @RequestMapping(value = "/beginConversation", method = RequestMethod.GET)
    public String beginConversation(@RequestParam(value = "userId", required = false) String userId,
                                    @RequestParam(value = "meta1", required = false) String meta1,
                                    @RequestParam(value = "meta2", required = false) String meta2,
                                    @RequestParam(value = "noRepeat", required = false) String noRepeat,
                                    @RequestParam(value = "restart") int restart,
                                    @RequestParam(value = "session", required = false) String session,
                                    @RequestParam(value = "lang",required = false) String lang,
                                    @RequestParam(value = "titleOnly", required = false) String titleOnly,
                                        @RequestParam(value = "conversationId", required = false) String conversationId)
        throws Exception {

        String outcome;
        String oldSession = "";

        if(titleOnly!= null && titleOnly.equals("true")){
            return chatService.getConversationTitle(conversationId);
        }

        if(noRepeat == null){
            noRepeat = "";
        }

        if(session != null && session.contains("continue_s_")){
            oldSession = session;
        }

        if(restart == 0 && userId != null && !userId.equals("") && noRepeat.equals("noRepeat")){

            logger.info("[CHAT] No-repeat check, looking for previous compilation");
            String finishedSession = chatService.wasTheConversationFinished(userId, conversationId);
            if(finishedSession != null){
                logger.error("[CHAT] ERROR: Customer has already completed this survey");
                throw new MethodNotAllowedException("You have already completed the survey for this project");
            }
        }

        if(restart == 0 && userId != null && !userId.equals("")) {
            logger.info("[CHAT] Looking for previous sessions");
            String sessionAlreadyStarted = chatService.wasTheConversationStarted(userId, conversationId);
            if(sessionAlreadyStarted != null){
                logger.info("[CHAT] Conversation already started by the user");
                JsonObject resultJson = new JsonObject();
                resultJson.addProperty("re-session", sessionAlreadyStarted);
                return resultJson.toString();
            } else {

                if(userId.contains("u_") && oldSession.equals("")){
                    userId = null;
                }

            }
        } else if(restart == 1){ //=restart

            if(userId.contains("u_")){
                userId = null;
            }

            logger.info("[CHAT] Restarting the conversation, previous answers will be deleted with session " + session);
            chatService.deletePreviousAnswers(userId, conversationId, session);

        } else if(restart == 2){ //=continue

            oldSession = session;
            logger.info("[CHAT] Continue chosen, session: "+session);
        }


        outcome = (chatService.beginConversation(userId, conversationId, meta1, meta2, oldSession, lang));

        switch (outcome) {
            case "no_converstion":
                logger.error("[CHAT] Failed to get conversation and add it to the JSON");
                throw new ResourceNotFoundException("Failed to get conversation and add it to the JSON");
            case "no_startend":
                logger.error("[CHAT] ERROR: Failed to create Starting relationship");
                throw new ResourceNotFoundException("ERROR: Failed to create Starting relationship");
            case "no_firstblock":
                logger.error("[CHAT] ERROR: Failed to get first block");
                throw new ResourceNotFoundException("ERROR: Failed to get first block");
        }

        return outcome;
    }

    @ApiOperation(value = "Given an answer, returns the next conversation nodes up until a question or the last node")
    @RequestMapping(value = "/continueConversation", method = RequestMethod.POST)
    public String continueConversation(@RequestBody String json_answer)
            throws ResourceNotFoundException, ParsingException
    {

        if(json_answer.length()==0){
            logger.error("[CHAT] ERROR: no answer given, empty json");
            throw new ParsingException("No answer given");
        }

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(json_answer);


        String convId = json.get("conversationId").getAsString();
        String userId = json.get("userId").getAsString();
        String lang = json.get("lang").getAsString();

        int blockId = json.get("blockId").getAsInt();
        String type = json.get("answerType").getAsString();
        String sessionId = json.get("session").getAsString();


        String res = "";

        String redo = json.get("redo").getAsString();


        if(redo.equals("true")){

            res = chatService.redoQuestion(userId, blockId, convId, sessionId, lang);

            if(res.equals("no_answer")){
                logger.error("Unable to re-find answers");
                throw new ResourceNotFoundException("Unable to re-find answers");
            }

        } else {
            if(type.equals("checkbox")){
    logger.info("*********************** checkbox");
                JsonArray answers = json.get("answer").getAsJsonArray();
                for(int i = 0; i<answers.size(); i++){
    logger.info("*********************** in loop");
                    JsonObject ans = answers.get(i).getAsJsonObject();
                    int answer = ans.get("order").getAsInt();
    logger.info("*********************** order: "+answer);
                    String cb_type = null; String other = "";
                    try{
                        cb_type = ans.get("type").getAsString();
                        other = ans.get("text").getAsString();
                    } catch (Exception ignored){}

                    if((i+1) == answers.size()){
     logger.info("*********************** last one");
                        if(cb_type!=null && cb_type.equals("other")){
                            type = "checkbox_other";
                        }
                        res = chatService.continueConversation(userId, blockId, type, answer+"", convId, sessionId, lang, other);

                    } else {
     logger.info("*********************** saving answer");
                        //to be removed
                        if(cb_type!=null && cb_type.equals("other")){
                            chatService.saveOtherCheckboxAnswer(userId, blockId, type, answer, convId, sessionId, other);
                        }

                        chatService.saveCheckboxAnswer(userId, blockId, type, answer, convId, sessionId);
                    }
                }
            } else {
                String answer = json.get("answer").getAsString();
                res = chatService.continueConversation(userId, blockId, type, answer, convId, sessionId, lang,"");
            }
        }

        if(res == null){
            logger.error("[CHAT] The answer doesn't match any available/permitted one");
            throw new ResourceNotFoundException("The answer doesn't match any available/permitted one");
        } else if(res.equals("invalid_json")){
            logger.error("[CHAT] ERROR: unable to add data to result json");
            throw new ParsingException("Unable to add data to result json");
        }

        return res;
    }

    @ApiOperation(value="Deletes all the nodes created for the preview")
    @RequestMapping(value = "/deletePreview", method = RequestMethod.DELETE)
    public boolean deletePreview(@RequestParam(value = "conversationId") String conversationId,
                                 @RequestParam(value = "session") String session)
            throws Exception {
        logger.info("[CONVERSATION] Closing preview, deleting temp nodes of conv: "+conversationId);
        return chatService.deletePreview(conversationId, session);
    }


    @ApiOperation(value = "Returns available languages of a specific conversation")
    @RequestMapping(value = "/getLanguagesOfConversation", method = RequestMethod.GET)
    public String  getLanguagesOfConversation(@RequestParam(value = "conversationId") String conversationId)
            throws ResourceNotFoundException {

        JsonArray langList;
        try{
            langList = chatService.getLanguagesOfConversation(conversationId);
        } catch (Exception e){
            logger.error("[CHAT] No languages found");
            throw new ResourceNotFoundException("No languages found");
        }

        return langList.toString();
    }


    @ApiOperation(value = "Returns available details of a specific conversation")
    @RequestMapping(value = "/getConversationDetails", method = RequestMethod.GET)
    public String getConversationDetails(@RequestParam(value = "conversationId") String conversationId,
                                         @RequestParam(value = "preview", required = false) String preview)
            throws ResourceNotFoundException {

        String result;
        boolean prev;
        if(preview == null || preview.equals("false")){
            prev = false;
        } else {
            prev = true;
        }

        try{
            result = chatService.getConversationDetails(conversationId, prev);
        } catch (Exception e){
            logger.error("[CHAT] No languages found");
            throw new ResourceNotFoundException("No languages found");
        }

        return result;
    }
}
