package com.company.hrms.document.infrastructure;

import com.company.hrms.document.domain.DocumentStorageAdapter;
import com.company.hrms.document.domain.StorageReference;
import com.company.hrms.document.domain.StorageRegistrationRequest;
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

    private final Map<String, StorageReference> storage = new ConcurrentHashMap<>();

    @Override
    public Mono<StorageReference> registerUploadMetadata(StorageRegistrationRequest request) {
        StorageReference reference = new StorageReference(
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
    public Mono<StorageReference> lookup(StorageReference reference) {
        return Mono.justOrEmpty(storage.get(composeKey(reference)));
    }

    @Override
    public Mono<Void> archive(StorageReference reference) {
        storage.remove(composeKey(reference));
        LOGGER.info("Archived document storage metadata provider={}, bucket={}, objectKey={}",
                reference.provider(),
                reference.bucket(),
                reference.objectKey());
        return Mono.empty();
    }

    private String composeKey(StorageReference reference) {
        String bucket = reference.bucket() == null ? "" : reference.bucket();
        return reference.provider() + ":" + bucket + ":" + reference.objectKey();
    }
}
