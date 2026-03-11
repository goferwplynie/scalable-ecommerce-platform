package main

import (
	"apiGateway/config"
	"apiGateway/internal/clients"
	"apiGateway/internal/handlers"
	"apiGateway/internal/utils"
	product "apiGateway/pb"
	"fmt"

	"github.com/go-playground/validator/v10"
	jwtware "github.com/gofiber/contrib/jwt"
	"github.com/gofiber/fiber/v3"
	"google.golang.org/grpc"
)

func main() {
	config.LoadConfig()

	catalogClient, err := grpc.NewClient(config.Config.Grpc.CatalogService.Address)
	if err != nil {
		panic(err)
	}
	productGrpcClient := product.NewProductCatalogServiceClient(catalogClient)
	productCatalogClient := clients.NewProductCatalogClient(productGrpcClient)

	catalogHandler := handlers.NewCatalogHandler(productCatalogClient)

	validator := utils.StructValidator{
		Validator: validator.New(),
	}

	app := fiber.New(fiber.Config{
		AppName:         config.Config.Server.Name,
		StructValidator: &validator,
	})

	jwtMiddleware := jwtware.New(jwtware.Config{
		SigningKey: jwtware.SigningKey{
			Key: []byte(config.Config.Security.Jwt.Secret),
		},
	})

	app.Get("/products/:id", catalogHandler.GetProduct)
	app.Get("/products", catalogHandler.GetProducts)
	app.Get("/categories", catalogHandler.GetCategories)

	protected := app.Group("/", jwtMiddleware)

	protected.Post("/products", catalogHandler.CreateProduct)
	protected.Put("/products/stock", catalogHandler.UpdateStock)
	protected.Post("/categories", catalogHandler.CreateCategory)

	port := fmt.Sprintf(":%d", config.Config.Server.Port)
	if err := app.Listen(port); err != nil {
		panic(err)
	}
}
