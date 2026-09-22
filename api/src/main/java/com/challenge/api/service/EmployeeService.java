package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Contains employee business logic and manages the in-memory employee store.
 */
@Service
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    // employees are keyed by UUID for efficient lookup and deletion.
    private final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees.values());
    }

    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = employees.get(uuid);

        if (employee == null) {
            logger.warn("Employee not found for UUID {}", uuid);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }

        return employee;
    }

    public void deleteEmployee(UUID uuid) {
        Employee employee = employees.remove(uuid);

        if (employee == null) {
            logger.warn("Cannot delete employee: UUID {} was not found", uuid);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        }

        logger.info("Deleted employee with UUID {}", uuid);
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

        if (requestBody.getJobTitle() == null || requestBody.getJobTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job title is required");
        }

        if (requestBody.getEmail() == null || requestBody.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (requestBody.getContractHireDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract hire date is required");
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

        employees.put(employee.getUuid(), employee);

        logger.info("Created employee with UUID {}", employee.getUuid());

        return employee;
    }

    public List<Employee> searchEmployees(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search name is required");
        }

        List<Employee> results = new ArrayList<>();

        // Search first, last, and full names using case-insensitive partial matching.
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
