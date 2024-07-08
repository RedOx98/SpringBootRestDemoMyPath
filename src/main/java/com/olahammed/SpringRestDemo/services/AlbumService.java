package com.olahammed.SpringRestDemo.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.olahammed.SpringRestDemo.models.Album;
import com.olahammed.SpringRestDemo.repositories.AlbumRepository;

@Service
public class AlbumService {
    
    @Autowired
    private AlbumRepository albumRepository;

    public Album save(Album album){
        return albumRepository.save(album);
    }

    public List<Album> findByAccount_id(Long id){
        return albumRepository.findByAccount_id(id);
    }

    public Optional<Album> findById(Long id){
        return albumRepository.findById(id);
    }
}
