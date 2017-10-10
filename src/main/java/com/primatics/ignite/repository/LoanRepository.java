package com.primatics.ignite.repository;

import java.io.Serializable;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import com.primatics.ignite.dto.Loan;

@Component
public interface LoanRepository extends MongoRepository<Loan, String>, LoanRepositoryCustom, Serializable {
	
}