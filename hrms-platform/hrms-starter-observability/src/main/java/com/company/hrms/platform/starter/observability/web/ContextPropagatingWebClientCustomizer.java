package com.company.hrms.platform.starter.observability.web;

import com.company.hrms.platform.sharedkernel.context.PlatformContextKeys;
import com.company.hrms.platform.sharedkernel.web.HrmsHeaders;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class ContextPropagatingWebClientCustomizer implements WebClientCustomizer {

    @Override
    public void customize(WebClient.Builder webClientBuilder) {
        webClientBuilder.filter(contextPropagationFilter());
    }

    private ExchangeFilterFunction contextPropagationFilter() {
        return (request, next) -> Mono.deferContextual(contextView -> {
            ClientRequest.Builder builder = ClientRequest.from(request);

            if (contextView.hasKey(PlatformContextKeys.CORRELATION_ID)
                    && !request.headers().containsKey(HrmsHeaders.CORRELATION_ID)) {
                builder.header(HrmsHeaders.CORRELATION_ID, contextView.get(PlatformContextKeys.CORRELATION_ID));
            }

            if (contextView.hasKey(PlatformContextKeys.TENANT_ID)
                    && !request.headers().containsKey(HrmsHeaders.TENANT_ID)) {
                builder.header(HrmsHeaders.TENANT_ID, contextView.get(PlatformContextKeys.TENANT_ID));
            }

            return next.exchange(builder.build());
        });
    }
}
