package com.intermidateproject.fees.fees_service.repository;

import com.intermidateproject.fees.fees_service.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String>
{

}
