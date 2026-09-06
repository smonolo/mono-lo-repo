package cmd

import (
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"dev.smnl/minecraft-cli/internal/api"
	"dev.smnl/minecraft-cli/internal/config"
	"dev.smnl/minecraft-cli/internal/ui"
)

var (
	envFileFlag string
	apiURLFlag  string
	apiKeyFlag  string
	jsonFlag    bool
	quietFlag   bool

	appConfig *config.Config
)

// RootCmd represents the base command when called without any subcommands.
var RootCmd = &cobra.Command{
	Use:   "minecraft-cli",
	Short: "Minecraft administration CLI toolkit",
	Long: `A robust Go CLI toolkit for administering the Minecraft project via minecraft-api.
Provides commands for managing players and administrative tasks.`,
	SilenceUsage:  true,
	SilenceErrors: true,
	PersistentPreRunE: func(cmd *cobra.Command, args []string) error {
		cfg, err := config.LoadConfig(envFileFlag)
		if err != nil {
			return err
		}

		if apiURLFlag != "" {
			cfg.ApiURL = apiURLFlag
		}
		if apiKeyFlag != "" {
			cfg.ApiKey = apiKeyFlag
		}

		appConfig = cfg
		return nil
	},
}

// Execute adds all child commands to the root command and sets flags appropriately.
func Execute() {
	if err := RootCmd.Execute(); err != nil {
		ui.Error("%v", err)
		os.Exit(1)
	}
}

func init() {
	RootCmd.PersistentFlags().StringVar(&envFileFlag, "env-file", "", "Path to custom .env file")
	RootCmd.PersistentFlags().StringVar(&apiURLFlag, "api-url", "", "minecraft-api base URL (overrides MINECRAFT_API_URL)")
	RootCmd.PersistentFlags().StringVar(&apiKeyFlag, "api-key", "", "Admin API Key for minecraft-api (overrides ADMIN_API_KEY)")
	RootCmd.PersistentFlags().BoolVar(&jsonFlag, "json", false, "Output results in JSON format")
	RootCmd.PersistentFlags().BoolVarP(&quietFlag, "quiet", "q", false, "Suppress banner and non-essential output")
}

// GetAPIClient returns a configured minecraft-api HTTP client.
func GetAPIClient() (*api.Client, error) {
	if appConfig == nil {
		return nil, fmt.Errorf("application configuration is not loaded")
	}
	return api.NewClient(appConfig.ApiURL, appConfig.ApiKey), nil
}
