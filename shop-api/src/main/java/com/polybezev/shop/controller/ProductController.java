package com.polybezev.shop.controller;

import com.polybezev.shop.dto.request.ProductRequest;
import com.polybezev.shop.dto.response.ProductResponse;
import com.polybezev.shop.entity.Category;
import com.polybezev.shop.entity.Product;
import com.polybezev.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProductResponse> page = productService.getAll(categoryId, pageable)
                .map(p -> {
                    ProductResponse r = new ProductResponse();
                    r.setId(p.getId());
                    r.setName(p.getName());
                    r.setDescription(p.getDescription());
                    r.setPrice(p.getPrice());
                    r.setStock(p.getStock());
                    r.setCategoryId(p.getCategory().getId());
                    r.setCategoryName(p.getCategory().getName());
                    return r;
                });
        return ResponseEntity.ok(page);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Category category = new Category();
        category.setId(request.getCategoryId());
        product.setCategory(category);

        Product saved = productService.create(product);

        ProductResponse response = new ProductResponse();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        response.setPrice(saved.getPrice());
        response.setStock(saved.getStock());
        response.setCategoryId(saved.getCategory().getId());
        response.setCategoryName(saved.getCategory().getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id,
                                                   @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setId(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product updated = productService.update(product);

        ProductResponse response = new ProductResponse();
        response.setId(updated.getId());
        response.setName(updated.getName());
        response.setDescription(updated.getDescription());
        response.setPrice(updated.getPrice());
        response.setStock(updated.getStock());
        response.setCategoryId(updated.getCategory().getId());
        response.setCategoryName(updated.getCategory().getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
