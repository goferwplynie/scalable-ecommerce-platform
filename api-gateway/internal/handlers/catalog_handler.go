package handlers

import (
	"apiGateway/internal/clients"
	"apiGateway/internal/models"
	"apiGateway/internal/utils"

	"github.com/gofiber/fiber/v3"
)

type CatalogHandler struct {
	ProductCatalogClient clients.ProductCatalogClient
}

func NewCatalogHandler(catalogClient clients.ProductCatalogClient) *CatalogHandler {
	return &CatalogHandler{
		ProductCatalogClient: catalogClient,
	}
}

func (h *CatalogHandler) GetProduct(c fiber.Ctx) error {
	productId := c.Params("id")

	if productId == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Product id not found in url",
		})
	}

	req := models.GetProductRequest{
		ProductId: productId,
	}

	resp, err := h.ProductCatalogClient.GetProduct(c.Context(), "", req)
	if err != nil {
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)

	}

	return c.JSON(resp)
}
