package org.example.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Collections;

@Service
public class GoogleDocsService {

    private final Docs docsService;
    private final String documentId;

    public GoogleDocsService(@Value("${google.credentials}") String credentialsPath,
                             @Value("${google.docs.documentId}") String documentId) {
        try {
            var resource = new ClassPathResource(credentialsPath);

            ServiceAccountCredentials credentials = (ServiceAccountCredentials) ServiceAccountCredentials
                    .fromStream(resource.getInputStream())
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/documents"));

            docsService = new Docs.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("TelegramFeedbackBot")
                    .build();

            this.documentId = documentId;

        } catch (Exception e) {
            e.printStackTrace();  // <- тут буде повний stack trace реальної помилки
            throw new RuntimeException(e);
        }

    }

    public void appendFeedback(String feedbackText) throws IOException {
        Request request = new Request()
                .setInsertText(new InsertTextRequest()
                        .setText(feedbackText + "\n\n")
                        .setEndOfSegmentLocation(new EndOfSegmentLocation()));

        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest()
                .setRequests(Collections.singletonList(request));

        docsService.documents().batchUpdate(documentId, body).execute();
    }
}
