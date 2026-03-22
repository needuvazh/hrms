package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import com.company.hrms.masterdata.reference.infrastructure.ReferenceResourceDao;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MaritalStatusesService extends BaseReferenceControllerService {
    public MaritalStatusesService(ReferenceMasterMapper mapper, AuditEventPublisher auditEventPublisher, @Qualifier("maritalStatusesReferenceDao") ReferenceResourceDao dao) {
        super(mapper, auditEventPublisher, dao, ReferenceResource.MARITAL_STATUSES);
    }
}
