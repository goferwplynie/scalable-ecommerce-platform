package com.gofer.productcatalogservice.config;

import com.gofer.productcatalogservice.security.SecurityInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GlobalClientInterceptor;

@Configuration
public class GlobalInterceptorConfiguration {

    @GlobalClientInterceptor
    SecurityInterceptor securityInterceptor(SecurityProperties securityProperties) {
        return new SecurityInterceptor(securityProperties);
    }
}
