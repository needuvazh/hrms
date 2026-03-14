package com.company.hrms.document.service;

import com.company.hrms.document.model.*;
import com.company.hrms.contracts.document.AttachDocumentCommandDto;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentModuleApi {

    Mono<DocumentRecordViewDto> attachDocument(AttachDocumentCommandDto command);

    Flux<DocumentRecordViewDto> listDocuments(DocumentListQueryDto query);

    Flux<DocumentRecordViewDto> findExpiringDocuments(DocumentExpiryQueryDto query);

    Mono<DocumentRecordViewDto> archiveDocument(UUID documentId);

    Mono<DocumentRecordViewDto> getDocument(UUID documentId);
}
