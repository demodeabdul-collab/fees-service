package com.intermidateproject.fees.fees_service.repository;

import com.intermidateproject.fees.fees_service.entity.Invoice;
import com.intermidateproject.fees.fees_service.entity.Student;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {


}
