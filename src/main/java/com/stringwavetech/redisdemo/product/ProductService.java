package com.stringwavetech.redisdemo.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product getProduct(UUID id) {

        // 1. Check Redis
        Product cachedProduct = productCacheService.get(id);

        if (cachedProduct != null) {
            System.out.println("CACHE HIT");
            return cachedProduct;
        }

        // 2. Redis miss → PostgreSQL
        System.out.println("CACHE MISS");

        Product product = productRepository.findById(id)
                .orElseThrow();

        // 3. Save in Redis
        productCacheService.save(product);

        return product;
    }
}