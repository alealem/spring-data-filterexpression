package com.example.demo.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.domain.Content;

public interface ContentRepository
    extends JpaRepository<Content, UUID>, JpaSpecificationExecutor<Content> {
  //    @Query("""
  //        select distinct c
  //        from Content c
  //        left join fetch c.items
  //        where c.id = :id
  //    """)
  Optional<Content> findWithItemsById(UUID id);
}
