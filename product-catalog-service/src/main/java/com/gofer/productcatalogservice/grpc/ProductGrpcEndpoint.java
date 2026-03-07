package com.gofer.productcatalogservice.grpc;

import com.gofer.ecommerce.product.grpc.*;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class ProductGrpcEndpoint extends ProductCatalogServiceGrpc.ProductCatalogServiceImplBase {

    @Value("${catalog.limits.default}")
    private int defaultLimit;

    @Value("${catalog.limits.max}")
    private int maxLimit;

    @Override
    public void getProduct(GetProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        String requestedId = request.getProductId();

        ProductResponse response = ProductResponse.newBuilder()
                .setId(requestedId)
                .setName("meow")
                .setDescription("silly cat :3")
                .setPrice(20.0)
                .setStockQuantity(10)
                .build();
        responseObserver.onNext(response);

        responseObserver.onCompleted();
    }

    @Override
    public void checkAvailability(CheckAvailabilityRequest request, StreamObserver<AvailabilityResponse> responseObserver) {
        AvailabilityResponse response;
        String productId = request.getProductId();
        int requestedQuantity = request.getRequestedQuantity();

        boolean isAvailable = requestedQuantity <= 10;

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

        List<ProductResponse> products = List.of(
                ProductResponse.newBuilder()
                        .setId("1")
                        .setName("cat")
                        .setDescription("meows")
                        .setPrice(10.0)
                        .setStockQuantity(100)
                        .build(),
                ProductResponse.newBuilder()
                        .setId("2")
                        .setName("mrreeow")
                        .setDescription("cats")
                        .setPrice(20.0)
                        .setStockQuantity(50)
                        .build()
        );

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

        ProductResponse response = ProductResponse.newBuilder()
                .setId("1")
                .setName(name)
                .setDescription(description)
                .setPrice(price)
                .setStockQuantity(stockQuantity)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStock(UpdateStockRequest request, StreamObserver<Empty> responseObserver) {
        String productId = request.getProductId();
        int newStockQuantity = request.getNewStockQuantity();


        super.updateStock(request, responseObserver);
    }

    @Override
    public void getAllCategories(Empty request, StreamObserver<CategoryResponse> responseObserver) {
        super.getAllCategories(request, responseObserver);
    }
}
