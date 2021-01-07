package com.cefriel.coneyapi.repository;

import com.cefriel.coneyapi.model.db.custom.AnswersResponse;
import com.cefriel.coneyapi.model.db.custom.QuestionBlock;
import com.cefriel.coneyapi.model.db.custom.UserSession;
import com.cefriel.coneyapi.model.db.entities.Block;
import com.cefriel.coneyapi.model.db.entities.Conversation;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface DataRepository extends Neo4jRepository<Conversation, Long> {

    @Query("MATCH (cust:Customer {username: {0}})-[wo:WORKS_ON]->(pr:Project), " +
            "(pr)<-[bt:BELONGS_TO]-(c:Conversation {conv_id:{1}}) " +
            "RETURN EXISTS ( (cust)-[wo:WORKS_ON]-(pr)-[bt]-(c) ) AND wo.access_level >= c.access_level")
    String hasUserPermission(String username, String conversationId);

    @Query("MATCH (a:Block {block_type:\"Answer\"})<-[:LEADS_TO]-(q:Block {of_conversation: {0}})  " +
            "OPTIONAL MATCH (u:User)-[ans:ANSWERED]->(a), (u)-[se:STARTEND]->(c:Conversation {conv_id: {0}}) " +
            "WHERE se.session = ans.session " +
            "OPTIONAL MATCH (q)-[:ABOUT]->(t:Tag) " +
            "RETURN q.of_conversation AS conversation_id, u.user_id AS user, se.meta1 as meta1, " +
            "se.meta2 as meta2, t.text as tag, q.text as question, q.visualization as question_type, id(q) as question_id, " +
            "id(a) as answer_id, a.block_subtype as answer_type, a.text as option, a.value as value, a.order as order, " +
            "ans.value as free_answer, a.points as points, " +
            "ans.timestamp as timestamp, ans.session as session, se.start_timestamp as start_timestamp, " +
            "se.end_timestamp as end_timestamp, se.lang as language;")
    List<AnswersResponse> getAnswersOfConversation(String conversationId);

    @Query("MATCH (b:Block {block_id: {0}, of_conversation: {1}})-[:LEADS_TO]->(a:Block {block_type: 'Answer'}) return a")
    List<Block> getAnswersToQuestion(int blockId, String conversationId);

    @Query("MATCH (n:Block {of_conversation:{0}}) RETURN n")
    List<Block> getBlocksOfConversation(String conversationId);

    @Query("MATCH (c:Conversation {conv_id:{0}})-[:STARTS]->(n:Block)" +
            "RETURN n LIMIT 1")
    Block getFirstBlock(String conversationId);

    @Query("MATCH (t:Tag)<-[:ABOUT]-(b:Block {block_id:{0}, of_conversation: {1}}) return t.text LIMIT 1")
    String getTagOfBlock(int blockId, String conversationId);

    @Query("MATCH (prev {block_id:{0}})-[:LEADS_TO]->(o) " +
            "WHERE prev.of_conversation = {1} " +
            "RETURN o")
    List<Block> getNextBlock(int blockId, String conversationId);

    @Query("MATCH (c:Conversation {conv_id:{0}}) return c.lang")
    String getDefaultLanguageOfConversation(String conversationId);

    @Query("MATCH (c:Conversation {conv_id:{0}}), (b:Block {block_type:'Question', of_conversation:{0}}), " +
            "p = shortestPath((c)-[a:STARTS|LEADS_TO*]-(b)) " +
            "RETURN id(b) as neo4jId, b.block_id as reteId, " +
            "b.block_type as type, b.block_subtype as subtype, b.of_conversation as ofConversation, " +
            "b.visualization as questionType, b.text as text, length(p) as depth")

    List<QuestionBlock> getOrderedQuestionsOfConversation(String conversationId);

    @Query("MATCH (n {conv_id:{0}})<-[st:STARTEND]-(u:User) " +
            "RETURN st.session as session, u.user_id as userId, st.start_timestamp as startTimestamp, " +
            "st.end_timestamp as endTimestamp, st.lang as language, st.meta1 as meta1, st.meta2 as meta2")
    ArrayList<UserSession> getRespondentsOfConversation(String conversationId);
}
