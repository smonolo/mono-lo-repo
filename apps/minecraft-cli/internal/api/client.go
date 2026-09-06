package api

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"time"

	"dev.smnl/minecraft-cli/internal/player"
)

// Client handles communication with the minecraft-api REST service.
type Client struct {
	BaseURL    string
	ApiKey     string
	HTTPClient *http.Client
}

// NewClient creates a new API client.
func NewClient(baseURL, apiKey string) *Client {
	trimmedURL := strings.TrimRight(baseURL, "/")
	if !strings.HasSuffix(trimmedURL, "/v1") {
		trimmedURL += "/v1"
	}
	return &Client{
		BaseURL: trimmedURL,
		ApiKey:  apiKey,
		HTTPClient: &http.Client{
			Timeout: 10 * time.Second,
		},
	}
}

type summaryResponse struct {
	Success bool                  `json:"success"`
	Summary *player.PlayerSummary `json:"summary"`
	Message string                `json:"message"`
}

type deleteResponse struct {
	Success bool                 `json:"success"`
	Result  *player.DeleteResult `json:"result"`
	Message string               `json:"message"`
}

type errorResponse struct {
	Message    interface{} `json:"message"`
	Error      string      `json:"error"`
	StatusCode int         `json:"statusCode"`
}

// GetPlayerSummary fetches player details and related records slated for deletion.
func (c *Client) GetPlayerSummary(ctx context.Context, identifier string) (*player.PlayerSummary, error) {
	endpoint := fmt.Sprintf("%s/admin/players/%s", c.BaseURL, url.PathEscape(identifier))
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, endpoint, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create HTTP request: %w", err)
	}

	c.setHeaders(req)

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to minecraft-api at %s: %w", c.BaseURL, err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	if resp.StatusCode == http.StatusNotFound {
		return nil, player.ErrPlayerNotFound
	}
	if resp.StatusCode == http.StatusUnauthorized || resp.StatusCode == http.StatusForbidden {
		return nil, fmt.Errorf("unauthorized: missing or invalid admin API key (check ADMIN_API_KEY)")
	}
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return nil, parseAPIError(resp.StatusCode, body)
	}

	var res summaryResponse
	if err := json.Unmarshal(body, &res); err != nil {
		return nil, fmt.Errorf("failed to parse API response: %w", err)
	}

	if res.Summary == nil {
		return nil, player.ErrPlayerNotFound
	}

	return res.Summary, nil
}

// DeletePlayer sends a DELETE request to minecraft-api to completely remove a player.
func (c *Client) DeletePlayer(ctx context.Context, identifier string) (*player.DeleteResult, error) {
	endpoint := fmt.Sprintf("%s/admin/players/%s", c.BaseURL, url.PathEscape(identifier))
	req, err := http.NewRequestWithContext(ctx, http.MethodDelete, endpoint, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create HTTP request: %w", err)
	}

	c.setHeaders(req)

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to minecraft-api at %s: %w", c.BaseURL, err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	if resp.StatusCode == http.StatusNotFound {
		return nil, player.ErrPlayerNotFound
	}
	if resp.StatusCode == http.StatusUnauthorized || resp.StatusCode == http.StatusForbidden {
		return nil, fmt.Errorf("unauthorized: missing or invalid admin API key (check ADMIN_API_KEY)")
	}
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return nil, parseAPIError(resp.StatusCode, body)
	}

	var res deleteResponse
	if err := json.Unmarshal(body, &res); err != nil {
		return nil, fmt.Errorf("failed to parse API response: %w", err)
	}

	if res.Result == nil {
		return nil, fmt.Errorf("API response did not return deletion result")
	}

	return res.Result, nil
}

func (c *Client) setHeaders(req *http.Request) {
	req.Header.Set("Accept", "application/json")
	if c.ApiKey != "" {
		req.Header.Set("Authorization", "Bearer "+c.ApiKey)
	}
}

func parseAPIError(statusCode int, body []byte) error {
	var errRes errorResponse
	if err := json.Unmarshal(body, &errRes); err == nil {
		switch msg := errRes.Message.(type) {
		case string:
			return fmt.Errorf("API error (HTTP %d): %s", statusCode, msg)
		case []interface{}:
			var strMsgs []string
			for _, m := range msg {
				strMsgs = append(strMsgs, fmt.Sprint(m))
			}
			return fmt.Errorf("API error (HTTP %d): %s", statusCode, strings.Join(strMsgs, ", "))
		}
	}
	return fmt.Errorf("API error (HTTP %d): %s", statusCode, strings.TrimSpace(string(body)))
}
