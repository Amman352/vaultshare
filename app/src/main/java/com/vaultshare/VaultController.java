package com.vaultshare;

import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
class VaultController {
    private static final Logger audit = LoggerFactory.getLogger("VaultShareAudit");
    private final FileRepository files; private final ShareRepository shares;
    private final StorageService storage;
    VaultController(FileRepository files, ShareRepository shares, StorageService storage) {
        this.files=files; this.shares=shares; this.storage=storage;
    }
    record ShareRequest(int hours,int maxDownloads) {}
    @GetMapping("/files") List<Map<String,Object>> list() {
        return files.findAll().stream().map(f -> Map.<String,Object>of("id",f.id,"filename",f.filename,"sizeBytes",f.sizeBytes,"createdAt",f.createdAt)).toList();
    }
    @PostMapping(value="/files",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) Map<String,String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if(file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Empty file");
        byte[] bytes=file.getBytes();
        String objectKey=UUID.randomUUID().toString(); storage.put(objectKey,bytes);
        FileRecord record=files.save(new FileRecord(Objects.requireNonNullElse(file.getOriginalFilename(),"file"),Objects.requireNonNullElse(file.getContentType(),"application/octet-stream"),objectKey,bytes.length));
        audit.info("event=upload fileId={} bytes={}",record.id,record.sizeBytes);
        return Map.of("id",record.id);
    }
    private FileRecord file(String id) {
        return files.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
    private ResponseEntity<byte[]> download(FileRecord f) throws Exception {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(f.contentType)).header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename*=UTF-8''"+java.net.URLEncoder.encode(f.filename,java.nio.charset.StandardCharsets.UTF_8)).body(storage.get(f.objectKey));
    }
    @GetMapping("/files/{id}") ResponseEntity<byte[]> get(@PathVariable String id) throws Exception {
        FileRecord f=file(id); audit.info("event=download fileId={}",id); return download(f);
    }
    @DeleteMapping("/files/{id}") void delete(@PathVariable String id) throws Exception {
        FileRecord f=file(id); shares.findAll().stream().filter(s -> s.fileId.equals(id)).forEach(shares::delete); storage.delete(f.objectKey); files.delete(f);
        audit.info("event=delete fileId={}",id);
    }
    @PostMapping("/files/{id}/shares") Map<String,String> share(@PathVariable String id,@RequestBody ShareRequest req) {
        file(id);
        if(req.hours()<1 || req.hours()>168 || req.maxDownloads()<1 || req.maxDownloads()>100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid link limits");
        ShareRecord s=shares.save(new ShareRecord(id,Instant.now().plusSeconds(req.hours()*3600L),req.maxDownloads()));
        audit.info("event=share_created fileId={} shareId={}",id,s.id);
        return Map.of("path","/api/share/"+s.id,"expiresAt",s.expiresAt.toString());
    }
    @Transactional
    @GetMapping("/share/{shareId}") ResponseEntity<byte[]> shared(@PathVariable String shareId) throws Exception {
        ShareRecord s=shares.findLocked(shareId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(s.expiresAt.isBefore(Instant.now()) || s.remainingDownloads<=0) throw new ResponseStatusException(HttpStatus.GONE);
        FileRecord f=files.findById(s.fileId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        s.remainingDownloads--; shares.save(s); audit.info("event=shared_download fileId={} shareId={}",s.fileId,shareId); return download(f);
    }
}
