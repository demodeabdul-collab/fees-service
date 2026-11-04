package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.entity.*;
import com.intermidateproject.fees.fees_service.exception.ResourceNotFoundException;
import com.intermidateproject.fees.fees_service.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {
  private final StudentRepository studentRepository;
  public StudentService(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  public Student createStudent(String name, String email){
      Student student = new Student(name,email);

      return studentRepository.save(student);
  }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

  public List<Student> getAllStudents(){
    return studentRepository.findAll();
  }
}
