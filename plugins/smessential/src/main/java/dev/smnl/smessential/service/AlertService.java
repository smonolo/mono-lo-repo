package dev.smnl.smessential.service;

import dev.smnl.smessential.util.MessageFormatter;
import dev.smnl.smessential.util.MessageFormatter.MessageType;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class AlertService {

  public void sendAlert(@NotNull String message) {
    Bukkit.getServer()
        .sendMessage(MessageFormatter.format("Alert", message, MessageType.INFO, true));
  }
}
