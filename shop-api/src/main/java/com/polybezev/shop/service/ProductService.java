package com.polybezev.shop.service;

import com.polybezev.shop.entity.Category;
import com.polybezev.shop.entity.Product;
import com.polybezev.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Page<Product> getAll(Long categoryId,Pageable page) {
        if (categoryId == null)
            return productRepository.findByStockGreaterThan(0, page);

        categoryService.getById(categoryId);

        return productRepository.findByCategoryIdAndStockGreaterThan(categoryId, 0, page);
    }

    public Product create(Product request) {
        String name = request.getName() == null || request.getName().isBlank() ?
                null : request.getName().trim();
        if (name == null)
            throw new RuntimeException("Name is empty. Plz, try again");

        request.setName(name);

        if (request.getPrice() == null)
            throw new RuntimeException("Prise must be not empty!");

        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Price must be greater than 0");

        if (request.getStock() == null)
            throw new RuntimeException("Stock is empty!");

        if (request.getStock() <= 0)
            throw new RuntimeException("Stock must be greater than 0");

        if (request.getCategory() == null)
            throw new RuntimeException("Category is empty!");

        Category category = categoryService.getById(request.getCategory().getId());

        request.setCategory(category);

        return productRepository.save(request);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        productRepository.delete(product);
    }

    @Transactional
    public Product update(Product request) {
        if (request.getId() == null)
            throw new RuntimeException("Id is empty!");
        Product product = getById(request.getId());

        if (request.getName() != null) {
            if (request.getName().isBlank())
                throw new RuntimeException("Name is empty. Plz, try again");
            product.setName(request.getName());
        }

        if (request.getDescription() != null)
            product.setDescription(request.getDescription());

        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
                throw new RuntimeException("Price must be greater than 0");
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            if (request.getStock() <= 0)
                throw new RuntimeException("Stock must be greater than 0");
            product.setStock(request.getStock());
        }

        return productRepository.save(product);
    }
}
