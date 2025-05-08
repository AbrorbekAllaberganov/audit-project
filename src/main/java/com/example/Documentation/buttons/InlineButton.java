package com.example.Documentation.buttons;

import com.example.Documentation.entity.Document;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

public class InlineButton {

    public static InlineKeyboardMarkup getDocuments(List<Document> documentList, int page) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("<");
        button.setCallbackData("document-page_BACK" + page);

        row.add(button);

        for (int i = 0; i < documentList.size(); i++) {
            button = new InlineKeyboardButton();
            button.setText(String.valueOf(i + 1));
            button.setCallbackData("document:" + documentList.get(i).getId());

            row.add(button);

            if ((i + 1) % 5 == 0) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }


        button = new InlineKeyboardButton();
        button.setText(">");
        button.setCallbackData("document-page_FRONT" + page);

        row.add(button);

        rows.add(row);

        markup.setKeyboard(rows);
        return markup;

    }

    public static String textBuilderForDocuments(List<Document> documentList) {
        StringBuilder stb = new StringBuilder();

        stb.append("Mavjud hujjatlar: ");

        for (int i = 0; i < documentList.size(); i++) {
            stb.append("\n").append(i + 1).append(". ").append(documentList.get(i).getProductName());
        }

        return stb.toString();
    }
}
