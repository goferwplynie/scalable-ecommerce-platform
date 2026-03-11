package models

import product "apiGateway/pb"

type GetProductRequest struct {
	ProductId string ``
}

func ProductRequestModelToProto(productRequest GetProductRequest) *product.GetProductRequest {
	return &product.GetProductRequest{
		ProductId: productRequest.ProductId,
	}
}

type ListProductRequest struct {
	Category string `query:"category" validate:"omitempty,min=3"`
	Offset   int32  `query:"offset" validate:"gte=0"`
	Limit    int32  `query:"limit" validate:"gte=1"`
}

func ListRequestModeToProto(listProductRequest ListProductRequest) *product.ListProductsRequest {
	return &product.ListProductsRequest{
		Category: listProductRequest.Category,
		Offset:   listProductRequest.Offset,
		Limit:    listProductRequest.Limit,
	}
}

type CreateProductRequest struct {
	Name          string  `json:"name" validate:"required,min=3,max=255"`
	Description   string  `json:"description" validate:"omitempty,max=5000"`
	Category      string  `json:"category" validate:"required,min=3"`
	Price         float64 `json:"price" validate:"required,gte=1"`
	StockQuantity int32   `json:"stock_quantity" validate:"required,gte=0"`
}

func CreateProductModelToProto(m CreateProductRequest) *product.CreateProductRequest {
	return &product.CreateProductRequest{
		Name:          m.Name,
		Description:   m.Description,
		Category:      m.Category,
		Price:         m.Price,
		StockQuantity: m.StockQuantity,
	}
}

type CreateCategoryRequest struct {
	Name string `json:"name" validate:"required,min=3,max=255"`
}

func CreateCategoryModelToProto(m CreateCategoryRequest) *product.CreateCategoryRequest {
	return &product.CreateCategoryRequest{
		CategoryName: m.Name,
	}
}

type UpdateStockRequest struct {
	ProductId string `json:"product_id" validate:"required,uuid"`
	NewStock  int32  `json:"new_stock" validate:"required,gte=0"`
}

func UpdateStockModelToProto(m UpdateStockRequest) *product.UpdateStockRequest {
	return &product.UpdateStockRequest{
		ProductId:        m.ProductId,
		NewStockQuantity: m.NewStock,
	}
}
