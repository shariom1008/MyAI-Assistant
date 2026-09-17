package com.example.myaiassistant.knowledge.deep

import com.example.myaiassistant.knowledge.AurixDeepKnowledgeItem
import com.example.myaiassistant.knowledge.ConfidenceLevel

object AurixAviationKnowledgeExpansion {
    val entries = listOf(
        AurixDeepKnowledgeItem("Aviation", "Aircraft Engines", listOf("turbofan turbojet turboprop engine", "aircraft engines"), """Aircraft propulsion can use engines such as turbofans, turbojets and turboprops, each suited to different operating requirements.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Aviation", "Flight Instruments", listOf("aviation altimeter airspeed attitude", "flight instruments"), """Flight instruments provide information such as altitude, airspeed, attitude, heading and vertical motion to support aircraft operation.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Aviation", "Aerodynamic Stall", listOf("aircraft stall critical angle attack", "aerodynamic stall"), """An aerodynamic stall occurs when an airfoil exceeds its critical angle of attack and loses substantial lift; it is not simply a low-speed condition.""", ConfidenceLevel.HIGH, false),
        AurixDeepKnowledgeItem("Aviation", "Airport Runway", listOf("airport runway taxiway apron", "airport runway"), """An airport movement area includes runways for takeoff and landing and associated taxiways and aprons for aircraft ground movement.""", ConfidenceLevel.HIGH, false)
    )
}
