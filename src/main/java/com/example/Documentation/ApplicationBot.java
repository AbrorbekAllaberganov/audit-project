package com.example.Documentation;

import com.example.Documentation.buttons.InlineButton;
import com.example.Documentation.entity.Document;
import com.example.Documentation.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class ApplicationBot extends TelegramLongPollingBot {

    @Autowired
    private DocumentRepository documentRepository;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage() != null) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                Long chatId = message.getChatId();

                if (text.equals("/start")){

                    Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
                    Page<Document> documentList = documentRepository.findAll(pageable);

                    sendMessage(chatId,
                            InlineButton.textBuilderForDocuments(documentList.getContent()),
                            InlineButton.getDocuments(documentList.getContent(), 0));

                }

            }
        }
    }

    public void sendMessage(Long chatId, String text, ReplyKeyboard markup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(markup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
