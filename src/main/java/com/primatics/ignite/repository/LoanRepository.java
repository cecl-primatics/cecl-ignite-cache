package com.primatics.ignite.repository;

import java.io.Serializable;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.primatics.ignite.dto.Loan;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String>, Serializable {

}