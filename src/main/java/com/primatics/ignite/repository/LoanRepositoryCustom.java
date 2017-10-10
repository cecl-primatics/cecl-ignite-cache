package com.primatics.ignite.repository;

import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public interface LoanRepositoryCustom {
	
	public Set<Integer> getKeys();

}
