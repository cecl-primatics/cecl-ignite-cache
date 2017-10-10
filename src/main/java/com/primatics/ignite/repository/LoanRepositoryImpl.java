package com.primatics.ignite.repository;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LoanRepositoryImpl implements LoanRepositoryCustom {

	@Autowired
	MongoTemplate mongoTemplate;
	
	public Set<Integer> getKeys() {
				
		return (Set<Integer>) mongoTemplate.getCollection("loans").distinct("key");
	}
}
