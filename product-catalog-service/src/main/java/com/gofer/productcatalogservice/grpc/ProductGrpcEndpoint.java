package com.gofer.productcatalogservice.grpc;

import com.gofer.ecommerce.product.grpc.*;
import com.gofer.productcatalogservice.entity.Category;
import com.gofer.productcatalogservice.entity.Product;
import com.gofer.productcatalogservice.exceptions.CategoryNotFoundException;
import com.gofer.productcatalogservice.exceptions.ProductNotFoundException;
import com.gofer.productcatalogservice.security.SecurityInterceptor;
import com.gofer.productcatalogservice.security.UserRole;
import com.gofer.productcatalogservice.service.CatalogService;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.grpc.server.service.GrpcService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class ProductGrpcEndpoint extends ProductCatalogServiceGrpc.ProductCatalogServiceImplBase {
    private final CatalogService catalogService;

    @Value("${catalog.limits.default}")
    private int defaultLimit;

    @Value("${catalog.limits.max}")
    private int maxLimit;

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        String requestedId = request.getProductId();

        UUID productId;
        try {
            productId = UUID.fromString(requestedId);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Invalid product ID format").asRuntimeException());
            return;
        }

        Product product;
        try {
            product = catalogService.getProductById(productId);
        } catch (ProductNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Product not found").asRuntimeException());
            return;
        }

        ProductResponse response = ProductResponse.newBuilder()
                .setId(product.getId().toString())
                .setSellerId(product.getSellerId().toString())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setPrice(product.getPrice().doubleValue())
                .setStockQuantity(product.getStockQuantity())
                .setCategory(product.getCategory().getName())
                .build();
        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void checkAvailability(CheckAvailabilityRequest request, StreamObserver<AvailabilityResponse> responseObserver) {
        AvailabilityResponse response;
        String requestedId = request.getProductId();

        UUID productId;
        try {
            productId = UUID.fromString(requestedId);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Invalid product ID format").asRuntimeException());
            return;
        }

        int requestedQuantity = request.getRequestedQuantity();

        boolean isAvailable;
        try {
            isAvailable = catalogService.checkAvailability(productId, requestedQuantity);
        } catch (ProductNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Product not found").asRuntimeException());
            return;
        }

        response = AvailabilityResponse.newBuilder()
                .setIsAvailable(isAvailable)
                .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void listProducts(ListProductsRequest request, StreamObserver<ProductResponse> responseObserver) {
        String categoryFilter = request.getCategory();
        int offset = request.getOffset();

        //if client provided a limit check if it exceeds max limit and use it otherwise fall back to default limit
        int targetLimit = (request.getLimit() > 0) ? Math.min(request.getLimit(), maxLimit) : defaultLimit;

        List<ProductResponse> products = catalogService.listProducts(categoryFilter, offset, targetLimit).stream()
                .map(product -> ProductResponse.newBuilder()
                        .setId(product.getId().toString())
                        .setSellerId(product.getSellerId().toString())
                        .setName(product.getName())
                        .setDescription(product.getDescription())
                        .setPrice(product.getPrice().doubleValue())
                        .setStockQuantity(product.getStockQuantity())
                        .setCategory(product.getCategory().getName())
                        .build())
                .toList();

        for (ProductResponse product : products) {
            responseObserver.onNext(product);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void createProduct(CreateProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        String name = request.getName();
        String description = request.getDescription();
        double price = request.getPrice();
        int stockQuantity = request.getStockQuantity();

        UserRole role = SecurityInterceptor.USER_ROLE_KEY.get();
        if (role != UserRole.SELLER && role != UserRole.ADMIN) {
            responseObserver.onError(io.grpc.Status.PERMISSION_DENIED.withDescription("Only sellers and admins can create products").asRuntimeException());
            return;
        }

        String requestSellerId = SecurityInterceptor.USER_ID_KEY.get();

        UUID sellerId;
        try {
            sellerId = UUID.fromString(requestSellerId);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Invalid seller ID format").asRuntimeException());
            return;
        }

        Product product;
        try {
            product = catalogService.createProduct(name,
                    sellerId,
                    description,
                    BigDecimal.valueOf(price),
                    stockQuantity,
                    request.getCategory());
        } catch (CategoryNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Category not found: " + request.getCategory()).asRuntimeException());
            return;
        }

        ProductResponse response = ProductResponse.newBuilder()
                .setId(product.getId().toString())
                .setSellerId(product.getSellerId().toString())
                .setName(product.getName())
                .setDescription(product.getDescription())
                .setPrice(product.getPrice().doubleValue())
                .setStockQuantity(product.getStockQuantity())
		        .setCategory(product.getCategory().getName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStock(UpdateStockRequest request, StreamObserver<Empty> responseObserver) {
        String requestProductId = request.getProductId();
        UUID productId;
        try {
            productId = UUID.fromString(requestProductId);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Invalid product ID format").asRuntimeException());
            return;
        }

        int newStockQuantity = request.getNewStockQuantity();

        if (newStockQuantity < 0) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Stock quantity cannot be negative").asRuntimeException());
            return;
        }

        String requestSellerId = SecurityInterceptor.USER_ID_KEY.get();

        UUID sellerId;
        try {
            sellerId = UUID.fromString(requestSellerId);
        } catch (IllegalArgumentException e) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription("Invalid seller ID format").asRuntimeException());
            return;
        }

        try {
            catalogService.updateStock(productId, sellerId, newStockQuantity);
        } catch (ProductNotFoundException e) {
            responseObserver.onError(io.grpc.Status.NOT_FOUND.withDescription("Product not found or seller does not own the product").asRuntimeException());
            return;
        }

        Empty response = Empty.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createCategory(CreateCategoryRequest request, StreamObserver<CategoryResponse> responseObserver) {
        UserRole role = SecurityInterceptor.USER_ROLE_KEY.get();
        if (role != UserRole.ADMIN) {
            responseObserver.onError(io.grpc.Status.PERMISSION_DENIED.withDescription("Only admins can create categories").asRuntimeException());
            return;
        }

        String categoryName = request.getCategoryName();

        Category category = catalogService.addCategory(categoryName);

        CategoryResponse response = CategoryResponse.newBuilder()
                .setCategoryName(category.getName())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllCategories(Empty request, StreamObserver<CategoryResponse> responseObserver) {

        List<CategoryResponse> categories = catalogService.getAllCategories().stream()
                .map(category -> CategoryResponse.newBuilder()
                        .setCategoryName(category.getName())
                        .build())
                .toList();

        for (CategoryResponse category : categories) {
            responseObserver.onNext(category);
        }
        responseObserver.onCompleted();
    }
}
