package com.challenge.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.service.EmployeeService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the employee REST API using MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllEmployeesShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createEmployeeShouldReturnCreatedEmployee() throws Exception {
        String requestBody =
                """
                {
                    "firstName": "Sneha",
                    "lastName": "Patel",
                    "salary": 85000,
                    "age": 23,
                    "jobTitle": "Software Engineer",
                    "email": "sneha@example.com",
                    "contractHireDate": "2026-09-21T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.firstName").value("Sneha"))
                .andExpect(jsonPath("$.lastName").value("Patel"))
                .andExpect(jsonPath("$.fullName").value("Sneha Patel"))
                .andExpect(jsonPath("$.salary").value(85000))
                .andExpect(jsonPath("$.age").value(23));
    }

    @Test
    void getEmployeeByUuidShouldReturnEmployee() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Sneha");
        request.setLastName("Patel");
        request.setSalary(85000);
        request.setAge(23);
        request.setJobTitle("Software Engineer");
        request.setEmail("sneha@example.com");
        request.setContractHireDate(Instant.parse("2026-09-21T00:00:00Z"));

        Employee employee = employeeService.createEmployee(request);

        mockMvc.perform(get("/api/v1/employee/{uuid}", employee.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(employee.getUuid().toString()))
                .andExpect(jsonPath("$.firstName").value("Sneha"))
                .andExpect(jsonPath("$.lastName").value("Patel"))
                .andExpect(jsonPath("$.fullName").value("Sneha Patel"));
    }

    @Test
    void getEmployeeByUuidShouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {

        UUID nonexistentUuid = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/employee/{uuid}", nonexistentUuid)).andExpect(status().isNotFound());
    }

    @Test
    void createEmployeeShouldReturnBadRequestWhenFirstNameIsMissing() throws Exception {

        String requestBody =
                """
                {
                    "lastName": "Patel",
                    "salary": 85000,
                    "age": 23,
                    "jobTitle": "Software Engineer",
                    "email": "sneha@example.com",
                    "contractHireDate": "2026-09-21T00:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployeeShouldRemoveEmployee() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Sneha");
        request.setLastName("Patel");
        request.setSalary(85000);
        request.setAge(23);
        request.setJobTitle("Software Engineer");
        request.setEmail("sneha@example.com");
        request.setContractHireDate(Instant.parse("2026-09-21T00:00:00Z"));

        Employee employee = employeeService.createEmployee(request);

        mockMvc.perform(delete("/api/v1/employee/{uuid}", employee.getUuid())).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/employee/{uuid}", employee.getUuid())).andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployeeShouldReturnNotFoundWhenEmployeeDoesNotExist() throws Exception {

        UUID nonexistentUuid = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/employee/{uuid}", nonexistentUuid)).andExpect(status().isNotFound());
    }

    @Test
    void searchEmployeesShouldReturnMatchingEmployees() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Sneha");
        request.setLastName("Patel");
        request.setSalary(85000);
        request.setAge(23);
        request.setJobTitle("Software Engineer");
        request.setEmail("sneha@example.com");
        request.setContractHireDate(Instant.parse("2026-09-21T00:00:00Z"));

        employeeService.createEmployee(request);

        mockMvc.perform(get("/api/v1/employee/search").param("name", "Sneha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("Sneha"))
                .andExpect(jsonPath("$[0].lastName").value("Patel"));
    }

    @Test
    void searchEmployeesShouldBeCaseInsensitiveAndAllowPartialNames() throws Exception {

        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName("Sneha");
        request.setLastName("Patel");
        request.setSalary(85000);
        request.setAge(23);
        request.setJobTitle("Software Engineer");
        request.setEmail("sneha@example.com");
        request.setContractHireDate(Instant.parse("2026-09-21T00:00:00Z"));

        employeeService.createEmployee(request);

        mockMvc.perform(get("/api/v1/employee/search").param("name", "sneh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].fullName").value("Sneha Patel"));
    }

    @Test
    void searchEmployeesShouldReturnBadRequestWhenNameIsMissing() throws Exception {

        mockMvc.perform(get("/api/v1/employee/search")).andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeeByUuidShouldReturnBadRequestForInvalidUuid() throws Exception {

        mockMvc.perform(get("/api/v1/employee/not-a-uuid")).andExpect(status().isBadRequest());
    }
}
