package com.example.myaiassistant

import java.util.Locale

object AurixPharmaEngine {

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
        // GMP / CGMP
        // =====================================================

        if (has(
                c,
                "what is gmp",
                "gmp kya hai",
                "gmp kya hota hai",
                "good manufacturing practice",
                "good manufacturing practices"
            )
        ) {
            return "GMP ka matlab Good Manufacturing Practice hai. Iska purpose medicines ko consistently required quality, safety aur efficacy standards ke according manufacture aur control karna hai, Boss."
        }

        if (has(
                c,
                "what is cgmp",
                "cgmp kya hai",
                "current good manufacturing practice"
            )
        ) {
            return "cGMP ka matlab current Good Manufacturing Practice hai. Isme current scientific knowledge, regulatory expectations aur controlled manufacturing practices ke according pharmaceutical products ki quality ensure ki jati hai, Boss."
        }

        if (has(
                c,
                "why gmp is important",
                "gmp important",
                "gmp ka importance",
                "gmp kyun important"
            )
        ) {
            return "GMP important hai kyunki ye contamination, mix-up, errors aur quality failures ko control karne mein help karta hai aur consistent product quality maintain karta hai, Boss."
        }

        // =====================================================
        // QA / QC
        // =====================================================

        if (has(
                c,
                "what is qa",
                "qa kya hai",
                "quality assurance kya hai"
            )
        ) {
            return "QA yani Quality Assurance ek system-based approach hai jo processes aur systems ko control karke pharmaceutical product ki required quality ensure karti hai, Boss."
        }

        if (has(
                c,
                "what is qc",
                "qc kya hai",
                "quality control kya hai"
            )
        ) {
            return "QC yani Quality Control mein sampling, testing, specifications aur analytical results ke through materials aur products ki quality verify ki jati hai, Boss."
        }

        if (has(
                c,
                "qa and qc difference",
                "difference between qa and qc",
                "qa qc difference"
            )
        ) {
            return "QA primarily systems aur processes par focus karta hai, jabki QC testing aur analytical verification par focus karta hai. QA quality ko prevent aur assure karta hai, QC quality ko test aur verify karta hai, Boss."
        }

        // =====================================================
        // IPQC
        // =====================================================

        if (has(
                c,
                "what is ipqc",
                "ipqc kya hai",
                "in process quality control",
                "in process quality check"
            )
        ) {
            return "IPQC ka matlab In-Process Quality Control hai. Manufacturing ke dauran critical process aur product parameters ko monitor aur control karne ke liye IPQC checks kiye jate hain, Boss."
        }

        // =====================================================
        // API / EXCIPIENT
        // =====================================================

        if (has(
                c,
                "what is api",
                "api kya hai",
                "active pharmaceutical ingredient"
            )
        ) {
            return "API ka matlab Active Pharmaceutical Ingredient hai. Ye pharmaceutical product ka woh active component hota hai jo intended pharmacological effect provide karta hai, Boss."
        }

        if (has(
                c,
                "what is excipient",
                "excipient kya hai",
                "excipients kya hote hain"
            )
        ) {
            return "Excipient ek inactive pharmaceutical ingredient hota hai jo formulation ki manufacturing, stability, appearance, processing ya drug delivery characteristics mein help kar sakta hai, Boss."
        }

        // =====================================================
        // DOSAGE FORMS
        // =====================================================

        if (has(
                c,
                "what is tablet",
                "tablet kya hai"
            )
        ) {
            return "Tablet ek solid pharmaceutical dosage form hai jo generally active ingredient aur suitable excipients ko compress karke ya other suitable manufacturing process se banayi jati hai, Boss."
        }

        if (has(
                c,
                "what is capsule",
                "capsule kya hai"
            )
        ) {
            return "Capsule ek dosage form hai jisme drug formulation ko generally hard ya soft shell ke andar fill kiya jata hai, Boss."
        }

        // =====================================================
        // DISPENSING
        // =====================================================

        if (has(
                c,
                "what is dispensing",
                "dispensing kya hai",
                "dispensing in pharma"
            )
        ) {
            return "Pharmaceutical dispensing mein approved raw materials ko required quantity mein identify, weigh aur issue kiya jata hai according to approved manufacturing documents aur controls, Boss."
        }

        if (has(
                c,
                "dispensing precautions",
                "precautions during dispensing",
                "dispensing mein precautions"
            )
        ) {
            return "Dispensing mein material identity, status, quantity, balance suitability, line clearance, cleanliness, label verification, segregation aur documentation ko carefully verify kiya jata hai, Boss."
        }

        // =====================================================
        // GRANULATION
        // =====================================================

        if (has(
                c,
                "what is granulation",
                "granulation kya hai",
                "granulation in pharma"
            )
        ) {
            return "Granulation ek process hai jisme powder particles ko larger, more uniform granules mein convert kiya jata hai, jisse flow, compressibility aur content uniformity jaise properties improve ho sakti hain, Boss."
        }

        if (has(
                c,
                "wet granulation",
                "wet granulation kya hai"
            )
        ) {
            return "Wet granulation mein powder blend ko binder solution ya suitable granulating liquid ke saath agglomerate karke wet granules banaye jate hain, jo baad mein dry kiye jate hain, Boss."
        }

        if (has(
                c,
                "dry granulation",
                "dry granulation kya hai"
            )
        ) {
            return "Dry granulation mein powder ko liquid ke bina compaction ke through granules mein convert kiya jata hai. Roller compaction aur slugging iske common methods hain, Boss."
        }

        // =====================================================
        // BLENDING
        // =====================================================

        if (has(
                c,
                "what is blending",
                "blending kya hai",
                "blending in pharma"
            )
        ) {
            return "Blending pharmaceutical powders ya granules ko uniformly mix karne ka process hai, jisse formulation mein ingredients ki distribution consistent rahe, Boss."
        }

        if (has(
                c,
                "blend uniformity",
                "blend uniformity kya hai",
                "blend uniformity test"
            )
        ) {
            return "Blend uniformity ka objective ye verify karna hai ki active ingredient blend mein adequately aur uniformly distributed hai, Boss."
        }

        // =====================================================
        // LUBRICATION
        // =====================================================

        if (has(
                c,
                "what is lubrication",
                "lubrication kya hai",
                "lubrication in tablet manufacturing"
            )
        ) {
            return "Lubrication mein lubricant ko powder ya granule blend mein add karke friction aur sticking ko control kiya jata hai, especially compression process ke dauran, Boss."
        }

        if (has(
                c,
                "magnesium stearate",
                "magnesium stearate ka use",
                "magnesium stearate why used"
            )
        ) {
            return "Magnesium stearate pharmaceutical formulations mein commonly lubricant ke roop mein use hota hai. Ye die wall aur tooling ke saath friction reduce karne mein help karta hai, Boss."
        }

        // =====================================================
        // COMPRESSION
        // =====================================================

        if (has(
                c,
                "what is compression",
                "compression kya hai",
                "tablet compression kya hai"
            )
        ) {
            return "Tablet compression mein granules ya powder blend ko punches aur dies ki help se compact karke tablets banayi jati hain, Boss."
        }

        if (has(
                c,
                "compression parameters",
                "tablet compression parameters",
                "compression mein kya parameters"
            )
        ) {
            return "Common tablet compression parameters mein tablet weight, hardness, thickness, compression force, pre-compression force, turret speed, ejection force aur appearance jaise parameters include ho sakte hain, Boss."
        }

        // =====================================================
        // TABLET DEFECTS
        // =====================================================

        if (has(
                c,
                "tablet capping",
                "capping kya hai"
            )
        ) {
            return "Capping ek tablet defect hai jisme tablet ka upper ya lower portion tablet body se separate ho jata hai. Causes mein air entrapment, poor granulation, excessive compression ya unsuitable formulation factors ho sakte hain, Boss."
        }

        if (has(
                c,
                "tablet lamination",
                "lamination kya hai"
            )
        ) {
            return "Lamination mein tablet horizontally multiple layers mein separate ya split ho sakti hai. Iske causes formulation, compression, air entrapment aur process conditions se related ho sakte hain, Boss."
        }

        if (has(
                c,
                "tablet sticking",
                "sticking in tablet",
                "sticking defect"
            )
        ) {
            return "Sticking mein tablet material punch face ya tooling surface par adhere karta hai. Moisture, formulation properties, insufficient lubrication aur tooling condition possible contributing factors ho sakte hain, Boss."
        }

        if (has(
                c,
                "tablet picking",
                "picking defect"
            )
        ) {
            return "Picking tablet ke surface se material ka punch face ke saath chipak kar remove hona hai, especially engraved areas mein. Moisture, formulation properties aur tooling condition contributing factors ho sakte hain, Boss."
        }

        // =====================================================
        // COATING
        // =====================================================

        if (has(
                c,
                "what is tablet coating",
                "tablet coating kya hai",
                "coating kya hai"
            )
        ) {
            return "Tablet coating mein tablet ke surface par coating material ki controlled layer apply ki jati hai. Iska purpose protection, appearance, taste masking ya modified release jaise objectives ho sakte hain, Boss."
        }

        if (has(
                c,
                "coating defects",
                "tablet coating defects",
                "coating mein defects"
            )
        ) {
            return "Common coating defects mein picking, peeling, orange peel, mottling, twinning, bridging, chipping aur logo filling jaise defects include ho sakte hain, Boss."
        }

        if (has(
                c,
                "orange peel coating",
                "orange peel defect"
            )
        ) {
            return "Orange peel coating defect mein tablet surface rough ya uneven appearance develop karta hai. Spray conditions, drying, coating formulation aur atomization jaise factors contribute kar sakte hain, Boss."
        }

        // =====================================================
        // DISSOLUTION
        // =====================================================

        if (has(
                c,
                "what is dissolution",
                "dissolution kya hai",
                "dissolution test kya hai"
            )
        ) {
            return "Dissolution test ek in-vitro test hai jo specified conditions mein dosage form se drug substance ke dissolution ya release ko evaluate karta hai, Boss."
        }

        if (has(
                c,
                "dissolution apparatus",
                "dissolution apparatus kya hai",
                "types of dissolution apparatus"
            )
        ) {
            return "Common USP dissolution apparatus mein Apparatus 1 Basket, Apparatus 2 Paddle, Apparatus 3 Reciprocating Cylinder, Apparatus 4 Flow-Through Cell, Apparatus 5 Paddle over Disk, Apparatus 6 Rotating Cylinder aur Apparatus 7 Reciprocating Holder include hote hain, Boss."
        }

        if (has(
                c,
                "apparatus 1 dissolution",
                "usp apparatus 1"
            )
        ) {
            return "Dissolution Apparatus 1 ko Basket apparatus kaha jata hai, Boss."
        }

        if (has(
                c,
                "apparatus 2 dissolution",
                "usp apparatus 2"
            )
        ) {
            return "Dissolution Apparatus 2 ko Paddle apparatus kaha jata hai, Boss."
        }

        // =====================================================
        // DISINTEGRATION
        // =====================================================

        if (has(
                c,
                "what is disintegration",
                "disintegration kya hai",
                "disintegration test"
            )
        ) {
            return "Disintegration test dosage form ke breakdown ko specified test conditions mein evaluate karta hai. Ye dissolution se different test hai, Boss."
        }

        // =====================================================
        // HARDNESS
        // =====================================================

        if (has(
                c,
                "tablet hardness",
                "hardness test kya hai",
                "tablet hardness kya hai"
            )
        ) {
            return "Tablet hardness test tablet ki crushing strength ya mechanical resistance ko evaluate karta hai, Boss."
        }

        // =====================================================
        // FRIABILITY
        // =====================================================

        if (has(
                c,
                "what is friability",
                "friability kya hai",
                "friability test"
            )
        ) {
            return "Friability test tablets ki resistance to abrasion aur mechanical stress ko evaluate karta hai, Boss."
        }

        // =====================================================
        // ASSAY
        // =====================================================

        if (has(
                c,
                "what is assay",
                "assay kya hai",
                "assay in pharma"
            )
        ) {
            return "Assay test pharmaceutical material ya product mein active ingredient ki quantity ya potency ko specified analytical method ke according determine karta hai, Boss."
        }

        // =====================================================
        // RELATED SUBSTANCES
        // =====================================================

        if (has(
                c,
                "related substances",
                "related substance kya hai",
                "related substances test"
            )
        ) {
            return "Related substances testing pharmaceutical product mein related compounds, degradation products aur specified impurities ko detect aur quantify karne ke liye ki jati hai, Boss."
        }

        // =====================================================
        // OOS
        // =====================================================

        if (has(
                c,
                "what is oos",
                "oos kya hai",
                "out of specification",
                "out of specification kya hai"
            )
        ) {
            return "OOS yani Out of Specification result tab hota hai jab analytical test result approved specification ya acceptance criteria ke bahar hota hai. OOS investigation documented aur scientifically justified honi chahiye, Boss."
        }

        // =====================================================
        // OOT
        // =====================================================

        if (has(
                c,
                "what is oot",
                "oot kya hai",
                "out of trend",
                "out of trend kya hai"
            )
        ) {
            return "OOT yani Out of Trend result generally aisa result hota hai jo established historical ya expected trend se unusual deviation show karta hai, even when specification ke andar ho sakta hai, Boss."
        }

        // =====================================================
        // DEVIATION
        // =====================================================

        if (has(
                c,
                "what is deviation",
                "deviation kya hai",
                "pharma deviation"
            )
        ) {
            return "Pharmaceutical deviation ek approved procedure, process, instruction ya expected condition se documented departure hai. Isko assess, investigate aur appropriately document kiya jata hai, Boss."
        }

        // =====================================================
        // CAPA
        // =====================================================

        if (has(
                c,
                "what is capa",
                "capa kya hai",
                "corrective preventive action"
            )
        ) {
            return "CAPA ka matlab Corrective and Preventive Action hai. Corrective Action existing problem ya cause ko address karta hai, jabki Preventive Action potential recurrence ya related risk ko prevent karne par focus karta hai, Boss."
        }

        // =====================================================
        // CHANGE CONTROL
        // =====================================================

        if (has(
                c,
                "what is change control",
                "change control kya hai",
                "change control in pharma"
            )
        ) {
            return "Change Control ek formal documented system hai jiske through proposed changes ko evaluate, assess, approve, implement aur close kiya jata hai while maintaining product quality and compliance, Boss."
        }

        // =====================================================
        // VALIDATION
        // =====================================================

        if (has(
                c,
                "what is validation",
                "validation kya hai",
                "validation in pharma"
            )
        ) {
            return "Validation documented evidence establish karne ka process hai ki ek process, procedure, method ya system consistently predetermined requirements ko fulfill karta hai, Boss."
        }

        if (has(
                c,
                "process validation",
                "process validation kya hai"
            )
        ) {
            return "Process Validation documented evidence provide karti hai ki established manufacturing process defined parameters ke within consistently product ko predetermined quality attributes ke according produce karta hai, Boss."
        }

        if (has(
                c,
                "cleaning validation",
                "cleaning validation kya hai"
            )
        ) {
            return "Cleaning Validation documented evidence establish karti hai ki approved cleaning procedure equipment surfaces ko predetermined acceptance criteria ke according adequately clean kar sakti hai, Boss."
        }

        // =====================================================
        // QUALIFICATION
        // =====================================================

        if (has(
                c,
                "what is qualification",
                "qualification kya hai",
                "equipment qualification"
            )
        ) {
            return "Qualification documented evidence ka process hai jisse establish kiya jata hai ki equipment, system ya facility intended purpose ke liye properly installed aur operating condition mein suitable hai, Boss."
        }

        if (has(
                c,
                "iq oq pq",
                "what is iq oq pq",
                "iq oq pq kya hai"
            )
        ) {
            return "IQ ka matlab Installation Qualification, OQ ka Operational Qualification aur PQ ka Performance Qualification hai. Ye qualification lifecycle ke important stages hain, Boss."
        }

        // =====================================================
        // CALIBRATION
        // =====================================================

        if (has(
                c,
                "what is calibration",
                "calibration kya hai",
                "calibration in pharma"
            )
        ) {
            return "Calibration mein measuring instrument ke readings ko suitable reference standard ke saath compare karke uski accuracy aur measurement performance establish ki jati hai, Boss."
        }

        if (has(
                c,
                "calibration and qualification difference",
                "difference between calibration and qualification"
            )
        ) {
            return "Calibration primarily measurement accuracy aur instrument performance se related hai, jabki qualification equipment ya system ki intended use ke liye suitability establish karti hai, Boss."
        }

        // =====================================================
        // SOP
        // =====================================================

        if (has(
                c,
                "what is sop",
                "sop kya hai",
                "standard operating procedure"
            )
        ) {
            return "SOP yani Standard Operating Procedure ek approved written instruction hai jo kisi activity ko consistent aur controlled manner mein perform karne ka defined method provide karti hai, Boss."
        }

        // =====================================================
        // BMR / BPR
        // =====================================================

        if (has(
                c,
                "what is bmr",
                "bmr kya hai",
                "batch manufacturing record"
            )
        ) {
            return "BMR ka matlab Batch Manufacturing Record hai. Isme batch manufacturing ke execution, process steps, material details, equipment aur required records ka documented evidence maintain kiya jata hai, Boss."
        }

        if (has(
                c,
                "what is bpr",
                "bpr kya hai",
                "batch packing record"
            )
        ) {
            return "BPR ka matlab Batch Packing Record hai. Isme batch packing operation ke relevant details, checks, reconciliation aur documentation maintain ki jati hai, Boss."
        }

        // =====================================================
        // ALCOA+
        // =====================================================

        if (has(
                c,
                "what is alcoa",
                "alcoa plus",
                "alcoa kya hai",
                "data integrity alcoa"
            )
        ) {
            return "ALCOA principles ka matlab Attributable, Legible, Contemporaneous, Original aur Accurate hai. ALCOA+ mein Complete, Consistent, Enduring aur Available principles bhi include kiye jate hain, Boss."
        }

        // =====================================================
        // DATA INTEGRITY
        // =====================================================

        if (has(
                c,
                "data integrity",
                "data integrity kya hai"
            )
        ) {
            return "Data Integrity ka matlab data ka complete, consistent, accurate, reliable aur attributable rehna throughout its lifecycle hai, Boss."
        }

        // =====================================================
        // STABILITY
        // =====================================================

        if (has(
                c,
                "what is stability study",
                "stability study kya hai",
                "stability testing kya hai",
                "stability in pharma"
            )
        ) {
            return "Stability studies ka purpose time aur environmental conditions ke effect ko evaluate karna hai, jisse pharmaceutical product ki quality aur shelf-life related characteristics establish aur monitor ki ja saken, Boss."
        }

        if (has(
                c,
                "accelerated stability",
                "accelerated stability kya hai"
            )
        ) {
            return "Accelerated stability study elevated stress conditions ka use karke product ke stability behaviour ko evaluate karti hai according to an approved stability protocol, Boss."
        }

        // =====================================================
        // HVAC
        // =====================================================

        if (has(
                c,
                "what is hvac",
                "hvac kya hai",
                "hvac in pharma"
            )
        ) {
            return "HVAC ka matlab Heating, Ventilation and Air Conditioning hai. Pharmaceutical facilities mein HVAC temperature, humidity, air movement, filtration aur required environmental conditions ko control karne mein important role play karta hai, Boss."
        }

        // =====================================================
        // PHARMACEUTICAL WATER
        // =====================================================

        if (has(
                c,
                "purified water",
                "purified water kya hai",
                "pw in pharma"
            )
        ) {
            return "Purified Water pharmaceutical applications mein controlled water quality requirements ko meet karne wala water system output hai. Iska use application ke according manufacturing aur cleaning activities mein ho sakta hai, Boss."
        }

        if (has(
                c,
                "what is wfi",
                "wfi kya hai",
                "water for injection"
            )
        ) {
            return "WFI ka matlab Water for Injection hai. Ye pharmaceutical applications mein high-quality water requirement ke liye use kiya jata hai aur applicable pharmacopoeial requirements ke according control kiya jata hai, Boss."
        }

        // =====================================================
        // MICROBIOLOGY
        // =====================================================

        if (has(
                c,
                "what is bioburden",
                "bioburden kya hai"
            )
        ) {
            return "Bioburden kisi material, product ya system mein present viable microorganisms ki population ko refer karta hai, Boss."
        }

        if (has(
                c,
                "what is sterility",
                "sterility kya hai",
                "sterility test"
            )
        ) {
            return "Sterility ka matlab viable microorganisms ki absence hai within the conditions and sensitivity of the applicable sterility test. Sterility testing controlled microbiological procedure ke according perform ki jati hai, Boss."
        }

        // =====================================================
        // ENVIRONMENTAL MONITORING
        // =====================================================

        if (has(
                c,
                "environmental monitoring",
                "environment monitoring",
                "em in pharma"
            )
        ) {
            return "Environmental Monitoring pharmaceutical controlled areas mein environmental conditions aur microbial contamination levels ko monitor karne ka systematic program hai, Boss."
        }

        // =====================================================
        // SAMPLING
        // =====================================================

        if (has(
                c,
                "what is sampling",
                "sampling kya hai",
                "pharma sampling"
            )
        ) {
            return "Pharmaceutical sampling mein representative samples ko approved sampling plan aur procedure ke according collect kiya jata hai taaki material ya product ki quality evaluate ki ja sake, Boss."
        }

        // =====================================================
        // RAW MATERIAL
        // =====================================================

        if (has(
                c,
                "raw material testing",
                "raw material kya hai",
                "raw material in pharma"
            )
        ) {
            return "Raw materials pharmaceutical manufacturing mein use hone wale APIs, excipients aur other approved materials ho sakte hain. Inki identity, quality aur applicable specifications ke according testing aur control kiya jata hai, Boss."
        }

        // =====================================================
        // SPECIFICATIONS
        // =====================================================

        if (has(
                c,
                "what is specification",
                "specification kya hai",
                "pharma specification"
            )
        ) {
            return "Specification approved tests, analytical procedures aur acceptance criteria ka defined set hota hai jiske against material ya product ki quality evaluate ki jati hai, Boss."
        }

        // =====================================================
        // PHARMACOPOEIA
        // =====================================================

        if (has(
                c,
                "what is usp",
                "usp kya hai",
                "united states pharmacopeia"
            )
        ) {
            return "USP yani United States Pharmacopeia ek recognized compendium hai jo pharmaceutical substances, products aur analytical procedures ke standards aur requirements provide karta hai, Boss."
        }

        if (has(
                c,
                "what is bp",
                "bp kya hai",
                "british pharmacopoeia"
            )
        ) {
            return "BP yani British Pharmacopoeia pharmaceutical substances aur products ke standards, specifications aur analytical methods provide karti hai, Boss."
        }

        if (has(
                c,
                "what is ip",
                "ip kya hai",
                "indian pharmacopoeia"
            )
        ) {
            return "IP yani Indian Pharmacopoeia India mein medicines aur pharmaceutical substances ke applicable standards, specifications aur analytical requirements ka official pharmacopoeial reference hai, Boss."
        }

        // =====================================================
        // ICH
        // =====================================================

        if (has(
                c,
                "what is ich",
                "ich kya hai",
                "ich guidelines"
            )
        ) {
            return "ICH ka matlab International Council for Harmonisation hai. ICH pharmaceutical development, quality, safety aur efficacy se related harmonised guidelines develop karta hai, Boss."
        }

        // =====================================================
        // COMMON ICH QUALITY GUIDELINES
        // =====================================================

        if (has(
                c,
                "ich q1",
                "q1 stability"
            )
        ) {
            return "ICH Q1 series pharmaceutical stability testing se related guidelines cover karti hai, Boss."
        }

        if (has(
                c,
                "ich q2",
                "q2 analytical validation"
            )
        ) {
            return "ICH Q2 analytical procedure validation se related guidance provide karta hai, Boss."
        }

        if (has(
                c,
                "ich q3",
                "q3 impurities"
            )
        ) {
            return "ICH Q3 series pharmaceutical impurities se related guidelines cover karti hai, Boss."
        }

        if (has(
                c,
                "ich q7",
                "q7 api",
                "api gmp"
            )
        ) {
            return "ICH Q7 Active Pharmaceutical Ingredients ke liye Good Manufacturing Practice guidance provide karta hai, Boss."
        }

        if (has(
                c,
                "ich q8",
                "q8 pharmaceutical development"
            )
        ) {
            return "ICH Q8 Pharmaceutical Development se related guidance provide karta hai aur science- and risk-based development approach ko support karta hai, Boss."
        }

        if (has(
                c,
                "ich q9",
                "q9 quality risk management"
            )
        ) {
            return "ICH Q9 Quality Risk Management se related guidance provide karta hai, Boss."
        }

        if (has(
                c,
                "ich q10",
                "q10 pharmaceutical quality system"
            )
        ) {
            return "ICH Q10 Pharmaceutical Quality System ke framework se related guidance provide karta hai, Boss."
        }

        // =====================================================
        // RISK MANAGEMENT
        // =====================================================

        if (has(
                c,
                "quality risk management",
                "risk management kya hai",
                "pharma risk management"
            )
        ) {
            return "Quality Risk Management ka objective scientific knowledge aur patient protection ko consider karte hue pharmaceutical quality risks ko identify, assess, control, communicate aur review karna hai, Boss."
        }

        // =====================================================
        // ROOT CAUSE
        // =====================================================

        if (has(
                c,
                "root cause analysis",
                "root cause kya hai",
                "rca kya hai"
            )
        ) {
            return "Root Cause Analysis ka purpose problem ke underlying cause ya causes ko systematically identify karna hai, taaki effective corrective actions define kiye ja saken, Boss."
        }

        // =====================================================
        // 5 WHY
        // =====================================================

        if (has(
                c,
                "five why",
                "5 why",
                "5 why analysis"
            )
        ) {
            return "5 Why ek root cause analysis technique hai jisme repeatedly why question karke problem ke underlying cause tak pahunchne ki koshish ki jati hai, Boss."
        }

        // =====================================================
        // FMEA
        // =====================================================

        if (has(
                c,
                "what is fmea",
                "fmea kya hai",
                "failure mode effects analysis"
            )
        ) {
            return "FMEA yani Failure Mode and Effects Analysis ek systematic risk assessment technique hai jisme potential failure modes, effects aur causes evaluate karke risks prioritize kiye jate hain, Boss."
        }

        // =====================================================
        // DOCUMENT CONTROL
        // =====================================================

        if (has(
                c,
                "document control",
                "document control kya hai",
                "controlled document"
            )
        ) {
            return "Document Control approved documents ko create, review, approve, issue, revise, distribute aur obsolete karne ka controlled system hai, Boss."
        }

        // =====================================================
        // LINE CLEARANCE
        // =====================================================

        if (has(
                c,
                "line clearance",
                "line clearance kya hai"
            )
        ) {
            return "Line Clearance ek documented verification process hai jisme previous product, material, labels, documents aur unwanted items ko remove karke next operation ke liye area aur line ki readiness verify ki jati hai, Boss."
        }

        // =====================================================
        // CROSS CONTAMINATION
        // =====================================================

        if (has(
                c,
                "cross contamination",
                "cross contamination kya hai",
                "cross contamination in pharma"
            )
        ) {
            return "Cross-contamination ka matlab ek material, product ya microorganism ka doosre material ya product mein unintended transfer hona hai, Boss."
        }

        // =====================================================
        // MIX-UP
        // =====================================================

        if (has(
                c,
                "mix up in pharma",
                "mix-up kya hai",
                "material mix up"
            )
        ) {
            return "Pharmaceutical mix-up mein wrong material, product, label, component ya document ka unintended use ya association ho sakta hai. Identification aur segregation controls is risk ko reduce karte hain, Boss."
        }

        // =====================================================
        // HOLD TIME
        // =====================================================

        if (has(
                c,
                "what is hold time",
                "hold time kya hai",
                "hold time study"
            )
        ) {
            return "Hold Time Study defined storage conditions aur time period ke dauran intermediate, bulk ya material ki quality maintain rehne ko evaluate karti hai, Boss."
        }

        // =====================================================
        // PROCESS PARAMETERS
        // =====================================================

        if (has(
                c,
                "critical process parameter",
                "cpp kya hai",
                "critical process parameters"
            )
        ) {
            return "Critical Process Parameter yani CPP ek process parameter hai jiska variability critical quality attribute ko impact kar sakta hai aur isliye controlled within defined limits hona important hai, Boss."
        }

        // =====================================================
        // CQA
        // =====================================================

        if (has(
                c,
                "what is cqa",
                "cqa kya hai",
                "critical quality attribute"
            )
        ) {
            return "Critical Quality Attribute yani CQA ek physical, chemical, biological ya microbiological property hai jo predetermined limits, range ya distribution ke andar honi chahiye to ensure required product quality, Boss."
        }

        // =====================================================
        // CPP / CQA DIFFERENCE
        // =====================================================

        if (has(
                c,
                "cpp and cqa difference",
                "difference between cpp and cqa",
                "cpp cqa difference"
            )
        ) {
            return "CPP process se related critical parameter hai, jabki CQA product ki critical quality property hai. CPP mein change CQA ko potentially impact kar sakta hai, Boss."
        }

        // =====================================================
        // GRANULE TESTING
        // =====================================================

        if (has(
                c,
                "granule testing",
                "granules ke tests",
                "granules testing"
            )
        ) {
            return "Granules ke applicable tests mein moisture or loss on drying, particle size distribution, bulk density, tapped density, flow properties aur formulation-specific tests include ho sakte hain, Boss."
        }

        // =====================================================
        // BULK DENSITY
        // =====================================================

        if (has(
                c,
                "bulk density",
                "bulk density kya hai"
            )
        ) {
            return "Bulk density powder ke mass per bulk volume ko represent karti hai, jisme interparticle void spaces bhi include hote hain, Boss."
        }

        // =====================================================
        // TAPPED DENSITY
        // =====================================================

        if (has(
                c,
                "tapped density",
                "tapped density kya hai"
            )
        ) {
            return "Tapped density powder ko defined tapping procedure ke baad obtained mass per tapped volume ko represent karti hai, Boss."
        }

        // =====================================================
        // CARR INDEX
        // =====================================================

        if (has(
                c,
                "carr index",
                "carr's index",
                "carr index kya hai"
            )
        ) {
            return "Carr Index powder flowability ka indirect indicator hai aur commonly bulk density aur tapped density ke relationship se calculate kiya jata hai, Boss."
        }

        // =====================================================
        // HAUSNER RATIO
        // =====================================================

        if (has(
                c,
                "hausner ratio",
                "hausner ratio kya hai"
            )
        ) {
            return "Hausner Ratio tapped density aur bulk density ke relationship par based powder flowability ka indirect indicator hai, Boss."
        }

        // =====================================================
        // LOSS ON DRYING
        // =====================================================

        if (has(
                c,
                "what is lod",
                "lod kya hai",
                "loss on drying",
                "loss on drying kya hai"
            )
        ) {
            return "LOD yani Loss on Drying ek test hai jo specified drying conditions mein sample ke mass loss ko determine karta hai. Result mein moisture ke saath other volatile components bhi contribute kar sakte hain, Boss."
        }

        // =====================================================
        // MOISTURE
        // =====================================================

        if (has(
                c,
                "moisture content",
                "moisture content kya hai",
                "moisture in granules"
            )
        ) {
            return "Moisture content material ya granules mein present water ya specified volatile moisture-related content ko indicate karta hai. Acceptable limit product aur process requirements par depend karti hai, Boss."
        }

        // =====================================================
        // PH
        // =====================================================

        if (has(
                c,
                "what is ph",
                "ph kya hai",
                "ph in pharma"
            )
        ) {
            return "pH aqueous system ki acidity ya alkalinity ko express karta hai. Pharmaceutical products aur water systems mein pH applicable specifications ke according control kiya ja sakta hai, Boss."
        }

        // =====================================================
        // HPLC
        // =====================================================

        if (has(
                c,
                "what is hplc",
                "hplc kya hai",
                "hplc in pharma"
            )
        ) {
            return "HPLC yani High Performance Liquid Chromatography ek analytical separation technique hai jo pharmaceutical analysis mein assay, related substances aur other analytical applications ke liye commonly use hoti hai, Boss."
        }

        // =====================================================
        // GC
        // =====================================================

        if (has(
                c,
                "what is gc",
                "gc kya hai",
                "gas chromatography"
            )
        ) {
            return "GC yani Gas Chromatography volatile ya suitable analytes ko separate aur analyze karne ke liye use hone wali analytical technique hai, Boss."
        }

        // =====================================================
        // UV
        // =====================================================

        if (has(
                c,
                "what is uv spectroscopy",
                "uv spectroscopy kya hai",
                "uv kya hai"
            )
        ) {
            return "UV-Visible spectroscopy light absorption ko measure karne wali analytical technique hai. Pharmaceutical analysis mein suitable compounds ke quantitative ya qualitative evaluation ke liye use ho sakti hai, Boss."
        }

        // =====================================================
        // FTIR
        // =====================================================

        if (has(
                c,
                "what is ftir",
                "ftir kya hai",
                "ftir in pharma"
            )
        ) {
            return "FTIR yani Fourier Transform Infrared Spectroscopy infrared radiation ke interaction ke basis par material identification aur characterization mein use hoti hai, Boss."
        }

        // =====================================================
        // DISSOLUTION CALCULATION
        // =====================================================

        if (has(
                c,
                "dissolution calculation",
                "dissolution percentage calculation"
            )
        ) {
            return "Dissolution calculation generally analytical result, dilution factors, sample volume, standard concentration aur label claim jaise method-specific factors par depend karti hai. Exact calculation approved analytical method ke according karni chahiye, Boss."
        }

        // =====================================================
        // ASSAY CALCULATION
        // =====================================================

        if (has(
                c,
                "assay calculation",
                "assay percentage calculation"
            )
        ) {
            return "Assay calculation method-specific hoti hai aur standard/sample response, concentration, dilution factor, potency aur label claim jaise factors use kar sakti hai. Exact formula approved analytical method se lena chahiye, Boss."
        }

        // =====================================================
        // DEVIATION INVESTIGATION
        // =====================================================

        if (has(
                c,
                "deviation investigation",
                "deviation investigation kya hai"
            )
        ) {
            return "Deviation investigation mein event ko accurately describe karke immediate actions, impact assessment, root cause analysis, product or process impact aur appropriate CAPA evaluate kiye jate hain, Boss."
        }

        // =====================================================
        // OOS INVESTIGATION
        // =====================================================

        if (has(
                c,
                "oos investigation",
                "oos investigation kya hai"
            )
        ) {
            return "OOS investigation ka objective laboratory aur manufacturing-related potential causes ko scientifically evaluate karna hai. Investigation approved procedure aur applicable regulatory expectations ke according documented honi chahiye, Boss."
        }

        // =====================================================
        // OOT INVESTIGATION
        // =====================================================

        if (has(
                c,
                "oot investigation",
                "oot investigation kya hai"
            )
        ) {
            return "OOT investigation mein unusual analytical trend ko historical data, analytical process aur applicable manufacturing or stability information ke context mein evaluate kiya jata hai, Boss."
        }

        // =====================================================
        // AUDIT
        // =====================================================

        if (has(
                c,
                "what is audit",
                "audit kya hai",
                "pharma audit"
            )
        ) {
            return "Pharmaceutical audit ek systematic aur documented examination hai jisme processes, systems aur records ko applicable requirements aur established procedures ke against evaluate kiya jata hai, Boss."
        }

        // =====================================================
        // SELF INSPECTION
        // =====================================================

        if (has(
                c,
                "self inspection",
                "self inspection kya hai",
                "self inspection pharma"
            )
        ) {
            return "Self-inspection internal assessment hoti hai jiska purpose GMP compliance, systems aur operational practices mein gaps identify karna aur improvement opportunities find karna hai, Boss."
        }

        // =====================================================
        // MARKET COMPLAINT
        // =====================================================

        if (has(
                c,
                "market complaint",
                "market complaint kya hai",
                "product complaint"
            )
        ) {
            return "Market complaint customer ya market se received product-related concern hota hai. Complaint ko appropriately record, evaluate, investigate aur required action ke according close kiya jata hai, Boss."
        }

        // =====================================================
        // RECALL
        // =====================================================

        if (has(
                c,
                "what is product recall",
                "product recall kya hai",
                "recall in pharma"
            )
        ) {
            return "Product recall ek controlled process hai jisme market se affected pharmaceutical product ko remove ya return karne ke actions liye jate hain according to applicable procedures and regulatory requirements, Boss."
        }

        // =====================================================
        // TRAINING
        // =====================================================

        if (has(
                c,
                "gmp training",
                "pharma training",
                "training in pharma"
            )
        ) {
            return "Pharmaceutical training mein GMP, SOPs, job-specific procedures, data integrity, safety aur applicable quality requirements par personnel ko appropriate training di jati hai, Boss."
        }

        // =====================================================
        // CLEANING
        // =====================================================

        if (has(
                c,
                "equipment cleaning",
                "equipment cleaning kya hai",
                "cleaning in pharma"
            )
        ) {
            return "Equipment cleaning ka objective previous product, residues, cleaning agents aur contamination ko defined procedure ke according remove karna hai, Boss."
        }

        // =====================================================
        // EQUIPMENT STATUS
        // =====================================================

        if (has(
                c,
                "equipment status",
                "equipment status kya hai",
                "equipment label status"
            )
        ) {
            return "Pharmaceutical equipment status labels ya electronic status systems ke through equipment ki current condition, cleanliness, usage ya calibration/qualification status identify kiya ja sakta hai, Boss."
        }

        // =====================================================
        // CALIBRATION STATUS
        // =====================================================

        if (has(
                c,
                "calibration status",
                "calibration due",
                "calibration overdue"
            )
        ) {
            return "Calibration status verify karte waqt instrument identification, calibration due date, current status aur applicable calibration record ko check karna important hai, Boss."
        }

        // =====================================================
        // BALANCE
        // =====================================================

        if (has(
                c,
                "pharma balance",
                "weighing balance",
                "balance calibration"
            )
        ) {
            return "Pharmaceutical weighing balance ko appropriate capacity, readability, calibration status, environmental conditions aur routine verification requirements ke according control kiya jata hai, Boss."
        }

        // =====================================================
        // DISSOLUTION BASIC PARAMETERS
        // =====================================================

        if (has(
                c,
                "dissolution rpm",
                "dissolution speed",
                "dissolution rotation speed"
            )
        ) {
            return "Dissolution rotation speed, medium, temperature, apparatus, sampling time aur other conditions approved method aur applicable monograph ke according set kiye jate hain, Boss."
        }

        // =====================================================
        // GENERAL PHARMA
        // =====================================================

        if (has(
                c,
                "pharmaceutical industry",
                "pharma industry",
                "pharmaceutical industry kya hai",
                "pharma industry kya hai"
            )
        ) {
            return "Pharmaceutical industry medicines aur healthcare products ki research, development, manufacturing, testing, quality control, packaging, storage aur distribution se related industry hai, Boss."
        }

        return null
    }
}
