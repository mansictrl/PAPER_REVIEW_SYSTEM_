package com.project.paperreview.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Async
    public void sendResolutionAlert(String prn, String question) {

        System.out.println("📩 Notification sent to PRN: " + prn + " for question: " + question);

        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}