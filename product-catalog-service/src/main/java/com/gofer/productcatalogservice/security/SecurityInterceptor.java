package com.gofer.productcatalogservice.security;

import com.gofer.productcatalogservice.config.SecurityProperties;
import io.grpc.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

@Slf4j
@Order(1)
public class SecurityInterceptor implements ServerInterceptor {

    private final SecurityProperties securityProperties;

    public SecurityInterceptor(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    //auth headers
    private static final Metadata.Key<String> API_KEY_HEADER = Metadata.Key.of("X-Internal-Api-Key", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> AUTH_HEADER = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);

    // jwt claims
    public static final Context.Key<String> USER_ID_KEY = Context.key("user_id");
    public static final Context.Key<UserRole> USER_ROLE_KEY = Context.key("user_role");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String incomingApiKey = metadata.get(API_KEY_HEADER);

        // verify provided api key. if it's null or wrong return permission denied
        if (incomingApiKey == null || !incomingApiKey.equals(securityProperties.getInternalApiKey())) {
            log.warn("Denied service access. Wrong api key");
            serverCall.close(Status.PERMISSION_DENIED.withDescription("Missing or invalid X-Internal-Api-Key header"), metadata);
            return new ServerCall.Listener<ReqT>() {
            };
        }

        String calledMethod = serverCall.getMethodDescriptor().getFullMethodName();

        boolean isProtected = securityProperties.getProtectedMethods().stream().anyMatch(calledMethod::endsWith);


        if (isProtected) {
            // check if provided jwt auth header has correct format
            String authHeader = metadata.get(AUTH_HEADER);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                serverCall.close(Status.UNAUTHENTICATED.withDescription("Missing or invalid Authorization header"), metadata);
                return new ServerCall.Listener<ReqT>() {
                };
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(securityProperties.getJwtSecret().getBytes())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                Context context = Context.current()
                        .withValue(USER_ID_KEY, userId)
                        .withValue(USER_ROLE_KEY, UserRole.fromString(role));

                return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
            } catch (Exception e) {
                serverCall.close(Status.UNAUTHENTICATED.withDescription("Invalid or expired JWT token"), metadata);
                return new ServerCall.Listener<ReqT>() {
                };
            }

        }
        return serverCallHandler.startCall(serverCall, metadata);
    }
}
