package com.polybezev.shop.repository;

import com.polybezev.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStockGreaterThan(Integer stock, Pageable pageable);
    Page<Product> findByCategoryIdAndStockGreaterThan(Long categoryId,
                                                      Integer stock,
                                                      Pageable pageable);
}
