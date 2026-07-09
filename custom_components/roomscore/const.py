DOMAIN = "roomscore"

ATTR_TEMPERATURE = "temperature"
ATTR_HUMIDITY = "humidity"
ATTR_ILLUMINANCE = "illuminance"
ATTR_CO2 = "co2"
ATTR_PRESSURE = "pressure"
ATTR_NOISE = "noise"
ATTR_AIR_QUALITY = "air_quality"
ATTR_WINDOW_OPEN = "is_window_open"
ATTR_MOTION = "has_motion"
ATTR_ROOM_TYPE = "room_type"
ATTR_ROOM_SCORE = "room_score"
ATTR_SUGGESTIONS = "suggestions"
ATTR_CHORES = "chores"

SENSOR_TYPES = {
    ATTR_TEMPERATURE: {"name": "Temperature", "unit": "\u00b0C", "icon": "mdi:thermometer"},
    ATTR_HUMIDITY: {"name": "Humidity", "unit": "%", "icon": "mdi:water-percent"},
    ATTR_ILLUMINANCE: {"name": "Illuminance", "unit": "lux", "icon": "mdi:brightness-5"},
    ATTR_CO2: {"name": "CO2", "unit": "ppm", "icon": "mdi:molecule-co2"},
    ATTR_PRESSURE: {"name": "Pressure", "unit": "hPa", "icon": "mdi:gauge"},
    ATTR_NOISE: {"name": "Noise", "unit": "dB", "icon": "mdi:ear-hearing"},
}

CONF_ROOM_SENSORS = "room_sensors"
DEFAULT_SCAN_INTERVAL = 60
