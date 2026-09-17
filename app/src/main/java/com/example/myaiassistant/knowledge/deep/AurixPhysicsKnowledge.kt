package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixPhysicsKnowledge {
    val entries = listOf(
        AurixDeepKnowledgeItem("Physics", "Newton First Law", listOf("newton first law", "first law motion", "inertia"), """An object remains at rest or in uniform straight-line motion unless acted on by a net external force.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Newton Second Law", listOf("newton second law", "force mass acceleration"), """For constant mass, net force equals mass times acceleration: F = m a.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Newton Third Law", listOf("newton third law", "action reaction"), """Forces between interacting bodies occur in equal magnitude and opposite direction pairs.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Momentum", listOf("momentum"), """Linear momentum p = m v.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Kinetic Energy", listOf("kinetic energy"), """Kinetic energy is the energy of motion: KE = 1/2 m v².""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Potential Energy", listOf("potential energy", "mgh"), """Near Earth, gravitational potential energy is approximately mgh.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Work", listOf("work physics"), """For a constant force, work is W = F d cos(theta).""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Power", listOf("power physics"), """Power is the rate of doing work: P = W/t.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Pressure", listOf("pressure physics"), """Pressure is force per unit area: P = F/A.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Density", listOf("density"), """Density is mass per unit volume: rho = m/V.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Ohms Law", listOf("ohm law"), """For an ohmic resistor under fixed conditions, V = I R.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Electric Power", listOf("electric power"), """Electrical power can be P = VI, I²R, or V²/R as applicable.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Frequency", listOf("frequency"), """Frequency is cycles per second and is measured in hertz.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Wavelength", listOf("wavelength"), """Wavelength is the distance between corresponding points of successive waves; v = f lambda.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Reflection", listOf("reflection light"), """For reflection, angle of incidence equals angle of reflection.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Refraction", listOf("refraction light"), """Refraction is the change in direction/speed of light when it crosses media; Snell law relates the angles and refractive indices.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "First Law Thermodynamics", listOf("first law thermodynamics"), """Energy conservation in thermodynamics is commonly written ΔU = Q − W when W is work done by the system.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Second Law Thermodynamics", listOf("second law thermodynamics", "entropy"), """The second law gives the direction of spontaneous thermodynamic processes and relates to entropy increase.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Lens", listOf("lens convex concave"), """A convex lens generally converges parallel rays while a concave lens generally diverges them.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Physics", "Relativity", listOf("relativity einstein"), """Special relativity states that physical laws are the same in inertial frames and vacuum light speed is invariant.""", ConfidenceLevel.HIGH, false),
    )
}
