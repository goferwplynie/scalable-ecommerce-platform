package handlers

import (
	"log"

	"apiGateway/internal/clients"
	"apiGateway/internal/models"
	"apiGateway/internal/utils"

	"github.com/gofiber/fiber/v3"
	"github.com/golang-jwt/jwt/v5"
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
		return WriteJsonError(c, fiber.StatusBadRequest, "Product id not found in url")
	}

	req := models.GetProductRequest{
		ProductId: productId,
	}

	resp, err := h.ProductCatalogClient.GetProduct(c.Context(), req)
	if err != nil {
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)

	}

	return c.JSON(resp)
}

func (h *CatalogHandler) GetProducts(c fiber.Ctx) error {
	var req models.ListProductRequest

	if err := c.Bind().Query(&req); err != nil {
		return WriteJsonError(c, fiber.StatusBadRequest, "Invalid query parameters: "+err.Error())
	}

	resp, err := h.ProductCatalogClient.GetProducts(c.Context(), req)
	if err != nil {
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)
	}

	return c.JSON(resp)
}

func (h *CatalogHandler) CreateProduct(c fiber.Ctx) error {
	var req models.CreateProductRequest

	if err := c.Bind().JSON(&req); err != nil {
		return WriteJsonError(c, fiber.StatusBadRequest, "Wrong body provided: "+err.Error())
	}

	userToken := c.Locals("user").(*jwt.Token)

	rawTokenString := userToken.Raw

	resp, err := h.ProductCatalogClient.CreateProduct(c.Context(), rawTokenString, req)
	if err != nil {
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)
	}
	return c.Status(fiber.StatusCreated).JSON(resp)
}

func (h *CatalogHandler) UpdateStock(c fiber.Ctx) error {
	var req models.UpdateStockRequest

	if err := c.Bind().JSON(&req); err != nil {
		return WriteJsonError(c, fiber.StatusBadRequest, "Wrong body provided: "+err.Error())
	}

	userToken := c.Locals("user").(*jwt.Token)

	rawTokenString := userToken.Raw

	err := h.ProductCatalogClient.UpdateStock(c.Context(), rawTokenString, req)
	if err != nil {
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)
	}
	return c.SendStatus(fiber.StatusOK)
}

func (h *CatalogHandler) GetCategories(c fiber.Ctx) error {
	resp, err := h.ProductCatalogClient.GetAllCategories(c.Context())
	if err != nil {
		log.Println(err)
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)
	}

	return c.JSON(resp)
}

func (h *CatalogHandler) CreateCategory(c fiber.Ctx) error {
	var req models.CreateCategoryRequest

	if err := c.Bind().JSON(&req); err != nil {
		return WriteJsonError(c, fiber.StatusBadRequest, "Wrong body provided: "+err.Error())
	}

	userToken := c.Locals("user").(*jwt.Token)

	rawTokenString := userToken.Raw

	resp, err := h.ProductCatalogClient.CreateCategory(c.Context(), rawTokenString, req)
	if err != nil {
		statusCode, msg := utils.GRPCToHTTPResponse(err)
		return c.Status(statusCode).JSON(msg)
	}
	return c.Status(fiber.StatusCreated).JSON(resp)
}

func WriteJsonError(c fiber.Ctx, status int, errorMessage any) error {
	return c.Status(status).JSON(fiber.Map{
		"error": errorMessage,
	})
}
