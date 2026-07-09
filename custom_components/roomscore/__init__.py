from __future__ import annotations

import logging
from typing import Any

from homeassistant.components.http import HomeAssistantView
from homeassistant.config_entries import ConfigEntry
from homeassistant.const import STATE_ON, STATE_OFF
from homeassistant.core import HomeAssistant
from homeassistant.helpers import device_registry as dr
import voluptuous as vol

from .const import DOMAIN, ATTR_TEMPERATURE, ATTR_HUMIDITY, ATTR_ILLUMINANCE, ATTR_CO2, ATTR_PRESSURE, ATTR_NOISE, ATTR_WINDOW_OPEN, ATTR_MOTION

_LOGGER = logging.getLogger(__name__)

PLATFORMS = ["sensor"]

CONFIG_SCHEMA = vol.Schema(
    {
        DOMAIN: vol.Schema(
            {
                vol.Optional("room_sensors", default=list): list,
            }
        )
    },
    extra=vol.ALLOW_EXTRA,
)


async def async_setup_entry(hass: HomeAssistant, entry: ConfigEntry) -> bool:
    hass.data.setdefault(DOMAIN, {})
    hass.data[DOMAIN][entry.entry_id] = entry.data

    hass.http.register_view(RoomScoreDataView(hass))

    await hass.config_entries.async_forward_entry_setups(entry, PLATFORMS)
    return True


async def async_unload_entry(hass: HomeAssistant, entry: ConfigEntry) -> bool:
    unload_ok = await hass.config_entries.async_unload_platforms(entry, PLATFORMS)
    if unload_ok:
        hass.data[DOMAIN].pop(entry.entry_id, None)
    return unload_ok


class RoomScoreDataView(HomeAssistantView):
    url = "/api/roomscore"
    name = "api:roomscore"
    requires_auth = True

    def __init__(self, hass: HomeAssistant) -> None:
        self.hass = hass

    async def get(self, request):
        sensors = self._find_nearby_sensors()
        return self.json(sensors)

    def _find_nearby_sensors(self) -> dict[str, Any]:
        state_machine = self.hass.states
        result = {}

        temp_match = _find_sensor(state_machine, "temperature")
        if temp_match:
            result[ATTR_TEMPERATURE] = temp_match

        hum_match = _find_sensor(state_machine, "humidity", ["moisture", "wet"])
        if hum_match:
            result[ATTR_HUMIDITY] = hum_match

        ill_match = _find_sensor(state_machine, "illuminance", ["lux", "light_sensor", "illuminance"])
        if ill_match:
            result[ATTR_ILLUMINANCE] = ill_match

        co2_match = _find_sensor(state_machine, "co2", ["carbon_dioxide", "co2"])
        if co2_match:
            result[ATTR_CO2] = co2_match

        press_match = _find_sensor(state_machine, "pressure", ["barometric"])
        if press_match:
            result[ATTR_PRESSURE] = press_match

        noise_match = _find_sensor(state_machine, "noise", ["sound", "db"])
        if noise_match:
            result[ATTR_NOISE] = noise_match

        aq_match = _find_sensor(state_machine, "air_quality", ["aqi", "airquality"])
        if aq_match:
            result["air_quality"] = aq_match

        window_entity = _find_binary_sensor(state_machine, "window")
        if window_entity:
            window_state = state_machine.get(window_entity)
            if window_state:
                result[ATTR_WINDOW_OPEN] = window_state.state == STATE_ON

        motion_entity = _find_binary_sensor(state_machine, "motion")
        if motion_entity:
            motion_state = state_machine.get(motion_entity)
            if motion_state:
                result[ATTR_MOTION] = motion_state.state == STATE_ON

        return result


def _find_sensor(state_machine, *keywords: str) -> str | None:
    for state in state_machine.async_all():
        entity_id = state.entity_id
        if not entity_id.startswith("sensor."):
            continue
        eid_lower = entity_id.lower()
        for kw_group in keywords:
            if isinstance(kw_group, str):
                if kw_group in eid_lower:
                    return state.state
            else:
                if any(kw in eid_lower for kw in kw_group):
                    return state.state
    return None


def _find_binary_sensor(state_machine, keyword: str) -> str | None:
    for state in state_machine.async_all():
        entity_id = state.entity_id
        if entity_id.startswith("binary_sensor.") and keyword in entity_id.lower():
            return entity_id
    return None
