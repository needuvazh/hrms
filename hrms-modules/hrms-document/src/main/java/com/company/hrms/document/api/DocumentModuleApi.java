package com.company.hrms.document.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentModuleApi {

    Mono<DocumentRecordView> attachDocument(AttachDocumentCommand command);

    Flux<DocumentRecordView> listDocuments(DocumentListQuery query);

    Flux<DocumentRecordView> findExpiringDocuments(DocumentExpiryQuery query);

    Mono<DocumentRecordView> archiveDocument(UUID documentId);

    Mono<DocumentRecordView> getDocument(UUID documentId);
}
