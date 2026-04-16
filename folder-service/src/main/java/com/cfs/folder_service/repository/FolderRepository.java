package com.cfs.folder_service.repository;

import com.cfs.folder_service.model.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity,Long> {

    List<FolderEntity> findByParentId(@PathVariable Long id);
}
