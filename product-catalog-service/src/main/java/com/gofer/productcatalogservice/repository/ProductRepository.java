package com.gofer.productcatalogservice.repository;

import com.gofer.productcatalogservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByCategory_Name(String categoryName, Pageable pageable);

    Optional<Product> findByIdAndSellerId(UUID productId, UUID sellerId);
}
