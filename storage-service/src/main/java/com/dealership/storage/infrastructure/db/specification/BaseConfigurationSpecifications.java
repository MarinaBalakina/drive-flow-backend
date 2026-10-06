package com.dealership.storage.infrastructure.db.specification;

import com.dealership.storage.infrastructure.db.entity.BaseComponentOptionJpaEntity;
import com.dealership.storage.infrastructure.db.entity.BaseConfigurationJpaEntity;
import com.dealership.storage.infrastructure.db.entity.CarJpaEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class BaseConfigurationSpecifications {

    private BaseConfigurationSpecifications() {
    }

    public static Specification<BaseConfigurationJpaEntity> byFilters(
            String brand,
            Set<UUID> componentOptionIds
    ) {
        String normalizedBrand = normalizeBrand(brand);
        Set<UUID> normalizedOptionIds = normalizeOptionIds(componentOptionIds);

        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isFalse(root.get("removed")));
            predicates.add(cb.isFalse(root.get("carModel").get("removed")));

            if (normalizedBrand != null) {
                predicates.add(hasBrand(root, query.subquery(Integer.class), normalizedBrand, cb));
            }

            if (!normalizedOptionIds.isEmpty()) {
                predicates.add(hasAllComponentOptions(root, query.subquery(Long.class), normalizedOptionIds, cb));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate hasBrand(
            Root<BaseConfigurationJpaEntity> root,
            Subquery<Integer> subquery,
            String normalizedBrand,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        Root<CarJpaEntity> car = subquery.from(CarJpaEntity.class);
        subquery.select(cb.literal(1));
        subquery.where(
                cb.equal(car.get("carModel").get("id"), root.get("carModel").get("id")),
                cb.isFalse(car.get("removed")),
                cb.equal(cb.lower(car.get("brand")), normalizedBrand)
        );
        return cb.exists(subquery);
    }

    private static Predicate hasAllComponentOptions(
            Root<BaseConfigurationJpaEntity> root,
            Subquery<Long> subquery,
            Set<UUID> componentOptionIds,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        Root<BaseComponentOptionJpaEntity> option = subquery.from(BaseComponentOptionJpaEntity.class);
        subquery.select(cb.countDistinct(option.get("componentOption").get("id")));
        subquery.where(
                cb.equal(option.get("baseConfiguration").get("id"), root.get("id")),
                cb.isFalse(option.get("componentOption").get("removed")),
                option.get("componentOption").get("id").in(componentOptionIds)
        );
        return cb.equal(subquery, (long) componentOptionIds.size());
    }

    private static String normalizeBrand(String brand) {
        if (brand == null) {
            return null;
        }
        String value = brand.trim();
        if (value.isEmpty()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private static Set<UUID> normalizeOptionIds(Set<UUID> componentOptionIds) {
        if (componentOptionIds == null || componentOptionIds.isEmpty()) {
            return Set.of();
        }
        Set<UUID> normalized = new LinkedHashSet<>();
        for (UUID id : componentOptionIds) {
            if (id != null) {
                normalized.add(id);
            }
        }
        return normalized;
    }
}
