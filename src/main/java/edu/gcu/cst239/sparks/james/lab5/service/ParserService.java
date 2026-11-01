package edu.gcu.cst239.sparks.james.lab5.service;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.gcu.cst239.sparks.james.lab5.model.Person;

public class ParserService {
    private final DataService dataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ParserService(DataService dataService) {
        this.dataService = dataService;
    }

    public String processMessage(String message) {
        if (message == null || message.isBlank()) {
            return error("Command is required");
        }

        String[] parts = message.trim().split("\s+");
        String command = parts[0].toUpperCase();

        try {
            return switch (command) {
                case "ALL" -> json(dataService.getPeople());
                case "GET" -> processGet(parts);
                case "ADD" -> processAdd(parts);
                case "DELETE" -> processDelete(parts);
                case "FIND" -> processFind(parts);
                default -> error("Unknown command");
            };
        } catch (IllegalArgumentException ex) {
            return error(ex.getMessage());
        }
    }

    private String processGet(String[] parts) {
        requireLength(parts, 2, "Usage: GET id");
        Person person = dataService.getPerson(parseInt(parts[1], "Invalid id"));
        return json(person == null ? List.of() : List.of(person));
    }

    private String processAdd(String[] parts) {
        requireLength(parts, 4, "Usage: ADD id name age");
        Person person = new Person(
                parseInt(parts[1], "Invalid id"),
                parts[2],
                parseInt(parts[3], "Invalid age"));
        if (!dataService.addPerson(person)) {
            return error("Duplicate id");
        }
        return json(List.of(person));
    }

    private String processDelete(String[] parts) {
        requireLength(parts, 2, "Usage: DELETE id");
        Person removed = dataService.deletePerson(parseInt(parts[1], "Invalid id"));
        return json(removed == null ? List.of() : List.of(removed));
    }

    private String processFind(String[] parts) {
        // STUDENT WORK: validate FIND name and return matching people as JSON.
        requireLength(parts, 2, "Usage: FIND name");
        return json(dataService.findByName(parts[1]));
    }

    private int parseInt(String text, String errorMessage) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void requireLength(String[] parts, int length, String message) {
        if (parts.length != length) {
            throw new IllegalArgumentException(message);
        }
    }

    private String json(List<Person> people) {
        try {
            return objectMapper.writeValueAsString(people);
        } catch (JsonProcessingException ex) {
            return error("Unable to create JSON");
        }
    }

    private String error(String message) {
        try {
            return objectMapper.writeValueAsString(List.of("ERROR: " + message));
        } catch (JsonProcessingException ex) {
            return "[\"ERROR\"]";
        }
    }
}
