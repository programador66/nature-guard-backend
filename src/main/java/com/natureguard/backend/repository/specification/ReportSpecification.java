package com.natureguard.backend.repository.specification;

import com.natureguard.backend.domain.model.Report;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class ReportSpecification {

    private ReportSpecification() {}

    public static Specification<Report> hasTags(List<String> tags) {
        return (root, query, cb) -> {
            if (tags == null || tags.isEmpty()) {
                return cb.conjunction();
            }
            var tagsJoin = root.join("tags", JoinType.INNER);
            return tagsJoin.in(tags);
        };
    }

    public static Specification<Report> createdAfter(LocalDateTime startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
        };
    }

    public static Specification<Report> createdBefore(LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    public static Specification<Report> searchByText(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Report> userIdIn(List<String> userEmails) {
        return (root, query, cb) -> {
            if (userEmails == null || userEmails.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("userId").in(userEmails);
        };
    }

    public static Specification<Report> searchByTextOrUser(String search, List<String> matchingUserEmails) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + search.toLowerCase() + "%";

            var titleMatch = cb.like(cb.lower(root.get("title")), pattern);
            var descriptionMatch = cb.like(cb.lower(root.get("description")), pattern);

            if (matchingUserEmails != null && !matchingUserEmails.isEmpty()) {
                var userMatch = root.get("userId").in(matchingUserEmails);
                return cb.or(titleMatch, descriptionMatch, userMatch);
            }

            return cb.or(titleMatch, descriptionMatch);
        };
    }

    public static Specification<Report> withFilters(List<String> tags, LocalDateTime startDate, LocalDateTime endDate,
                                                     String search, List<String> matchingUserEmails) {
        return Specification
                .where(hasTags(tags))
                .and(createdAfter(startDate))
                .and(createdBefore(endDate))
                .and(searchByTextOrUser(search, matchingUserEmails));
    }
}

