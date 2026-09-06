package ui

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"strings"
	"time"

	"github.com/fatih/color"
	"dev.smnl/minecraft-cli/internal/player"
)

var (
	cyan    = color.New(color.FgCyan, color.Bold).SprintFunc()
	green   = color.New(color.FgGreen, color.Bold).SprintFunc()
	red     = color.New(color.FgRed, color.Bold).SprintFunc()
	yellow  = color.New(color.FgYellow, color.Bold).SprintFunc()
	magenta = color.New(color.FgMagenta).SprintFunc()
	dim     = color.New(color.Faint).SprintFunc()
	bold    = color.New(color.Bold).SprintFunc()
)

// PrintBanner outputs the CLI header
func PrintBanner() {
	fmt.Println(cyan("⛏  Minecraft SMEssential CLI"))
	fmt.Println(dim("──────────────────────────────────────────────────"))
}

// PrintPlayerSummary displays details of a player and the records that will be affected.
func PrintPlayerSummary(w io.Writer, s *player.PlayerSummary) {
	fmt.Fprintln(w, bold("\nTarget Player Details:"))
	fmt.Fprintf(w, "  • %-16s %s\n", bold("Username:"), green(s.Player.Username))
	fmt.Fprintf(w, "  • %-16s %s\n", bold("UUID:"), cyan(s.Player.UUID))

	if s.Player.FirstJoin > 0 {
		firstJoinTime := time.UnixMilli(s.Player.FirstJoin).Format("2006-01-02 15:04:05 MST")
		fmt.Fprintf(w, "  • %-16s %s\n", bold("First Join:"), dim(firstJoinTime))
	}
	if s.Player.LastJoin > 0 {
		lastJoinTime := time.UnixMilli(s.Player.LastJoin).Format("2006-01-02 15:04:05 MST")
		fmt.Fprintf(w, "  • %-16s %s\n", bold("Last Join:"), dim(lastJoinTime))
	}

	fmt.Fprintln(w, bold("\nDatabase Records to Delete:"))

	// 1. User Profile
	if s.Player.InUsers {
		fmt.Fprintf(w, "  %s smessential_users (profile)\n", red("[-]"))
	} else {
		fmt.Fprintf(w, "  %s smessential_users (not found)\n", dim("[ ]"))
	}

	// 2. Ranks
	if len(s.Ranks) > 0 {
		fmt.Fprintf(w, "  %s smessential_user_ranks (%d assigned: %s)\n",
			red("[-]"), len(s.Ranks), magenta(strings.Join(s.Ranks, ", ")))
	} else {
		fmt.Fprintf(w, "  %s smessential_user_ranks (no ranks assigned)\n", dim("[ ]"))
	}

	// 3. Display Rank
	if s.DisplayRank != nil {
		fmt.Fprintf(w, "  %s smessential_user_display_ranks (custom display: %s)\n",
			red("[-]"), magenta(*s.DisplayRank))
	} else {
		fmt.Fprintf(w, "  %s smessential_user_display_ranks (no custom display rank)\n", dim("[ ]"))
	}

	// 4. Target Punishments
	if len(s.TargetPunishments) > 0 {
		fmt.Fprintf(w, "  %s smessential_punishments (%d target records):\n",
			red("[-]"), len(s.TargetPunishments))
		for _, p := range s.TargetPunishments {
			fmt.Fprintf(w, "      - [%s] Reason: %q (by %s)\n",
				yellow(p.Type), p.Reason, dim(p.Issuer))
		}
	} else {
		fmt.Fprintf(w, "  %s smessential_punishments (0 target punishments)\n", dim("[ ]"))
	}

	// 5. Whitelist
	if s.Whitelist != nil {
		fmt.Fprintf(w, "  %s smessential_whitelist (whitelisted as %q, added by %s)\n",
			red("[-]"), s.Whitelist.Name, dim(s.Whitelist.AddedBy))
	} else {
		fmt.Fprintf(w, "  %s smessential_whitelist (not whitelisted)\n", dim("[ ]"))
	}

	// Preserved records info
	fmt.Fprintln(w, bold("\nPreserved Records (Integrity Protection):"))
	if s.IssuerPunishmentsCount > 0 {
		fmt.Fprintf(w, "  %s %d punishment(s) issued as staff will be %s (audit history retained)\n",
			green("[✓]"), s.IssuerPunishmentsCount, bold("PRESERVED"))
	} else {
		fmt.Fprintf(w, "  %s 0 staff punishments issued by this player\n", dim("[✓]"))
	}

	fmt.Fprintf(w, "\nTotal records slated for deletion: %s\n", red(fmt.Sprintf("%d", s.TotalRecordsToDelete)))
}

// AskConfirmation prompts the user for yes/no confirmation. Default is No.
func AskConfirmation(prompt string) bool {
	reader := bufio.NewReader(os.Stdin)
	fmt.Printf("\n%s %s ", yellow("?"), bold(prompt))
	fmt.Print(dim("[y/N]: "))

	input, err := reader.ReadString('\n')
	if err != nil {
		return false
	}
	trimmed := strings.ToLower(strings.TrimSpace(input))
	return trimmed == "y" || trimmed == "yes"
}

// PrintDeleteSuccess displays a detailed report of the deletion result.
func PrintDeleteSuccess(w io.Writer, res *player.DeleteResult) {
	fmt.Fprintln(w, green("\n✔ Player successfully deleted from SMEssential!"))
	fmt.Fprintln(w, dim("──────────────────────────────────────────────────"))
	fmt.Fprintf(w, "  • Target:                  %s (%s)\n", green(res.Player.Username), cyan(res.Player.UUID))
	fmt.Fprintf(w, "  • Users Table:             %d deleted\n", res.DeletedUsers)
	fmt.Fprintf(w, "  • User Ranks:              %d deleted\n", res.DeletedUserRanks)
	fmt.Fprintf(w, "  • User Display Ranks:      %d deleted\n", res.DeletedUserDisplayRanks)
	fmt.Fprintf(w, "  • Target Punishments:      %d deleted\n", res.DeletedPunishments)
	fmt.Fprintf(w, "  • Whitelist Entries:       %d deleted\n", res.DeletedWhitelist)
	fmt.Fprintf(w, "  • Staff Issuer Punishments: %d preserved\n", res.PreservedIssuerPunishments)
	fmt.Fprintln(w, dim("──────────────────────────────────────────────────"))
	fmt.Fprintf(w, "  %s %s\n", bold("Total Records Deleted:"), green(fmt.Sprintf("%d", res.TotalDeleted)))
}

// PrintJSON formats and prints any struct as indented JSON.
func PrintJSON(w io.Writer, v interface{}) error {
	data, err := json.MarshalIndent(v, "", "  ")
	if err != nil {
		return err
	}
	fmt.Fprintln(w, string(data))
	return nil
}

// Error prints an error message in red.
func Error(format string, a ...interface{}) {
	fmt.Fprintf(os.Stderr, "%s %s\n", red("✖ Error:"), fmt.Sprintf(format, a...))
}

// Warn prints a warning message in yellow.
func Warn(format string, a ...interface{}) {
	fmt.Fprintf(os.Stderr, "%s %s\n", yellow("! Warning:"), fmt.Sprintf(format, a...))
}

// Success prints a success message in green.
func Success(format string, a ...interface{}) {
	fmt.Printf("%s %s\n", green("✔"), fmt.Sprintf(format, a...))
}
