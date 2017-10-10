package com.primatics.ignite.repository;

import java.util.HashSet;
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
				
		List<Integer> keylist = mongoTemplate.getCollection("loans").distinct("key");
		
		Set set = new HashSet(keylist);
		
		return set;
	}
}
