package com.vaultshare;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

interface FileRepository extends JpaRepository<FileRecord,String> {}
interface ShareRepository extends JpaRepository<ShareRecord,String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ShareRecord s where s.id = :id")
    Optional<ShareRecord> findLocked(@Param("id") String id);
}
