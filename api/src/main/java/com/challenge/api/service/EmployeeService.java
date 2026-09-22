package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Contains employee business logic and manages the in-memory employee store.
 */
@Service
public class EmployeeService {
    // employees are keyed by UUID for efficient lookup and deletion.
    private final Map<UUID, Employee> employees =
            new ConcurrentHashMap<>(); // in-memory storage is sufficient because persistence is not required.

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employees.get(uuid);

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
        return employee;
    }

    public void deleteEmployee(UUID uuid) {
        Employee employee = employees.remove(uuid); // Delete the employee

        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }
    }

    public Employee createEmployee(CreateEmployeeRequest requestBody) {
        // validate client-provided fields before creating the employee.
        if (requestBody.getFirstName() == null || requestBody.getFirstName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name is required");
        }

        if (requestBody.getLastName() == null || requestBody.getLastName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last name is required");
        }

        if (requestBody.getSalary() == null || requestBody.getSalary() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be zero or greater");
        }

        if (requestBody.getAge() == null || requestBody.getAge() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be greater than zero");
        }
        // UUID and full name are controlled by the service rather than the client.
        EmployeeImpl employee = new EmployeeImpl();

        employee.setUuid(UUID.randomUUID());
        employee.setFirstName(requestBody.getFirstName());
        employee.setLastName(requestBody.getLastName());
        employee.setFullName(requestBody.getFirstName() + " " + requestBody.getLastName());
        employee.setSalary(requestBody.getSalary());
        employee.setAge(requestBody.getAge());
        employee.setJobTitle(requestBody.getJobTitle());
        employee.setEmail(requestBody.getEmail());
        employee.setContractHireDate(requestBody.getContractHireDate());
        employee.setContractTerminationDate(requestBody.getContractTerminationDate());

        employees.put(
                employee.getUuid(), employee); // store the completed employee using its generated UUID as the key.

        return employee;
    }

    public List<Employee> searchEmployees(String name) {
        List<Employee> results = new ArrayList<>();
        // search first, last, and full names using case-insensitive partial matching.
        for (Employee employee : employees.values()) {
            if (employee.getFirstName().toLowerCase().contains(name.toLowerCase())
                    || employee.getLastName().toLowerCase().contains(name.toLowerCase())
                    || employee.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(employee);
            }
        }
        return results;
    }
}
