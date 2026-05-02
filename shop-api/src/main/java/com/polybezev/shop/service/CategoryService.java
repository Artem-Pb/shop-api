package com.polybezev.shop.service;

import com.polybezev.shop.entity.Category;
import com.polybezev.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found!"));
    }

    public Category create(Category request) {
        String name = request.getName() == null || request.getName().isBlank() ?
                null : request.getName().trim();

        if (name == null)
            throw new RuntimeException("Name is blank! Plz, return");

        if (name.length() > 50) {
            throw new RuntimeException("Over length!");
        }

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("No uniquer name. Plz, return");
        }

        request.setName(name);

        return categoryRepository.save(request);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }
}
