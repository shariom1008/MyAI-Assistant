package com.example.myaiassistant

import java.util.Locale

object AurixBilingualAnswerEngine {
    fun format(question: String, answer: String): String {
        if (answer.isBlank()) return answer
        return if (AurixLanguageLayer.detectLanguage(question) == AurixLanguageLayer.Language.HINGLISH)
            toHinglish(question, answer)
        else answer
    }

    private fun toHinglish(question: String, answer: String): String {
        val q = question.lowercase(Locale.ENGLISH)
        return when {
            q.contains("newton") && q.contains("first law") ->
                "Newton ka first law, jise law of inertia bhi kehte hain, kehta hai ki koi object rest mein hai ya uniform straight-line motion mein hai, to woh waise hi rahega jab tak us par koi net external force na lage."
            q.contains("newton") && q.contains("second law") ->
                "Newton ka second law batata hai ki net force, mass aur acceleration ke product ke barabar hota hai: F = m a."
            q.contains("newton") && q.contains("third law") ->
                "Newton ka third law kehta hai ki interacting bodies ke forces equal magnitude ke aur opposite direction mein hote hain."
            q.contains("momentum") ->
                "Linear momentum kisi object ke mass aur velocity ka product hota hai: p = m v."
            q.contains("kinetic energy") ->
                "Kinetic energy motion ki wajah se object mein stored energy hoti hai: KE = 1/2 m v²."
            q.contains("potential energy") ->
                "Potential energy kisi object ki position ya configuration ki wajah se stored energy hoti hai. Earth ke paas gravitational potential energy approximately mgh hoti hai."
            q.contains("ohm") && q.contains("law") ->
                "Ohm's law ke according fixed conditions mein voltage, current aur resistance ka relation V = I R hota hai."
            q.contains("frequency") ->
                "Frequency batati hai ki ek second mein wave kitne cycles complete karti hai. Iski unit hertz, yaani Hz, hoti hai."
            q.contains("wavelength") ->
                "Wavelength successive waves ke corresponding points ke beech ki distance hoti hai. Iska relation v = f lambda hota hai."
            q.contains("reflection") ->
                "Reflection mein light surface se bounce back karti hai. Ismein angle of incidence, angle of reflection ke barabar hota hai."
            q.contains("refraction") ->
                "Refraction tab hota hai jab light ek medium se doosre medium mein jaate waqt apni speed aur direction change karti hai."
            q.contains("pressure") ->
                "Pressure force per unit area hota hai: P = F/A."
            q.contains("density") ->
                "Density mass per unit volume hoti hai: rho = m/V."
            q.contains("lens") ->
                "Convex lens generally parallel light rays ko converge karta hai, jabki concave lens unhe diverge karta hai."
            q.contains("relativity") ->
                "Special relativity ke according inertial frames mein physical laws same hote hain aur vacuum mein light ki speed invariant hoti hai."
            else -> answer
        }
    }
}
