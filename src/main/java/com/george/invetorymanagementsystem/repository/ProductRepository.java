package com.george.invetorymanagementsystem.repository;

import com.george.invetorymanagementsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
