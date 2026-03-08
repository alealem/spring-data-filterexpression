package com.example.demo.repositories;

import com.example.demo.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {
//    @Query("""
//        select distinct c
//        from Content c
//        left join fetch c.items
//        where c.id = :id
//    """)
    Optional<Content> findWithItemsById(UUID id);
}