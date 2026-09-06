#ifndef UI_H
#define UI_H

#include <stdbool.h>
#include <stdio.h>
#include "player.h"

void ui_init(void);
bool ui_is_color_enabled(void);

void ui_print_banner(void);
void ui_print_player_profile(FILE *out, const player_summary_t *summary);
void ui_print_player_summary(FILE *out, const player_summary_t *summary);
void ui_print_player_list(FILE *out, const player_list_t *list);
void ui_print_delete_success(FILE *out, const delete_result_t *res);
bool ui_ask_confirmation(const char *prompt);

void ui_print_json_profile(FILE *out, const player_summary_t *summary);
void ui_print_json_dry_run(FILE *out, const player_summary_t *summary);
void ui_print_json_delete_success(FILE *out, const delete_result_t *res);
void ui_print_json_player_list(FILE *out, const player_list_t *list);
void ui_print_json_not_found(FILE *out, const char *identifier);

void ui_error(const char *fmt, ...);
void ui_warn(const char *fmt, ...);
void ui_success(const char *fmt, ...);

#endif /* UI_H */
