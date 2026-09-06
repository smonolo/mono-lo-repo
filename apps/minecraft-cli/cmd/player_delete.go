package cmd

import (
	"errors"
	"fmt"
	"os"

	"github.com/spf13/cobra"
	"dev.smnl/minecraft-cli/internal/player"
	"dev.smnl/minecraft-cli/internal/ui"
)

var (
	yesFlag    bool
	dryRunFlag bool
)

// playerDeleteCmd handles the deletion of a player from all SMEssential database tables.
var playerDeleteCmd = &cobra.Command{
	Use:     "delete <username|uuid>",
	Aliases: []string{"remove", "rm", "del"},
	Short:   "Completely delete a player and their target records via minecraft-api",
	Long: `Permanently removes a player and all linked records from the SMEssential database via minecraft-api.

Affected tables:
  • smessential_users (player profile)
  • smessential_user_ranks (assigned rank permissions)
  • smessential_user_display_ranks (custom player display rank)
  • smessential_punishments (ONLY where the player is the target, preserving staff audit logs)
  • smessential_whitelist (whitelist entries targeting this player)

Safe operations:
  • Punishments issued BY this player as staff are strictly PRESERVED to maintain audit history.
  • Cache invalidation on minecraft-api is automatically triggered.
  • All deletions are wrapped in an atomic PostgreSQL transaction on the API.`,
	Args: cobra.ExactArgs(1),
	RunE: runPlayerDelete,
}

func init() {
	playerDeleteCmd.Flags().BoolVarP(&yesFlag, "yes", "y", false, "Skip interactive confirmation prompt")
	playerDeleteCmd.Flags().BoolVar(&yesFlag, "force", false, "Alias for --yes")
	playerDeleteCmd.Flags().BoolVar(&dryRunFlag, "dry-run", false, "Simulate deletion without modifying the database")

	playerCmd.AddCommand(playerDeleteCmd)
}

func runPlayerDelete(cmd *cobra.Command, args []string) error {
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
			return fmt.Errorf("player %q not found in any SMEssential database records", identifier)
		}
		return err
	}

	// If dry-run mode is requested
	if dryRunFlag {
		if jsonFlag {
			return ui.PrintJSON(os.Stdout, map[string]interface{}{
				"dry_run": true,
				"summary": summary,
			})
		}

		if !quietFlag {
			ui.PrintBanner()
		}
		ui.PrintPlayerSummary(os.Stdout, summary)
		ui.Warn("Dry run completed: no database modifications were made.")
		return nil
	}

	// Interactive confirmation unless --yes / --force is specified
	if !yesFlag {
		if !quietFlag {
			ui.PrintBanner()
		}
		ui.PrintPlayerSummary(os.Stdout, summary)

		if summary.TotalRecordsToDelete == 0 {
			ui.Warn("No records found to delete.")
			return nil
		}

		confirmed := ui.AskConfirmation(fmt.Sprintf(
			"Permanently delete player %q (%s) and all %d related record(s)?",
			summary.Player.Username, summary.Player.UUID, summary.TotalRecordsToDelete,
		))

		if !confirmed {
			ui.Warn("Deletion cancelled by user. No records were modified.")
			return nil
		}
	}

	// Execute deletion via API
	res, err := apiClient.DeletePlayer(ctx, identifier)
	if err != nil {
		return fmt.Errorf("deletion failed via minecraft-api: %w", err)
	}

	if jsonFlag {
		return ui.PrintJSON(os.Stdout, map[string]interface{}{
			"success": true,
			"result":  res,
		})
	}

	if quietFlag {
		ui.Success("Deleted player %s (UUID: %s) - %d total records removed",
			res.Player.Username, res.Player.UUID, res.TotalDeleted)
		return nil
	}

	ui.PrintDeleteSuccess(os.Stdout, res)
	return nil
}
