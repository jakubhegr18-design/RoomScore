package com.roomanalyzer.roomscore.analysis

import com.google.mlkit.vision.label.ImageLabel
import com.roomanalyzer.roomscore.data.HomeAssistantSensorData

data class IkeaRecommendation(
    val productName: String,
    val description: String,
    val price: String,
    val improvementContext: String,
    val productUrl: String
)

data class RoomAnalysis(
    val score: Int,
    val grade: String,
    val roomType: String,
    val strengths: List<String>,
    val improvements: List<String>,
    val chores: List<String>,
    val detectedItems: List<String>,
    val ikeaRecommendations: List<IkeaRecommendation>,
    val environmentalData: HomeAssistantSensorData? = null
)

object RoomAnalyzer {

    private val furnitureItems = setOf(
        "bed", "chair", "table", "desk", "sofa", "couch", "cabinet",
        "shelf", "bookshelf", "dresser", "wardrobe", "stool", "bench",
        "nightstand", "armchair", "cushion", "pillow", "mattress",
        "coffee table", "ottoman"
    )

    private val decorItems = setOf(
        "plant", "flower", "vase", "lamp", "clock", "painting", "picture",
        "mirror", "curtain", "rug", "carpet", "candle", "sculpture",
        "frame", "blind"
    )

    private val clutterItems = setOf(
        "bottle", "cup", "glass", "plate", "bowl", "food", "snack",
        "bag", "backpack", "clothing", "shoe", "towel", "paper", "magazine",
        "box", "container"
    )

    private val electronics = setOf(
        "television", "laptop", "computer", "phone", "tablet", "speaker",
        "headphone", "cable", "charger", "remote", "monitor", "keyboard",
        "mouse"
    )

    private val ikeaMap = mapOf(
        "nightstand" to listOf(
            IkeaRecommendation("MALM Nightstand", "Sleek nightstand with 2 drawers", "CZK 1,990", "Add a nightstand", "https://www.ikea.com/cz/en/p/malm-nightstand-white-90243276/"),
            IkeaRecommendation("RAST Nightstand", "Budget-friendly pine nightstand", "CZK 990", "Add a nightstand", "https://www.ikea.com/cz/en/p/rast-nightstand-pine-40529180/")
        ),
        "wardrobe" to listOf(
            IkeaRecommendation("PAX Wardrobe", "Customizable wardrobe system", "CZK 4,990+", "Add storage furniture", "https://www.ikea.com/cz/en/p/pax-wardrobe-white-s69022647/"),
            IkeaRecommendation("KLEPPSTAD Wardrobe", "Simple and affordable wardrobe", "CZK 2,290", "Add storage furniture", "https://www.ikea.com/cz/en/p/kleppstad-wardrobe-white-40464712/")
        ),
        "curtain" to listOf(
            IkeaRecommendation("MAJGULL Curtains", "Light-filtering cotton curtains", "CZK 499", "Add curtains or blinds", "https://www.ikea.com/cz/en/p/majgull-curtains-white-30455454/"),
            IkeaRecommendation("HOPPVALS Blind", "Blackout roller blind", "CZK 349", "Add curtains or blinds", "https://www.ikea.com/cz/en/p/hoppvals-blind-blackout-grey-80516403/")
        ),
        "lamp" to listOf(
            IkeaRecommendation("TERTIAL Work Lamp", "Adjustable task lighting", "CZK 499", "Add a lamp", "https://www.ikea.com/cz/en/p/tertial-work-lamp-dark-grey-20364853/"),
            IkeaRecommendation("RANARP Table Lamp", "Classic design with fabric shade", "CZK 799", "Add a lamp", "https://www.ikea.com/cz/en/p/ranarp-table-lamp-dark-gray-70470680/")
        ),
        "coffee table" to listOf(
            IkeaRecommendation("LACK Coffee Table", "Simple, lightweight coffee table", "CZK 799", "Add a coffee table", "https://www.ikea.com/cz/en/p/lack-coffee-table-white-20011413/"),
            IkeaRecommendation("VITTSJÖ Laptop Table", "Industrial-style coffee table", "CZK 1,290", "Add a coffee table", "https://www.ikea.com/cz/en/p/vittsjo-laptop-table-black-brown-glass-60352357/")
        ),
        "rug" to listOf(
            IkeaRecommendation("STOENSE Rug", "Textured wool-blend rug", "CZK 1,990", "Add a rug", "https://www.ikea.com/cz/en/p/stoense-rug-handwoven-flatwoven-gray-40546259/"),
            IkeaRecommendation("LANGSTED Rug", "Soft high-pile rug", "CZK 1,490", "Add a rug", "https://www.ikea.com/cz/en/p/langsted-rug-high-pile-gray-60500080/")
        ),
        "plant" to listOf(
            IkeaRecommendation("FEJKA Artificial Plant", "Low-maintenance artificial plant", "CZK 249", "Add a plant", "https://www.ikea.com/cz/en/p/fejka-artificial-potted-plant-indoor-outdoor-green-40424586/"),
            IkeaRecommendation("SNÄRT Plant Pot", "Minimalist ceramic pot + plant", "CZK 399", "Add a plant", "https://www.ikea.com/cz/en/p/snaert-plant-pot-piglet-terracotta-20597582/")
        ),
        "desk" to listOf(
            IkeaRecommendation("MICKE Desk", "Compact desk with storage", "CZK 1,990", "Add a desk", "https://www.ikea.com/cz/en/p/micke-desk-white-10349570/"),
            IkeaRecommendation("BEKANT Corner Desk", "Spacious sit-stand desk", "CZK 4,990", "Add a desk", "https://www.ikea.com/cz/en/p/bekant-corner-desk-right-white-70358273/")
        ),
        "chair" to listOf(
            IkeaRecommendation("MARKUS Office Chair", "Ergonomic mesh chair", "CZK 3,990", "Add an ergonomic chair", "https://www.ikea.com/cz/en/p/markus-office-chair-vissle-gray-90251128/"),
            IkeaRecommendation("FLINTAN Office Chair", "Budget ergonomic chair", "CZK 1,990", "Add an ergonomic chair", "https://www.ikea.com/cz/en/p/flintan-office-chair-black-10510405/")
        ),
        "bookshelf" to listOf(
            IkeaRecommendation("BILLY Bookshelf", "Classic versatile bookshelf", "CZK 1,290", "Add shelving", "https://www.ikea.com/cz/en/p/billy-bookshelf-white-70383433/"),
            IkeaRecommendation("KALLAX Shelf Unit", "Modern cube shelving", "CZK 1,690", "Add shelving", "https://www.ikea.com/cz/en/p/kallax-shelf-unit-white-40460179/")
        ),
        "mirror" to listOf(
            IkeaRecommendation("NISSEDAL Mirror", "Full-length standing mirror", "CZK 1,490", "Add a mirror", "https://www.ikea.com/cz/en/p/nissedal-mirror-white-20512560/"),
            IkeaRecommendation("LINDBYN Mirror", "Round wall mirror", "CZK 799", "Add a mirror", "https://www.ikea.com/cz/en/p/lindbyn-mirror-black-brown-40482492/")
        ),
        "cushion" to listOf(
            IkeaRecommendation("SÖDERHAMN Cushion", "Soft decorative cushion", "CZK 399", "Add cushions", "https://www.ikea.com/cz/en/p/soederhamn-cushion-cover-beige-70583710/"),
            IkeaRecommendation("GURLI Cushion", "Textured knit cushion", "CZK 249", "Add cushions", "https://www.ikea.com/cz/en/p/gurli-cushion-graywhite-40522068/")
        ),
        "decor" to listOf(
            IkeaRecommendation("SKOGSVIK Picture Frame", "Set of 3 photo frames", "CZK 499", "Add personal decor", "https://www.ikea.com/cz/en/p/skogsvik-picture-frame-set-white-80558351/"),
            IkeaRecommendation("BONDLIV Candle Set", "Scented candle set", "CZK 299", "Add personal decor", "https://www.ikea.com/cz/en/p/bondliv-scented-candle-set-white-beige-70571975/")
        ),
        "clutter" to listOf(
            IkeaRecommendation("KVISSLE Letterbox", "Wall-mounted mail organizer", "CZK 349", "Reduce clutter", "https://www.ikea.com/cz/en/p/kvissle-letterbox-white-20256304/"),
            IkeaRecommendation("SKÅDIS Pegboard", "Wall organization system", "CZK 499", "Reduce clutter", "https://www.ikea.com/cz/en/p/skadis-pegboard-white-30516470/")
        )
    )

    fun analyze(
        labels: List<ImageLabel>,
        environmentalData: HomeAssistantSensorData? = null
    ): RoomAnalysis {
        val detectedLabels = labels
            .filter { it.confidence > 0.5 }
            .map { it.text.lowercase() }

        val roomType = detectRoomType(detectedLabels)

        val foundFurniture = detectedLabels.filter { it in furnitureItems }.toSet()
        val foundDecor = detectedLabels.filter { it in decorItems }.toSet()
        val foundClutter = detectedLabels.filter { it in clutterItems }.toSet()
        val foundElectronics = detectedLabels.filter { it in electronics }.toSet()

        val envStrengths = buildEnvironmentalStrengths(environmentalData)
        val envImprovements = buildEnvironmentalImprovements(environmentalData)

        val strengths = envStrengths + buildStrengths(foundFurniture, foundDecor, foundElectronics, roomType)
        val improvements = envImprovements + buildImprovements(foundFurniture, foundDecor, foundClutter, roomType)
        val chores = buildChores(foundClutter, foundFurniture, foundDecor)
        val score = calculateScore(foundFurniture, foundDecor, foundClutter, roomType, environmentalData)
        val ikeaRecs = buildIkeaRecommendations(improvements, foundFurniture, foundDecor)

        return RoomAnalysis(
            score = score,
            grade = getGrade(score),
            roomType = roomType,
            strengths = strengths,
            improvements = improvements,
            chores = chores,
            detectedItems = detectedLabels,
            ikeaRecommendations = ikeaRecs,
            environmentalData = environmentalData
        )
    }

    private fun detectRoomType(labels: List<String>): String {
        return when {
            labels.any { it in setOf("bed", "mattress", "pillow", "bedroom", "nightstand") } -> "Bedroom"
            labels.any { it in setOf("sofa", "couch", "television", "coffee table", "living room") } -> "Living Room"
            labels.any { it in setOf("desk", "computer", "monitor", "office", "bookshelf") } -> "Home Office"
            labels.any { it in setOf("stove", "oven", "refrigerator", "kitchen", "sink") } -> "Kitchen"
            labels.any { it in setOf("toilet", "bathroom", "shower", "sink") } -> "Bathroom"
            labels.any { it in setOf("dining table", "chair", "dining room") } -> "Dining Room"
            else -> "Room"
        }
    }

    private fun buildStrengths(
        furniture: Set<String>,
        decor: Set<String>,
        electronics: Set<String>,
        roomType: String
    ): List<String> {
        val strengths = mutableListOf<String>()

        if (furniture.size >= 3) {
            strengths.add("Well-furnished with ${furniture.size} types of furniture detected")
        }
        if (decor.size >= 2) {
            strengths.add("Good decoration sense with ${decor.size} decorative items")
        }
        if (decor.any { it in setOf("plant", "flower") }) {
            strengths.add("Plants bring life and freshness to the room")
        }
        if (decor.any { it in setOf("lamp") }) {
            strengths.add("Good lighting setup enhances ambiance")
        }
        if (furniture.any { it in setOf("bed") }) {
            strengths.add("Proper sleeping arrangement in place")
        }
        if (furniture.any { it in setOf("desk") }) {
            strengths.add("Dedicated workspace available for productivity")
        }
        if (furniture.any { it in setOf("sofa", "couch", "armchair") }) {
            strengths.add("Comfortable seating available for relaxation")
        }
        if (electronics.any { it in setOf("television") }) {
            strengths.add("Entertainment setup in the room")
        }
        if (decor.any { it in setOf("mirror") }) {
            strengths.add("Mirror helps with spatial perception and grooming")
        }

        return strengths.ifEmpty { listOf("You have the basics covered") }
    }

    private fun buildImprovements(
        furniture: Set<String>,
        decor: Set<String>,
        clutter: Set<String>,
        roomType: String
    ): List<String> {
        val improvements = mutableListOf<String>()

        if (roomType == "Bedroom") {
            if (furniture.none { it in setOf("nightstand") }) {
                improvements.add("Add a nightstand for convenience beside the bed")
            }
            if (furniture.none { it in setOf("wardrobe", "dresser", "cabinet") }) {
                improvements.add("Install storage furniture like a wardrobe or dresser")
            }
            if (decor.none { it in setOf("curtain") }) {
                improvements.add("Add curtains or blinds for privacy and light control")
            }
            if (decor.none { it in setOf("lamp") }) {
                improvements.add("Add a bedside lamp for better ambient lighting")
            }
        }

        if (roomType == "Living Room") {
            if (furniture.none { it in setOf("coffee table", "table") }) {
                improvements.add("A coffee table would complete the seating area")
            }
            if (decor.none { it in setOf("rug", "carpet") }) {
                improvements.add("A rug would define the space and add warmth")
            }
            if (decor.none { it in setOf("plant", "flower") }) {
                improvements.add("Add indoor plants to freshen the air and decor")
            }
        }

        if (roomType == "Home Office") {
            if (furniture.none { it in setOf("chair") }) {
                improvements.add("An ergonomic chair is essential for a home office")
            }
            if (decor.none { it in setOf("lamp") }) {
                improvements.add("Good task lighting reduces eye strain while working")
            }
            if (decor.none { it in setOf("plant") }) {
                improvements.add("A desk plant can reduce stress and improve focus")
            }
        }

        if (furniture.none { it in setOf("shelf", "bookshelf", "cabinet") }) {
            improvements.add("Add shelving for better storage and display")
        }
        if (decor.none { it in setOf("mirror") }) {
            improvements.add("A mirror makes a room feel larger and brighter")
        }

        if (clutter.size >= 3) {
            improvements.add("Reduce visible clutter \u2014 ${clutter.size} loose items detected")
        }

        if (decor.none { it in setOf("plant", "flower") }) {
            improvements.add("Add at least one plant to improve air quality and mood")
        }

        if (decor.isEmpty()) {
            improvements.add("Consider adding decorative elements to make the space feel more personal")
        }

        if (furniture.isEmpty()) {
            improvements.add("The room appears under-furnished \u2014 consider adding essential furniture")
        }

        return improvements.distinct().ifEmpty { listOf("Your room looks well put together!") }
    }

    private fun buildChores(
        clutter: Set<String>,
        furniture: Set<String>,
        decor: Set<String>
    ): List<String> {
        val chores = mutableListOf<String>()

        if (clutter.size >= 3) {
            chores.add("Tidy up and put away ${clutter.size} loose items")
        }
        if (clutter.any { it in setOf("bottle", "cup", "glass", "plate", "bowl") }) {
            chores.add("Clear empty dishes and glasses to the kitchen")
        }
        if (clutter.any { it in setOf("clothing", "shoe") }) {
            chores.add("Pick up clothes and shoes and put them in storage")
        }
        if (clutter.any { it in setOf("paper", "magazine") }) {
            chores.add("Recycle old papers and magazines")
        }
        if (clutter.any { it in setOf("bag", "backpack") }) {
            chores.add("Store bags and backpacks in their designated spot")
        }
        if (decor.any { it in setOf("plant", "flower") }) {
            chores.add("Water the plants")
        }
        if (furniture.any { it in setOf("bed") }) {
            chores.add("Make the bed")
        }
        if (furniture.any { it in setOf("desk") }) {
            chores.add("Organize and wipe down the desk")
        }
        if (furniture.any { it in setOf("sofa", "couch", "armchair") }) {
            chores.add("Fluff cushions and straighten upholstery")
        }
        chores.add("Vacuum or sweep the floor")
        chores.add("Dust surfaces and shelves")
        if (decor.any { it in setOf("mirror") }) {
            chores.add("Clean the mirror")
        }

        return chores
    }

    private fun buildIkeaRecommendations(
        improvements: List<String>,
        furniture: Set<String>,
        decor: Set<String>
    ): List<IkeaRecommendation> {
        val recommendations = mutableListOf<IkeaRecommendation>()
        val improvementText = improvements.joinToString(" ").lowercase()

        for ((key, products) in ikeaMap) {
            if (improvementText.contains(key) || !furniture.any { it == key }) {
                if (recommendations.size < 6) {
                    recommendations.add(products.first())
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add(
                IkeaRecommendation(
                    "IKEA Gift Card",
                    "Let them pick their own upgrade",
                    "From CZK 200",
                    "General improvement",
                    "https://www.ikea.com/cz/en/customer-service/gift-cards/"
                )
            )
        }

        return recommendations.distinctBy { it.productName }.take(6)
    }

    private fun buildEnvironmentalStrengths(data: HomeAssistantSensorData?): List<String> {
        if (data == null) return emptyList()
        val strengths = mutableListOf<String>()
        data.temperature?.let { t ->
            if (t in 19f..25f) strengths.add("Comfortable room temperature at ${t.toInt()}\u00b0C")
        }
        data.humidity?.let { h ->
            if (h in 40f..60f) strengths.add("Optimal humidity level at ${h.toInt()}%")
        }
        data.illuminance?.let { l ->
            if (l > 300) strengths.add("Well-lit room with ${l.toInt()} lux")
        }
        data.airQuality?.let {
            if (it.lowercase() in setOf("good", "excellent", "healthy"))
                strengths.add("Good air quality detected")
        }
        return strengths
    }

    private fun buildEnvironmentalImprovements(data: HomeAssistantSensorData?): List<String> {
        if (data == null) return emptyList()
        val improvements = mutableListOf<String>()
        data.temperature?.let { t ->
            if (t < 18f) improvements.add("Room is cold (${t.toInt()}\u00b0C) \u2014 consider heating")
            if (t > 27f) improvements.add("Room is warm (${t.toInt()}\u00b0C) \u2014 ventilate or cool down")
        }
        data.humidity?.let { h ->
            if (h < 30f) improvements.add("Air is too dry (${h.toInt()}%) \u2014 use a humidifier")
            if (h > 70f) improvements.add("Air is too humid (${h.toInt()}%) \u2014 ventilate or use a dehumidifier")
        }
        data.illuminance?.let { l ->
            if (l < 100) improvements.add("Low light level (${l.toInt()} lux) \u2014 open curtains or add lighting")
        }
        data.isWindowOpen?.let { open ->
            if (open) improvements.add("Window is open")
        }
        data.co2?.let { c ->
            if (c > 1000) improvements.add("CO\u2082 level elevated (${c.toInt()} ppm) \u2014 ventilate the room")
        }
        return improvements
    }

    private fun calculateScore(
        furniture: Set<String>,
        decor: Set<String>,
        clutter: Set<String>,
        roomType: String,
        environmentalData: HomeAssistantSensorData? = null
    ): Int {
        var score = 60

        score += minOf(furniture.size * 5, 20)
        score += minOf(decor.size * 4, 16)
        score -= minOf(clutter.size * 4, 20)
        score += if (roomType != "Room") 4 else 0

        environmentalData?.let { env ->
            env.temperature?.let { t ->
                score += if (t in 19f..25f) 4 else if (t < 10f || t > 35f) -6 else 0
            }
            env.humidity?.let { h ->
                score += if (h in 40f..60f) 3 else if (h < 20f || h > 80f) -4 else 0
            }
            env.illuminance?.let { l ->
                score += if (l > 300) 3 else if (l < 50) -3 else 0
            }
            env.co2?.let { c ->
                if (c > 1500) score -= 5
            }
        }

        return score.coerceIn(0, 100)
    }

    private fun getGrade(score: Int): String = when {
        score >= 90 -> "A+"
        score >= 80 -> "A"
        score >= 70 -> "B"
        score >= 60 -> "C"
        score >= 50 -> "D"
        else -> "F"
    }
}
