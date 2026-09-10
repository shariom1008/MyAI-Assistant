package com.example.myaiassistant

import java.util.Locale

object AurixScienceEngine {

    fun answer(command: String): String? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")

        if (c.isBlank()) return null

        // =========================================================
        // PHYSICS
        // =========================================================

        if (
            c.contains("speed of light") ||
            c.contains("light ki speed") ||
            c.contains("light speed")
        ) {
            return "Vacuum mein light ki speed approximately 299,792,458 meter per second hai, Boss."
        }

        if (
            c.contains("gravity") ||
            c.contains("earth gravity") ||
            c.contains("g ki value")
        ) {
            return "Earth par gravitational acceleration approximately 9.81 meter per second square hai, Boss."
        }

        if (
            c.contains("newton first law") ||
            c.contains("newton ka first law")
        ) {
            return "Newton ka first law kehta hai ki koi object rest mein hai to rest mein rahega aur motion mein hai to same motion mein rahega, jab tak external unbalanced force act na kare."
        }

        if (
            c.contains("newton second law") ||
            c.contains("newton ka second law")
        ) {
            return "Newton ka second law ke according force equals mass multiplied by acceleration, yani F equals m a, Boss."
        }

        if (
            c.contains("newton third law") ||
            c.contains("newton ka third law")
        ) {
            return "Newton ka third law kehta hai ki every action ke equal aur opposite reaction hota hai, Boss."
        }

        if (
            c.contains("formula of force") ||
            c.contains("force ka formula")
        ) {
            return "Force ka formula F equals m a hai, Boss."
        }

        if (
            c.contains("formula of work") ||
            c.contains("work ka formula")
        ) {
            return "Work ka basic formula W equals F into d hai, jab force aur displacement same direction mein ho."
        }

        if (
            c.contains("formula of power") ||
            c.contains("power ka formula")
        ) {
            return "Power ka formula P equals work divided by time hai, Boss."
        }

        if (
            c.contains("formula of kinetic energy") ||
            c.contains("kinetic energy formula")
        ) {
            return "Kinetic energy ka formula half m v square hai, Boss."
        }

        if (
            c.contains("formula of potential energy") ||
            c.contains("potential energy formula")
        ) {
            return "Gravitational potential energy ka formula m g h hai, Boss."
        }

        if (
            c.contains("unit of force") ||
            c.contains("force ki unit")
        ) {
            return "Force ki SI unit Newton hai, Boss."
        }

        if (
            c.contains("unit of energy") ||
            c.contains("energy ki unit")
        ) {
            return "Energy ki SI unit Joule hai, Boss."
        }

        if (
            c.contains("unit of power") ||
            c.contains("power ki unit")
        ) {
            return "Power ki SI unit Watt hai, Boss."
        }

        if (
            c.contains("unit of pressure") ||
            c.contains("pressure ki unit")
        ) {
            return "Pressure ki SI unit Pascal hai, Boss."
        }

        if (
            c.contains("what is pressure") ||
            c.contains("pressure kya hai")
        ) {
            return "Pressure force per unit area hota hai, Boss."
        }

        // =========================================================
        // CHEMISTRY
        // =========================================================

        if (
            c.contains("chemical formula of water") ||
            c.contains("water ka formula") ||
            c.contains("paani ka formula") ||
            c.contains("pani ka formula") ||
            c == "h2o"
        ) {
            return "Water ka chemical formula H2O hai, Boss."
        }

        if (
            c.contains("carbon dioxide formula") ||
            c.contains("co2 formula") ||
            c.contains("carbon dioxide ka formula")
        ) {
            return "Carbon dioxide ka chemical formula CO2 hai, Boss."
        }

        if (
            c.contains("oxygen formula") ||
            c.contains("oxygen ka formula")
        ) {
            return "Oxygen normally O2 molecule ke form mein hoti hai, Boss."
        }

        if (
            c.contains("hydrogen formula") ||
            c.contains("hydrogen ka formula")
        ) {
            return "Hydrogen normally H2 molecule ke form mein hota hai, Boss."
        }

        if (
            c.contains("what is atom") ||
            c.contains("atom kya hai") ||
            c.contains("atom kya hota hai")
        ) {
            return "Atom kisi element ki smallest unit hoti hai jo us element ki chemical identity maintain karti hai, Boss."
        }

        if (
            c.contains("what is molecule") ||
            c.contains("molecule kya hai") ||
            c.contains("molecule kya hota hai")
        ) {
            return "Molecule do ya do se zyada atoms ke chemical bonding se bana particle hota hai, Boss."
        }

        if (
            c.contains("what is element") ||
            c.contains("element kya hai") ||
            c.contains("element kya hota hai")
        ) {
            return "Element ek pure substance hai jo ek hi type ke atoms se bana hota hai, Boss."
        }

        if (
            c.contains("what is compound") ||
            c.contains("compound kya hai") ||
            c.contains("compound kya hota hai")
        ) {
            return "Compound do ya do se zyada different elements ke chemically combined form ko kehte hain, Boss."
        }

        if (
            c.contains("what is ph") ||
            c.contains("ph kya hai") ||
            c.contains("ph kya hota hai")
        ) {
            return "pH kisi aqueous solution ki acidity ya basicity ko express karta hai. Generally pH 7 neutral, 7 se kam acidic aur 7 se zyada basic hota hai."
        }

        if (
            c.contains("neutral ph") ||
            c.contains("neutral solution ph")
        ) {
            return "Pure water ka neutral pH approximately 7 hota hai at room temperature, Boss."
        }

        if (
            c.contains("what is acid") ||
            c.contains("acid kya hai") ||
            c.contains("acid kya hota hai")
        ) {
            return "Acid aisa substance hai jo aqueous solution mein hydrogen ions increase karta hai, Boss."
        }

        if (
            c.contains("what is base") ||
            c.contains("base kya hai") ||
            c.contains("base kya hota hai")
        ) {
            return "Base aisa substance hai jo acid ko neutralize kar sakta hai aur aqueous solution mein hydroxide ions provide kar sakta hai."
        }

        if (
            c.contains("periodic table") &&
            c.contains("elements kitne")
        ) {
            return "Periodic table mein currently 118 officially recognized elements hain, Boss."
        }

        if (
            c.contains("chemical symbol of sodium") ||
            c.contains("sodium ka symbol")
        ) {
            return "Sodium ka chemical symbol Na hai, Boss."
        }

        if (
            c.contains("chemical symbol of potassium") ||
            c.contains("potassium ka symbol")
        ) {
            return "Potassium ka chemical symbol K hai, Boss."
        }

        if (
            c.contains("chemical symbol of iron") ||
            c.contains("iron ka symbol")
        ) {
            return "Iron ka chemical symbol Fe hai, Boss."
        }

        if (
            c.contains("chemical symbol of gold") ||
            c.contains("gold ka symbol")
        ) {
            return "Gold ka chemical symbol Au hai, Boss."
        }

        // =========================================================
        // BIOLOGY
        // =========================================================

        if (
            c.contains("what is cell") ||
            c.contains("cell kya hai") ||
            c.contains("cell kya hota hai")
        ) {
            return "Cell living organisms ki basic structural aur functional unit hoti hai, Boss."
        }

        if (
            c.contains("what is dna") ||
            c.contains("dna kya hai") ||
            c.contains("dna kya hota hai")
        ) {
            return "DNA yani deoxyribonucleic acid genetic information ko store aur transmit karta hai, Boss."
        }

        if (
            c.contains("what is rna") ||
            c.contains("rna kya hai") ||
            c.contains("rna kya hota hai")
        ) {
            return "RNA yani ribonucleic acid gene expression aur protein synthesis jaise biological processes mein important role play karta hai."
        }

        if (
            c.contains("largest organ") ||
            c.contains("human body ka largest organ") ||
            c.contains("largest organ of human body")
        ) {
            return "Human body ka largest organ skin hai, Boss."
        }

        if (
            c.contains("human body mein bones") ||
            c.contains("human body has how many bones") ||
            c.contains("body mein kitni bones")
        ) {
            return "Adult human body mein normally 206 bones hoti hain, Boss."
        }

        if (
            c.contains("human heart chambers") ||
            c.contains("heart mein kitne chambers") ||
            c.contains("heart chambers")
        ) {
            return "Human heart mein 4 chambers hote hain: 2 atria aur 2 ventricles."
        }

        if (
            c.contains("human teeth") ||
            c.contains("adult teeth") ||
            c.contains("insaan ke kitne daant")
        ) {
            return "Adult human ke normally 32 permanent teeth hote hain, Boss."
        }

        if (
            c.contains("what is photosynthesis") ||
            c.contains("photosynthesis kya hai") ||
            c.contains("photosynthesis kya hota hai")
        ) {
            return "Photosynthesis mein green plants sunlight ki help se carbon dioxide aur water se glucose banate hain aur oxygen release karte hain."
        }

        if (
            c.contains("what is chlorophyll") ||
            c.contains("chlorophyll kya hai") ||
            c.contains("chlorophyll kya hota hai")
        ) {
            return "Chlorophyll plants mein present green pigment hai jo photosynthesis ke liye light energy absorb karta hai."
        }

        if (
            c.contains("what is blood") ||
            c.contains("blood kya hai") ||
            c.contains("blood kya hota hai")
        ) {
            return "Blood ek connective tissue hai jo body mein oxygen, nutrients, hormones aur waste products transport karta hai."
        }

        if (
            c.contains("red blood cells") ||
            c.contains("rbc kya hai") ||
            c.contains("rbc kya hota hai")
        ) {
            return "Red blood cells yani RBCs mainly hemoglobin ki help se oxygen transport karte hain, Boss."
        }

        if (
            c.contains("white blood cells") ||
            c.contains("wbc kya hai") ||
            c.contains("wbc kya hota hai")
        ) {
            return "White blood cells yani WBCs immune system ka important part hain aur infections se fight karne mein help karte hain."
        }

        if (
            c.contains("hemoglobin kya hai") ||
            c.contains("what is hemoglobin")
        ) {
            return "Hemoglobin red blood cells mein present protein hai jo mainly oxygen transport karta hai, Boss."
        }

        if (
            c.contains("brain kya hai") ||
            c.contains("what is brain")
        ) {
            return "Brain nervous system ka major control center hai jo movement, thoughts, memory aur many body functions ko coordinate karta hai."
        }

        // =========================================================
        // VITAMINS
        // =========================================================

        if (
            c.contains("vitamin c") &&
            (
                c.contains("source") ||
                c.contains("sources") ||
                c.contains("kahan") ||
                c.contains("kaam") ||
                c.contains("benefit")
            )
        ) {
            return "Vitamin C immune function, collagen formation aur antioxidant protection mein important role play karta hai. Citrus fruits iske common sources hain."
        }

        if (
            c.contains("vitamin d") &&
            (
                c.contains("source") ||
                c.contains("sources") ||
                c.contains("kahan") ||
                c.contains("kaam") ||
                c.contains("benefit")
            )
        ) {
            return "Vitamin D calcium absorption aur bone health ke liye important hai. Sunlight exposure body mein vitamin D production mein help karta hai."
        }

        if (
            c.contains("vitamin a") &&
            (
                c.contains("source") ||
                c.contains("sources") ||
                c.contains("kaam") ||
                c.contains("benefit")
            )
        ) {
            return "Vitamin A normal vision, immune function aur cell growth ke liye important hai."
        }

        if (
            c.contains("vitamin b12") &&
            (
                c.contains("kaam") ||
                c.contains("benefit") ||
                c.contains("what")
            )
        ) {
            return "Vitamin B12 red blood cell formation aur nervous system function ke liye important hai."
        }

        // =========================================================
        // GENERAL SCIENCE
        // =========================================================

        if (
            c.contains("what is science") ||
            c.contains("science kya hai") ||
            c.contains("science kya hota hai")
        ) {
            return "Science observation, experimentation aur evidence ke through natural world ko samajhne ka systematic method hai, Boss."
        }

        if (
            c.contains("what is physics") ||
            c.contains("physics kya hai") ||
            c.contains("physics kya hota hai")
        ) {
            return "Physics matter, energy, motion, forces aur nature ke fundamental laws ka study hai."
        }

        if (
            c.contains("what is chemistry") ||
            c.contains("chemistry kya hai") ||
            c.contains("chemistry kya hota hai")
        ) {
            return "Chemistry matter ki composition, properties, structure aur chemical reactions ka study hai."
        }

        if (
            c.contains("what is biology") ||
            c.contains("biology kya hai") ||
            c.contains("biology kya hota hai")
        ) {
            return "Biology living organisms aur unki life processes ka scientific study hai."
        }

        if (
            c.contains("boiling point of water") ||
            c.contains("water boiling point")
        ) {
            return "Normal atmospheric pressure par water ka boiling point 100 degree Celsius hai, Boss."
        }

        if (
            c.contains("freezing point of water") ||
            c.contains("water freezing point")
        ) {
            return "Normal atmospheric pressure par water ka freezing point 0 degree Celsius hai, Boss."
        }

        return null
    }
}
