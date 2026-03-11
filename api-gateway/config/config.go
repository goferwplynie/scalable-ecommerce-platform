package config

import (
	"log"
	"strings"

	"github.com/spf13/viper"
)

type AppConfig struct {
	Server   Server   `mapstructure:"server"`
	Grpc     Grpc     `mapstructure:"grpc"`
	Redis    Redis    `mapstructure:"redis"`
	Security Security `mapstructure:"security"`
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

type Security struct {
	Jwt Jwt `mapstructure:"jwt"`
}

type Jwt struct {
	Secret string `mapstructure:"secret"`
}

var Config AppConfig

func LoadConfig() {
	viper.SetConfigName("application")
	viper.SetConfigType("yml")
	viper.AddConfigPath(".")

	viper.SetEnvPrefix("GATEWAY")
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		log.Fatalf("Can't find app config: %v", err)
	}

	if err := viper.Unmarshal(&Config); err != nil {
		log.Fatalf("Failed to bind config: %v", err)
	}

	log.Printf("Loaded config for: %s", Config.Server.Name)
}
