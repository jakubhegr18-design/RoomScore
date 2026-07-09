from __future__ import annotations

import logging
from typing import Any

from homeassistant.components.sensor import SensorEntity, SensorStateClass
from homeassistant.config_entries import ConfigEntry
from homeassistant.const import UnitOfTemperature, PERCENTAGE, UnitOfPressure, CONCENTRATION_PARTS_PER_MILLION
from homeassistant.core import HomeAssistant
from homeassistant.helpers.entity_platform import AddEntitiesCallback
from homeassistant.helpers.update_coordinator import CoordinatorEntity

from .const import DOMAIN, SENSOR_TYPES

_LOGGER = logging.getLogger(__name__)


async def async_setup_entry(
    hass: HomeAssistant,
    config_entry: ConfigEntry,
    async_add_entities: AddEntitiesCallback,
) -> None:
    entities = []
    for sensor_type, config in SENSOR_TYPES.items():
        entities.append(RoomScoreSensor(hass, sensor_type, config))
    async_add_entities(entities)


class RoomScoreSensor(SensorEntity):
    _attr_should_poll = True

    def __init__(
        self, hass: HomeAssistant, sensor_type: str, config: dict[str, Any]
    ) -> None:
        self.hass = hass
        self._sensor_type = sensor_type
        self._config = config
        self._attr_name = f"RoomScore {config['name']}"
        self._attr_unique_id = f"{DOMAIN}_{sensor_type}"
        self._attr_icon = config["icon"]
        self._attr_native_unit_of_measurement = config.get("unit")
        self._attr_state_class = SensorStateClass.MEASUREMENT

    @property
    def native_value(self) -> str | None:
        state_machine = self.hass.states
        for state in state_machine.async_all():
            entity_id = state.entity_id
            if not entity_id.startswith("sensor."):
                continue
            if self._sensor_type in entity_id.lower():
                try:
                    return state.state
                except (ValueError, TypeError):
                    continue
        return None
