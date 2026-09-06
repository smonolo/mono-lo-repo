package cmd

import (
	"errors"
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"dev.smnl/minecraft-cli/internal/player"
	"dev.smnl/minecraft-cli/internal/ui"
)

// playerGetCmd fetches and displays player profile information.
var playerGetCmd = &cobra.Command{
	Use:     "get <username|uuid>",
	Aliases: []string{"info", "show", "view"},
	Short:   "Get details of a player (UUID, username, first login, last login, ranks)",
	Long: `Retrieves and displays a player's core profile information from SMEssential via minecraft-api.
Displays UUID, username, first login timestamp, last login timestamp, and assigned ranks.`,
	Args: cobra.ExactArgs(1),
	RunE: runPlayerGet,
}

func init() {
	playerCmd.AddCommand(playerGetCmd)
}

type playerProfileOutput struct {
	UUID        string              `json:"uuid"`
	Username    string              `json:"username"`
	FirstLogin  int64               `json:"first_login"`
	LastLogin   int64               `json:"last_login"`
	Ranks       []player.RankDetail `json:"ranks"`
	DisplayRank *player.RankDetail  `json:"display_rank,omitempty"`
}

func runPlayerGet(cmd *cobra.Command, args []string) error {
	identifier := args[0]
	ctx := cmd.Context()

	apiClient, err := GetAPIClient()
	if err != nil {
		return err
	}

	summary, err := apiClient.GetPlayerSummary(ctx, identifier)
	if err != nil {
		if errors.Is(err, player.ErrPlayerNotFound) {
			if jsonFlag {
				return ui.PrintJSON(os.Stdout, map[string]interface{}{
					"success":    false,
					"error":      "player not found in SMEssential database",
					"identifier": identifier,
				})
			}
			return fmt.Errorf("player %q not found in SMEssential database", identifier)
		}
		return err
	}

	if jsonFlag {
		output := playerProfileOutput{
			UUID:        summary.Player.UUID,
			Username:    summary.Player.Username,
			FirstLogin:  summary.Player.FirstJoin,
			LastLogin:   summary.Player.LastJoin,
			Ranks:       summary.Ranks,
			DisplayRank: summary.DisplayRank,
		}
		return ui.PrintJSON(os.Stdout, output)
	}

	if !quietFlag {
		ui.PrintBanner()
	}

	ui.PrintPlayerProfile(os.Stdout, summary)
	return nil
}
