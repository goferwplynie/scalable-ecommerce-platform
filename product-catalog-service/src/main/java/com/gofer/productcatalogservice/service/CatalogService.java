package com.gofer.productcatalogservice.service;

import com.gofer.productcatalogservice.entity.Category;
import com.gofer.productcatalogservice.entity.Product;
import com.gofer.productcatalogservice.exceptions.CategoryNotFoundException;
import com.gofer.productcatalogservice.exceptions.ProductNotFoundException;
import com.gofer.productcatalogservice.repository.CategoryRepository;
import com.gofer.productcatalogservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Category addCategory(String categoryName) {
        Category category = Category.builder()
                .name(categoryName)
                .build();
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Product createProduct(String name, UUID sellerId, String description, BigDecimal price, int stockQuantity, String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + categoryName));

        Product product = Product.builder()
                .name(name)
                .sellerId(sellerId)
                .description(description)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build();

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<Product> listProducts(String categoryName, int offset, int limit) {
        int pageNumber = (limit > 0) ? (offset / limit) : 0;
        Pageable pageable = PageRequest.of(pageNumber, limit);

        if (categoryName == null || categoryName.isEmpty()) {
            return productRepository.findAll(pageable);
        } else {
            return productRepository.findByCategory_Name(categoryName, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Product getProductById(UUID productId) throws ProductNotFoundException {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }

    @Transactional(readOnly = true)
    public boolean checkAvailability(UUID productId, int requestedQuantity) {
        Product product = getProductById(productId);
        return product.getStockQuantity() >= requestedQuantity;
    }

    @Transactional
    public void updateStock(UUID productId, UUID sellerId, int newStockQuantity) {
        if (newStockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found or seller does not own the product: " + productId));

        Product product = getProductById(productId);
        product.setStockQuantity(newStockQuantity);
    }
}
