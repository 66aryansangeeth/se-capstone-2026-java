package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.SearchRequest;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductAudit;
import com.ecommerce.product.repository.ProductAuditRepository;
import com.ecommerce.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductAuditRepository auditRepository;

    public void createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(request.category())
                .build();
        Product savedproduct = productRepository.save(product);
        saveAudit(savedproduct.getId(), "CREATE");
    }

    public void updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(request.category());

        productRepository.save(product);

        saveAudit(id, "UPDATE");
    }

    public void deleteProduct(Long id) {
        if(!productRepository.existsById(id)) {
            throw new RuntimeException("Product doesn't exist");
        }

        productRepository.deleteById(id);

        saveAudit(id, "DELETE");
    }

    private void saveAudit(Long productId, String action) {
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        ProductAudit audit = ProductAudit.builder()
                .productId(productId)
                .action(action)
                .adminEmail(adminEmail)
                .timestamp(LocalDateTime.now())
                .build();

        auditRepository.save(audit);
    }

    public List<ProductResponse> getProducts(String category) {
        List<Product> products;

        if (category != null && !category.isEmpty()) {
            products = productRepository.findByCategoryIgnoreCase(category);
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToProductResponse)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    public List<ProductResponse> searchProducts(SearchRequest request) {
        List<Product> products;

        String name = request.name();
        String category = request.category();

        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasCategory = category != null && !category.trim().isEmpty();

        if (hasName && hasCategory) {
            products = productRepository.findByNameContainingIgnoreCaseAndCategoryIgnoreCase(
                    name.trim(), category.trim());
        } else if (hasName) {
            products = productRepository.findByNameContainingIgnoreCase(name.trim());
        } else if (hasCategory) {
            products = productRepository.findByCategoryIgnoreCase(category.trim());
        } else {
            products = productRepository.findAll();
        }

        return products.stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    @Transactional
    public void reduceStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + id);
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                product.getStockQuantity() != null && product.getStockQuantity() > 0
        );
    }
}
