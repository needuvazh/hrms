package com.company.hrms.document.model;

import reactor.core.publisher.Mono;

public interface DocumentStorageAdapter {

    Mono<StorageReferenceDto> registerUploadMetadata(StorageRegistrationRequestDto request);

    Mono<StorageReferenceDto> lookup(StorageReferenceDto reference);

    Mono<Void> archive(StorageReferenceDto reference);
}
