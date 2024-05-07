/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.extension.special.move;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MoverDeserializer implements JsonDeserializer<Mover> {

  private final String typeElementName;
  private Gson gson;
  private final Map<String, Class<? extends Mover>> moverTypeRegistry;

  public MoverDeserializer(String typeElementName) {
    this.typeElementName = typeElementName;
    this.gson = new Gson();
    this.moverTypeRegistry = new HashMap<>();
  }

  public void registerBarnType(String typeName, Class<? extends Mover> moverType) {
    moverTypeRegistry.put(typeName, moverType);
  }

  public void setGson(Gson gson) {
    this.gson = gson;
  }

  @Override
  public Mover deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    JsonObject moverObject = json.getAsJsonObject();
    JsonElement typeElement = moverObject.get(typeElementName);

    Class<? extends Mover> moverClass = moverTypeRegistry.get(typeElement.getAsString());
    return gson.fromJson(moverObject, moverClass);
  }
}
