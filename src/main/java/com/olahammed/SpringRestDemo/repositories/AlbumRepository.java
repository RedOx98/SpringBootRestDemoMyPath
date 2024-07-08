package com.olahammed.SpringRestDemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.olahammed.SpringRestDemo.models.Album;
import java.util.List;


@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByAccount_id(Long id);
}
