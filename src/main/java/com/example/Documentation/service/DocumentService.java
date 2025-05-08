package com.example.Documentation.service;


import com.example.Documentation.dto.ApiResponse;
import com.example.Documentation.dto.DocumentDto;
import com.example.Documentation.dto.PaginationGetDto;
import com.example.Documentation.entity.Attachment;
import com.example.Documentation.entity.Document;
import com.example.Documentation.entity.DocumentChangeLog;
import com.example.Documentation.repository.AttachmentRepository;
import com.example.Documentation.repository.DocumentChangeLogRepository;
import com.example.Documentation.repository.DocumentRepository;
import com.example.Documentation.constants.StaticMethods;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.print.Doc;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final AttachmentRepository attachmentRepository;
    private final DocumentChangeLogRepository documentChangeLogRepository;

    public ApiResponse saveDocument(DocumentDto documentDto) {
        Document document = new Document();

        mapDtoToDocument(documentDto, document);

        documentRepository.save(document);
        auditInsert(document);

        return new ApiResponse("Document saved", true, document);
    }

    public ApiResponse updateDocument(Long id, DocumentDto documentDto) {
        Optional<Document> documentOptional = documentRepository.findById(id);
        if (documentOptional.isEmpty())
            return new ApiResponse("Document not found", false);

        Document document = documentOptional.get();
        Document original = (Document) deepCopy(document);

        mapDtoToDocument(documentDto, document);

        documentRepository.save(document);
        auditUpdate(original, document);

        return new ApiResponse("Document updated", true);
    }

    public ApiResponse getAllDocuments(String fromDateString, String toDateString, int page, int size) {
        java.sql.Date fromDate = null;
        java.sql.Date toDate = null;

        if (fromDateString != null) {
            Optional<java.sql.Date> fromDateOptional = StaticMethods.stringToSqlDate(fromDateString);
            if (fromDateOptional.isPresent()) {
                fromDate = fromDateOptional.get();
            }
        }

        if (toDateString != null) {
            Optional<java.sql.Date> toDateOptional = StaticMethods.stringToSqlDate(toDateString);
            if (toDateOptional.isPresent()) {
                toDate = toDateOptional.get();
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<Document> documentPage = documentRepository.getDocumentsByDateRange(fromDate, toDate, pageable);
        return new ApiResponse("documents", true, new PaginationGetDto<>(
                documentPage.getContent(),
                documentPage.getTotalElements()
        ));
    }

    public ApiResponse getFileOfDocuments(String fromDateString, String toDateString) {
        java.sql.Date fromDate = null;
        java.sql.Date toDate = null;

        if (fromDateString != null) {
            Optional<java.sql.Date> fromDateOptional = StaticMethods.stringToSqlDate(fromDateString);
            if (fromDateOptional.isPresent()) {
                fromDate = fromDateOptional.get();
            }
        }

        if (toDateString != null) {
            Optional<java.sql.Date> toDateOptional = StaticMethods.stringToSqlDate(toDateString);
            if (toDateOptional.isPresent()) {
                toDate = toDateOptional.get();
            }
        }

        List<Document> documents = documentRepository.getDocumentsByDateRange(fromDate, toDate);
        return new ApiResponse("documents", true, generateBase64(documents));
    }



    public String generateBase64(List<Document> documentList) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Data");

            String[] headers = {
                    "№",
                    "Tashilotlar nomi",
                    "Shartnoma summasi",
                    "Pulni birjaga o'tkazilganligi",
                    "Pul birjaga tushdi",
                    "Maxsulot yetkazilganligi",
                    "Maxsulot tannarxi AQSH dollarida",
                    "Kurs",
                    "So'mda",
                    "Soliq",
                    "Bank xizmatidan so'ng summa",
                    "Ayrim xarajatlar % da",
                    "Foizlardan so'ng",
                    "Lot raqami",
                    "Transport va boshqa xarajatlar",
                    "Sof foyda",
                    "Sof foyda % ko'rinishid",
                    "Shartnomada yozilgan maxsulot",
                    "Telefon nomer"
            };

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setItalic(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setFontName("Calibri");

            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);


            CellStyle style = workbook.createCellStyle();

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            BigDecimal totalCostOfContract = new BigDecimal(0);
            BigDecimal totalCostInUSD = new BigDecimal(0);
            BigDecimal totalCostInUZS = new BigDecimal(0);
            BigDecimal totalTax = new BigDecimal(0);
            BigDecimal totalAfterBank = new BigDecimal(0);
            BigDecimal totalAfterExpenses = new BigDecimal(0);
            BigDecimal totalProfit = new BigDecimal(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (int i = 0; i < documentList.size(); i++) {
                Document document = documentList.get(i);
                Row row = sheet.createRow(rowIndex++);

                if (document.getCostOfContract() != null)
                    totalCostOfContract = totalCostOfContract.add(document.getCostOfContract());

                if (document.getCostInUSD() != null)
                    totalCostInUSD = totalCostInUSD.add(document.getCostInUSD());

                if (document.getCostInUZS() != null)
                    totalCostInUZS = totalCostInUZS.add(document.getCostInUZS());

                if (document.getTax() != null)
                    totalTax = totalTax.add(document.getTax());

                if (document.getSummaAfterBankService() != null)
                    totalAfterBank = totalAfterBank.add(document.getSummaAfterBankService());

                if (document.getMoneyAfterExpenses() != null)
                    totalAfterExpenses = totalAfterExpenses.add(document.getMoneyAfterExpenses());

                if (document.getProfit() != null)
                    totalProfit = totalProfit.add(document.getProfit());


                String[] values = {
                        String.valueOf(i + 1),
                        document.getCompanyName(),
                        String.valueOf(document.getCostOfContract()),
                        document.getIsMoneyTransferred() ? "+" : "-",
                        document.getIsMoneyAccepted() ? "+" : "-",
                        document.getIsMoneySent() ? "+" : "-",
                        document.getCostInUSD() != null ? String.valueOf(document.getCostInUSD()) : "",
                        document.getExchangingRate() != null ? String.valueOf(document.getExchangingRate()) : "",
                        document.getCostInUZS() != null ? String.valueOf(document.getCostInUZS()) : "",
                        String.valueOf(document.getTax()),
                        String.valueOf(document.getSummaAfterBankService()),
                        String.valueOf(document.getAdditionalExpenseInPercentage()),
                        String.valueOf(document.getMoneyAfterExpenses()),
                        document.getLatNumber() != null ? document.getLatNumber() : "",
                        String.valueOf(document.getTransportExpenses()),
                        String.valueOf(document.getProfit()),
                        String.valueOf(document.getProfitInPercentage()),
                        document.getProductName(),
                        document.getPhoneNumber()
                };

                for (int j = 0; j < values.length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(values[j]);
                    cell.setCellStyle(style);
                }
            }

            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setFontHeightInPoints((short) 11);
            summaryFont.setFontName("Calibri");
            summaryFont.setBold(true);
            summaryStyle.setFont(summaryFont);
            summaryStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summaryStyle.setAlignment(HorizontalAlignment.LEFT);


            Row totalRow = sheet.createRow(rowIndex++);
            Cell cell0 = totalRow.createCell(0);
            cell0.setCellValue(rowIndex);
            cell0.setCellStyle(summaryStyle);

            Cell cell1 = totalRow.createCell(1);
            cell1.setCellValue("Umumiy");
            cell1.setCellStyle(summaryStyle);

            Cell cell2 = totalRow.createCell(2);
            cell2.setCellValue(String.valueOf(totalCostOfContract));
            cell2.setCellStyle(summaryStyle);

            Cell cell3 = totalRow.createCell(3);
            cell3.setCellValue("");
            cell3.setCellStyle(summaryStyle);

            Cell cell4 = totalRow.createCell(4);
            cell4.setCellValue("");
            cell4.setCellStyle(summaryStyle);

            Cell cell5 = totalRow.createCell(5);
            cell5.setCellValue("");
            cell5.setCellStyle(summaryStyle);

            Cell cell6 = totalRow.createCell(6);
            cell6.setCellValue(String.valueOf(totalCostInUSD));
            cell6.setCellStyle(summaryStyle);

            Cell cell7 = totalRow.createCell(7);
            cell7.setCellValue("");
            cell7.setCellStyle(summaryStyle);

            Cell cell8 = totalRow.createCell(8);
            cell8.setCellValue(String.valueOf(totalCostInUZS));
            cell8.setCellStyle(summaryStyle);

            Cell cell9 = totalRow.createCell(9);
            cell9.setCellValue(String.valueOf(totalTax));
            cell9.setCellStyle(summaryStyle);

            Cell cell10 = totalRow.createCell(10);
            cell10.setCellValue(String.valueOf(totalAfterBank));
            cell10.setCellStyle(summaryStyle);

            Cell cell11 = totalRow.createCell(11);
            cell11.setCellValue("");
            cell11.setCellStyle(summaryStyle);

            Cell cell12 = totalRow.createCell(12);
            cell12.setCellValue(String.valueOf(totalAfterExpenses));
            cell12.setCellStyle(summaryStyle);

            Cell cell13 = totalRow.createCell(13);
            cell13.setCellValue("");
            cell13.setCellStyle(summaryStyle);

            Cell cell14 = totalRow.createCell(14);
            cell14.setCellValue("");
            cell14.setCellStyle(summaryStyle);


            Cell cell15 = totalRow.createCell(15);
            cell15.setCellValue(String.valueOf(totalProfit));
            cell15.setCellStyle(summaryStyle);

            Cell cell16 = totalRow.createCell(16);
            BigDecimal profitPercentage = totalProfit
                    .divide(totalAfterExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            cell16.setCellValue(String.valueOf(profitPercentage));
            cell16.setCellStyle(summaryStyle);

            Cell cell17 = totalRow.createCell(17);
            cell17.setCellValue("");
            cell17.setCellStyle(summaryStyle);


            Cell cell18 = totalRow.createCell(18);
            cell18.setCellValue("");
            cell18.setCellStyle(summaryStyle);


            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(currentWidth + 1000, 10000));
            }

            workbook.write(outputStream);
            byte[] xlsxBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(xlsxBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @PrePersist
    public void auditInsert(Document doc) {
        Map<String, Object[]> diff = new LinkedHashMap<>();
        extractFields(null, doc, diff);
        saveAudit(doc.getId(), getUsername(), "CREATE", diff);
    }

    @PreUpdate
    public void auditUpdate(Document doc, Document updated) {
        Map<String, Object[]> diffs = new LinkedHashMap<>();
        extractFields(doc, updated, diffs);
        saveAudit(doc.getId(), getUsername(), "UPDATE", diffs);
    }


    private void extractFields(Document oldDocument, Document newDocument, Map<String, Object[]> diff) {
        Object target = oldDocument != null ? oldDocument : newDocument;
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldVal = oldDocument != null ? field.get(oldDocument) : null;
                Object newVal = newDocument != null ? field.get(newDocument) : null;

                Class<?> type = field.getType();

                boolean isDifferent = false;

                if (type == BigDecimal.class) {
                    isDifferent = oldVal == null && newVal != null ||
                            oldVal != null && newVal == null ||
                            oldVal != null && ((BigDecimal) oldVal).compareTo((BigDecimal) newVal) != 0;
                } else if (type == Attachment.class) {
                    isDifferent = !Objects.equals(oldVal, newVal);
                } else if (type != LocalDateTime.class){
                    isDifferent = !Objects.equals(oldVal, newVal);
                }

                if (isDifferent) {
                    diff.put(field.getName(), new Object[]{oldVal, newVal});
                }
            } catch (IllegalAccessException ex) {
                log.error(ex.getMessage());
            }
        }
    }


    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void saveAudit(Long docId, String user, String operation, Map<String, Object[]> diffs) {
        DocumentChangeLog log = new DocumentChangeLog();
        log.setDocumentId(docId);
        log.setUsername(user);
        log.setTimestamp(LocalDateTime.now());
        log.setOperation(operation);

        try {
            String changes = new ObjectMapper().writeValueAsString(diffs);
            log.setChangesJson(changes);
        } catch (JsonProcessingException e) {
            log.setChangesJson("{\"error\": \"Could not serialize changes\"}");
            e.printStackTrace();
        }

        documentChangeLogRepository.save(log);
    }

    private Object deepCopy(Object entity) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(entity, entity.getClass());
    }


    public ApiResponse getDocumentById(Long id) {
        Optional<Document> documentOptional = documentRepository.findById(id);
        return documentOptional.map(document -> new ApiResponse("document", true, document))
                .orElseGet(() -> new ApiResponse("document not found", false));
    }

    private void mapDtoToDocument(DocumentDto documentDto, Document document) {
        document.setCompanyName(documentDto.getCompanyName());
        document.setCostOfContract(documentDto.getCostOfContract());
        document.setIsMoneyTransferred(documentDto.getIsMoneyTransferred());
        document.setIsMoneySent(documentDto.getIsMoneySent());
        document.setIsMoneyAccepted(documentDto.getIsMoneyAccepted());
        document.setCostInUSD(documentDto.getCostInUSD());

        LocalDateTime date = StaticMethods.stringToLocalDateTime(documentDto.getDate());
        if (date == null) {
            throw new IllegalArgumentException("Invalid date format");
        }
        document.setDate(date);

        Optional<BigDecimal> currencyOptional = StaticMethods.getCurrencyByDate(documentDto.getDate());
        if (currencyOptional.isEmpty()) {
            throw new IllegalArgumentException("Currency not found for the provided date");
        }
        BigDecimal currency = currencyOptional.get();
        if (documentDto.getCostInUSD() != null) {
            document.setCostInUZS(documentDto.getCostInUSD().multiply(currency));
            document.setExchangingRate(currency);
        }

        document.setTax(documentDto.getTax());
        document.setSummaAfterBankService(documentDto.getSummaAfterBankService());
        document.setAdditionalExpenseInPercentage(documentDto.getAdditionalExpenseInPercentage());
        document.setMoneyAfterExpenses(documentDto.getMoneyAfterExpenses());
        document.setLatNumber(documentDto.getLatNumber());
        document.setTransportExpenses(documentDto.getTransportExpenses());
        document.setProfit(documentDto.getProfit());
        document.setProfitInPercentage(documentDto.getProfitInPercentage());
        document.setProductName(documentDto.getProductName());
        document.setPhoneNumber(documentDto.getPhoneNumber());

        if (documentDto.getHashIdOfContract() != null) {
            Optional<Attachment> attachmentOfContractOptional = attachmentRepository.findByHashId(documentDto.getHashIdOfContract());
            attachmentOfContractOptional.ifPresentOrElse(
                    document::setContractAttachment,
                    () -> {
                        throw new IllegalArgumentException("Contract file not found");
                    }
            );
        }

        if (documentDto.getHashIdOfInvoice() != null) {
            Optional<Attachment> attachmentOfInvoiceOptional = attachmentRepository.findByHashId(documentDto.getHashIdOfInvoice());
            attachmentOfInvoiceOptional.ifPresentOrElse(
                    document::setInvoiceAttachment,
                    () -> {
                        throw new IllegalArgumentException("Invoice file not found");
                    }
            );
        }
    }
}
