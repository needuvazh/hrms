package com.company.hrms.document.service.impl;

import com.company.hrms.document.model.*;
import com.company.hrms.document.repository.*;
import com.company.hrms.document.service.*;

import com.company.hrms.document.model.DocumentStorageAdapter;
import com.company.hrms.document.model.StorageReferenceDto;
import com.company.hrms.document.model.StorageRegistrationRequestDto;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InMemoryDocumentStorageAdapter implements DocumentStorageAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryDocumentStorageAdapter.class);
    private static final String PROVIDER = "LOCAL_DEV";

    private final Map<String, StorageReferenceDto> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<StorageReferenceDto> registerUploadMetadata(StorageRegistrationRequestDto request) {
        StorageReferenceDto reference = new StorageReferenceDto(
                PROVIDER,
                request.tenantId(),
                request.objectKey(),
                request.checksum(),
                request.contentType(),
                request.sizeBytes());

        storage.put(composeKey(reference), reference);
        LOGGER.info("Registered document storage metadata provider={}, bucket={}, objectKey={}",
                reference.provider(),
                reference.bucket(),
                reference.objectKey());
        return Mono.just(reference);
    }

    @Override
    public Mono<StorageReferenceDto> lookup(StorageReferenceDto reference) {
        return Mono.justOrEmpty(storage.get(composeKey(reference)));
    }

    @Override
    public Mono<Void> archive(StorageReferenceDto reference) {
        storage.remove(composeKey(reference));
        LOGGER.info("Archived document storage metadata provider={}, bucket={}, objectKey={}",
                reference.provider(),
                reference.bucket(),
                reference.objectKey());
        return Mono.empty();
    }

    private String composeKey(StorageReferenceDto reference) {
        String bucket = reference.bucket() == null ? "" : reference.bucket();
        return reference.provider() + ":" + bucket + ":" + reference.objectKey();
    }
}
