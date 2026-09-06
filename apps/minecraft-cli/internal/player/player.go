package player

import (
	"errors"
	"fmt"
	"regexp"
	"strings"
)

var (
	ErrPlayerNotFound = errors.New("player not found in SMEssential database")
	uuidRegexNoDashes = regexp.MustCompile(`^[0-9a-fA-F]{32}$`)
	uuidRegexDashed   = regexp.MustCompile(`^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$`)
)

// NormalizeUUID normalizes 32-char hex string into 8-4-4-4-12 standard UUID format if needed.
func NormalizeUUID(input string) string {
	trimmed := strings.TrimSpace(input)
	if uuidRegexNoDashes.MatchString(trimmed) {
		return fmt.Sprintf("%s-%s-%s-%s-%s",
			trimmed[0:8], trimmed[8:12], trimmed[12:16], trimmed[16:20], trimmed[20:32])
	}
	return trimmed
}

// IsUUID checks if a given string matches UUID formats (dashed or un-dashed).
func IsUUID(input string) bool {
	trimmed := strings.TrimSpace(input)
	return uuidRegexDashed.MatchString(trimmed) || uuidRegexNoDashes.MatchString(trimmed)
}

// PlayerInfo holds basic identity and profile data.
type PlayerInfo struct {
	UUID      string `json:"uuid"`
	Username  string `json:"username"`
	FirstJoin int64  `json:"first_join"`
	LastJoin  int64  `json:"last_join"`
	InUsers   bool   `json:"in_users"`
}

// PunishmentSummary represents a punishment record targeting this player.
type PunishmentSummary struct {
	ID        string `json:"id"`
	Type      string `json:"type"`
	Reason    string `json:"reason"`
	Issuer    string `json:"issuer"`
	CreatedAt int64  `json:"created_at"`
	ExpiresAt int64  `json:"expires_at"`
}

// WhitelistSummary represents a whitelist record.
type WhitelistSummary struct {
	Name    string `json:"name"`
	AddedBy string `json:"added_by"`
	AddedAt int64  `json:"added_at"`
}

// RankDetail contains rank information with proper display name and color.
type RankDetail struct {
	ID     string `json:"id"`
	Name   string `json:"name"`
	Color  string `json:"color"`
	Prefix string `json:"prefix"`
}

// PlayerSummary aggregates all database entities associated with a player.
type PlayerSummary struct {
	Player                 PlayerInfo          `json:"player"`
	Ranks                  []RankDetail        `json:"ranks"`
	DisplayRank            *RankDetail         `json:"display_rank"`
	TargetPunishments      []PunishmentSummary `json:"target_punishments"`
	IssuerPunishmentsCount int64               `json:"issuer_punishments_count"`
	Whitelist              *WhitelistSummary   `json:"whitelist"`
	TotalRecordsToDelete   int                 `json:"total_records_to_delete"`
}

// DeleteResult holds detailed counts of deleted records per table.
type DeleteResult struct {
	Player                     PlayerInfo `json:"player"`
	DeletedUsers               int64      `json:"deleted_users"`
	DeletedUserRanks           int64      `json:"deleted_user_ranks"`
	DeletedUserDisplayRanks    int64      `json:"deleted_user_display_ranks"`
	DeletedPunishments         int64      `json:"deleted_punishments"`
	DeletedWhitelist           int64      `json:"deleted_whitelist"`
	PreservedIssuerPunishments int64      `json:"preserved_issuer_punishments"`
	TotalDeleted               int64      `json:"total_deleted"`
}
