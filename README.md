# Audit System Project

This project implements an **audit logging system** for tracking changes made to entities in a Spring Boot application. It records the modifications (including the old and new values) and stores them in an audit log. This is particularly useful for compliance, debugging, and understanding how data evolves over time.

---

## Features

- **Track updates** to entities in the application (such as `Document`).
- Record **old and new values** for each modified field.
- Store audit logs in the database for tracking purposes.
- **Customizable** to include different entities and fields.
- Centralized **error handling** for audit-related processes.

---

## Tech Stack

The project uses the following technologies:

- **Java** (version 8 or later)
- **Spring Boot** (version 2.x)
- **Spring Data JPA** (for interacting with the database)
- **PostgreSQL** or **MySQL** (database for storing audit logs)
- **Lombok** (for reducing boilerplate code)
- **MapStruct** or **ModelMapper** (optional, for DTO-to-entity mapping)
- **Junit** (for testing)
- **Slf4j/Logback** (for logging)

---

## Setup

### 1. Clone the Repository

To clone the repository, run:

```bash
git clone https://github.com/AbrorbekAllaberganov/documentation.git
cd audit-project
```

### 2. Configure Database
Make sure to configure your database connection in application.properties (or application.yml), for example:
```bash
spring.datasource.url=jdbc:postgresql://localhost:5432/your_db_name
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update
```

## Audit Logic

### Overview

The audit system is designed to capture the changes made to `Document` entities. The changes are tracked using two main hooks:
- **`@PrePersist`** for capturing **create** actions.
- **`@PreUpdate`** for capturing **update** actions.

---

### Key Methods

#### 1. `auditInsert(Document doc)`

This method is triggered before an entity is persisted (i.e., when a new `Document` is created). It collects the current field values of the new `Document` and saves them in the audit log.

```java
@PrePersist
public void auditInsert(Document doc) {
    Map<String, Object[]> diff = new LinkedHashMap<>();
    extractFields(null, doc, diff); // Extract fields for a new document (no old value)
    saveAudit(doc.getId(), getUsername(), "CREATE", diff); // Save audit log with "CREATE" action
}
```

Parameters:

- doc: The Document entity being created.

Functionality:
- Calls extractFields to collect differences (for a new entity, there are no old values, so only new values are recorded).
- Saves the audit log with action type "CREATE".

### 2. `auditUpdate(Document doc, Document updated)`
This method is triggered before an entity is updated (i.e., when an existing Document is modified). It compares the old values of the Document with the updated ones and logs the differences.

```java
@PreUpdate
public void auditUpdate(Document doc, Document updated) {
    Map<String, Object[]> diffs = new LinkedHashMap<>();
    extractFields(doc, updated, diffs); // Extract fields comparing old and new values
    saveAudit(doc.getId(), getUsername(), "UPDATE", diffs); // Save audit log with "UPDATE" action
}
```
Parameters:

- doc: The existing Document entity before the update.

- updated: The updated Document entity.

Functionality:

- Calls extractFields to compare old and new values of fields.

- Saves the audit log with action type "UPDATE".

### 3. `extractFields(Document oldDocument, Document newDocument, Map<String, Object[]> diff)`
This helper method compares the fields of the old and new Document entities and identifies the differences.

```java
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
            } else if (type != LocalDateTime.class) {
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
```

Parameters:

- oldDocument: The previous version of the Document (null for a new entity).

- newDocument: The updated Document (null for an existing entity).

- diff: A map that will hold the differences between the old and new values.

Functionality:

- Iterates through each field of the Document and compares old and new values.

- If a field has changed (e.g., a BigDecimal value is different or a Attachment has changed), the difference is logged in the diff map.


# Contact
If you have any questions or issues, feel free to open an issue or contact me directly at abror.developer@gmail.com.
