package com.cefriel.coneyapi.scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cefriel.coneyapi.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PreviewDeleteTask {

    private static final Logger logger = LoggerFactory.getLogger(PreviewDeleteTask.class);
    private final ChatService chatService;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public PreviewDeleteTask(ChatService chatService){
            this.chatService = chatService;
            logger.info("initialized scheduled task");
    }

    @Scheduled(cron = "0 30 1 1 * ?", zone="Europe/Rome")
    public void reportCurrentTime() {

        chatService.deleteAllPreviews();
        logger.info("[UTILS] Clearing all preview relationships, time: {}", dateFormat.format(new Date()));

    }

}
