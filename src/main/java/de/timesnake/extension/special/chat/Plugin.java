/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin SPECIAL = new Plugin("Special", "EXS");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
