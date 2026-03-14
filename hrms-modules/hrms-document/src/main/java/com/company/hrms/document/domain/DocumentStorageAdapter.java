package com.company.hrms.document.domain;

import reactor.core.publisher.Mono;

public interface DocumentStorageAdapter {

    Mono<StorageReference> registerUploadMetadata(StorageRegistrationRequest request);

    Mono<StorageReference> lookup(StorageReference reference);

    Mono<Void> archive(StorageReference reference);
}
