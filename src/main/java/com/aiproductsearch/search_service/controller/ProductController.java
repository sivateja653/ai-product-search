package com.aiproductsearch.search_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import com.aiproductsearch.search_service.model.Product;
import com.aiproductsearch.search_service.repository.ProductRespository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

	@Autowired
	ProductRespository productrepo;
	
	//private static Logger log = LoggerFactory.getLogger(ProductController.class);
	
	@GetMapping("")
	public List<Product> getProducts(){
		
		var products = productrepo.findAll();
		log.info("Found {} products: {}", products.size(), products);
	    return products;
	}
	
	
}
