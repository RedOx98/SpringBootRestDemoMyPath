package com.olahammed.SpringRestDemo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.olahammed.SpringRestDemo.models.Photo;
import com.olahammed.SpringRestDemo.repositories.PhotoRepository;

@Service
// @RequiredArgsConstructor
public class PhotoService {
    
    @Autowired
    private PhotoRepository photoRepository;

    public Photo save(Photo photo){
        return photoRepository.save(photo);
    }

    public Optional<Photo> findById(Long id){
        return photoRepository.findById(id);
    }
}
