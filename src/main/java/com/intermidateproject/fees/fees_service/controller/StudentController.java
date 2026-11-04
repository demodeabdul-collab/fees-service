package com.intermidateproject.fees.fees_service.controller;

import com.intermidateproject.fees.fees_service.dto.CreateStudentRequest;
import com.intermidateproject.fees.fees_service.entity.Student;
import com.intermidateproject.fees.fees_service.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private  final StudentService studentService;
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody CreateStudentRequest request) {
           Student savedStudent = studentService.createStudent(request.getName(), request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedStudent);
    }

    @GetMapping("/{id}")
    public ResponseEntity <Student> getStudent(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        return  ResponseEntity.ok(student);
    }

    @GetMapping
    public ResponseEntity <List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);

    }

}
