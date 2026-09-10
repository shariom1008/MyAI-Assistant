package com.example.myaiassistant

import java.util.Locale

object AurixPharmaEngine {

    private fun normalize(command: String): String {
        return command
            .lowercase(Locale.getDefault())
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun has(c: String, vararg words: String): Boolean {
        return words.any { c.contains(it) }
    }

    fun answer(command: String): String? {

        val c = normalize(command)

        if (c.isBlank()) return null


        // =========================================================
        // GMP / cGMP
        // =========================================================

        if (has(c, "what is gmp", "gmp kya", "gmp kya hai", "define gmp")) {
            return "GMP yani Good Manufacturing Practice pharmaceutical products ko consistently quality standards ke according manufacture aur control karne ke guidelines hain, Boss."
        }

        if (has(c, "cgmp", "c gmp", "current good manufacturing practice")) {
            return "cGMP ka matlab Current Good Manufacturing Practice hai. Iska focus current applicable GMP requirements ke according manufacturing aur quality control par hota hai."
        }


        // =========================================================
        // QA / QC / IPQC
        // =========================================================

        if (has(c, "qa qc difference", "difference between qa and qc", "qa aur qc")) {
            return "QA process aur system ko assure karta hai, jabki QC testing aur inspection ke through product quality ko verify karta hai."
        }

        if (has(c, "what is qa", "qa kya", "define qa")) {
            return "QA yani Quality Assurance ek preventive system hai jo processes aur systems ko control karke quality assure karta hai."
        }

        if (has(c, "what is qc", "qc kya", "define qc")) {
            return "QC yani Quality Control testing, inspection aur analytical evaluation ke through material aur product ki quality verify karta hai."
        }

        if (has(c, "ipqc", "in process quality control")) {
            return "IPQC yani In Process Quality Control manufacturing ke during kiye jaane wale quality checks hain, jaise weight variation, hardness, thickness, friability aur appearance."
        }


        // =========================================================
        // API / EXCIPIENT
        // =========================================================

        if (has(c, "what is api", "api kya", "active pharmaceutical ingredient")) {
            return "API yani Active Pharmaceutical Ingredient woh pharmacologically active substance hai jo medicine ka therapeutic effect provide karta hai."
        }

        if (has(c, "what is excipient", "excipient kya")) {
            return "Excipient formulation mein use hone wala non-active ingredient hota hai jo manufacturing, stability, appearance ya drug delivery mein help karta hai."
        }


        // =========================================================
        // TABLET / CAPSULE
        // =========================================================

        if (has(c, "tablet kya", "what is tablet")) {
            return "Tablet ek solid pharmaceutical dosage form hai jo powder ya granules ko compression ke through banaya jaata hai."
        }

        if (has(c, "capsule kya", "what is capsule")) {
            return "Capsule ek solid dosage form hai jisme drug formulation ko generally hard ya soft capsule shell ke andar fill kiya jaata hai."
        }


        // =========================================================
        // DISPENSING
        // =========================================================

        if (has(c, "dispensing", "dispensing kya")) {
            return "Dispensing ka matlab approved material ko required quantity mein accurately weigh karke manufacturing ke liye issue karna hai."
        }

        if (has(c, "dispensing precaution", "dispensing precautions")) {
            return "Dispensing mein material identity, status, label, quantity, balance status, line clearance, cleanliness aur cross contamination prevention verify karna important hai."
        }


        // =========================================================
        // GRANULATION
        // =========================================================

        if (has(c, "wet granulation", "wet granulation kya")) {
            return "Wet granulation mein powders ko binder solution ki help se agglomerate karke wet granules banaye jaate hain, phir drying aur sizing ki jaati hai."
        }

        if (has(c, "dry granulation", "dry granulation kya")) {
            return "Dry granulation mein liquid binder ke bina powder ko compaction ya slugging ke through granules mein convert kiya jaata hai."
        }


        // =========================================================
        // BLENDING
        // =========================================================

        if (has(c, "blending", "blend uniformity", "blend uniformity kya")) {
            return "Blending ka purpose formulation components ko homogeneous mixture banana hai. Blend Uniformity testing se API distribution ki consistency verify ki jaati hai."
        }

        if (has(c, "lubrication", "magnesium stearate", "lubrication kya")) {
            return "Lubrication mein magnesium stearate jaise lubricant ka controlled mixing kiya jaata hai taaki powder punches aur dies se properly release ho aur manufacturing smooth rahe."
        }


        // =========================================================
        // COMPRESSION
        // =========================================================

        if (has(c, "compression kya", "tablet compression", "compression parameters")) {
            return "Tablet compression mein important parameters mein tablet weight, hardness, thickness, compression force, pre-compression, turret speed, feeder speed aur ejection force jaise parameters include ho sakte hain."
        }

        if (has(c, "capping", "tablet capping")) {
            return "Capping tablet ke upper ya lower portion ke separate hone ko kehte hain. Possible causes mein air entrapment, excessive fines, inadequate granule properties aur improper compression conditions ho sakti hain."
        }

        if (has(c, "lamination", "tablet lamination")) {
            return "Lamination tablet ka horizontal layers mein separate hona hai. Iske causes formulation, granule properties, compression conditions aur trapped air se related ho sakte hain."
        }

        if (has(c, "sticking", "tablet sticking")) {
            return "Sticking mein granule ya tablet material punch surface par adhere karta hai. Moisture, formulation properties, inadequate lubrication aur punch condition possible factors ho sakte hain."
        }

        if (has(c, "picking", "tablet picking")) {
            return "Picking tablet ke surface se material ka punch face par transfer hona hai, often embossing area mein. Moisture, formulation properties aur punch condition possible factors hain."
        }


        // =========================================================
        // COATING
        // =========================================================

        if (has(c, "coating kya", "tablet coating")) {
            return "Tablet coating mein tablet surface par coating material ki controlled layer apply ki jaati hai. Important parameters process aur formulation ke according vary karte hain."
        }

        if (has(c, "orange peel", "orange peel defect")) {
            return "Orange peel coating defect mein tablet surface rough ya textured appearance deta hai. Spray conditions, drying, atomization aur coating suspension properties possible factors ho sakte hain."
        }


        // =========================================================
        // DISSOLUTION
        // =========================================================

        if (has(c, "dissolution kya", "what is dissolution", "dissolution test")) {
            return "Dissolution test pharmaceutical dosage form se drug substance ke dissolution ya release behaviour ko evaluate karta hai under specified test conditions."
        }

        if (has(c, "apparatus 1", "dissolution apparatus 1", "basket apparatus")) {
            return "Dissolution Apparatus 1 Basket method hai."
        }

        if (has(c, "apparatus 2", "dissolution apparatus 2", "paddle apparatus")) {
            return "Dissolution Apparatus 2 Paddle method hai."
        }

        if (has(c, "apparatus 3")) {
            return "Dissolution Apparatus 3 Reciprocating Cylinder hai."
        }

        if (has(c, "apparatus 4")) {
            return "Dissolution Apparatus 4 Flow-Through Cell hai."
        }

        if (has(c, "apparatus 5")) {
            return "Dissolution Apparatus 5 Paddle over Disk hai."
        }

        if (has(c, "apparatus 6")) {
            return "Dissolution Apparatus 6 Rotating Cylinder hai."
        }

        if (has(c, "apparatus 7")) {
            return "Dissolution Apparatus 7 Reciprocating Holder hai."
        }

        if (has(c, "dissolution rpm", "dissolution speed")) {
            return "Dissolution RPM test method aur applicable specification ke according set kiya jaata hai. Universal RPM assume nahi karna chahiye."
        }


        // =========================================================
        // DISINTEGRATION / HARDNESS / FRIABILITY
        // =========================================================

        if (has(c, "disintegration", "disintegration test")) {
            return "Disintegration test dosage form ke specified conditions mein break down hone ka time evaluate karta hai."
        }

        if (has(c, "hardness", "tablet hardness")) {
            return "Tablet hardness tester tablet ko break karne ke liye required crushing force measure karta hai."
        }

        if (has(c, "friability", "friability test")) {
            return "Friability test tablets ki mechanical resistance aur weight loss tendency evaluate karta hai."
        }


        // =========================================================
        // ASSAY / RELATED SUBSTANCES / OOS / OOT
        // =========================================================

        if (has(c, "assay kya", "what is assay")) {
            return "Assay test sample mein active pharmaceutical ingredient ki quantity ya content determine karta hai according to the approved analytical method."
        }

        if (has(c, "related substances", "related substance")) {
            return "Related substances testing drug substance ya product mein specified aur unspecified impurities ko evaluate karta hai."
        }

        if (has(c, "oos", "out of specification")) {
            return "OOS yani Out of Specification result approved specification ya acceptance criteria ke outside aane wala result hai."
        }

        if (has(c, "oot", "out of trend")) {
            return "OOT yani Out of Trend result historical ya expected trend se unusual deviation ko indicate karta hai, even when it may still be within specification."
        }

        if (has(c, "deviation kya", "what is deviation")) {
            return "Deviation approved procedure, process, instruction ya expected condition se departure ko kehte hain."
        }

        if (has(c, "capa", "what is capa")) {
            return "CAPA yani Corrective and Preventive Action ka purpose identified problem ko correct karna aur recurrence prevent karna hai."
        }

        if (has(c, "change control", "change control kya")) {
            return "Change Control ek controlled system hai jiske through proposed changes ko assess, approve, implement aur document kiya jaata hai."
        }


        // =========================================================
        // VALIDATION / QUALIFICATION / CALIBRATION
        // =========================================================

        if (has(c, "validation kya", "what is validation")) {
            return "Validation documented evidence establish karta hai ki process ya system predetermined requirements ke according consistently perform karta hai."
        }

        if (has(c, "process validation")) {
            return "Process Validation documented evidence provide karti hai ki manufacturing process defined parameters ke andar consistently quality product produce karta hai."
        }

        if (has(c, "cleaning validation")) {
            return "Cleaning Validation documented evidence establish karti hai ki approved cleaning procedure equipment ko predetermined cleanliness criteria tak effectively clean karti hai."
        }

        if (has(c, "qualification kya", "equipment qualification")) {
            return "Qualification documented evidence hai ki facility, system ya equipment intended purpose ke liye properly installed aur capable hai."
        }

        if (has(c, "iq oq pq", "iq oq", "oq pq")) {
            return "IQ Installation Qualification, OQ Operational Qualification aur PQ Performance Qualification ko refer karta hai."
        }

        if (has(c, "calibration kya", "what is calibration")) {
            return "Calibration mein instrument ki measurement performance ko known reference standard ke against compare kiya jaata hai aur required adjustment ya assessment kiya jaata hai."
        }

        if (has(c, "calibration qualification difference")) {
            return "Calibration measurement accuracy ko reference standard ke against verify karta hai, jabki qualification establish karti hai ki equipment ya system intended purpose ke liye suitable aur capable hai."
        }


        // =========================================================
        // SOP / BMR / BPR / ALCOA+
        // =========================================================

        if (has(c, "sop kya", "what is sop")) {
            return "SOP yani Standard Operating Procedure kisi activity ko consistently perform karne ke approved instructions provide karta hai."
        }

        if (has(c, "bmr kya", "batch manufacturing record")) {
            return "BMR yani Batch Manufacturing Record manufacturing batch ki processing activities, materials, parameters aur relevant records ka documented record hai."
        }

        if (has(c, "bpr kya", "batch packaging record")) {
            return "BPR yani Batch Packaging Record packaging operation ke materials, activities, checks aur records ko document karta hai."
        }

        if (has(c, "alcoa", "alcoa plus", "data integrity")) {
            return "ALCOA+ data integrity principles mein attributable, legible, contemporaneous, original, accurate ke saath complete, consistent, enduring aur available principles include hote hain."
        }


        // =========================================================
        // STABILITY / HVAC / WATER
        // =========================================================

        if (has(c, "stability", "stability study")) {
            return "Stability studies time aur defined environmental conditions ke effect ko evaluate karke product ki quality characteristics aur shelf life assess karte hain."
        }

        if (has(c, "accelerated stability")) {
            return "Accelerated stability study elevated stress conditions ke under product quality changes ko evaluate karne ke liye ki jaati hai. Exact conditions applicable guideline aur approved protocol par depend karti hain."
        }

        if (has(c, "hvac kya", "what is hvac")) {
            return "HVAC yani Heating, Ventilation and Air Conditioning system temperature, humidity, air movement, filtration aur pressure conditions ko control karne mein help karta hai."
        }

        if (has(c, "ah u", "ahu", "air handling unit")) {
            return "AHU yani Air Handling Unit conditioned air ko filter, control aur distribute karne ke liye use hoti hai."
        }

        if (has(c, "purified water", "pw system")) {
            return "Purified Water system pharmaceutical manufacturing aur laboratory applications ke liye controlled quality water produce karta hai according to applicable requirements."
        }

        if (has(c, "wfi", "water for injection")) {
            return "WFI yani Water for Injection pharmaceutical applications mein high-purity water system hai jiske requirements applicable pharmacopoeia aur regulatory standards ke according define hote hain."
        }


        // =========================================================
        // PRODUCTION EQUIPMENT
        // =========================================================

        if (has(c, "rmg", "rapid mixer granulator", "rapid mixer")) {
            return "RMG yani Rapid Mixer Granulator high-shear mixing aur wet granulation ke liye use hota hai. Important process parameters mein impeller speed, chopper speed, mixing time aur granulation endpoint jaise parameters method ke according evaluate kiye ja sakte hain."
        }

        if (has(c, "octagonal blender", "octagonal blender kya")) {
            return "Octagonal Blender powder aur granules ko homogeneous blend karne ke liye use hota hai. Rotation speed, fill volume, blending time aur loading pattern important process considerations hain."
        }

        if (has(c, "conta blender", "container blender")) {
            return "Conta Blender ek contained blending system hai jo powders aur granules ki uniform mixing ke liye use hota hai."
        }

        if (has(c, "bin blender")) {
            return "Bin Blender material container ya bin ke andar powder aur granules ko blend karne ke liye use hota hai."
        }

        if (has(c, "double cone blender")) {
            return "Double Cone Blender dry powders aur granules ki gentle blending ke liye use hota hai."
        }

        if (has(c, "multi mill", "multimill")) {
            return "Multi Mill wet ya dry material ko size reduction aur granule sizing ke liye use kiya jaata hai. Screen, impeller aur speed process requirement ke according select hote hain."
        }

        if (has(c, "co mill", "comill")) {
            return "Co-mill controlled size reduction aur deagglomeration ke liye use hota hai."
        }

        if (has(c, "oscillating granulator", "oscillating granulator kya")) {
            return "Oscillating Granulator wet mass ya dried material ko required granule size mein convert karne ke liye use hota hai."
        }

        if (has(c, "fbd", "fluid bed dryer", "fluid bed processor")) {
            return "FBD yani Fluid Bed Dryer/Processor mein heated air ko product bed ke through pass karke drying aur, applicable process mein, fluid-bed processing ki jaati hai. Air temperature, product temperature, airflow aur endpoint important parameters ho sakte hain."
        }

        if (has(c, "tray dryer", "tray dryer kya")) {
            return "Tray Dryer trays par spread material ko controlled heated air circulation ke through dry karta hai."
        }

        if (has(c, "vacuum tray dryer", "vtd")) {
            return "Vacuum Tray Dryer reduced pressure ke under drying ke liye use hota hai, especially heat-sensitive materials ke liye useful ho sakta hai."
        }

        if (has(c, "mass mixer", "mass mixer kya")) {
            return "Mass Mixer powders ya wet mass ko uniformly mix aur knead karne ke liye use hota hai."
        }

        if (has(c, "paste kettle", "paste kettle kya")) {
            return "Paste Kettle liquid ya paste-type formulation components ko heating aur mixing ke saath prepare karne ke liye use ho sakta hai."
        }

        if (has(c, "manufacturing vessel", "manufacturing vessel kya")) {
            return "Manufacturing Vessel liquid ya semi-solid pharmaceutical processing ke liye mixing, heating ya holding operations mein use hota hai."
        }

        if (has(c, "storage vessel", "storage vessel kya")) {
            return "Storage Vessel processed material ko controlled conditions mein temporarily ya defined duration ke liye hold/store karne ke liye use hota hai."
        }

        if (has(c, "tablet compression machine", "rotary tablet press", "tablet press")) {
            return "Rotary Tablet Press granules ya powder ko punches aur dies ke through compressed tablets mein convert karta hai. Weight, hardness, thickness, compression force, turret speed aur feeder settings important parameters ho sakte hain."
        }

        if (has(c, "deduster", "tablet deduster")) {
            return "Tablet Deduster compression ke baad tablets ki surface se loose powder aur fines remove karne ke liye use hota hai."
        }

        if (has(c, "metal detector")) {
            return "Metal Detector tablets ya other product stream mein unwanted metallic contamination detect karne ke liye use hota hai."
        }

        if (has(c, "tablet inspection machine", "tablet inspection")) {
            return "Tablet Inspection Machine tablets ki visual defects, shape, color aur other configured defects ko inspect karne ke liye use hoti hai."
        }

        if (has(c, "capsule filling machine", "capsule filler")) {
            return "Capsule Filling Machine powder, granules ya suitable formulation ko hard capsules mein fill aur close karne ke liye use hoti hai."
        }

        if (has(c, "capsule polisher", "capsule polishing")) {
            return "Capsule Polisher capsules ki external surface se loose powder remove karke cleaned appearance provide karta hai."
        }

        if (has(c, "coating pan", "auto coater", "automatic coater")) {
            return "Coating Pan ya Auto Coater tablets par coating solution ya suspension ko controlled spray ke through apply karta hai. Pan speed, spray rate, inlet air conditions, exhaust conditions aur product temperature important process considerations ho sakte hain."
        }

        if (has(c, "blister packing machine", "blister packing")) {
            return "Blister Packing Machine tablets ya capsules ko formed blister cavities mein pack karke lidding material se seal karti hai."
        }

        if (has(c, "strip packing machine", "strip packing")) {
            return "Strip Packing Machine tablets ya capsules ko two packaging strips ke beech seal karke pack karti hai."
        }

        if (has(c, "bottle packing line", "bottle packing")) {
            return "Bottle Packing Line bottles ko count/fill, cap, seal, label aur other configured packaging operations ke through pack kar sakti hai."
        }

        if (has(c, "cartoner", "cartoning machine")) {
            return "Cartoner cartons ko form aur product components ko carton mein insert karke close karne ke liye use hota hai."
        }

        if (has(c, "labeling machine", "labelling machine")) {
            return "Labeling Machine containers ya packs par approved labels ko controlled position mein apply karti hai."
        }

        if (has(c, "checkweigher", "check weigher")) {
            return "Checkweigher packed product ka weight automatically check karta hai aur configured acceptance criteria ke according reject ya accept action perform kar sakta hai."
        }


        // =========================================================
        // QC INSTRUMENTS
        // =========================================================

        if (has(c, "analytical balance", "analytical weighing balance")) {
            return "Analytical Balance high-precision weighing ke liye QC laboratory mein use hota hai. Calibration status, level, cleanliness, environmental conditions aur suitable weighing practice important hain."
        }

        if (has(c, "precision balance")) {
            return "Precision Balance materials aur samples ki accurate weighing ke liye use hota hai. Required readability aur capacity application ke according select ki jaati hai."
        }

        if (has(c, "ph meter", "ph meter kya")) {
            return "pH Meter solution ki acidity ya alkalinity measure karta hai. Electrode condition, calibration, temperature compensation aur buffer suitability important considerations hain."
        }

        if (has(c, "conductivity meter", "conductivity meter kya")) {
            return "Conductivity Meter solution ki electrical conductivity measure karta hai aur water system monitoring jaise applications mein use hota hai."
        }

        if (has(c, "dissolution tester", "dissolution apparatus", "dissolution instrument")) {
            return "Dissolution Tester specified medium, temperature, apparatus aur agitation conditions ke under dosage form se drug release evaluate karta hai."
        }

        if (has(c, "disintegration tester", "disintegration instrument")) {
            return "Disintegration Tester tablets ya capsules ke specified conditions mein disintegrate hone ka time determine karta hai."
        }

        if (has(c, "friability tester", "friability instrument")) {
            return "Friability Tester tablets ko controlled mechanical tumbling ke exposure ke baad weight loss evaluate karne ke liye use hota hai."
        }

        if (has(c, "hardness tester", "tablet hardness tester")) {
            return "Tablet Hardness Tester tablet ko fracture karne ke liye required force measure karta hai."
        }

        if (has(c, "thickness tester", "tablet thickness")) {
            return "Thickness Tester tablet ki thickness measure karta hai aur in-process ya finished-product checks mein use ho sakta hai."
        }

        if (has(c, "uv visible", "uv vis", "uv spectrophotometer", "uv visible spectrophotometer")) {
            return "UV-Visible Spectrophotometer ultraviolet aur visible wavelength range mein sample ki absorbance measure karta hai aur quantitative ya qualitative analytical methods mein use hota hai."
        }

        if (has(c, "hplc kya", "what is hplc", "hplc instrument")) {
            return "HPLC yani High Performance Liquid Chromatography complex mixtures ko separate, identify aur quantify karne ke liye use hoti hai."
        }

        if (has(c, "gc kya", "gas chromatography", "gc instrument")) {
            return "GC yani Gas Chromatography volatile ya suitably derivatized compounds ko separate aur analyze karne ke liye use hoti hai."
        }

        if (has(c, "ftir", "ft-ir")) {
            return "FTIR yani Fourier Transform Infrared Spectroscopy material ki infrared absorption characteristics ke through identification aur characterization mein use hoti hai."
        }

        if (has(c, "karl fischer", "kf moisture", "kf titration")) {
            return "Karl Fischer titration sample mein water content determine karne ke liye use hoti hai."
        }

        if (has(c, "moisture analyzer", "moisture analyser")) {
            return "Moisture Analyzer sample ke moisture ya loss-on-drying type measurement ko instrument ke defined method ke according determine karta hai."
        }

        if (has(c, "melting point apparatus", "melting point")) {
            return "Melting Point Apparatus material ke melting behaviour aur melting range determine karne ke liye use hota hai."
        }

        if (has(c, "polarimeter", "polarimetry")) {
            return "Polarimeter optically active substances ke optical rotation ko measure karta hai."
        }

        if (has(c, "refractometer", "refractive index")) {
            return "Refractometer sample ka refractive index measure karta hai aur liquids ki identification ya concentration-related applications mein use ho sakta hai."
        }

        if (has(c, "toc analyzer", "toc analyser", "total organic carbon")) {
            return "TOC Analyzer yani Total Organic Carbon Analyzer water mein organic carbon level ko measure karta hai."
        }

        if (has(c, "autoclave", "autoclave kya")) {
            return "Autoclave moist heat under pressure ka use karke suitable materials aur equipment ko sterilize karne ke liye use hota hai."
        }

        if (has(c, "hot air oven", "hot air oven kya")) {
            return "Hot Air Oven dry heat ke through suitable materials ko sterilize ya dry karne ke liye use hota hai, according to validated application."
        }

        if (has(c, "muffle furnace", "muffle furnace kya")) {
            return "Muffle Furnace high-temperature heating applications jaise ash determination aur suitable thermal treatment ke liye use hota hai."
        }

        if (has(c, "stability chamber", "stability chamber kya")) {
            return "Stability Chamber controlled temperature aur humidity conditions maintain karke stability studies ke samples ko store karne ke liye use hota hai."
        }

        if (has(c, "refrigerator", "lab refrigerator", "freezer")) {
            return "Laboratory Refrigerator ya Freezer temperature-sensitive samples aur materials ko specified controlled temperature conditions mein store karne ke liye use hota hai."
        }

        if (has(c, "microscope", "microscope kya")) {
            return "Microscope small particles, microorganisms ya sample morphology ko magnify karke observe karne ke liye use hota hai."
        }

        if (has(c, "colony counter", "colony counter kya")) {
            return "Colony Counter microbiological plates par visible microbial colonies ko count karne mein help karta hai."
        }

        if (has(c, "air sampler", "air sampler kya")) {
            return "Air Sampler controlled method ke through environmental air se microbiological monitoring samples collect karne ke liye use hota hai."
        }

        if (has(c, "particle counter", "particle counter kya")) {
            return "Particle Counter air mein specified particle sizes aur counts ko monitor karne ke liye use hota hai."
        }


        // =========================================================
        // UTILITY / ENGINEERING
        // =========================================================

        if (has(c, "hvac", "hvac system")) {
            return "HVAC yani Heating, Ventilation and Air Conditioning system temperature, humidity, air movement, filtration aur pressure conditions ko control karne mein help karta hai."
        }

        if (has(c, "chiller", "chiller kya")) {
            return "Chiller process ya HVAC applications ke liye chilled water ya cooling medium provide karta hai."
        }

        if (has(c, "cooling tower", "cooling tower kya")) {
            return "Cooling Tower heat ko circulating water se atmosphere mein reject karke cooling water temperature reduce karta hai."
        }

        if (has(c, "air compressor", "air compressor kya")) {
            return "Air Compressor compressed air generate karta hai jo pneumatic equipment, instruments aur process applications mein use ho sakti hai."
        }

        if (has(c, "nitrogen generator", "nitrogen generator kya")) {
            return "Nitrogen Generator compressed air ya other feed se nitrogen produce karke suitable pharmaceutical process applications ko supply kar sakta hai."
        }

        if (has(c, "boiler", "boiler kya")) {
            return "Boiler water ko heat karke steam generate karta hai jo pharmaceutical utility aur process applications mein use ho sakti hai."
        }

        if (has(c, "steam system", "pure steam", "steam generation")) {
            return "Steam System steam generate, distribute aur control karta hai. Pharmaceutical application mein steam quality requirements system aur intended use ke according defined hoti hain."
        }

        if (has(c, "ro plant", "reverse osmosis", "ro system")) {
            return "RO Plant Reverse Osmosis membrane technology ke through water se dissolved impurities aur contaminants ko reduce karne mein help karta hai."
        }

        if (has(c, "dm plant", "demineralized water", "demineralisation")) {
            return "DM Plant ion exchange ya related treatment technology ke through dissolved ionic impurities ko remove karke demineralized water produce karta hai."
        }

        if (has(c, "etp", "effluent treatment plant")) {
            return "ETP yani Effluent Treatment Plant industrial wastewater ko treatment karke applicable discharge ya reuse requirements ke liye process karta hai."
        }

        if (has(c, "dg", "diesel generator", "generator")) {
            return "DG yani Diesel Generator electrical power backup provide karta hai jab normal electrical supply unavailable ho."
        }

        if (has(c, "vacuum pump", "vacuum pump kya")) {
            return "Vacuum Pump system se gas ya air remove karke reduced pressure generate karta hai aur vacuum drying, filtration ya process applications mein use ho sakta hai."
        }


        // =========================================================
        // EQUIPMENT STATUS / CALIBRATION STATUS
        // =========================================================

        if (has(c, "equipment status", "equipment status kya")) {
            return "Equipment status se pata chalta hai ki equipment clean, under maintenance, ready for use, in operation ya kisi defined status mein hai. Actual status equipment label aur applicable SOP se verify karna chahiye."
        }

        if (has(c, "calibration status", "instrument calibration status")) {
            return "Calibration status instrument ki current calibration validity ko indicate karta hai. Use se pehle calibration label aur applicable record verify karna chahiye."
        }


        // =========================================================
        // BALANCE
        // =========================================================

        if (has(c, "balance kya", "weighing balance", "balance principle")) {
            return "Weighing Balance mass determine karne ke liye load measurement system use karta hai. Pharmaceutical use mein capacity, readability, calibration status, leveling aur environmental conditions important hain."
        }


        // =========================================================
        // SAMPLING
        // =========================================================

        if (has(c, "sampling kya", "what is sampling")) {
            return "Sampling ka purpose representative sample obtain karna hai taaki material ya product ki quality ko approved test method ke according evaluate kiya ja sake."
        }


        // =========================================================
        // SPECIFICATION / PHARMACOPOEIA
        // =========================================================

        if (has(c, "specification kya", "what is specification")) {
            return "Specification approved tests, analytical procedures aur acceptance criteria ka defined set hota hai jiske against material ya product quality evaluate ki jaati hai."
        }

        if (has(c, "usp kya", "united states pharmacopeia")) {
            return "USP yani United States Pharmacopeia ek major pharmacopoeial reference hai containing standards and monographs for applicable pharmaceutical materials and products."
        }

        if (has(c, "bp kya", "british pharmacopoeia")) {
            return "BP yani British Pharmacopoeia pharmaceutical substances aur products ke applicable quality standards aur monographs provide karti hai."
        }

        if (has(c, "ip kya", "indian pharmacopoeia")) {
            return "IP yani Indian Pharmacopoeia India mein applicable pharmaceutical quality standards aur monographs ka major pharmacopoeial reference hai."
        }


        // =========================================================
        // ICH
        // =========================================================

        if (has(c, "ich kya", "what is ich")) {
            return "ICH yani International Council for Harmonisation pharmaceutical technical requirements ko harmonize karne ke liye guidelines develop karta hai."
        }

        if (has(c, "ich q1", "q1 guideline")) {
            return "ICH Q1 stability testing se related guideline series hai."
        }

        if (has(c, "ich q2", "q2 guideline")) {
            return "ICH Q2 analytical procedure validation se related guideline hai."
        }

        if (has(c, "ich q3", "q3 guideline")) {
            return "ICH Q3 impurities se related guideline series hai."
        }

        if (has(c, "ich q7", "q7 guideline")) {
            return "ICH Q7 Active Pharmaceutical Ingredients ke Good Manufacturing Practice se related guideline hai."
        }

        if (has(c, "ich q8", "q8 guideline")) {
            return "ICH Q8 Pharmaceutical Development se related guideline hai."
        }

        if (has(c, "ich q9", "q9 guideline")) {
            return "ICH Q9 Quality Risk Management se related guideline hai."
        }

        if (has(c, "ich q10", "q10 guideline")) {
            return "ICH Q10 Pharmaceutical Quality System se related guideline hai."
        }


        // =========================================================
        // RISK / RCA
        // =========================================================

        if (has(c, "quality risk management", "qrm", "risk management")) {
            return "Quality Risk Management quality risks ko identify, assess, control, communicate aur review karne ka systematic approach hai."
        }

        if (has(c, "root cause analysis", "rca", "root cause")) {
            return "Root Cause Analysis problem ke underlying cause ko systematically identify karne ka process hai."
        }

        if (has(c, "5 why", "five why")) {
            return "5 Why technique mein repeatedly why question karke problem ke underlying root cause tak pahunchne ki koshish ki jaati hai."
        }

        if (has(c, "fmea", "failure mode and effects analysis")) {
            return "FMEA yani Failure Mode and Effects Analysis potential failure modes ko identify aur risk-prioritize karne ka structured method hai."
        }


        // =========================================================
        // DOCUMENT CONTROL / LINE CLEARANCE
        // =========================================================

        if (has(c, "document control", "document control kya")) {
            return "Document Control ensures karta hai ki GMP documents approved, current, traceable aur controlled form mein available rahen."
        }

        if (has(c, "line clearance", "line clearance kya")) {
            return "Line Clearance manufacturing ya packaging start karne se pehle previous product, material, documents aur unwanted items ko remove aur area readiness verify karne ka documented check hai."
        }

        if (has(c, "cross contamination", "cross contamination kya")) {
            return "Cross contamination ka matlab ek material ya product ka doosre material ya product mein unintended contamination ya carryover hai."
        }

        if (has(c, "mix up", "mix-up", "mixup")) {
            return "Mix-up mein wrong material, component, product, label ya information ka unintended use ya association ho jaata hai."
        }


        // =========================================================
        // HOLD TIME
        // =========================================================

        if (has(c, "hold time", "hold time study")) {
            return "Hold Time Study defined storage conditions mein intermediate ya material ko specified duration tak hold karne ke effect ko evaluate karti hai."
        }


        // =========================================================
        // CPP / CQA
        // =========================================================

        if (has(c, "cpp cqa difference", "difference between cpp and cqa")) {
            return "CPP yani Critical Process Parameter process ka critical parameter hai jo product quality ko affect kar sakta hai, jabki CQA yani Critical Quality Attribute product ki critical quality characteristic hai."
        }

        if (has(c, "cpp kya", "critical process parameter")) {
            return "CPP yani Critical Process Parameter woh process parameter hai jiska variability critical quality attributes par impact daal sakta hai."
        }

        if (has(c, "cqa kya", "critical quality attribute")) {
            return "CQA yani Critical Quality Attribute ek physical, chemical, biological ya microbiological characteristic hai jo product quality ke liye critical hoti hai."
        }


        // =========================================================
        // GRANULE TESTING
        // =========================================================

        if (has(c, "bulk density", "bulk density kya")) {
            return "Bulk Density powder ke mass ko uske untapped bulk volume ke relation mein express karti hai."
        }

        if (has(c, "tapped density", "tapped density kya")) {
            return "Tapped Density defined tapping procedure ke baad powder ke mass ko resulting tapped volume ke relation mein express karti hai."
        }

        if (has(c, "carr index", "carr's index")) {
            return "Carr Index powder flow characteristics ka indicator hai jo bulk aur tapped density se calculate kiya jaata hai."
        }

        if (has(c, "hausner ratio", "hausner")) {
            return "Hausner Ratio powder flowability ka indicator hai jo tapped density aur bulk density ke relationship se calculate kiya jaata hai."
        }

        if (has(c, "lod", "loss on drying")) {
            return "LOD yani Loss on Drying defined drying conditions ke under sample ke mass loss ko determine karta hai, jo moisture aur volatile components se related ho sakta hai."
        }


        // =========================================================
        // AUDIT / COMPLAINT / RECALL
        // =========================================================

        if (has(c, "audit kya", "what is audit")) {
            return "Audit systematic aur documented evaluation hai jiske through compliance aur effectiveness of systems ya processes assess kiye jaate hain."
        }

        if (has(c, "self inspection", "self-inspection")) {
            return "Self-inspection internal GMP compliance assessment ka systematic process hai."
        }

        if (has(c, "market complaint", "product complaint")) {
            return "Market Complaint customer ya market se received product quality related complaint hoti hai jise applicable procedure ke according evaluate aur investigate kiya jaata hai."
        }

        if (has(c, "recall kya", "product recall")) {
            return "Product Recall market se affected product ko defined controlled process ke through withdraw karne ki action hai."
        }


        // =========================================================
        // GENERAL EQUIPMENT QUESTION
        // =========================================================

        if (has(c, "pharma equipment", "pharmaceutical equipment", "pharma machine", "pharma instrument")) {
            return "Pharmaceutical equipment ko broadly Production Equipment, QC Instruments aur Utility or Engineering Systems mein classify kiya ja sakta hai. AURIX in categories ke purpose, basic working aur common parameters explain kar sakta hai."
        }


        // =========================================================
        // NOTHING FOUND
        // =========================================================

        return null
    }
}
