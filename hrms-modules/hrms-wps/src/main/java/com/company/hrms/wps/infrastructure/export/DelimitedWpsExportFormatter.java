package com.company.hrms.wps.infrastructure.export;

import com.company.hrms.wps.domain.WpsBatch;
import com.company.hrms.wps.domain.WpsEmployeeEntry;
import com.company.hrms.wps.domain.WpsExportFormatter;
import com.company.hrms.wps.domain.WpsExportPayload;
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
    public WpsExportPayload format(WpsBatch batch, List<WpsEmployeeEntry> entries) {
        StringBuilder builder = new StringBuilder();
        builder.append("BATCH_ID|TENANT|PAYROLL_RUN|CREATED_AT\n");
        builder.append(batch.id()).append("|")
                .append(batch.tenantId()).append("|")
                .append(batch.payrollRunId()).append("|")
                .append(batch.createdAt()).append("\n");
        builder.append("EMPLOYEE_ID|NET_AMOUNT|REFERENCE\n");
        for (WpsEmployeeEntry entry : entries) {
            builder.append(entry.employeeId()).append("|")
                    .append(entry.netAmount()).append("|")
                    .append(entry.paymentReference()).append("\n");
        }

        String payload = builder.toString();
        String hash = sha256(payload);
        String fileName = "wps-%s-%d.txt".formatted(batch.id(), Instant.now().toEpochMilli());
        return new WpsExportPayload(fileName, "text/plain", payload, hash);
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
