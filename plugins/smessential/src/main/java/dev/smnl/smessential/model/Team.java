package dev.smnl.smessential.model;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public class Team {

  private final Set<UUID> members = ConcurrentHashMap.newKeySet();

  public Team(@NotNull UUID firstMember, @NotNull UUID secondMember) {
    members.add(firstMember);
    members.add(secondMember);
  }

  public boolean addMember(@NotNull UUID uuid) {
    return members.add(uuid);
  }

  public boolean removeMember(@NotNull UUID uuid) {
    return members.remove(uuid);
  }

  public boolean contains(@NotNull UUID uuid) {
    return members.contains(uuid);
  }

  public @NotNull Set<UUID> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  public int size() {
    return members.size();
  }
}
