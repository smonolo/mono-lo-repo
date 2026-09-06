package player

import (
	"testing"
)

func TestNormalizeUUID(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected string
	}{
		{
			name:     "already dashed UUID",
			input:    "7cd493a1-1214-4da3-9ac1-a0bfef50b75c",
			expected: "7cd493a1-1214-4da3-9ac1-a0bfef50b75c",
		},
		{
			name:     "un-dashed 32 hex chars",
			input:    "7cd493a112144da39ac1a0bfef50b75c",
			expected: "7cd493a1-1214-4da3-9ac1-a0bfef50b75c",
		},
		{
			name:     "plain username",
			input:    "smnl",
			expected: "smnl",
		},
		{
			name:     "username with spaces trimmed",
			input:    "  Notch  ",
			expected: "Notch",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			actual := NormalizeUUID(tt.input)
			if actual != tt.expected {
				t.Errorf("NormalizeUUID(%q) = %q, want %q", tt.input, actual, tt.expected)
			}
		})
	}
}

func TestIsUUID(t *testing.T) {
	tests := []struct {
		input    string
		expected bool
	}{
		{"7cd493a1-1214-4da3-9ac1-a0bfef50b75c", true},
		{"7CD493A1-1214-4DA3-9AC1-A0BFEF50B75C", true},
		{"7cd493a112144da39ac1a0bfef50b75c", true},
		{"smnl", false},
		{"invalid-uuid-format-here-12345", false},
		{"", false},
	}

	for _, tt := range tests {
		actual := IsUUID(tt.input)
		if actual != tt.expected {
			t.Errorf("IsUUID(%q) = %v, want %v", tt.input, actual, tt.expected)
		}
	}
}
