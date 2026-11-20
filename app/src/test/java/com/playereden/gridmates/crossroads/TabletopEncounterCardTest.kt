package com.playereden.gridmates.crossroads

import org.junit.Assert.*
import org.junit.Test

/** Event artwork is presentation only; choice IDs and effects remain canonical. */
class TabletopEncounterCardTest {
    @Test fun shelterCardsUseOnlyTheSuppliedShelterChoices() {
        val choices = listOf("supplies", "cover")
        assertTrue(TabletopEncounterPresentation.isShelter(choices))
        assertEquals("Trail shelter", TabletopEncounterPresentation.title(choices))
        assertEquals("SUPPLIES", encounterActionCopy(choices[0]).title)
        assertEquals("+2 spaces", encounterActionCopy(choices[0]).impact)
        assertEquals("COVER", encounterActionCopy(choices[1]).title)
        assertEquals("+1 & shield", encounterActionCopy(choices[1]).impact)
    }

    @Test fun hazardCardsKeepTheExistingGambleOrBraceOutcomes() {
        val choices = listOf("gamble", "brace")
        assertFalse(TabletopEncounterPresentation.isShelter(choices))
        assertEquals("Trail hazard", TabletopEncounterPresentation.title(choices))
        assertEquals("GAMBLE", encounterActionCopy(choices[0]).title)
        assertEquals("Face hazard", encounterActionCopy(choices[0]).impact)
        assertEquals("BRACE", encounterActionCopy(choices[1]).title)
        assertEquals("Stop & shield", encounterActionCopy(choices[1]).impact)
    }

    @Test fun presentationNeverCreatesOrReordersLegalChoices() {
        val choices = listOf("cover", "supplies")
        val rendered = choices.chunked(2).flatten()
        assertEquals(choices, rendered)
        assertEquals(2, rendered.size)
        assertFalse(TabletopEncounterPresentation.isShelter(emptyList()))
        assertEquals("Trail hazard", TabletopEncounterPresentation.title(emptyList()))
    }
}
