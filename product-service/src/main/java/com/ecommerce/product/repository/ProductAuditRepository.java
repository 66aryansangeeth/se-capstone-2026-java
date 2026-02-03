package com.ecommerce.product.repository;

import com.ecommerce.product.entity.ProductAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductAuditRepository extends JpaRepository<ProductAudit, Long> {
}
