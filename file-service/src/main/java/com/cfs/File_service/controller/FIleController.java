package com.cfs.File_service.controller;

import com.cfs.File_service.model.FileEntity;
import com.cfs.File_service.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/files")
public class FIleController {

    @Autowired
    private FileRepository fileRepository;

    private  static final String UPLOAD_DIR="uploads";


    @GetMapping
    public List<FileEntity>  getAllFiles()
    {
        return fileRepository.findAll();
    }
    @GetMapping("/{fileId}")
    public FileEntity getFile(@PathVariable Long fileId)
    {
        return fileRepository.findById(fileId)
                .orElseThrow(()->new RuntimeException("File Not found"));
    }

    @PostMapping
    public FileEntity createFile(@RequestBody FileEntity file)
    {
        return  fileRepository.save(file);
    }


    @DeleteMapping("/{fileId}")
    public void deleteFile(@PathVariable Long fileId)
    {
        fileRepository.deleteById(fileId);
    }

    @GetMapping("/folder/{folderId}")
    public List<FileEntity> getFilesByFolder(@PathVariable Long folderId)
    {
        return  fileRepository.findByFolderId(folderId);
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("name") String name,
                                          @RequestParam("folderId") Long folderId,
                                          @RequestParam("file")MultipartFile file
                                          )
    {
        try {
            Long fileSize=file.getSize();
            String fileName= file.getOriginalFilename();

            FileEntity newFile=new FileEntity();

            newFile.setId(System.currentTimeMillis());
            newFile.setName(fileName !=null ? fileName:  name);
            newFile.setFolderId(folderId);
            newFile.setPath("/files/"+newFile.getId());


            String uploadDirectoryPath=UPLOAD_DIR+ File.separator+newFile.getId();
            Files.createDirectory(Paths.get(uploadDirectoryPath));

            Path filePath=Paths.get(uploadDirectoryPath,newFile.getName());

            Files.write(filePath,file.getBytes());

            FileEntity saved=fileRepository.save(newFile);

            return Map.of("Success","true","files",saved);
        }
        catch (Exception e)
        {
            return Map.of("Sucess",false,"Error",e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?>downloadFile(@PathVariable Long id)
    {
        try {
            FileEntity file =fileRepository.findById(id).orElse(null);
            if (file==null)
            {
                return ResponseEntity.notFound().build();
            }

            Path filePath=Paths.get(UPLOAD_DIR,id.toString(), file.getName());

            if (!Files.exists(filePath))
            {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent=Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,"attachment: filename\""+file.getName()+"\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_LENGTH,String.valueOf(fileContent.length))
                    .body(fileContent);
        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().body("Error in Downloading "+e.getMessage());
        }

    }

}
