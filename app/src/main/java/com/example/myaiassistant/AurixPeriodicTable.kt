package com.example.myaiassistant
import java.util.Locale

data class AurixElement(
    val number: Int,
    val symbol: String,
    val name: String,
    val mass: String,
    val category: String
)

object AurixPeriodicTable {

    private val elements = listOf(

        AurixElement(1, "H", "Hydrogen", "1.008", "Nonmetal"),
        AurixElement(2, "He", "Helium", "4.0026", "Noble Gas"),
        AurixElement(3, "Li", "Lithium", "6.94", "Alkali Metal"),
        AurixElement(4, "Be", "Beryllium", "9.0122", "Alkaline Earth Metal"),
        AurixElement(5, "B", "Boron", "10.81", "Metalloid"),
        AurixElement(6, "C", "Carbon", "12.011", "Nonmetal"),
        AurixElement(7, "N", "Nitrogen", "14.007", "Nonmetal"),
        AurixElement(8, "O", "Oxygen", "15.999", "Nonmetal"),
        AurixElement(9, "F", "Fluorine", "18.998", "Halogen"),
        AurixElement(10, "Ne", "Neon", "20.180", "Noble Gas"),

        AurixElement(11, "Na", "Sodium", "22.990", "Alkali Metal"),
        AurixElement(12, "Mg", "Magnesium", "24.305", "Alkaline Earth Metal"),
        AurixElement(13, "Al", "Aluminium", "26.982", "Post-transition Metal"),
        AurixElement(14, "Si", "Silicon", "28.085", "Metalloid"),
        AurixElement(15, "P", "Phosphorus", "30.974", "Nonmetal"),
        AurixElement(16, "S", "Sulfur", "32.06", "Nonmetal"),
        AurixElement(17, "Cl", "Chlorine", "35.45", "Halogen"),
        AurixElement(18, "Ar", "Argon", "39.948", "Noble Gas"),

        AurixElement(19, "K", "Potassium", "39.098", "Alkali Metal"),
        AurixElement(20, "Ca", "Calcium", "40.078", "Alkaline Earth Metal"),
        AurixElement(21, "Sc", "Scandium", "44.956", "Transition Metal"),
        AurixElement(22, "Ti", "Titanium", "47.867", "Transition Metal"),
        AurixElement(23, "V", "Vanadium", "50.942", "Transition Metal"),
        AurixElement(24, "Cr", "Chromium", "51.996", "Transition Metal"),
        AurixElement(25, "Mn", "Manganese", "54.938", "Transition Metal"),
        AurixElement(26, "Fe", "Iron", "55.845", "Transition Metal"),
        AurixElement(27, "Co", "Cobalt", "58.933", "Transition Metal"),
        AurixElement(28, "Ni", "Nickel", "58.693", "Transition Metal"),
        AurixElement(29, "Cu", "Copper", "63.546", "Transition Metal"),
        AurixElement(30, "Zn", "Zinc", "65.38", "Transition Metal"),
        AurixElement(31, "Ga", "Gallium", "69.723", "Post-transition Metal"),
        AurixElement(32, "Ge", "Germanium", "72.630", "Metalloid"),
        AurixElement(33, "As", "Arsenic", "74.922", "Metalloid"),
        AurixElement(34, "Se", "Selenium", "78.971", "Nonmetal"),
        AurixElement(35, "Br", "Bromine", "79.904", "Halogen"),
        AurixElement(36, "Kr", "Krypton", "83.798", "Noble Gas"),

        AurixElement(37, "Rb", "Rubidium", "85.468", "Alkali Metal"),
        AurixElement(38, "Sr", "Strontium", "87.62", "Alkaline Earth Metal"),
        AurixElement(39, "Y", "Yttrium", "88.906", "Transition Metal"),
        AurixElement(40, "Zr", "Zirconium", "91.224", "Transition Metal"),
        AurixElement(41, "Nb", "Niobium", "92.906", "Transition Metal"),
        AurixElement(42, "Mo", "Molybdenum", "95.95", "Transition Metal"),
        AurixElement(43, "Tc", "Technetium", "[98]", "Transition Metal"),
        AurixElement(44, "Ru", "Ruthenium", "101.07", "Transition Metal"),
        AurixElement(45, "Rh", "Rhodium", "102.91", "Transition Metal"),
        AurixElement(46, "Pd", "Palladium", "106.42", "Transition Metal"),
        AurixElement(47, "Ag", "Silver", "107.87", "Transition Metal"),
        AurixElement(48, "Cd", "Cadmium", "112.41", "Transition Metal"),
        AurixElement(49, "In", "Indium", "114.82", "Post-transition Metal"),
        AurixElement(50, "Sn", "Tin", "118.71", "Post-transition Metal"),
        AurixElement(51, "Sb", "Antimony", "121.76", "Metalloid"),
        AurixElement(52, "Te", "Tellurium", "127.60", "Metalloid"),
        AurixElement(53, "I", "Iodine", "126.90", "Halogen"),
        AurixElement(54, "Xe", "Xenon", "131.29", "Noble Gas"),

        AurixElement(55, "Cs", "Cesium", "132.91", "Alkali Metal"),
        AurixElement(56, "Ba", "Barium", "137.33", "Alkaline Earth Metal"),
        AurixElement(57, "La", "Lanthanum", "138.91", "Lanthanide"),
        AurixElement(58, "Ce", "Cerium", "140.12", "Lanthanide"),
        AurixElement(59, "Pr", "Praseodymium", "140.91", "Lanthanide"),
        AurixElement(60, "Nd", "Neodymium", "144.24", "Lanthanide"),
        AurixElement(61, "Pm", "Promethium", "[145]", "Lanthanide"),
        AurixElement(62, "Sm", "Samarium", "150.36", "Lanthanide"),
        AurixElement(63, "Eu", "Europium", "151.96", "Lanthanide"),
        AurixElement(64, "Gd", "Gadolinium", "157.25", "Lanthanide"),
        AurixElement(65, "Tb", "Terbium", "158.93", "Lanthanide"),
        AurixElement(66, "Dy", "Dysprosium", "162.50", "Lanthanide"),
        AurixElement(67, "Ho", "Holmium", "164.93", "Lanthanide"),
        AurixElement(68, "Er", "Erbium", "167.26", "Lanthanide"),
        AurixElement(69, "Tm", "Thulium", "168.93", "Lanthanide"),
        AurixElement(70, "Yb", "Ytterbium", "173.05", "Lanthanide"),
        AurixElement(71, "Lu", "Lutetium", "174.97", "Lanthanide"),

        AurixElement(72, "Hf", "Hafnium", "178.49", "Transition Metal"),
        AurixElement(73, "Ta", "Tantalum", "180.95", "Transition Metal"),
        AurixElement(74, "W", "Tungsten", "183.84", "Transition Metal"),
        AurixElement(75, "Re", "Rhenium", "186.21", "Transition Metal"),
        AurixElement(76, "Os", "Osmium", "190.23", "Transition Metal"),
        AurixElement(77, "Ir", "Iridium", "192.22", "Transition Metal"),
        AurixElement(78, "Pt", "Platinum", "195.08", "Transition Metal"),
        AurixElement(79, "Au", "Gold", "196.97", "Transition Metal"),
        AurixElement(80, "Hg", "Mercury", "200.59", "Transition Metal"),
        AurixElement(81, "Tl", "Thallium", "204.38", "Post-transition Metal"),
        AurixElement(82, "Pb", "Lead", "207.2", "Post-transition Metal"),
        AurixElement(83, "Bi", "Bismuth", "208.98", "Post-transition Metal"),
        AurixElement(84, "Po", "Polonium", "[209]", "Post-transition Metal"),
        AurixElement(85, "At", "Astatine", "[210]", "Halogen"),
        AurixElement(86, "Rn", "Radon", "[222]", "Noble Gas"),

        AurixElement(87, "Fr", "Francium", "[223]", "Alkali Metal"),
        AurixElement(88, "Ra", "Radium", "[226]", "Alkaline Earth Metal"),
        AurixElement(89, "Ac", "Actinium", "[227]", "Actinide"),
        AurixElement(90, "Th", "Thorium", "232.04", "Actinide"),
        AurixElement(91, "Pa", "Protactinium", "231.04", "Actinide"),
        AurixElement(92, "U", "Uranium", "238.03", "Actinide"),
        AurixElement(93, "Np", "Neptunium", "[237]", "Actinide"),
        AurixElement(94, "Pu", "Plutonium", "[244]", "Actinide"),
        AurixElement(95, "Am", "Americium", "[243]", "Actinide"),
        AurixElement(96, "Cm", "Curium", "[247]", "Actinide"),
        AurixElement(97, "Bk", "Berkelium", "[247]", "Actinide"),
        AurixElement(98, "Cf", "Californium", "[251]", "Actinide"),
        AurixElement(99, "Es", "Einsteinium", "[252]", "Actinide"),
        AurixElement(100, "Fm", "Fermium", "[257]", "Actinide"),
        AurixElement(101, "Md", "Mendelevium", "[258]", "Actinide"),
        AurixElement(102, "No", "Nobelium", "[259]", "Actinide"),
        AurixElement(103, "Lr", "Lawrencium", "[266]", "Actinide"),

        AurixElement(104, "Rf", "Rutherfordium", "[267]", "Transition Metal"),
        AurixElement(105, "Db", "Dubnium", "[268]", "Transition Metal"),
        AurixElement(106, "Sg", "Seaborgium", "[269]", "Transition Metal"),
        AurixElement(107, "Bh", "Bohrium", "[270]", "Transition Metal"),
        AurixElement(108, "Hs", "Hassium", "[277]", "Transition Metal"),
        AurixElement(109, "Mt", "Meitnerium", "[278]", "Transition Metal"),
        AurixElement(110, "Ds", "Darmstadtium", "[281]", "Transition Metal"),
        AurixElement(111, "Rg", "Roentgenium", "[282]", "Transition Metal"),
        AurixElement(112, "Cn", "Copernicium", "[285]", "Transition Metal"),
        AurixElement(113, "Nh", "Nihonium", "[286]", "Post-transition Metal"),
        AurixElement(114, "Fl", "Flerovium", "[289]", "Post-transition Metal"),
        AurixElement(115, "Mc", "Moscovium", "[290]", "Post-transition Metal"),
        AurixElement(116, "Lv", "Livermorium", "[293]", "Post-transition Metal"),
        AurixElement(117, "Ts", "Tennessine", "[294]", "Halogen"),
        AurixElement(118, "Og", "Oganesson", "[294]", "Noble Gas")
    )

    fun find(command: String): AurixElement? {

        val c = command
            .lowercase(Locale.getDefault())
            .trim()

        val numberMatch =
            Regex("""\b(?:element|atomic number)\s*(\d{1,3})\b""")
                .find(c)

        if (numberMatch != null) {
            val number = numberMatch.groupValues[1].toIntOrNull()
            return elements.firstOrNull { it.number == number }
        }

        val byNumber =
            Regex("""\b(\d{1,3})\s*(?:element|atomic number)\b""")
                .find(c)

        if (byNumber != null) {
            val number = byNumber.groupValues[1].toIntOrNull()
            return elements.firstOrNull { it.number == number }
        }

        for (element in elements) {

            if (
                c == element.name.lowercase() ||
                c == element.symbol.lowercase() ||
                c.contains("${element.name.lowercase()} element") ||
                c.contains("element ${element.name.lowercase()}") ||
                c.contains("${element.symbol.lowercase()} ka")
            ) {
                return element
            }
        }

        return null
    }

    fun answer(command: String): String? {

        val element = find(command) ?: return null

        val c = command.lowercase(Locale.getDefault())

        return when {

            c.contains("symbol") ||
            c.contains("symbol kya") ->
                "${element.name} ka chemical symbol ${element.symbol} hai, Boss."

            c.contains("atomic number") ||
            c.contains("atomic no") ||
            c.contains("number kya") ->
                "${element.name} ka atomic number ${element.number} hai, Boss."

            c.contains("atomic mass") ||
            c.contains("mass kya") ||
            c.contains("mass kitna") ->
                "${element.name} ka atomic mass ${element.mass} hai, Boss."

            else ->
                "${element.name}, symbol ${element.symbol}, atomic number ${element.number}, atomic mass ${element.mass}, category ${element.category} hai, Boss."
        }
    }
}
