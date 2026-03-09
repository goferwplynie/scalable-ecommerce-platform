package models

import product "apiGateway/pb"

type GetProductRequest struct {
	ProductId string `json:"product_id,omitempty"`
}

func ProductRequestModelToProto(productRequest GetProductRequest) *product.GetProductRequest {
	return &product.GetProductRequest{
		ProductId: productRequest.ProductId,
	}
}

type ListProductRequest struct {
	Category string `json:"category,omitempty"`
	Offset   int32  `json:"offset,omitempty"`
	Limit    int32  `json:"limit,omitempty"`
}

func ListRequestModeToProto(listProductRequest ListProductRequest) *product.ListProductsRequest {
	return &product.ListProductsRequest{
		Category: listProductRequest.Category,
		Offset:   listProductRequest.Offset,
		Limit:    listProductRequest.Limit,
	}
}

type CreateProductRequest struct {
	Name          string  `json:"name,omitempty"`
	Description   string  `json:"description,omitempty"`
	Category      string  `json:"category,omitempty"`
	Price         float64 `json:"price,omitempty"`
	StockQuantity int32   `json:"stock_quantity,omitempty"`
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
	Name string `json:"name,omitempty"`
}

func CreateCategoryModelToProto(m CreateCategoryRequest) *product.CreateCategoryRequest {
	return &product.CreateCategoryRequest{
		CategoryName: m.Name,
	}
}

type UpdateStockRequest struct {
	ProductId string `json:"product_id,omitempty"`
	NewStock  int32  `json:"new_stock,omitempty"`
}

func UpdateStockModelToProto(m UpdateStockRequest) *product.UpdateStockRequest {
	return &product.UpdateStockRequest{
		ProductId:        m.ProductId,
		NewStockQuantity: m.NewStock,
	}
}
