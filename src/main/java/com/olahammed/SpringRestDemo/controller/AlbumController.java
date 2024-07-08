package com.olahammed.SpringRestDemo.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import com.olahammed.SpringRestDemo.models.Account;
import com.olahammed.SpringRestDemo.models.Album;
import com.olahammed.SpringRestDemo.models.Photo;
import com.olahammed.SpringRestDemo.payload.auth.album.AlbumPayloadDTO;
import com.olahammed.SpringRestDemo.payload.auth.album.AlbumViewDTO;
import com.olahammed.SpringRestDemo.services.AccountService;
import com.olahammed.SpringRestDemo.services.AlbumService;
import com.olahammed.SpringRestDemo.services.PhotoService;
import com.olahammed.SpringRestDemo.util.AppUtils.AppUtils;
import com.olahammed.SpringRestDemo.util.constants.AlbumError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@Tag(name = "Album Controller", description = "Controller for album and photo management")
// @RequiredArgsConstructor
public class AlbumController {

    static final String PHOTOS_FOLDER_NAME = "photos";
    static final String THUMBNAIL_FOLDER_NAME = "thumbnails";
    static final int THUMBNAIL_WIDTH = 300;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PhotoService photoService;

    @PostMapping(value = "/albums/add", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "Please add valid name a description")
    @ApiResponse(responseCode = "201", description = "Account added")
    @Operation(summary = "Add an Album")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<AlbumViewDTO> addAlbum(@Valid @RequestBody AlbumPayloadDTO albumPayloadDTO,
            Authentication authentication) {
        try {
            Album album = new Album();
            album.setName(albumPayloadDTO.getName());
            album.setDescription(albumPayloadDTO.getDescription());
            String email = authentication.getName();
            System.out.println(email);
            Optional<Account> optionalAccount = accountService.findByEmail(email);
            Account account = optionalAccount.get();
            album.setAccount(account);
            album = albumService.save(album);
            AlbumViewDTO albumViewDTO = new AlbumViewDTO(album.getId(), album.getName(), album.getDescription());
            return ResponseEntity.ok(albumViewDTO);
        } catch (Exception e) {
            log.debug(AlbumError.ADD_ALBUM_ERROR.toString() + ": " + e.getMessage());
            // return new ResponseEntity<AlbumViewDTO>(new AlbumViewDTO(),
            // HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(value = "/albums", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "List of albums")
    @ApiResponse(responseCode = "401", description = "Token missing")
    @ApiResponse(responseCode = "403", description = "Token Error")
    @Operation(summary = "List album api")
    @SecurityRequirement(name = "olahammed-demo-api")
    public List<AlbumViewDTO> albums(Authentication authentication) {
        String email = authentication.getName();
        Optional<Account> optionaAccount = accountService.findByEmail(email);
        Account account = optionaAccount.get();
        List<AlbumViewDTO> albums = new ArrayList<>();
        for (Album album : albumService.findByAccount_id(account.getId())) {
            albums.add(new AlbumViewDTO(album.getId(), album.getName(), album.getDescription()));
        }
        return albums;
    }

    @PostMapping(value = "/albums/{albumId}/upload-photos", consumes = "multipart/form-data")
    @Operation(summary = "Upload photo into album")
    @ApiResponse(responseCode = "400", description = "Please check the payload or token")
    @ApiResponse(responseCode = "201", description = "Photos created")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<List<HashMap<String, List<String>>>> photos(
            @RequestPart(required = true) MultipartFile[] files,
            @PathVariable Long albumId, Authentication authentication) {

        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionaAlbum = albumService.findById(albumId);
        Album album;
        if (optionaAlbum.isPresent()) {
            album = optionaAlbum.get();
            if (account.getId() != album.getAccount().getId()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<String> fileNamesWithSuccess = new ArrayList<>();
        List<String> fileNamesWithError = new ArrayList<>();

        Arrays.asList(files).stream().forEach(file -> {
            String contentType = file.getContentType();
            if (contentType.equals("image/png")
                    || contentType.equals("image/jpg")
                    || contentType.equals("image/jpeg")) {
                fileNamesWithSuccess.add(file.getOriginalFilename());

                int length = 10;
                boolean useLetters = true;
                boolean useNumbers = true;

                try {
                    String fileName = file.getOriginalFilename();
                    String generatedString = RandomStringUtils.random(length, useLetters, useNumbers);
                    String final_photo_name = generatedString + fileName;
                    String absolute_fileLocation = AppUtils.get_photo_upload_path(final_photo_name, PHOTOS_FOLDER_NAME,
                            albumId);
                    Path path = Paths.get(absolute_fileLocation);
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    Photo photo = new Photo();
                    photo.setName(fileName);
                    photo.setFileName(final_photo_name);
                    photo.setOriginalFileName(fileName);
                    photo.setAlbum(album);
                    photoService.save(photo);

                    BufferedImage thumbImg = AppUtils.getThumbnail(file, THUMBNAIL_WIDTH);
                    File thumbnail_location = new File(
                            AppUtils.get_photo_upload_path(final_photo_name, THUMBNAIL_FOLDER_NAME, albumId));
                    ImageIO.write(thumbImg, file.getContentType().split("/")[1], thumbnail_location);

                } catch (Exception e) {
                    log.debug(AlbumError.PHOTO_UPLOAD_ERROR.toString() + ": " + e.getMessage());
                    fileNamesWithError.add(file.getOriginalFilename());
                }

            } else {
                fileNamesWithError.add(file.getOriginalFilename());
            }
        });

        HashMap<String, List<String>> result = new HashMap<>();
        result.put("SUCCESS", fileNamesWithSuccess);
        result.put("ERRORS", fileNamesWithError);

        List<HashMap<String, List<String>>> response = new ArrayList<>();
        response.add(result);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> downloadFile(Long albumId, Long photoId, String folderName, Authentication authentication){
        String email = authentication.getName();
        Optional<Account> optionalAccount = accountService.findByEmail(email);
        Account account = optionalAccount.get();
        Optional<Album> optionaAlbum = albumService.findById(albumId);
        Album album;
        if (optionaAlbum.isPresent()) {
            album = optionaAlbum.get();
            if (account.getId() != album.getAccount().getId()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Optional<Photo> optionalPhoto = photoService.findById(photoId);
        if (optionalPhoto.isPresent()) {
            Photo photo = optionalPhoto.get();
            Resource resource = null;
            try {
                resource = AppUtils.getFileAsResource(albumId, PHOTOS_FOLDER_NAME, photo.getFileName());
            } catch (IOException e) {
                return ResponseEntity.internalServerError().build();
            }
            if (resource == null) {
                return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
            }
            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"" + photo.getOriginalFileName() + "\"";

            return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .body(resource);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/albums/{albumId}/{photoId}/download-photo")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<?> downloadPhoto(@PathVariable("albumId") Long albumId, @PathVariable("photoId") Long photoId, Authentication authentication) {
        return downloadFile(albumId, photoId, PHOTOS_FOLDER_NAME, authentication);
    }

    @GetMapping("/albums/{albumId}/{photoId}/download-thumbnail")
    @SecurityRequirement(name = "olahammed-demo-api")
    public ResponseEntity<?> downloadThumbnail(@PathVariable("albumId") Long albumId, @PathVariable("photoId") Long photoId, Authentication authentication) {

        return downloadFile(albumId, photoId, THUMBNAIL_FOLDER_NAME, authentication);
    }

}
