package com.example.demo.repositories;

import java.util.UUID;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.example.demo.domain.ContentItem;

public interface ContentItemRepository
    extends JpaRepository<ContentItem, UUID>, JpaSpecificationExecutor<ContentItem> {

  @Query(
      value =
          """
            select ci.*
            from content_items ci
            where ci.content_id = :contentId
              and (
                    coalesce(trim(:q), '') = ''
                    or ci.body_tsv @@ websearch_to_tsquery('simple', trim(:q))
                  )
            order by
              case
                when coalesce(trim(:q), '') = '' then 0
                else ts_rank(ci.body_tsv, websearch_to_tsquery('simple', trim(:q)))
              end desc,
              ci.id desc
        """,
      countQuery =
          """
            select count(*)
            from content_items ci
            where ci.content_id = :contentId
              and (
                    coalesce(trim(:q), '') = ''
                    or ci.body_tsv @@ websearch_to_tsquery('simple', trim(:q))
                  )
        """,
      nativeQuery = true)
  Page<ContentItem> search(
      @Param("contentId") UUID contentId, @Param("q") String q, Pageable pageable);
}
