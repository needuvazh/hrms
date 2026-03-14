package com.company.hrms.wps.service.impl.export;

import com.company.hrms.wps.model.*;
import com.company.hrms.wps.repository.*;
import com.company.hrms.wps.service.*;

import com.company.hrms.wps.model.WpsBatchDto;
import com.company.hrms.wps.model.WpsEmployeeEntryDto;
import com.company.hrms.wps.model.WpsExportFormatter;
import com.company.hrms.wps.model.WpsExportPayloadDto;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DelimitedWpsExportFormatter implements WpsExportFormatter {

    @Override
    public String type() {
        return "WPS-TXT";
    }

    @Override
    public WpsExportPayloadDto format(WpsBatchDto batch, List<WpsEmployeeEntryDto> entries) {
        StringBuilder builder = new StringBuilder();
        builder.append("BATCH_ID|TENANT|PAYROLL_RUN|CREATED_AT\n");
        builder.append(batch.id()).append("|")
                .append(batch.tenantId()).append("|")
                .append(batch.payrollRunId()).append("|")
                .append(batch.createdAt()).append("\n");
        builder.append("EMPLOYEE_ID|NET_AMOUNT|REFERENCE\n");
        for (WpsEmployeeEntryDto entry : entries) {
            builder.append(entry.employeeId()).append("|")
                    .append(entry.netAmount()).append("|")
                    .append(entry.paymentReference()).append("\n");
        }

        String payload = builder.toString();
        String hash = sha256(payload);
        String fileName = "wps-%s-%d.txt".formatted(batch.id(), Instant.now().toEpochMilli());
        return new WpsExportPayloadDto(fileName, "text/plain", payload, hash);
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }
}
