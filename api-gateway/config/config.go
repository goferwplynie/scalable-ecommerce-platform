package config

import (
	"log"

	"github.com/spf13/viper"
)

type AppConfig struct {
	Server Server `mapstructure:"server"`
	Grpc   Grpc   `mapstructure:"grpc"`
	Redis  Redis  `mapstructure:"redis"`
}

type Server struct {
	Port int    `mapstructure:"port"`
	Name string `mapstructure:"name"`
}

type Grpc struct {
	CatalogService CatalogService `mapstructure:"catalog_service"`
}

type CatalogService struct {
	Address string `mapstructure:"address"`
	ApiKey  string `mapstructure:"api_key"`
}

type Redis struct {
	Address string `mapstructure:"address"`
}

var Config AppConfig

func LoadConfig() {
	viper.SetConfigName("application")
	viper.SetConfigType("yml")
	viper.AddConfigPath(".")

	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Can't find app config: %v", err)
	}

	if err := viper.Unmarshal(&Config); err != nil {
		log.Fatalf("Failed to bind config: %v", err)
	}

	log.Printf("Loaded config for: %s", Config.Server.Name)
}
