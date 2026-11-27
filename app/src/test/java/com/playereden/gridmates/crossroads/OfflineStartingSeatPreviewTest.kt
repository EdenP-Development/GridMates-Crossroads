package com.playereden.gridmates.crossroads

import com.playereden.crossroads.core.DecisionLab
import org.junit.Assert.*
import org.junit.Test

class OfflineStartingSeatPreviewTest {
    private val lab=CrossroadsCourse.CLASSIC_ID+DecisionLab.V5_SUFFIX
    @Test fun cyclesAllSeatsAndRandom() {
        for(seats in 2..4) {
            var choice=0
            val seen=mutableListOf<Int>()
            repeat(seats+1) {
                seen+=choice
                choice=OfflineStartingSeatPreview.nextChoice(choice,seats)
            }
            assertEquals((0 until seats).toList()+listOf(-1),seen)
            assertEquals(0,choice)
        }
    }

    @Test fun randomDrawOnlyOccursForEligibleRandomChoice() {
        var draws=0
        val draw:(Int)->Int={ n -> draws++; n-1 }
        assertEquals(3,OfflineStartingSeatPreview.resolve(true,false,lab,4,-1,draw))
        assertEquals(1,draws)
        assertEquals(1,OfflineStartingSeatPreview.resolve(true,false,lab,4,1,draw))
        assertEquals(0,OfflineStartingSeatPreview.resolve(false,false,lab,4,-1,draw))
        assertEquals(0,OfflineStartingSeatPreview.resolve(true,true,lab,4,-1,draw))
        assertEquals(0,OfflineStartingSeatPreview.resolve(true,false,CrossroadsCourse.CLASSIC_ID,4,-1,draw))
        assertEquals(0,OfflineStartingSeatPreview.resolve(true,false,lab,2,3,draw))
        assertEquals(1,draws)
    }
}
