package com.gofer.productcatalogservice.config;

import com.gofer.productcatalogservice.security.SecurityInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.server.GlobalServerInterceptor;

@Configuration
public class GlobalInterceptorConfiguration {

    @Bean
    @GlobalServerInterceptor
    ServerInterceptor securityInterceptor(SecurityProperties securityProperties) {
        return new SecurityInterceptor(securityProperties);
    }
}
