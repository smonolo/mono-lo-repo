package cmd

import (
	"github.com/spf13/cobra"
)

// playerCmd represents the player management parent command
var playerCmd = &cobra.Command{
	Use:     "player",
	Aliases: []string{"players", "p"},
	Short:   "Manage players in the SMEssential database",
	Long:    `Perform operations on players stored in the SMEssential PostgreSQL database.`,
}

func init() {
	RootCmd.AddCommand(playerCmd)
}
