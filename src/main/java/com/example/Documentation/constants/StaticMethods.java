package com.example.Documentation.constants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Slf4j
public class StaticMethods {


    public static LocalDate stringToLocalDate(String dateString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    public static Optional<Date> stringToDate(String dateString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            return Optional.of(formatter.parse(dateString));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public static Optional<java.sql.Date> stringToSqlDate(String dateString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date parsedDate = formatter.parse(dateString);
            return Optional.of(new java.sql.Date(parsedDate.getTime()));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }




    public static Optional<BigDecimal> getCurrencyByDate(String date) {
        RestTemplate restTemplate = new RestTemplate();

        final String URL = "https://cbu.uz/uz/arkhiv-kursov-valyut/json/USD/" + date + "/";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(URL, String.class);
            String responseBody = response.getBody();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootArray = mapper.readTree(responseBody);

            if (rootArray.isArray() && rootArray.size() > 0) {
                JsonNode firstObject = rootArray.get(0);
                String rateStr = firstObject.get("Rate").asText().replace(",", ".");
                return Optional.of(new BigDecimal(rateStr));
            }
        } catch (Exception e) {
            log.error("Error fetching currency rate: {}", e.getMessage());
        }

        return Optional.empty();
    }

}
