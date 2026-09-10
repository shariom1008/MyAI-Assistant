package com.example.myaiassistant

import java.util.Locale

object AurixEquipmentEngine {

    private fun normalize(text: String): String {
        return text
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun has(c: String, vararg words: String): Boolean {
        return words.any { c.contains(it) }
    }

    fun answer(command: String): String? {

        val c = normalize(command)

        if (c.isBlank()) {
            return null
        }

        // =====================================================
        // RMG — RAPID MIXER GRANULATOR
        // =====================================================

        if (has(
                c,
                "what is rmg",
                "rmg kya hai",
                "rapid mixer granulator",
                "rapid mixer granulator kya hai"
            )
        ) {
            return "RMG yani Rapid Mixer Granulator pharmaceutical manufacturing mein powders ko mix aur wet granulation ke liye commonly use hone wala equipment hai, Boss."
        }

        if (has(
                c,
                "rmg working",
                "rmg kaise work",
                "rmg kaise kaam",
                "rapid mixer granulator working"
            )
        ) {
            return "RMG mein impeller material ko mix aur agitate karta hai aur suitable granulating liquid ke addition ke baad wet mass ya granules develop hote hain. Chopper agglomerates ko break aur granulation ko control karne mein help karta hai, Boss."
        }

        if (has(
                c,
                "rmg parameters",
                "rmg critical parameters",
                "rmg ke parameters",
                "rmg mein kya parameters"
            )
        ) {
            return "RMG ke common process parameters mein impeller speed, chopper speed, mixing time, binder addition rate, binder quantity aur endpoint criteria include ho sakte hain. Exact ranges product aur validated process par depend karti hain, Boss."
        }

        if (has(
                c,
                "rmg impeller",
                "rmg impeller kya hai"
            )
        ) {
            return "RMG ka impeller bowl ke andar material ko mix aur move karta hai aur wet granulation process mein major mixing action provide karta hai, Boss."
        }

        if (has(
                c,
                "rmg chopper",
                "rmg chopper kya hai"
            )
        ) {
            return "RMG ka chopper high-speed cutting action provide karta hai aur wet mass ke agglomerates ko break karke granule size aur consistency control karne mein help karta hai, Boss."
        }

        // =====================================================
        // FBD — FLUID BED DRYER
        // =====================================================

        if (has(
                c,
                "what is fbd",
                "fbd kya hai",
                "fluid bed dryer",
                "fluid bed dryer kya hai"
            )
        ) {
            return "FBD yani Fluid Bed Dryer wet granules ko controlled hot air flow ke through dry karne ke liye pharmaceutical manufacturing mein commonly use hota hai, Boss."
        }

        if (has(
                c,
                "fbd working",
                "fbd kaise work",
                "fbd kaise kaam",
                "fluid bed dryer working"
            )
        ) {
            return "FBD mein heated filtered air product bed ke through pass hoti hai. Air flow particles ko fluidize karta hai aur moisture evaporation ke through granules dry hote hain, Boss."
        }

        if (has(
                c,
                "fbd parameters",
                "fbd critical parameters",
                "fbd ke parameters"
            )
        ) {
            return "FBD ke common parameters mein inlet air temperature, outlet air temperature, airflow, product temperature, drying time aur endpoint moisture criteria include ho sakte hain. Exact values product-specific hoti hain, Boss."
        }

        if (has(
                c,
                "fbd endpoint",
                "fbd drying endpoint",
                "fbd endpoint kaise"
            )
        ) {
            return "FBD drying endpoint generally predefined moisture or LOD target aur approved process criteria ke basis par establish kiya jata hai. Exact endpoint product-specific hota hai, Boss."
        }

        // =====================================================
        // BLENDERS
        // =====================================================

        if (has(
                c,
                "octagonal blender",
                "octagonal blender kya hai"
            )
        ) {
            return "Octagonal Blender pharmaceutical powders aur granules ko homogeneous mixing ke liye use hone wala tumble blender hai, Boss."
        }

        if (has(
                c,
                "octagonal blender working",
                "octagonal blender kaise work"
            )
        ) {
            return "Octagonal Blender container rotation ke through powder ya granules ko repeatedly tumble karta hai, jisse mixing aur distribution achieve hoti hai, Boss."
        }

        if (has(
                c,
                "bin blender",
                "bin blender kya hai"
            )
        ) {
            return "Bin Blender ek closed container-based blending equipment hai jisme material ko bin ke rotation ya tumbling action ke through mix kiya jata hai, Boss."
        }

        if (has(
                c,
                "double cone blender",
                "double cone blender kya hai"
            )
        ) {
            return "Double Cone Blender dry powders aur granules ki mixing ke liye use hota hai. Rotating double-cone vessel material ko tumbling action deta hai, Boss."
        }

        if (has(
                c,
                "conta blender",
                "conta blender kya hai"
            )
        ) {
            return "Conta Blender closed-system blending equipment hai jo pharmaceutical powders aur granules ko controlled mixing ke liye use kiya ja sakta hai, Boss."
        }

        if (has(
                c,
                "blender parameters",
                "blender critical parameters",
                "blending parameters"
            )
        ) {
            return "Blender ke common parameters mein blender speed, blending time, fill level, material characteristics aur loading sequence include ho sakte hain. Exact operating range equipment aur validated process par depend karti hai, Boss."
        }

        // =====================================================
        // MULTI MILL / CO-MILL / GRANULATOR
        // =====================================================

        if (has(
                c,
                "multi mill",
                "multimill",
                "multi mill kya hai"
            )
        ) {
            return "Multi Mill size reduction, de-lumping aur wet ya dry material processing ke liye pharmaceutical manufacturing mein commonly use hota hai, Boss."
        }

        if (has(
                c,
                "multi mill working",
                "multi mill kaise work"
            )
        ) {
            return "Multi Mill mein material rotating impeller ya beater action aur screen ke interaction se desired particle size range mein process hota hai, Boss."
        }

        if (has(
                c,
                "co mill",
                "comill",
                "co mill kya hai"
            )
        ) {
            return "Co-mill pharmaceutical powders ya granules ki controlled size reduction aur de-lumping ke liye use hota hai, Boss."
        }

        if (has(
                c,
                "oscillating granulator",
                "oscillating granulator kya hai"
            )
        ) {
            return "Oscillating Granulator wet mass ya dry material ko controlled granule size mein convert karne ke liye oscillating cutting action aur screen ka use karta hai, Boss."
        }

        // =====================================================
        // TRAY DRYER
        // =====================================================

        if (has(
                c,
                "tray dryer",
                "tray dryer kya hai"
            )
        ) {
            return "Tray Dryer ek batch drying equipment hai jisme material trays mein spread karke controlled heated air circulation ke through dry kiya jata hai, Boss."
        }

        if (has(
                c,
                "vacuum tray dryer",
                "vacuum tray dryer kya hai"
            )
        ) {
            return "Vacuum Tray Dryer reduced pressure conditions mein material ko dry karta hai aur heat-sensitive materials ke liye suitable applications ho sakti hain, Boss."
        }

        // =====================================================
        // MASS MIXER
        // =====================================================

        if (has(
                c,
                "mass mixer",
                "mass mixer kya hai"
            )
        ) {
            return "Mass Mixer powders ya wet mass ko mixing aur kneading action ke through process karne ke liye use hota hai, Boss."
        }

        // =====================================================
        // MANUFACTURING / STORAGE VESSELS
        // =====================================================

        if (has(
                c,
                "manufacturing vessel",
                "manufacturing vessel kya hai"
            )
        ) {
            return "Manufacturing Vessel pharmaceutical liquids ya other process materials ko mixing, heating, holding ya processing ke liye use kiya ja sakta hai, Boss."
        }

        if (has(
                c,
                "storage vessel",
                "storage vessel kya hai"
            )
        ) {
            return "Storage Vessel approved material ya process liquid ko controlled conditions mein temporarily ya defined duration tak hold karne ke liye use hota hai, Boss."
        }

        // =====================================================
        // TABLET COMPRESSION MACHINE
        // =====================================================

        if (has(
                c,
                "compression machine",
                "tablet compression machine",
                "rotary tablet press",
                "compression machine kya hai"
            )
        ) {
            return "Tablet Compression Machine granules ya powder blend ko punches aur dies ki help se compress karke tablets banati hai, Boss."
        }

        if (has(
                c,
                "compression machine working",
                "compression machine kaise work",
                "tablet press working"
            )
        ) {
            return "Rotary tablet press mein dies aur punches turret ke saath rotate karte hain. Filling, pre-compression, main compression aur ejection stages ke through tablet form hoti hai, Boss."
        }

        if (has(
                c,
                "compression parameters",
                "tablet compression parameters",
                "compression machine parameters"
            )
        ) {
            return "Compression machine ke common parameters mein tablet weight, hardness, thickness, compression force, pre-compression force, turret speed, feeder speed aur ejection force include ho sakte hain. Exact settings product-specific hoti hain, Boss."
        }

        if (has(
                c,
                "tablet weight variation",
                "weight variation compression",
                "tablet weight problem"
            )
        ) {
            return "Tablet weight variation ke possible contributing factors mein inconsistent powder flow, feeder setting, fill depth, turret speed, granule properties aur machine setup include ho sakte hain. Investigation approved process aur equipment controls ke according karni chahiye, Boss."
        }

        // =====================================================
        // DEDUSTER
        // =====================================================

        if (has(
                c,
                "tablet deduster",
                "deduster kya hai",
                "tablet deduster kya hai"
            )
        ) {
            return "Tablet Deduster compression ke baad tablets ki surface se loose powder aur dust remove karne ke liye use hota hai, Boss."
        }

        // =====================================================
        // METAL DETECTOR
        // =====================================================

        if (has(
                c,
                "metal detector",
                "metal detector kya hai",
                "pharma metal detector"
            )
        ) {
            return "Pharmaceutical Metal Detector tablets ya other products mein unwanted metallic contamination detect karne ke liye use hota hai. Sensitivity aur challenge checks approved procedure ke according control kiye jate hain, Boss."
        }

        // =====================================================
        // TABLET INSPECTION
        // =====================================================

        if (has(
                c,
                "tablet inspection machine",
                "tablet inspection",
                "tablet inspection machine kya hai"
            )
        ) {
            return "Tablet Inspection Machine tablets ko visual ya automated inspection ke through defects jaise broken, chipped, stained, damaged ya other specified defects ke liye inspect kar sakti hai, Boss."
        }

        // =====================================================
        // CAPSULE FILLING
        // =====================================================

        if (has(
                c,
                "capsule filling machine",
                "capsule filling machine kya hai"
            )
        ) {
            return "Capsule Filling Machine pharmaceutical powder, pellets, granules ya suitable formulation ko hard capsules ke shell mein fill karne ke liye use hoti hai, Boss."
        }

        if (has(
                c,
                "capsule polisher",
                "capsule polisher kya hai"
            )
        ) {
            return "Capsule Polisher filled capsules ki surface se loose powder remove karne aur capsule appearance improve karne ke liye use hota hai, Boss."
        }

        // =====================================================
        // COATING PAN / AUTO COATER
        // =====================================================

        if (has(
                c,
                "coating pan",
                "coating pan kya hai"
            )
        ) {
            return "Coating Pan tablets ko rotate karte hue coating solution ya suspension ko controlled spray ke through apply karne ke liye use hota hai, Boss."
        }

        if (has(
                c,
                "auto coater",
                "automatic coating machine",
                "auto coater kya hai"
            )
        ) {
            return "Automatic Tablet Coater controlled pan rotation, spray system, airflow aur drying conditions ke through tablets par coating apply karta hai, Boss."
        }

        if (has(
                c,
                "coating parameters",
                "tablet coating parameters",
                "coating machine parameters"
            )
        ) {
            return "Coating ke common parameters mein pan speed, spray rate, inlet air temperature, outlet temperature, airflow, atomization air aur coating suspension properties include ho sakte hain. Exact settings product-specific hoti hain, Boss."
        }

        // =====================================================
        // BLISTER / STRIP PACKING
        // =====================================================

        if (has(
                c,
                "blister packing machine",
                "blister machine",
                "blister packing kya hai"
            )
        ) {
            return "Blister Packing Machine tablets ya capsules ko formed cavities mein pack karke lidding foil se seal karti hai, Boss."
        }

        if (has(
                c,
                "strip packing machine",
                "strip packing",
                "strip packing machine kya hai"
            )
        ) {
            return "Strip Packing Machine tablets ya capsules ko suitable packing films ke beech seal karke strip packs banati hai, Boss."
        }

        if (has(
                c,
                "cartoner",
                "cartoning machine",
                "cartoner kya hai"
            )
        ) {
            return "Cartoner packing line mein primary packs ko cartons mein insert aur close karne ke liye use hota hai, Boss."
        }

        if (has(
                c,
                "labeling machine",
                "labeling machine kya hai"
            )
        ) {
            return "Labeling Machine containers ya packs par approved labels ko controlled manner mein apply karti hai, Boss."
        }

        if (has(
                c,
                "checkweigher",
                "checkweigher kya hai"
            )
        ) {
            return "Checkweigher packed product ya container ka weight automatically verify karne ke liye use hota hai according to approved requirements, Boss."
        }

        // =====================================================
        // DISSOLUTION TESTER
        // =====================================================

        if (has(
                c,
                "dissolution tester",
                "dissolution apparatus machine",
                "dissolution tester kya hai"
            )
        ) {
            return "Dissolution Tester specified medium, temperature, agitation aur sampling conditions mein dosage form se drug release ko evaluate karne ke liye use hota hai, Boss."
        }

        if (has(
                c,
                "dissolution tester parameters",
                "dissolution parameters",
                "dissolution machine parameters"
            )
        ) {
            return "Dissolution test ke common parameters mein apparatus type, medium, medium volume, temperature, rotation speed, sampling time aur analytical method conditions include ho sakti hain. Exact conditions approved method ya monograph ke according hoti hain, Boss."
        }

        // =====================================================
        // DISINTEGRATION TESTER
        // =====================================================

        if (has(
                c,
                "disintegration tester",
                "disintegration machine",
                "disintegration tester kya hai"
            )
        ) {
            return "Disintegration Tester tablets ya capsules ke breakdown ko specified test conditions mein evaluate karta hai, Boss."
        }

        // =====================================================
        // FRIABILITY TESTER
        // =====================================================

        if (has(
                c,
                "friability tester",
                "friability machine",
                "friability tester kya hai"
            )
        ) {
            return "Friability Tester tablets ki resistance to abrasion aur mechanical stress ko evaluate karta hai, Boss."
        }

        // =====================================================
        // HARDNESS TESTER
        // =====================================================

        if (has(
                c,
                "hardness tester",
                "tablet hardness tester",
                "hardness tester kya hai"
            )
        ) {
            return "Tablet Hardness Tester tablet ki crushing strength ya mechanical resistance measure karta hai, Boss."
        }

        if (has(
                c,
                "tablet thickness tester",
                "thickness tester",
                "tablet thickness"
            )
        ) {
            return "Tablet Thickness Tester tablet ki thickness ko measure karta hai. Thickness compression setup aur formulation characteristics se influence ho sakti hai, Boss."
        }

        // =====================================================
        // ANALYTICAL BALANCE
        // =====================================================

        if (has(
                c,
                "analytical balance",
                "analytical balance kya hai",
                "analytical weighing balance"
            )
        ) {
            return "Analytical Balance high-resolution weighing ke liye laboratory mein use hoti hai. Accurate measurement ke liye calibration status, leveling, environmental conditions aur proper weighing technique important hain, Boss."
        }

        if (has(
                c,
                "balance calibration",
                "analytical balance calibration",
                "balance calibration kya hai"
            )
        ) {
            return "Balance calibration mein suitable reference weights ke against balance indication verify ya calibrate ki jati hai according to approved calibration procedure. Acceptance criteria equipment procedure aur applicable requirements par depend karte hain, Boss."
        }

        if (has(
                c,
                "balance repeatability",
                "balance repeatability test"
            )
        ) {
            return "Balance repeatability test repeated weighing results ki consistency evaluate karta hai under defined conditions. Exact test procedure aur acceptance criteria approved calibration procedure ke according hone chahiye, Boss."
        }

        if (has(
                c,
                "corner load test",
                "corner loading balance",
                "balance corner load"
            )
        ) {
            return "Balance corner load ya eccentricity test mein load ko weighing pan ke different positions par place karke indication consistency evaluate ki jati hai according to the approved procedure, Boss."
        }

        // =====================================================
        // PRECISION BALANCE
        // =====================================================

        if (has(
                c,
                "precision balance",
                "precision balance kya hai"
            )
        ) {
            return "Precision Balance laboratory ya production weighing applications mein suitable readability aur capacity ke saath material weighing ke liye use hoti hai, Boss."
        }

        // =====================================================
        // PH METER
        // =====================================================

        if (has(
                c,
                "ph meter",
                "ph meter kya hai",
                "ph meter working"
            )
        ) {
            return "pH Meter solution ki acidity ya alkalinity ko pH measurement ke through determine karta hai. Glass electrode aur reference system commonly measurement mein use hote hain, Boss."
        }

        if (has(
                c,
                "ph meter calibration",
                "ph calibration",
                "ph meter calibration kya hai"
            )
        ) {
            return "pH meter calibration suitable certified or approved buffer solutions ke saath approved procedure ke according perform ki jati hai. Buffer selection aur acceptance criteria applicable method aur instrument procedure par depend karte hain, Boss."
        }

        // =====================================================
        // CONDUCTIVITY METER
        // =====================================================

        if (has(
                c,
                "conductivity meter",
                "conductivity meter kya hai",
                "conductivity kya measure karta hai"
            )
        ) {
            return "Conductivity Meter solution ki electrical conductivity measure karta hai aur pharmaceutical water systems mein water quality monitoring ke liye commonly use hota hai, Boss."
        }

        // =====================================================
        // UV-VISIBLE
        // =====================================================

        if (has(
                c,
                "uv visible spectrophotometer",
                "uv visible",
                "uv spectrophotometer",
                "uv spectrophotometer kya hai"
            )
        ) {
            return "UV-Visible Spectrophotometer sample ke light absorption ko specified wavelength range mein measure karta hai aur pharmaceutical analysis mein quantitative ya qualitative applications ke liye use ho sakta hai, Boss."
        }

        // =====================================================
        // HPLC
        // =====================================================

        if (has(
                c,
                "hplc instrument",
                "hplc machine",
                "hplc machine kya hai"
            )
        ) {
            return "HPLC instrument High Performance Liquid Chromatography perform karta hai. Iske major components mein solvent reservoir, pump, injector or autosampler, column, detector aur data system include hote hain, Boss."
        }

        if (has(
                c,
                "hplc main parts",
                "hplc ke parts",
                "hplc components"
            )
        ) {
            return "HPLC ke main components solvent reservoir, degasser, pump, injector ya autosampler, analytical column, column oven where applicable, detector aur chromatography data system hote hain, Boss."
        }

        if (has(
                c,
                "hplc working",
                "hplc kaise work",
                "hplc kaise kaam"
            )
        ) {
            return "HPLC mein mobile phase pump ke through column se pass hoti hai. Sample column mein inject hota hai, components different interactions ke basis par separate hote hain aur detector unhe detect karta hai, Boss."
        }

        if (has(
                c,
                "hplc pressure",
                "hplc high pressure",
                "hplc pressure problem"
            )
        ) {
            return "HPLC pressure change ke possible causes mein blocked filter, guard column ya analytical column restriction, mobile phase issue, tubing restriction aur other system conditions include ho sakte hain. Troubleshooting approved SOP aur instrument procedure ke according karni chahiye, Boss."
        }

        // =====================================================
        // GC
        // =====================================================

        if (has(
                c,
                "gc instrument",
                "gc machine",
                "gc machine kya hai"
            )
        ) {
            return "GC yani Gas Chromatography instrument volatile ya suitable analytes ko separate aur analyze karta hai. Major components mein carrier gas system, injector, column, oven, detector aur data system include hote hain, Boss."
        }

        // =====================================================
        // FTIR
        // =====================================================

        if (has(
                c,
                "ftir instrument",
                "ftir machine",
                "ftir machine kya hai"
            )
        ) {
            return "FTIR instrument infrared radiation ke interaction ko measure karke material identification aur characterization mein help karta hai, Boss."
        }

        // =====================================================
        // KARL FISCHER
        // =====================================================

        if (has(
                c,
                "karl fischer",
                "kf titrator",
                "karl fischer instrument"
            )
        ) {
            return "Karl Fischer instrument sample mein water content determine karne ke liye volumetric ya coulometric Karl Fischer titration principle ka use karta hai, Boss."
        }

        if (has(
                c,
                "karl fischer working",
                "kf working",
                "karl fischer kaise work"
            )
        ) {
            return "Karl Fischer titration water aur Karl Fischer reagent ke specific chemical reaction par based hoti hai. Instrument endpoint detect karke sample ka water content calculate karta hai, Boss."
        }

        // =====================================================
        // MOISTURE ANALYZER
        // =====================================================

        if (has(
                c,
                "moisture analyzer",
                "moisture analyzer kya hai",
                "moisture balance"
            )
        ) {
            return "Moisture Analyzer sample ko controlled heating ke through dry karke mass loss measure karta hai aur calculated result ko moisture ya moisture-related percentage ke form mein report kar sakta hai, Boss."
        }

        if (has(
                c,
                "moisture analyzer working",
                "moisture analyzer kaise work"
            )
        ) {
            return "Moisture Analyzer sample ka initial mass measure karta hai, controlled heating apply karta hai aur mass change ke basis par moisture result calculate karta hai, Boss."
        }

        // =====================================================
        // MELTING POINT
        // =====================================================

        if (has(
                c,
                "melting point apparatus",
                "melting point instrument",
                "melting point apparatus kya hai"
            )
        ) {
            return "Melting Point Apparatus solid material ke melting behaviour aur melting range ko controlled heating ke under evaluate karne ke liye use hota hai, Boss."
        }

        // =====================================================
        // POLARIMETER
        // =====================================================

        if (has(
                c,
                "polarimeter",
                "polarimeter kya hai",
                "polarimeter working"
            )
        ) {
            return "Polarimeter optically active substances ke plane-polarized light rotation ko measure karta hai aur pharmaceutical analysis mein specific rotation related applications ke liye use ho sakta hai, Boss."
        }

        // =====================================================
        // REFRACTOMETER
        // =====================================================

        if (has(
                c,
                "refractometer",
                "refractometer kya hai"
            )
        ) {
            return "Refractometer material ka refractive index measure karta hai aur suitable pharmaceutical liquid analysis mein use ho sakta hai, Boss."
        }

        // =====================================================
        // TOC ANALYZER
        // =====================================================

        if (has(
                c,
                "toc analyzer",
                "toc analyzer kya hai",
                "total organic carbon analyzer"
            )
        ) {
            return "TOC Analyzer yani Total Organic Carbon Analyzer water systems mein organic carbon level ko monitor karne ke liye use hota hai, Boss."
        }

        // =====================================================
        // AUTOCLAVE
        // =====================================================

        if (has(
                c,
                "autoclave",
                "autoclave kya hai",
                "autoclave working"
            )
        ) {
            return "Autoclave moist heat sterilization ke liye pressurized steam use karta hai. Sterilization cycle ke time, temperature, pressure aur load-related parameters approved cycle ke according control kiye jate hain, Boss."
        }

        // =====================================================
        // HOT AIR OVEN
        // =====================================================

        if (has(
                c,
                "hot air oven",
                "hot air oven kya hai"
            )
        ) {
            return "Hot Air Oven dry heat ke through suitable materials ya laboratory items ko heat ya sterilization-related applications mein process karne ke liye use ho sakta hai, Boss."
        }

        // =====================================================
        // MUFFLE FURNACE
        // =====================================================

        if (has(
                c,
                "muffle furnace",
                "muffle furnace kya hai"
            )
        ) {
            return "Muffle Furnace high-temperature heating applications ke liye use hota hai, including suitable laboratory testing such as residue on ignition or ash-related procedures where applicable, Boss."
        }

        // =====================================================
        // STABILITY CHAMBER
        // =====================================================

        if (has(
                c,
                "stability chamber",
                "stability chamber kya hai",
                "stability chamber working"
            )
        ) {
            return "Stability Chamber controlled temperature aur humidity conditions maintain karke pharmaceutical samples ki stability studies perform karne ke liye use hota hai, Boss."
        }

        if (has(
                c,
                "stability chamber parameters",
                "stability chamber monitoring"
            )
        ) {
            return "Stability Chamber mein temperature, relative humidity where applicable, alarm status, monitoring system aur chamber mapping or qualification controls important hote hain. Exact conditions approved stability protocol par depend karti hain, Boss."
        }

        // =====================================================
        // REFRIGERATOR / FREEZER
        // =====================================================

        if (has(
                c,
                "pharma refrigerator",
                "laboratory refrigerator",
                "pharma freezer"
            )
        ) {
            return "Pharmaceutical refrigerator ya freezer temperature-sensitive materials aur samples ko defined storage conditions mein maintain karne ke liye use kiya jata hai, Boss."
        }

        // =====================================================
        // MICROSCOPES
        // =====================================================

        if (has(
                c,
                "microscope",
                "microscope kya hai",
                "pharma microscope"
            )
        ) {
            return "Microscope small particles, microorganisms ya sample characteristics ko magnification ke through observe karne ke liye laboratory mein use hota hai, Boss."
        }

        // =====================================================
        // COLONY COUNTER
        // =====================================================

        if (has(
                c,
                "colony counter",
                "colony counter kya hai"
            )
        ) {
            return "Colony Counter microbiological plates par visible microbial colonies ko count karne mein help karta hai, Boss."
        }

        // =====================================================
        // AIR SAMPLER
        // =====================================================

        if (has(
                c,
                "air sampler",
                "air sampler kya hai",
                "microbial air sampler"
            )
        ) {
            return "Microbial Air Sampler controlled volume of air draw karke suitable culture medium par viable airborne microorganisms ke monitoring ke liye use hota hai, Boss."
        }

        // =====================================================
        // PARTICLE COUNTER
        // =====================================================

        if (has(
                c,
                "particle counter",
                "particle counter kya hai",
                "airborne particle counter"
            )
        ) {
            return "Particle Counter air mein specified particle sizes ki concentration ko monitor karne ke liye use hota hai, especially controlled pharmaceutical environments mein, Boss."
        }

        // =====================================================
        // HVAC
        // =====================================================

        if (has(
                c,
                "hvac system",
                "hvac equipment",
                "hvac kya hai"
            )
        ) {
            return "Pharmaceutical HVAC system temperature, humidity, airflow, filtration aur pressure relationships jaise environmental parameters ko control karne mein important role play karta hai, Boss."
        }

        // =====================================================
        // AHU
        // =====================================================

        if (has(
                c,
                "ahu",
                "ahu kya hai",
                "air handling unit"
            )
        ) {
            return "AHU yani Air Handling Unit HVAC system ka major equipment hai jo air ko filter, condition aur distribute karne mein help karta hai. Isme application ke according filters, coils, fans aur other components ho sakte hain, Boss."
        }

        if (has(
                c,
                "ahu and hvac difference",
                "difference between ahu and hvac",
                "ahu hvac difference"
            )
        ) {
            return "HVAC ek complete heating, ventilation aur air-conditioning system concept hai, jabki AHU us system ka ek major air-handling equipment hai, Boss."
        }

        // =====================================================
        // CHILLER
        // =====================================================

        if (has(
                c,
                "chiller",
                "chiller kya hai",
                "pharma chiller"
            )
        ) {
            return "Chiller process ya HVAC applications ke liye chilled water ya suitable cooling fluid provide karta hai, Boss."
        }

        // =====================================================
        // COOLING TOWER
        // =====================================================

        if (has(
                c,
                "cooling tower",
                "cooling tower kya hai"
            )
        ) {
            return "Cooling Tower warm water se heat atmosphere mein reject karne ke liye evaporation aur air contact ka use karta hai, Boss."
        }

        // =====================================================
        // AIR COMPRESSOR
        // =====================================================

        if (has(
                c,
                "air compressor",
                "air compressor kya hai",
                "pharma air compressor"
            )
        ) {
            return "Air Compressor compressed air generate karta hai jo pharmaceutical plant mein pneumatic equipment, valves, instruments aur other approved applications mein use ho sakti hai, Boss."
        }

        // =====================================================
        // NITROGEN GENERATOR
        // =====================================================

        if (has(
                c,
                "nitrogen generator",
                "nitrogen generator kya hai"
            )
        ) {
            return "Nitrogen Generator air separation technology ke through nitrogen-rich gas produce karta hai, jo suitable pharmaceutical processes mein inerting ya other controlled applications ke liye use ho sakti hai, Boss."
        }

        // =====================================================
        // BOILER / STEAM
        // =====================================================

        if (has(
                c,
                "boiler",
                "boiler kya hai",
                "pharma boiler"
            )
        ) {
            return "Boiler water ko heat karke steam generate karta hai. Pharmaceutical facility mein steam ka use application ke according process heating, utility systems ya other approved purposes ke liye ho sakta hai, Boss."
        }

        if (has(
                c,
                "pharma steam",
                "steam system",
                "steam system kya hai"
            )
        ) {
            return "Pharmaceutical steam systems process aur utility requirements ke according designed aur controlled hote hain. Steam quality requirements application-specific hoti hain, Boss."
        }

        // =====================================================
        // RO PLANT
        // =====================================================

        if (has(
                c,
                "ro plant",
                "ro plant kya hai",
                "reverse osmosis"
            )
        ) {
            return "RO yani Reverse Osmosis water purification process hai jisme pressure ke through water ko semi-permeable membrane se pass karke many dissolved impurities ko reduce kiya jata hai, Boss."
        }

        // =====================================================
        // DM PLANT
        // =====================================================

        if (has(
                c,
                "dm plant",
                "dm plant kya hai",
                "demineralized water"
            )
        ) {
            return "DM Plant yani Demineralization system water se dissolved ionic impurities ko reduce ya remove karne ke liye ion-exchange based processes use kar sakta hai, Boss."
        }

        // =====================================================
        // PURIFIED WATER SYSTEM
        // =====================================================

        if (has(
                c,
                "purified water system",
                "pw system",
                "purified water plant"
            )
        ) {
            return "Purified Water System pharmaceutical applications ke liye controlled quality water generate, store aur distribute karta hai. System monitoring, sanitization aur applicable quality controls important hote hain, Boss."
        }

        // =====================================================
        // WFI SYSTEM
        // =====================================================

        if (has(
                c,
                "wfi system",
                "wfi plant",
                "water for injection system"
            )
        ) {
            return "WFI System Water for Injection quality requirements ko meet karne ke liye designed pharmaceutical water system hai. Generation, storage, distribution, sanitization aur monitoring system-specific controls ke according hote hain, Boss."
        }

        // =====================================================
        // ETP
        // =====================================================

        if (has(
                c,
                "etp",
                "etp plant",
                "effluent treatment plant"
            )
        ) {
            return "ETP yani Effluent Treatment Plant industrial wastewater ko treatment processes ke through applicable discharge requirements ke according manage aur treat karta hai, Boss."
        }

        // =====================================================
        // DG
        // =====================================================

        if (has(
                c,
                "dg set",
                "dg",
                "diesel generator"
            )
        ) {
            return "DG yani Diesel Generator electrical power backup provide karta hai jab normal power supply unavailable ya interrupted ho, Boss."
        }

        // =====================================================
        // VACUUM PUMP
        // =====================================================

        if (has(
                c,
                "vacuum pump",
                "vacuum pump kya hai"
            )
        ) {
            return "Vacuum Pump system se gas ya air remove karke reduced pressure create karta hai. Pharmaceutical applications mein drying, filtration ya process equipment ke according use ho sakta hai, Boss."
        }

        // =====================================================
        // GENERAL EQUIPMENT TROUBLESHOOTING
        // =====================================================

        if (has(
                c,
                "equipment breakdown",
                "equipment breakdown kya hai",
                "machine breakdown"
            )
        ) {
            return "Pharmaceutical equipment breakdown ke time equipment ko safe condition mein rakhna, operation ko appropriately stop karna, event ko document karna aur authorized engineering procedure ke according troubleshooting aur repair perform karna important hai, Boss."
        }

        if (has(
                c,
                "equipment troubleshooting",
                "machine troubleshooting",
                "equipment problem"
            )
        ) {
            return "Equipment troubleshooting mein alarm ya symptom identify karna, equipment status check karna, relevant utilities aur basic conditions verify karna, SOP aur maintenance procedure follow karna aur intervention ko properly document karna important hai, Boss."
        }

        // =====================================================
        // EQUIPMENT CLEANING
        // =====================================================

        if (has(
                c,
                "equipment cleaning status",
                "equipment cleanliness status",
                "equipment cleaning verification"
            )
        ) {
            return "Equipment cleaning status verify karte waqt equipment identification, previous product, cleaning status label or electronic status, cleaning record aur applicable line clearance requirements check karne chahiye, Boss."
        }

        // =====================================================
        // EQUIPMENT CALIBRATION
        // =====================================================

        if (has(
                c,
                "instrument calibration",
                "instrument calibration kya hai",
                "equipment calibration"
            )
        ) {
            return "Instrument calibration suitable reference standards ke against measurement performance verify ya establish karne ki controlled activity hai. Exact procedure aur acceptance criteria instrument-specific approved calibration procedure ke according hone chahiye, Boss."
        }

        // =====================================================
        // EQUIPMENT QUALIFICATION
        // =====================================================

        if (has(
                c,
                "equipment qualification",
                "equipment qualification kya hai",
                "equipment iq oq pq"
            )
        ) {
            return "Equipment Qualification documented evidence establish karti hai ki equipment intended use ke liye appropriately installed, operated aur where applicable performance requirements ke according capable hai. IQ, OQ aur PQ qualification lifecycle ke common stages hain, Boss."
        }

        // =====================================================
        // EQUIPMENT / INSTRUMENT SUMMARY
        // =====================================================

        if (has(
                c,
                "pharma equipment",
                "pharmaceutical equipment",
                "pharma instruments",
                "pharmaceutical instruments",
                "pharma equipment list",
                "pharma instruments list"
            )
        ) {
            return "Pharma equipment aur instruments mein RMG, FBD, Blender, Multi Mill, Compression Machine, Coating Machine, Packing Machines, Dissolution Tester, Disintegration Tester, Friability Tester, Hardness Tester, Analytical Balance, pH Meter, HPLC, GC, FTIR, Karl Fischer, Moisture Analyzer, Stability Chamber, Autoclave, HVAC, AHU, Chiller, RO, Purified Water System aur WFI System jaise systems include ho sakte hain, Boss."
        }

        return null
    }
}
