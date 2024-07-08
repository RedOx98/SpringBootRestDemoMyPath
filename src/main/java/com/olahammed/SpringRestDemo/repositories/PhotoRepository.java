package com.olahammed.SpringRestDemo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.olahammed.SpringRestDemo.models.Photo;


@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
}
