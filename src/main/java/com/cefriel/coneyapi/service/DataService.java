package com.cefriel.coneyapi.service;

import com.cefriel.coneyapi.model.db.custom.AnswersResponse;
import com.cefriel.coneyapi.model.db.custom.QuestionBlock;
import com.cefriel.coneyapi.model.db.custom.UserSession;
import com.cefriel.coneyapi.model.db.entities.Block;
import com.cefriel.coneyapi.repository.DataRepository;
import com.cefriel.coneyapi.utils.RDFUtils;

import com.google.gson.JsonArray;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import java.util.Arrays;
import java.util.List;

@Service
public class DataService {

    @Autowired
    private DataRepository dataRepository;

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);

    public DataService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public String getAnswersOfConversation(String conversationId, String simplify, boolean anonymize, boolean trimData){

        if(!hasUserPermission(conversationId)){
            return null;
        }

        List<AnswersResponse> list = dataRepository.getAnswersOfConversation(conversationId);

        if(list.size()==0){
            return null;
        }

        if(simplify.equals("true")){
            return getSimplifiedAnswersOfConversation(conversationId, anonymize);
        } else {
            return answersToCSV(list, anonymize, trimData);
        }
    }

    public String getOrderedQuestions(String conversationId){

        if(!hasUserPermission(conversationId)){
            return "not_auth";
        }

        List<QuestionBlock> questions = dataRepository.getOrderedQuestionsOfConversation(conversationId);
        JsonArray questionsArray = new JsonArray();
        for(QuestionBlock question: questions){
            questionsArray.add(question.toJson());
        }
        return questionsArray.toString();
    }

    public String getSimplifiedAnswersOfConversation(String conversationId, boolean anonymize){

        if(!hasUserPermission(conversationId)){
            return "not_auth";
        }


        List<AnswersResponse> answers = dataRepository.getAnswersOfConversation(conversationId);
        List<QuestionBlock> questions = dataRepository.getOrderedQuestionsOfConversation(conversationId);
        int [] questionIds = new int[questions.size()];
        List<UserSession> users = dataRepository.getRespondentsOfConversation(conversationId);
        String [] sessions = new String[users.size()];

        String [][] resultMatrix = new String[users.size()][questions.size()];


        int userIndex;
        int questionIndex;

        StringBuilder sb = new StringBuilder();
        String line = "user,session,language,start_timestamp,end_timestamp,meta1,meta2";
        sb.append(line);

        for(int i = 0; i<users.size(); i++){//String user: users){
            Arrays.fill(resultMatrix[i], "");
            logger.info("[DATA] user: " + users.get(i));
            resultMatrix[i][0] = users.get(i).getSession();
            sessions[i] = users.get(i).getSession();
        }

        for(int i = 0; i<questions.size(); i++){//QuestionBlock question: questions){
            logger.info("[DATA] question: " + questions.get(i).getText());
            resultMatrix[0][i] = Integer.toString(questions.get(i).getNeo4jId());
            questionIds[i] = questions.get(i).getNeo4jId();
            sb.append(",");
            sb.append(questions.get(i).getText());
        }

        sb.append(System.getProperty("line.separator"));
        logger.info("[DATA] Users and questions gathered, CSV header: " + sb.toString());

        for (AnswersResponse element : answers) {//AnswersResponse element : answers) {

            userIndex = ArrayUtils.indexOf(sessions, element.getSession());
            questionIndex = ArrayUtils.indexOf(questionIds, element.getQuestionId());

            logger.info("[DATA] questionType: " + element.getQuestionType());

            String type = element.getQuestionType();
            if (type == null || type.equals("")) {
                type = element.getAnswerType();
            }

            if (element.getSession().equals("") || type.equals("")
                    || userIndex == -1 || questionIndex == -1) {
                continue;
            }

            switch (type) {
                case "text":
                    resultMatrix[userIndex][questionIndex] = csvString(element.getFreeAnswer());
                    break;
                case "star":
                    logger.info(resultMatrix[userIndex][questionIndex]);
                    resultMatrix[userIndex][questionIndex] = "" + element.getValue();
                    break;
                case "checkbox":
                    resultMatrix[userIndex][questionIndex] += "'" + csvString(element.getOption()) + "';";
                    break;
                default:
                    resultMatrix[userIndex][questionIndex] = csvString(element.getOption());
                    break;
            }
        }

        logger.info("[DATA] Result matrix filled, parsing result");

        for(int i=0; i<users.size(); i++){

            sb.append(users.get(i).getUserId(anonymize));
            sb.append(",");
            sb.append(users.get(i).getSession());
            sb.append(",");
            sb.append(users.get(i).getLanguage());
            sb.append(",");
            sb.append(users.get(i).getStartTimestamp());
            sb.append(",");
            sb.append(users.get(i).getEndTimestamp());
            sb.append(",");
            sb.append(users.get(i).getMeta1());
            sb.append(",");
            sb.append(users.get(i).getMeta2());

            for(int j=0; j<questions.size(); j++){
                sb.append(",");
                sb.append(resultMatrix[i][j]);
            }
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public String getRespondentsOfConversation(String conversationId){

        if(!hasUserPermission(conversationId)){
            return "not_auth";
        }

        List<UserSession> users = dataRepository.getRespondentsOfConversation(conversationId);
        JsonArray usersArray = new JsonArray();
        for (UserSession user : users) {
            usersArray.add(user.toJson());
        }
        return usersArray.toString();
    }

    //TODO Export literals also in other languages
    //Returns a "rdf-formatted" string for the blocks in a conversation
    public String getRDFOfConversation(String conversationId, String base, String format) {

        // Factory per creare nuovi Values e Statements in rdf4j
        ValueFactory factory = SimpleValueFactory.getInstance();

        // Model in cui inserire gli statements
        Model model = new LinkedHashModel();
        // Add namespaces
        RDFUtils.addDefaultNamespaces(model);

        List<Block> conversationBlocks = dataRepository.getBlocksOfConversation(conversationId);
        String defaultLanguageTag = dataRepository.getDefaultLanguageOfConversation(conversationId);

        model.add(
                factory.createIRI(base, "SurveyProcedure_" + conversationId),
                RDF.TYPE,
                factory.createIRI(RDFUtils.SUR, "SurveyProcedure")
        );

        Block firstBlock = dataRepository.getFirstBlock(conversationId);
        String firstBlockId = RDFUtils.getBlockId(conversationId, firstBlock);

        model.add(
                factory.createIRI(base, "SurveyProcedure_" + conversationId),
                factory.createIRI(RDFUtils.SUR, "startsWith"),
                factory.createIRI(base, firstBlockId)
        );


        for(Block b: conversationBlocks) {

            String blockId = RDFUtils.getBlockId(conversationId, b);

            model.add(
                    factory.createIRI(base, blockId),
                    RDF.TYPE,
                    factory.createIRI(RDFUtils.SUR, "SurveyElement")
            );

            model.add(
                    factory.createIRI(base, blockId),
                    factory.createIRI(RDFUtils.SUR, "inSurveyProcedure"),
                    factory.createIRI(base, "SurveyProcedure_" + conversationId)
            );

            // Answer
            if(b.getBlockType().toLowerCase().equals("answer")) {
                model.add(
                        factory.createIRI(base, blockId),
                        RDF.TYPE,
                        factory.createIRI(RDFUtils.SUR, "Answer")
                );

                if(b.getBlockSubtype().toLowerCase().equals("single")) {
                    model.add(
                            factory.createIRI(base, blockId),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "OpenAnswer")
                    );
                } else if (b.getBlockSubtype().toLowerCase().equals("multiple") ||
                		b.getBlockSubtype().toLowerCase().equals("checkbox")) {
                	
                    model.add(
                            factory.createIRI(base, blockId),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "ClosedAnswer")
                    );
                    
                    if (b.getPoints() != 0)
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasPoints"),
	                            factory.createLiteral((float) b.getPoints())
	                    );
                    
                    if (b.getOrder() != 0)
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasOrderNumber"),
	                            factory.createLiteral(b.getOrder())
	                    );
                    
                    if (b.getValue() != 0)
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasValue"),
	                            factory.createLiteral((float) b.getValue())
	                    );
                    if (b.getText() != null && !b.getText().equals(""))
    	                model.add(
    	                        factory.createIRI(base, blockId),
    	                        factory.createIRI(RDFUtils.SUR, "hasText"),
    	                        factory.createLiteral(b.getText(), defaultLanguageTag)
    	                );
                }  
            }
            // Talk
            else if(b.getBlockType().toLowerCase().equals("talk")) {

                model.add(
                        factory.createIRI(base, blockId),
                        RDF.TYPE,
                        factory.createIRI(RDFUtils.SUR, "Talk")
                );

                if(b.getBlockSubtype().toLowerCase().equals("text")){
                	if(b.getText() != null && !b.getText().equals(""))
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasText"),
	                            factory.createLiteral(b.getText(), defaultLanguageTag)
	                    );
                }
                else if(b.getBlockSubtype().toLowerCase().equals("link")) {
                	if(b.getText() != null && !b.getText().equals(""))
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasText"),
	                            factory.createLiteral(b.getText(), defaultLanguageTag)
	                    );
                	
                	if(b.getUrl() != null && !b.getUrl().equals(""))
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasLink"),
	                            factory.createLiteral(b.getUrl(), XMLSchema.ANYURI)
	                    );
                }
                else if(b.getBlockSubtype().toLowerCase().equals("imageurl")) {
                	if(b.getUrl() != null && !b.getUrl().equals(""))
	                    model.add(
	                            factory.createIRI(base, blockId),
	                            factory.createIRI(RDFUtils.SUR, "hasLink"),
	                            factory.createLiteral(b.getUrl(), XMLSchema.ANYURI)
	                    );
                }

            }

            // Question
            else if(b.getBlockType().toLowerCase().equals("question")) {
            	
                String tag = dataRepository.getTagOfBlock(b.getBlockId(), conversationId);

                model.add(
                        factory.createIRI(base, blockId),
                        RDF.TYPE,
                        factory.createIRI(RDFUtils.SUR, "Question")
                );

                model.add(
                        factory.createIRI(base, blockId),
                        factory.createIRI(RDFUtils.SUR, "hasText"),
                        factory.createLiteral(b.getText(), defaultLanguageTag)
                );

                model.add(
                        factory.createIRI(base, "ObservableVariable_" + Math.abs(b.getText().hashCode())),
                        RDF.TYPE,
                        factory.createIRI(RDFUtils.SUR, "ObservableVariable")
                );

                model.add(
                        factory.createIRI(base, blockId),
                        factory.createIRI(RDFUtils.SUR, "hasObservableVariable"),
                        factory.createIRI(base, "ObservableVariable_" + Math.abs(b.getText().hashCode()))
                );

                if(tag != null) {

                    String tag_id = "Tag_" + Math.abs(tag.toLowerCase().hashCode());

                    model.add(
                            factory.createIRI(base, tag_id),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "LatentVariable")
                    );

                    model.add(
                            factory.createIRI(base, tag_id),
                            RDFS.LABEL,
                            factory.createLiteral(tag.toLowerCase())
                    );


                    model.add(
                            factory.createIRI(base, blockId),
                            factory.createIRI(RDFUtils.SUR, "hasLatentVariable"),
                            factory.createIRI(base, tag_id)
                    );
                }


                if (b.getBlockSubtype().toLowerCase().equals("single")) {
                    model.add(
                            factory.createIRI(base, blockId),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "OpenQuestion")
                    );
                    model.add(
                            factory.createIRI(base, blockId),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "SingleInputQuestion")
                    );
                  //TODO Add different types for number, datetime, etc... ??
                } else if (b.getBlockSubtype().toLowerCase().equals("multiple")) {
                	
                    model.add(
                            factory.createIRI(base, blockId),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "ClosedQuestion")
                    );
                    
                    model.add(
                            factory.createIRI(base, blockId),
                            RDF.TYPE,
                            factory.createIRI(RDFUtils.SUR, "MultipleChoiceQuestion")
                    );               

                    switch(b.getVisualization()) {
                        case "options":
                            model.add(
                                    factory.createIRI(base, blockId),
                                    factory.createIRI(RDFUtils.SUR, "hasVisualization"),
                                    factory.createLiteral("Option")
                            );
                            break;
                        case "star":
                            model.add(
                                    factory.createIRI(base, blockId),
                                    factory.createIRI(RDFUtils.SUR, "hasVisualization"),
                                    factory.createLiteral("Scale")
                            );
                            break;
                        case "emoji":
                            model.add(
                                    factory.createIRI(base, blockId),
                                    factory.createIRI(RDFUtils.SUR, "hasVisualization"),
                                    factory.createLiteral("Emoji")
                            );
                            break;
                        case "slider":
                            model.add(
                                    factory.createIRI(base, blockId),
                                    factory.createIRI(RDFUtils.SUR, "hasVisualization"),
                                    factory.createLiteral("Slider")
                            );
                            break;
                        case "select":
                            model.add(
                                    factory.createIRI(base, blockId),
                                    factory.createIRI(RDFUtils.SUR, "hasVisualization"),
                                    factory.createLiteral("Dropdown")
                            );
                            break;
                        default:
                            break;
                    }
                } else if (b.getBlockSubtype().toLowerCase().equals("checkbox")) {            	
                	 model.add(
                             factory.createIRI(base, blockId),
                             RDF.TYPE,
                             factory.createIRI(RDFUtils.SUR, "ClosedQuestion")
                     );
                     
                	 model.add(
                             factory.createIRI(base, blockId),
                             RDF.TYPE,
                             factory.createIRI(RDFUtils.SUR, "CheckboxQuestion")
                     );             	
                }
            }

            List<Block> nextBlocks = dataRepository.getNextBlock(b.getBlockId(), conversationId);
            for(Block nb: nextBlocks)
                model.add(
                        factory.createIRI(base, blockId),
                        factory.createIRI(RDFUtils.SUR, "leadsTo"),
                        factory.createIRI(base, RDFUtils.getBlockId(conversationId, nb))
                );
        }

        return RDFUtils.writeRDFData(format, model);

    }

    public String getRDFOfAnswers(String conversationId, String base, String format, boolean anonymize){
        logger.info("[DATA] RDF of answers requested");
        logger.info("[DATA] Fetching answers");
        List<AnswersResponse> list = dataRepository.getAnswersOfConversation(conversationId);

        if (list != null && list.size() == 0) {
            logger.info("[DATA] No data found, returning");
            return null;
        }
        logger.info("[DATA] " + list.size() + " answers found");

        // Factory per creare nuovi Values e Statements in rdf4j
        ValueFactory factory = SimpleValueFactory.getInstance();

        // Model in cui inserire gli statements
        Model model = new LinkedHashModel();
        RDFUtils.addDefaultNamespaces(model);

        logger.info("[DATA] Creating model for conv: "+conversationId);
        String user;
        for(AnswersResponse l: list) {

            user = l.getUser();

            //la query torna anche le risposte non legate ad utenti, che non servono in questo export
            if(user == null || user.equals("")){
                continue;
            }

            if(anonymize){
                user = l.getAnonymizedUser();
            }

            String completionId = conversationId + "_" + user + "_" + l.getSession();
            String completedSurveyId = "CompletedSurvey_" + completionId;
            String surveyCompletionId = "SurveyCompletionTask_" + completionId;
            String completedQuestionId = "CompletedQuestion_" + completionId + "_" + l.getQuestionId();
            String surveyProcedureId = "SurveyProcedure_" + conversationId;
            String datasetId = "DataSet_" + conversationId;
            String participantId = "Participant_" + user;

            // Survey Completion
            model.add(
                    factory.createIRI(base, surveyCompletionId),
                    RDF.TYPE,
                    factory.createIRI(RDFUtils.SUR, "SurveyCompletionTask")
            );

            model.add(
                    factory.createIRI(base, surveyCompletionId),
                    factory.createIRI(RDFUtils.SUR, "sessionId"),
                    factory.createLiteral(l.getSession())
            );

            model.add(
                    factory.createIRI(base, surveyCompletionId),
                    factory.createIRI(RDFUtils.WFPROV, "describedByProcess"),
                    factory.createIRI(base, surveyProcedureId)
            );

            addTarget(model, factory, base, surveyCompletionId, l.getMeta1());
            addTarget(model, factory, base, surveyCompletionId, l.getMeta2());

            if (l.getStartTimestamp() != null && !l.getStartTimestamp().equals(""))
	            model.add(
	                    factory.createIRI(base, surveyCompletionId),
	                    factory.createIRI(RDFUtils.PROV, "startedAtTime"),
	                    factory.createLiteral(RDFUtils.formatDateTime(l.getStartTimestamp()), XMLSchema.DATETIME)
	            );
            
            if (l.getEndTimestamp() != null && !l.getEndTimestamp().equals(""))
	            model.add(
	                    factory.createIRI(base, surveyCompletionId),
	                    factory.createIRI(RDFUtils.PROV, "endedAtTime"),
	                    factory.createLiteral(RDFUtils.formatDateTime(l.getEndTimestamp()), XMLSchema.DATETIME)
	            );

            // Completed Survey
            model.add(
                    factory.createIRI(base, completedSurveyId),
                    RDF.TYPE,
                    factory.createIRI(RDFUtils.SUR, "CompletedSurvey")
            );

            model.add(
                    factory.createIRI(base, completedSurveyId),
                    factory.createIRI(RDFUtils.QB, "observation"),
                    factory.createIRI(base, completedQuestionId)
            );

            model.add(
                    factory.createIRI(base, completedSurveyId),
                    factory.createIRI(RDFUtils.WFPROV, "wasOutputFrom"),
                    factory.createIRI(base, surveyCompletionId)
            );
            
            model.add(
                    factory.createIRI(base, completedSurveyId),
                    factory.createIRI(RDFUtils.WFPROV, "describedByParameter"),
                    factory.createIRI(base, completedQuestionId)
            );

            // Survey Data Set
            model.add(
                    factory.createIRI(base, datasetId),
                    RDF.TYPE,
                    factory.createIRI(RDFUtils.SUR, "SurveyDataSet")
            );

            // User
            model.add(
                    factory.createIRI(base, participantId),
                    RDF.TYPE,
                    factory.createIRI(RDFUtils.SUR, "Participant")
            );

            model.add(
                    factory.createIRI(base, participantId),
                    factory.createIRI(RDFUtils.SUR, "participantId"),
                    factory.createLiteral(user)
            );
            
            model.add(
                    factory.createIRI(base, surveyCompletionId),
                    factory.createIRI(RDFUtils.PROV, "wasAssociatedWith"),
                    factory.createIRI(base, participantId)
            );
            
            model.add(
                    factory.createIRI(base, completedSurveyId),
                    factory.createIRI(RDFUtils.PROV, "wasAttributedTo"),
                    factory.createIRI(base, participantId)
            );
            
            model.add(
                    factory.createIRI(base, completedQuestionId),
                    factory.createIRI(RDFUtils.PROV, "wasAttributedTo"),
                    factory.createIRI(base, participantId)
            );
            
            // Completed Question
            model.add(
                    factory.createIRI(base, completedQuestionId),
                    RDF.TYPE,
                    factory.createIRI(RDFUtils.SUR, "CompletedQuestion")
            );
            
            String completesQuestionId = "Block_" + conversationId + "_" + l.getQuestionId();
            model.add(
                    factory.createIRI(base, completedQuestionId),
                    factory.createIRI(RDFUtils.SUR, "completesQuestion"),
                    factory.createIRI(base, completesQuestionId)
            );
            
            if(l.getTimestamp() != null && !(l.getTimestamp().equals("")))
		        model.add(
		                factory.createIRI(base, completedQuestionId),
		                factory.createIRI(RDFUtils.SUR, "hasCompletionTimestamp"),
		                factory.createLiteral(RDFUtils.formatDateTime(l.getTimestamp()), XMLSchema.DATETIME)
		        );

            model.add(
                    factory.createIRI(base, completedQuestionId),
                    factory.createIRI(RDFUtils.SUR, "answeredIn"),
                    factory.createIRI(base, surveyCompletionId)
            );

            model.add(
                    factory.createIRI(base, completedQuestionId),
                    factory.createIRI(RDFUtils.QB, "dataSet"),
                    factory.createIRI(base, datasetId)
            );
            
            if(l.getQuestionType().toLowerCase().equals("text")) {
            	String answerText = "";
	    		if(l.getFreeAnswer() != null 
	    				&& !l.getFreeAnswer().toLowerCase().equals("skip")
	    				&& !l.getFreeAnswer().toLowerCase().equals("")) 
	    			answerText = l.getFreeAnswer();
    			model.add(
                        factory.createIRI(base, completedQuestionId),
                        factory.createIRI(RDFUtils.SUR, "hasAnswerText"),
                        factory.createLiteral(answerText, l.getLanguage())
                );
            }
    		
    		if(l.getAnswerId() != 0) {
    			String hasAnswerId = "Block_" + conversationId + "_" + l.getAnswerId();
    			model.add(
                        factory.createIRI(base, completedQuestionId),
                        factory.createIRI(RDFUtils.SUR, "hasAnswer"),
                        factory.createIRI(base, hasAnswerId)
                );
    		}
        }

        return RDFUtils.writeRDFData(format, model);
    }

    private void addTarget(Model model, ValueFactory factory, String base, String surveyCompletionId, String target) {
    	
    	if(target != null && !(target.equals(""))) {
	    	String targetId = "SurveyTarget_" + target;
	    	
	    	model.add(
	                factory.createIRI(base, targetId),
	                RDF.TYPE,
	                factory.createIRI(RDFUtils.SUR, "SurveyTarget")
		        );
	    	
	    	model.add(
	                factory.createIRI(base, targetId),
	                RDFS.LABEL,
	                factory.createLiteral(target)
		        );	        	
	        
	        model.add(
                factory.createIRI(base, surveyCompletionId),
                factory.createIRI(RDFUtils.SUR, "completedFor"),
                factory.createIRI(base, targetId)
	        );
    	}
		
	}

	//returns a "csv-formatted" string
    private String answersToCSV(List<AnswersResponse> list, boolean anonymize, boolean trimData){

        logger.info("[CONVERSATION] Exporting CSV");

        String line = "questionId,question,questionType,tag,option,value,order,freeAnswer,points,user,language,date,time,session,totalDuration,meta1,meta2";
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        sb.append(System.getProperty("line.separator"));
        logger.info("[CONVERSATION] CSV header: " + line);

        for (AnswersResponse as : list)
        {

            //deleting non-visited nodes.
            if(trimData && as.getUser().equals("")){
                continue;
            }


            String type = "text";

            if(!"".equals(as.getQuestionType())){
                type = as.getQuestionType();
            }

            //TODO fix
            String tmpOp = as.getOption();
            if(as.getAnswerType().equals("checkbox")){
                if(tmpOp.startsWith("----")) {
                    as.setOption(tmpOp.substring(4));
                }
                type = "checkbox";
            }

            String user = as.getUser();
            if(anonymize && !user.equals("")){
                user = as.getAnonymizedUser();
            }


            line =  as.getQuestionId() + "," +
                    "\"" + csvString(as.getQuestion()) + "\"," +
                    "\"" + csvString(type) + "\"," +
                    "\"" + csvString(as.getTags()) + "\"," +
                    "\"" + csvString(as.getOption()) + "\"," +
                    as.getValue() + "," +
                    as.getOrder() + "," +
                    "\"" + csvString(as.getFreeAnswer()) + "\"," +
                    as.getPoints() + "," +
                    "\"" + user + "\"," +
                    "\"" + as.getLanguage() + "\"," +
                    as.getDate() + "," +
                    as.getTime() + "," +
                    "\"" + as.getSession() + "\"," +
                    as.getDuration() + "," +
                    "\"" + csvString(as.getMeta1()) + "\"," +
                    "\"" + csvString(as.getMeta2()) + "\"";

            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    private String csvString(String answer) {
        if (answer == null || answer.equals(""))
            return "";
        return answer.trim().replace("\n", "").replace("\r", "").replace("\"", "'");
    }

    private boolean hasUserPermission(String conversationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if(username.equals("anonymousUser")){
            return true;
        }

        String res =  dataRepository.hasUserPermission(username, conversationId);
        if(res == null || !Boolean.valueOf(res)){
            logger.error("[CONVERSATION] User not authorized to access conversation: "+conversationId);
            return false;
        }
        return Boolean.valueOf(res);
    }
}
