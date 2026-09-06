package config

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/joho/godotenv"
)

// Config holds the minecraft-api client configuration.
type Config struct {
	ApiURL string
	ApiKey string
}

// LoadConfig loads configuration from the environment and candidate .env files.
func LoadConfig(customEnvFile string) (*Config, error) {
	if customEnvFile != "" {
		if err := godotenv.Load(customEnvFile); err != nil {
			return nil, fmt.Errorf("failed to load custom env file %q: %w", customEnvFile, err)
		}
	} else {
		candidates := []string{
			".env",
			filepath.Join("apps", "minecraft-cli", ".env"),
			filepath.Join("apps", "minecraft-api", ".env"),
			filepath.Join("..", "minecraft-api", ".env"),
			filepath.Join("..", "..", "apps", "minecraft-api", ".env"),
		}
		for _, path := range candidates {
			if _, err := os.Stat(path); err == nil {
				_ = godotenv.Load(path)
				break
			}
		}
	}

	cfg := &Config{
		ApiURL: getEnv("MINECRAFT_API_URL", "http://localhost:3002"),
		ApiKey: os.Getenv("ADMIN_API_KEY"),
	}

	return cfg, nil
}

func getEnv(key, defaultVal string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return defaultVal
}
