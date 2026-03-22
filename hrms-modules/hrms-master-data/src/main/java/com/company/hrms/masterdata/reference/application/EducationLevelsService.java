package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import com.company.hrms.masterdata.reference.infrastructure.ReferenceResourceDao;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class EducationLevelsService extends BaseReferenceControllerService {
    public EducationLevelsService(ReferenceMasterMapper mapper, AuditEventPublisher auditEventPublisher, @Qualifier("educationLevelsReferenceDao") ReferenceResourceDao dao) {
        super(mapper, auditEventPublisher, dao, ReferenceResource.EDUCATION_LEVELS);
    }
}
