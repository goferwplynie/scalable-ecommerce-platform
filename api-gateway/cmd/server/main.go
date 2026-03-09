package main

import (
	"apiGateway/config"
	"fmt"

	"github.com/gofiber/fiber/v3"
)

func main() {
	config.LoadConfig()

	app := fiber.New(fiber.Config{
		AppName: config.Config.Server.Name,
	})

	port := fmt.Sprintf(":%d", config.Config.Server.Port)
	if err := app.Listen(port); err != nil {
		panic(err)
	}
}
