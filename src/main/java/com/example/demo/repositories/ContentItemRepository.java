package com.example.demo.repositories;



import com.example.demo.domain.ContentItem;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContentItemRepository extends JpaRepository<ContentItem, UUID>, JpaSpecificationExecutor<ContentItem> {

    @Query(
            value = """
        select *
        from content_items ci
        where ci.content_id = :contentId
          and (:q is null or :q = '' or ci.body_tsv @@ websearch_to_tsquery('simple', :q))
        order by ci.id desc
      """,
            countQuery = """
        select count(*)
        from content_items ci
        where ci.content_id = :contentId
          and (:q is null or :q = '' or ci.body_tsv @@ websearch_to_tsquery('simple', :q))
      """,
            nativeQuery = true
    )
    Page<ContentItem> search(@Param("contentId") UUID contentId, @Param("q") String q, Pageable pageable);
}