package com.example.myaiassistant.knowledge

data class AurixDeepKnowledgeItem(
    val category: String,
    val topic: String,
    val keywords: List<String>,
    val answer: String,
    val confidence: Int = 95,
    val currentInformation: Boolean = false
)

object AurixDeepKnowledge {

    // =====================================================
    // DEEP LOCAL KNOWLEDGE DATABASE
    // =====================================================

    val entries = listOf(

        // =================================================
        // PHYSICS — FUNDAMENTALS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Newton First Law",
            keywords = listOf(
                "newton first law",
                "newton's first law",
                "law of inertia",
                "inertia kya hai",
                "inertia law"
            ),
            answer = "Newton ka first law law of inertia kehlata hai. Iske according koi object rest mein hai to rest mein rahega aur motion mein hai to same velocity se move karta rahega jab tak us par external unbalanced force act na kare.",
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Newton Second Law",
            keywords = listOf(
                "newton second law",
                "newton's second law",
                "second law of motion",
                "f equals ma",
                "f ma"
            ),
            answer = "Newton ka second law kehta hai ki force object ke momentum ke change ki rate ke proportional hota hai. Constant mass ke liye iska common formula F = m × a hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Newton Third Law",
            keywords = listOf(
                "newton third law",
                "newton's third law",
                "third law of motion",
                "action reaction law",
                "action reaction"
            ),
            answer = "Newton ka third law kehta hai ki har action ke equal magnitude ka aur opposite direction mein reaction hota hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Momentum",
            keywords = listOf(
                "momentum kya hai",
                "what is momentum",
                "momentum formula",
                "momentum ka formula"
            ),
            answer = "Momentum kisi moving object ki quantity of motion hai. Iska formula p = m × v hai, jahan m mass aur v velocity hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Kinetic Energy",
            keywords = listOf(
                "kinetic energy",
                "kinetic energy kya hai",
                "kinetic energy formula",
                "motion energy"
            ),
            answer = "Kinetic energy motion ki wajah se object mein stored energy hoti hai. Iska formula KE = 1/2 × m × v² hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Potential Energy",
            keywords = listOf(
                "potential energy",
                "potential energy kya hai",
                "potential energy formula",
                "gravitational potential energy"
            ),
            answer = "Potential energy object ki position ya configuration ki wajah se stored energy hoti hai. Near Earth's surface gravitational potential energy ka common formula PE = m × g × h hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Work",
            keywords = listOf(
                "work in physics",
                "physics work",
                "work formula physics",
                "work kya hai physics"
            ),
            answer = "Physics mein work tab hota hai jab force ke component ki wajah se displacement hota hai. Constant force ke simple case mein W = F × d × cos(theta) hota hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Power",
            keywords = listOf(
                "power physics",
                "power kya hai",
                "power formula",
                "physics power formula"
            ),
            answer = "Power work karne ki rate hai. Iska basic formula P = W/t hai. SI unit watt hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Pressure",
            keywords = listOf(
                "pressure kya hai",
                "pressure formula",
                "physics pressure",
                "pressure ka formula"
            ),
            answer = "Pressure unit area par lagne wala normal force hai. Formula P = F/A hai aur SI unit pascal hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Density",
            keywords = listOf(
                "density kya hai",
                "density formula",
                "density ka formula",
                "mass per volume"
            ),
            answer = "Density kisi substance ke unit volume mein contained mass ko kehte hain. Formula rho = m/V hai aur SI unit kilogram per cubic meter hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Ohm Law",
            keywords = listOf(
                "ohm law",
                "ohm's law",
                "ohm law kya hai",
                "v equals ir",
                "v ir"
            ),
            answer = "Ohm's law ke according constant physical conditions mein current voltage ke directly proportional hota hai. Formula V = I × R hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Electric Power",
            keywords = listOf(
                "electric power",
                "electrical power formula",
                "electric power formula",
                "electrical power kya hai"
            ),
            answer = "Electrical power electrical energy transfer hone ki rate hai. Common formulas P = V × I, P = I²R aur P = V²/R hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Frequency",
            keywords = listOf(
                "frequency kya hai",
                "frequency formula",
                "frequency physics",
                "frequency meaning"
            ),
            answer = "Frequency kisi periodic event ke ek second mein hone wale cycles ki sankhya hai. SI unit hertz, yani Hz, hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Wavelength",
            keywords = listOf(
                "wavelength kya hai",
                "wavelength formula",
                "lambda kya hai",
                "wave length"
            ),
            answer = "Wavelength wave ke do successive points ke beech same phase wali minimum distance hoti hai, jaise crest se next crest tak. Wave relation v = f × lambda hai."
        ),

        // =================================================
        // THERMODYNAMICS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "First Law Thermodynamics",
            keywords = listOf(
                "first law thermodynamics",
                "thermodynamics first law",
                "first law of thermodynamics",
                "thermodynamics ka first law"
            ),
            answer = "Thermodynamics ka first law energy conservation ka application hai. System ki internal energy ka change heat supplied aur system dwara kiye gaye work ke relationship se determine hota hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Second Law Thermodynamics",
            keywords = listOf(
                "second law thermodynamics",
                "thermodynamics second law",
                "second law of thermodynamics",
                "entropy law"
            ),
            answer = "Thermodynamics ka second law batata hai ki natural processes ki direction hoti hai aur isolated system ki total entropy decrease nahi hoti."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Entropy",
            keywords = listOf(
                "entropy kya hai",
                "entropy thermodynamics",
                "what is entropy",
                "entropy meaning"
            ),
            answer = "Entropy thermodynamic state function hai jo energy dispersal aur microscopic possibilities se related hai. Isolated system mein spontaneous processes ke dauran total entropy generally increase karti hai ya ideal reversible case mein constant rehti hai."
        ),

        // =================================================
        // OPTICS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Reflection",
            keywords = listOf(
                "reflection of light",
                "light reflection",
                "reflection kya hai",
                "prakash ka paravartan"
            ),
            answer = "Reflection mein light kisi surface se takrakar same medium mein wapas propagate karti hai. Law of reflection ke according angle of incidence angle of reflection ke equal hota hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Refraction",
            keywords = listOf(
                "refraction of light",
                "light refraction",
                "refraction kya hai",
                "prakash ka apavartan"
            ),
            answer = "Refraction mein light ek medium se doosre medium mein enter karte waqt speed change hone ke karan direction change karti hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Physics",
            topic = "Lens",
            keywords = listOf(
                "what is lens",
                "lens kya hai",
                "convex lens",
                "concave lens"
            ),
            answer = "Lens ek transparent optical element hai jo refraction ke through light ko converge ya diverge kar sakta hai. Convex lens generally converging lens aur concave lens generally diverging lens hota hai."
        ),

        // =================================================
        // CHEMISTRY — FUNDAMENTALS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Atom",
            keywords = listOf(
                "atom kya hai",
                "what is atom",
                "atomic structure",
                "atom structure"
            ),
            answer = "Atom kisi chemical element ki basic unit hai jo us element ki chemical identity maintain karti hai. Atom mein nucleus ke andar protons aur neutrons hote hain aur electrons nucleus ke around quantum states mein present hote hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Molecule",
            keywords = listOf(
                "molecule kya hai",
                "what is molecule",
                "molecule meaning",
                "molecular structure"
            ),
            answer = "Molecule do ya do se zyada atoms ke chemical bonding se bana electrically neutral entity ho sakta hai, jaise H2O aur O2."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Mole",
            keywords = listOf(
                "mole chemistry",
                "mole kya hai",
                "what is mole",
                "one mole"
            ),
            answer = "Mole amount of substance ki SI unit hai. Ek mole mein exactly 6.02214076 × 10²³ specified entities hote hain. Is number ko Avogadro constant kaha jata hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "pH",
            keywords = listOf(
                "ph kya hai",
                "what is ph",
                "ph scale",
                "ph value"
            ),
            answer = "pH aqueous system ki acidity ya basicity ko express karne wali logarithmic quantity hai. Dilute aqueous solutions mein 25 degree Celsius par neutral water ka pH approximately 7 hota hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Acid",
            keywords = listOf(
                "acid kya hai",
                "what is acid chemistry",
                "acid definition",
                "acid meaning"
            ),
            answer = "Brønsted-Lowry definition ke according acid proton donor hota hai. Arrhenius concept mein acid aqueous solution mein hydrogen ion concentration increase karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Base",
            keywords = listOf(
                "base kya hai chemistry",
                "what is base chemistry",
                "base definition",
                "alkali kya hai"
            ),
            answer = "Brønsted-Lowry definition ke according base proton acceptor hota hai. Arrhenius concept mein base aqueous solution mein hydroxide ion concentration increase karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Catalyst",
            keywords = listOf(
                "catalyst kya hai",
                "what is catalyst",
                "catalyst chemistry",
                "catalyst meaning"
            ),
            answer = "Catalyst reaction ke alternate pathway ki activation energy ko reduce karke reaction rate ko change karta hai aur reaction ke end mein overall consume nahi hota."
        ),

        // =================================================
        // CHEMISTRY — ORGANIC
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Organic Chemistry",
            keywords = listOf(
                "organic chemistry kya hai",
                "what is organic chemistry",
                "organic chemistry"
            ),
            answer = "Organic chemistry carbon-containing compounds ki structure, properties, reactions aur synthesis ka study hai. Carbon ke bonding patterns ki wajah se organic chemistry mein bahut large number of compounds milte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Chemistry",
            topic = "Polymer",
            keywords = listOf(
                "polymer kya hai",
                "what is polymer",
                "polymer chemistry",
                "polymer meaning"
            ),
            answer = "Polymer large molecule hota hai jo repeating structural units, jinhe monomers se derive kiya ja sakta hai, se bana hota hai. Examples mein polyethylene, nylon aur PVC shamil hain."
        ),

        // =================================================
        // BIOLOGY
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Biology",
            topic = "Cell",
            keywords = listOf(
                "cell kya hai",
                "what is cell biology",
                "cell biology",
                "jeev koshika kya hai"
            ),
            answer = "Cell life ki fundamental structural aur functional unit hai. Living organisms ek ya multiple cells se bane hote hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Biology",
            topic = "DNA",
            keywords = listOf(
                "dna kya hai",
                "what is dna",
                "dna meaning",
                "dna function"
            ),
            answer = "DNA, yani deoxyribonucleic acid, genetic information store karne wala nucleic acid hai. Most cellular organisms mein DNA genetic instructions ko maintain aur transmit karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Biology",
            topic = "RNA",
            keywords = listOf(
                "rna kya hai",
                "what is rna",
                "rna meaning",
                "rna function"
            ),
            answer = "RNA, yani ribonucleic acid, genetic information ke expression aur kai cellular processes mein important role play karta hai. Messenger RNA protein synthesis ke liye genetic information carry karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Biology",
            topic = "Photosynthesis",
            keywords = listOf(
                "photosynthesis kya hai",
                "what is photosynthesis biology",
                "photosynthesis process",
                "plant food kaise banate hain"
            ),
            answer = "Photosynthesis mein plants aur kuch other organisms light energy ko chemical energy mein convert karte hain. Green plants carbon dioxide aur water se carbohydrates banate hain aur oxygen release karte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Biology",
            topic = "Mitochondria",
            keywords = listOf(
                "mitochondria kya hai",
                "mitochondria function",
                "powerhouse of cell",
                "cell powerhouse"
            ),
            answer = "Mitochondria eukaryotic cells ke organelles hain jahan aerobic cellular respiration se ATP production ka major portion hota hai. Isi wajah se mitochondria ko commonly cell ka powerhouse kaha jata hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Biology",
            topic = "Ribosome",
            keywords = listOf(
                "ribosome kya hai",
                "ribosome function",
                "protein synthesis organelle",
                "protein synthesis"
            ),
            answer = "Ribosomes molecular machines hain jo mRNA ki information ko read karke amino acids ko peptide chain mein assemble karte hain. Ye protein synthesis ka major site hain."
        ),

        // =================================================
        // HUMAN BODY
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Human Body",
            topic = "Blood",
            keywords = listOf(
                "blood kya hai",
                "blood components",
                "human blood",
                "blood mein kya hota hai"
            ),
            answer = "Blood ek connective tissue hai jisme plasma aur formed elements, jaise red blood cells, white blood cells aur platelets, hote hain. Ye oxygen, nutrients, hormones aur waste products ke transport mein important hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Human Body",
            topic = "Red Blood Cells",
            keywords = listOf(
                "red blood cells",
                "rbc kya hai",
                "rbc function",
                "erythrocytes"
            ),
            answer = "Red blood cells, yani erythrocytes, primarily hemoglobin ki help se oxygen transport karte hain aur carbon dioxide transport mein bhi contribute karte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Human Body",
            topic = "White Blood Cells",
            keywords = listOf(
                "white blood cells",
                "wbc kya hai",
                "wbc function",
                "leukocytes"
            ),
            answer = "White blood cells, yani leukocytes, immune system ka part hain aur infections aur foreign substances ke against defense mein role play karte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Human Body",
            topic = "Platelets",
            keywords = listOf(
                "platelets kya hai",
                "platelet function",
                "blood platelets",
                "thrombocytes"
            ),
            answer = "Platelets blood clot formation mein important cellular fragments hain. Injury ke baad ye hemostasis process mein contribute karte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Human Body",
            topic = "Kidney",
            keywords = listOf(
                "kidney kya karti hai",
                "kidney function",
                "kidney function human body",
                "renal function"
            ),
            answer = "Kidneys blood ko filter karke waste products aur excess substances ko urine ke through remove karne mein help karti hain. Ye fluid, electrolyte aur acid-base balance regulation mein bhi important hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Human Body",
            topic = "Liver",
            keywords = listOf(
                "liver kya karta hai",
                "liver function",
                "human liver",
                "liver functions"
            ),
            answer = "Liver metabolism, nutrient processing, bile production, detoxification-related biochemical processes aur plasma proteins ki synthesis jaise kai important functions perform karta hai."
        ),

        // =================================================
        // GEOGRAPHY
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Geography",
            topic = "Continents",
            keywords = listOf(
                "continents ke naam",
                "names of continents",
                "7 continents",
                "seven continents"
            ),
            answer = "Seven-continent model mein Asia, Africa, Europe, North America, South America, Antarctica aur Australia hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Geography",
            topic = "Equator",
            keywords = listOf(
                "equator kya hai",
                "what is equator",
                "equator line",
                "vishuv rekha"
            ),
            answer = "Equator Earth ke center ke around imaginary great circle hai jo Earth ko Northern aur Southern Hemispheres mein divide karta hai. Iska latitude 0 degree hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Geography",
            topic = "Prime Meridian",
            keywords = listOf(
                "prime meridian kya hai",
                "prime meridian",
                "zero longitude",
                "0 degree longitude"
            ),
            answer = "Prime Meridian 0 degree longitude ko define karta hai aur Greenwich, London se traditionally pass hota hai. Longitude measurements isi reference se east ya west mein kiye jate hain."
        ),

        // =================================================
        // INDIA
        // =================================================

        AurixDeepKnowledgeItem(
            category = "India",
            topic = "Indian Constitution",
            keywords = listOf(
                "indian constitution kya hai",
                "constitution of india",
                "bharat ka samvidhan",
                "indian constitution"
            ),
            answer = "Indian Constitution India ka supreme legal framework hai. Ye government ke institutions, powers, fundamental rights, directive principles aur citizens ke constitutional framework ko define karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "India",
            topic = "Indian Parliament",
            keywords = listOf(
                "indian parliament",
                "parliament of india",
                "bharat ki parliament",
                "sansad kya hai"
            ),
            answer = "India ki Parliament constitutional framework ke according President aur do Houses, Lok Sabha aur Rajya Sabha, se milkar banti hai."
        ),

        AurixDeepKnowledgeItem(
            category = "India",
            topic = "Fundamental Rights",
            keywords = listOf(
                "fundamental rights india",
                "mool adhikar",
                "fundamental rights kya hai",
                "indian fundamental rights"
            ),
            answer = "Indian Constitution ka Part III Fundamental Rights se related hai. Ye citizens aur persons ko specified constitutional protections aur freedoms provide karta hai."
        ),

        // =================================================
        // SPACE
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Space",
            topic = "Black Hole",
            keywords = listOf(
                "black hole kya hai",
                "what is black hole",
                "black hole",
                "black hole meaning"
            ),
            answer = "Black hole spacetime ka aisa region hai jahan gravity itni strong hoti hai ki event horizon ke andar se light bhi escape nahi kar sakti. Iski boundary ko event horizon kaha jata hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Space",
            topic = "Galaxy",
            keywords = listOf(
                "galaxy kya hai",
                "what is galaxy",
                "galaxy meaning",
                "milky way kya hai"
            ),
            answer = "Galaxy stars, gas, dust, dark matter aur other structures ka gravitationally bound system hoti hai. Milky Way woh galaxy hai jisme Solar System located hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Space",
            topic = "Light Year",
            keywords = listOf(
                "light year kya hai",
                "what is light year",
                "lightyear",
                "light year distance"
            ),
            answer = "Light-year time ki nahi, distance ki unit hai. Ye vacuum mein light dwara ek Julian year mein travel ki gayi distance ko represent karta hai."
        ),

        // =================================================
        // COMPUTER
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Computer",
            topic = "CPU",
            keywords = listOf(
                "cpu kya hai",
                "what is cpu",
                "processor kya hai",
                "cpu function"
            ),
            answer = "CPU, yani Central Processing Unit, computer ke instructions ko execute karta hai aur calculations aur control operations perform karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Computer",
            topic = "RAM",
            keywords = listOf(
                "ram kya hai",
                "what is ram",
                "ram function",
                "computer ram"
            ),
            answer = "RAM, yani Random Access Memory, temporary working memory hai jahan operating system aur currently used programs ka data active processing ke liye rakha jata hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Computer",
            topic = "Operating System",
            keywords = listOf(
                "operating system kya hai",
                "what is operating system",
                "os kya hai",
                "operating system meaning"
            ),
            answer = "Operating system system software hai jo hardware resources manage karta hai aur applications ko services provide karta hai. Android, Windows aur Linux operating systems ke examples hain."
        ),

        // =================================================
        // AI
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Artificial Intelligence",
            topic = "Artificial Intelligence",
            keywords = listOf(
                "artificial intelligence kya hai",
                "what is artificial intelligence",
                "ai kya hai",
                "ai meaning"
            ),
            answer = "Artificial Intelligence computer systems ki aisi capability hai jisme systems perception, reasoning, learning, language processing ya decision-support jaise tasks perform kar sakte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Artificial Intelligence",
            topic = "Machine Learning",
            keywords = listOf(
                "machine learning kya hai",
                "what is machine learning",
                "ml kya hai",
                "machine learning"
            ),
            answer = "Machine learning AI ka ek area hai jisme algorithms data se patterns learn karte hain aur explicit rules ke bina predictions ya decisions perform kar sakte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Artificial Intelligence",
            topic = "Large Language Model",
            keywords = listOf(
                "large language model",
                "llm kya hai",
                "what is llm",
                "language model kya hai"
            ),
            answer = "Large Language Model, ya LLM, machine learning model hota hai jo large text datasets se language patterns learn karta hai aur text ko understand, generate ya transform karne ke tasks perform kar sakta hai."
        ),

        // =================================================
        // ENGINEERING
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Engineering",
            topic = "Manufacturing",
            keywords = listOf(
                "manufacturing kya hai",
                "what is manufacturing",
                "manufacturing process",
                "manufacturing meaning"
            ),
            answer = "Manufacturing raw materials ya components ko controlled processes ke through finished products mein convert karne ka industrial process hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Engineering",
            topic = "CNC",
            keywords = listOf(
                "cnc kya hai",
                "what is cnc",
                "cnc machine",
                "cnc machining"
            ),
            answer = "CNC, yani Computer Numerical Control, machine tools ko programmed instructions ke through automatically control karne ki technology hai."
        ),

        // =================================================
        // ELECTRICAL / ELECTRONICS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Electronics",
            topic = "Diode",
            keywords = listOf(
                "diode kya hai",
                "what is diode",
                "diode function",
                "diode electronics"
            ),
            answer = "Diode semiconductor device hai jo generally current ko ek preferred direction mein conduct karta hai aur opposite direction mein blocking behavior show karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Electronics",
            topic = "Transistor",
            keywords = listOf(
                "transistor kya hai",
                "what is transistor",
                "transistor function",
                "transistor electronics"
            ),
            answer = "Transistor semiconductor device hai jo electronic signals ko amplify ya switch karne ke liye use hota hai. Ye modern electronic circuits ka fundamental component hai."
        ),

        // =================================================
        // CONSTRUCTION
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Construction",
            topic = "Concrete",
            keywords = listOf(
                "concrete kya hai",
                "what is concrete",
                "concrete construction",
                "concrete material"
            ),
            answer = "Concrete cementitious binder, water aur aggregates ka composite material hai. Iski properties mix design, curing, materials aur environmental conditions par depend karti hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Construction",
            topic = "Reinforced Concrete",
            keywords = listOf(
                "reinforced concrete",
                "rcc kya hai",
                "rcc construction",
                "reinforced cement concrete"
            ),
            answer = "Reinforced concrete mein concrete ke saath steel reinforcement provide kiya jata hai. Concrete compression resist karne mein strong hota hai aur reinforcement tensile stresses carry karne mein help karta hai."
        ),

        // =================================================
        // PHARMACEUTICAL
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "Granulation",
            keywords = listOf(
                "granulation kya hai",
                "pharmaceutical granulation",
                "wet granulation",
                "dry granulation",
                "tablet granulation"
            ),
            answer = "Pharmaceutical granulation powder particles ko larger, more uniform granules mein convert karne ki process hai. Wet granulation mein liquid binder commonly use hota hai, jabki dry granulation mein liquid binder ke bina compaction based methods use kiye ja sakte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "Tablet Compression",
            keywords = listOf(
                "tablet compression kya hai",
                "tablet compression",
                "tablet making process",
                "compression machine tablet"
            ),
            answer = "Tablet compression mein prepared powder ya granules ko punches aur dies ki help se controlled pressure ke under compact karke tablet banayi jati hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "Dissolution",
            keywords = listOf(
                "dissolution kya hai",
                "pharmaceutical dissolution",
                "tablet dissolution",
                "dissolution test"
            ),
            answer = "Dissolution test controlled laboratory conditions mein drug substance ke dosage form se dissolution medium mein release hone ki rate aur extent ko evaluate karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "Disintegration",
            keywords = listOf(
                "disintegration kya hai",
                "tablet disintegration",
                "disintegration test",
                "tablet disintegration test"
            ),
            answer = "Disintegration test evaluate karta hai ki tablet ya capsule specified conditions mein smaller particles mein kitni jaldi break down hota hai. Disintegration aur dissolution ek hi test nahi hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "Friability",
            keywords = listOf(
                "friability kya hai",
                "tablet friability",
                "friability test",
                "tablet friability test"
            ),
            answer = "Friability tablet ki tendency ko measure karti hai ki handling aur mechanical stress ke during tablet se particles ya mass kitna lose hota hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "HPLC",
            keywords = listOf(
                "hplc kya hai",
                "what is hplc",
                "hplc chromatography",
                "hplc principle"
            ),
            answer = "HPLC, yani High Performance Liquid Chromatography, liquid mobile phase aur stationary phase ke interaction ke basis par compounds ko separate, identify aur quantify karne ki analytical technique hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Pharmaceutical",
            topic = "FTIR",
            keywords = listOf(
                "ftir kya hai",
                "what is ftir",
                "ftir spectroscopy",
                "ftir principle"
            ),
            answer = "FTIR, yani Fourier Transform Infrared Spectroscopy, infrared radiation ke absorption pattern ke through molecules ke functional groups aur chemical identity ke baare mein information provide karti hai."
        ),

        // =================================================
        // QUALITY
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Quality",
            topic = "OOS",
            keywords = listOf(
                "oos kya hai",
                "out of specification",
                "oos pharmaceutical",
                "oos meaning pharma"
            ),
            answer = "OOS, yani Out of Specification, tab use hota hai jab test result approved specification ya acceptance criteria ko meet nahi karta. OOS investigation documented scientific procedure ke according ki jati hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Quality",
            topic = "OOT",
            keywords = listOf(
                "oot kya hai",
                "out of trend",
                "oot pharmaceutical",
                "oot meaning pharma"
            ),
            answer = "OOT, yani Out of Trend, generally aise result ya pattern ko refer karta hai jo established historical trend ke consistent behavior se unusual deviation show kare, chahe result specification ke andar ho."
        ),

        AurixDeepKnowledgeItem(
            category = "Quality",
            topic = "CAPA",
            keywords = listOf(
                "capa kya hai",
                "what is capa",
                "capa pharma",
                "corrective preventive action"
            ),
            answer = "CAPA ka full form Corrective and Preventive Action hai. Corrective action existing problem ke cause ko address karti hai, jabki preventive action potential problem ya recurrence ke risk ko address karne ke liye implement ki ja sakti hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Quality",
            topic = "ALCOA Plus",
            keywords = listOf(
                "alcoa plus",
                "alcoa kya hai",
                "data integrity alcoa",
                "alcoa principles"
            ),
            answer = "ALCOA data integrity principles ka framework hai: Attributable, Legible, Contemporaneous, Original aur Accurate. ALCOA Plus mein Complete, Consistent, Enduring aur Available jaise additional expectations include kiye jate hain."
        ),

        // =================================================
        // ANIMALS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Animals",
            topic = "Mammals",
            keywords = listOf(
                "mammal kya hai",
                "what is mammal",
                "mammals kya hote hain",
                "mammal definition"
            ),
            answer = "Mammals vertebrate animals hain jinmein generally hair ya fur hota hai aur female mammals mammary glands se milk produce karte hain. Most mammals warm-blooded endothermic animals hote hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Animals",
            topic = "Reptiles",
            keywords = listOf(
                "reptile kya hai",
                "what is reptile",
                "reptiles kya hote hain",
                "reptile definition"
            ),
            answer = "Reptiles vertebrate animals ka group hain jinki skin generally keratinized scales se covered hoti hai. Examples snakes, lizards, turtles aur crocodilians hain."
        ),

        // =================================================
        // PLANTS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Plants",
            topic = "Root",
            keywords = listOf(
                "root ka function",
                "plant root function",
                "roots kya karti hain",
                "root function"
            ),
            answer = "Plant roots plant ko soil mein anchor karne, water aur minerals absorb karne aur kuch plants mein food storage jaise functions perform karti hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Plants",
            topic = "Leaf",
            keywords = listOf(
                "leaf function",
                "leaf ka function",
                "leaves kya karti hain",
                "plant leaf function"
            ),
            answer = "Leaves photosynthesis ka major site hoti hain aur gas exchange aur water loss regulation mein bhi important role play karti hain."
        ),

        // =================================================
        // AVIATION
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Aviation",
            topic = "Lift",
            keywords = listOf(
                "airplane lift kya hai",
                "aircraft lift",
                "lift force aircraft",
                "plane hawa mein kaise udta hai"
            ),
            answer = "Aircraft par lift aerodynamic force hai jo flight mein weight ko oppose karne mein contribute karti hai. Wings ke shape, angle of attack, airflow aur other aerodynamic conditions lift generation ko affect karte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Aviation",
            topic = "Turbofan",
            keywords = listOf(
                "turbofan engine kya hai",
                "turbofan kya hai",
                "aircraft turbofan",
                "jet engine turbofan"
            ),
            answer = "Turbofan aircraft engine mein core engine ke saath fan hota hai. Fan se generate hone wala bypass airflow thrust generation mein major contribution kar sakta hai, especially modern commercial aircraft mein."
        ),

        // =================================================
        // MARITIME
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Maritime",
            topic = "Buoyancy",
            keywords = listOf(
                "ship float kaise karta hai",
                "ship water mein kaise float karta hai",
                "buoyancy kya hai",
                "archimedes principle ship"
            ),
            answer = "Ship buoyancy ki wajah se float karta hai. Archimedes principle ke according immersed body par upward buoyant force displaced fluid ke weight ke equal hoti hai."
        ),

        // =================================================
        // BUSINESS / ECONOMICS
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Economics",
            topic = "GDP",
            keywords = listOf(
                "gdp kya hai",
                "what is gdp",
                "gross domestic product",
                "gdp meaning"
            ),
            answer = "GDP, yani Gross Domestic Product, ek specified period mein economy ke andar produced final goods aur services ki monetary value ko measure karta hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Economics",
            topic = "Inflation",
            keywords = listOf(
                "inflation kya hai",
                "what is inflation",
                "mehngai kya hai",
                "inflation meaning"
            ),
            answer = "Inflation economy mein goods aur services ke general price level mein sustained increase ko refer karta hai, jisse same amount of money ki purchasing power generally reduce hoti hai."
        ),

        AurixDeepKnowledgeItem(
            category = "Business",
            topic = "Supply and Demand",
            keywords = listOf(
                "supply demand kya hai",
                "supply and demand",
                "demand supply economics",
                "supply demand meaning"
            ),
            answer = "Supply aur demand market prices aur quantities ko influence karne wale fundamental economic concepts hain. Demand buyers ki willingness aur ability to purchase ko aur supply sellers ki willingness aur ability to provide goods ko represent karti hai."
        ),

        // =================================================
        // EVERYDAY KNOWLEDGE
        // =================================================

        AurixDeepKnowledgeItem(
            category = "Everyday",
            topic = "Why Sky Blue",
            keywords = listOf(
                "sky blue kyu hai",
                "sky blue why",
                "aasman neela kyu hai",
                "why is sky blue"
            ),
            answer = "Daytime mein sky blue primarily Rayleigh scattering ki wajah se dikhta hai. Atmosphere ke molecules shorter-wavelength blue light ko longer-wavelength red light ke comparison mein zyada scatter karte hain."
        ),

        AurixDeepKnowledgeItem(
            category = "Everyday",
            topic = "Why Ice Floats",
            keywords = listOf(
                "ice water par kyu float karti hai",
                "ice floats why",
                "ice water mein kyu tairti hai",
                "why ice floats"
            ),
            answer = "Ice water se less dense hoti hai kyunki freezing ke time hydrogen bonding ek relatively open crystal structure banati hai. Isliye ice water ki surface par float karti hai."
        )
    )

    // =====================================================
    // SEARCH
    // =====================================================

    fun search(question: String): AurixDeepKnowledgeItem? {

        val q =
            question
                .lowercase(Locale.getDefault())
                .trim()
                .replace(Regex("\\s+"), " ")

        if (q.isBlank()) {
            return null
        }

        // Exact / phrase match first
        for (entry in entries) {

            for (keyword in entry.keywords) {

                val k =
                    keyword
                        .lowercase(Locale.getDefault())
                        .trim()

                if (
                    q == k ||
                    q.contains(k)
                ) {
                    return entry
                }
            }
        }

        return null
    }
}
