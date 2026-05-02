package com.polybezev.shop.service;

import com.polybezev.shop.entity.Category;
import com.polybezev.shop.exception.BadRequestException;
import com.polybezev.shop.exception.ConflictException;
import com.polybezev.shop.exception.NotFoundException;
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
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    public Category create(Category request) {
        String name = request.getName() == null || request.getName().isBlank() ?
                null : request.getName().trim();

        if (name == null)
            throw new BadRequestException("Name must not be blank");

        if (name.length() > 50)
            throw new BadRequestException("Name must not exceed 50 characters");

        if (categoryRepository.existsByNameIgnoreCase(name))
            throw new ConflictException("Category with this name already exists");

        request.setName(name);
        return categoryRepository.save(request);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }
}
