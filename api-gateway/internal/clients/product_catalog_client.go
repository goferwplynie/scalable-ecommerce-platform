package clients

import (
	"apiGateway/config"
	"apiGateway/internal/models"
	product "apiGateway/pb"
	"context"
	"io"

	"google.golang.org/grpc/metadata"
	"google.golang.org/protobuf/types/known/emptypb"
)

type ProductCatalogClient struct {
	grpcClient product.ProductCatalogServiceClient
}

func NewProductCatalogClient(grpcClient product.ProductCatalogServiceClient) ProductCatalogClient {
	return ProductCatalogClient{
		grpcClient: grpcClient,
	}
}

func (c ProductCatalogClient) GetProduct(ctx context.Context, req models.GetProductRequest) (*product.ProductResponse, error) {
	ctxWithAuth := c.setAuth(ctx)
	return c.grpcClient.GetProduct(ctxWithAuth, models.ProductRequestModelToProto(req))
}

func (c ProductCatalogClient) GetAllCategories(ctx context.Context) ([]*product.CategoryResponse, error) {
	var resp []*product.CategoryResponse
	ctxWithAuth := c.setAuth(ctx)
	respStream, err := c.grpcClient.GetAllCategories(ctxWithAuth, &emptypb.Empty{})
	if err != nil {
		return resp, err
	}

	for {
		category, err := respStream.Recv()
		if err == io.EOF {
			return resp, nil
		}
		if err != nil {
			return resp, err
		}
		resp = append(resp, category)
	}
}

func (c ProductCatalogClient) GetProducts(ctx context.Context, req models.ListProductRequest) ([]*product.ProductResponse, error) {
	var resp []*product.ProductResponse
	ctxWithAuth := c.setAuth(ctx)
	respStream, err := c.grpcClient.ListProducts(ctxWithAuth, models.ListRequestModeToProto(req))
	if err != nil {
		return resp, err
	}

	for {
		product, err := respStream.Recv()
		if err == io.EOF {
			return resp, nil
		}
		if err != nil {
			return nil, err
		}
		resp = append(resp, product)
	}
}

func (c ProductCatalogClient) CreateProduct(ctx context.Context, token string, req models.CreateProductRequest) (*product.ProductResponse, error) {
	ctxWithAuth := c.setAuth(ctx, token)
	return c.grpcClient.CreateProduct(ctxWithAuth, models.CreateProductModelToProto(req))
}

func (c ProductCatalogClient) CreateCategory(ctx context.Context, token string, req models.CreateCategoryRequest) (*product.CategoryResponse, error) {
	ctxWithAuth := c.setAuth(ctx, token)
	return c.grpcClient.CreateCategory(ctxWithAuth, models.CreateCategoryModelToProto(req))
}

func (c ProductCatalogClient) UpdateStock(ctx context.Context, token string, req models.UpdateStockRequest) error {
	ctxWithAuth := c.setAuth(ctx, token)
	_, err := c.grpcClient.UpdateStock(ctxWithAuth, models.UpdateStockModelToProto(req))
	return err
}

func (c ProductCatalogClient) setAuth(ctx context.Context, token ...string) context.Context {
	md := metadata.Pairs("X-Internal-Api-Key", config.Config.Grpc.CatalogService.ApiKey)

	if len(token) > 0 {
		md.Append("Authorization", "Bearer "+token[0])
	}

	ctxWithAuth := metadata.NewOutgoingContext(ctx, md)
	return ctxWithAuth
}
