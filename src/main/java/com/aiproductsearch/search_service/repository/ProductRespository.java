package com.aiproductsearch.search_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aiproductsearch.search_service.model.Product;

public interface ProductRespository extends JpaRepository<Product, UUID>{

	/*@Query( nativeQuery = true, value="SELECT category FROM products;")
	List<String> findCategory();*/
	
}
