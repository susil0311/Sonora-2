/*
 * Sonora Project (2026)
 * Original project attribution: Chartreux Westia (github.com/koiverse)
 * Maintained by Susil (github.com/susil0311)
 * Licensed under GPL-3.0 | see git history for contributors
 */




package com.my.kizzy.gateway.entities

import com.my.kizzy.gateway.entities.op.OpCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Payload(
    @SerialName("t")
    val t: String? = null,
    @SerialName("s")
    val s: Int? = null,
    @SerialName("op")
    val op: OpCode? = null,
    @SerialName("d")
    val d: JsonElement? = null,
)