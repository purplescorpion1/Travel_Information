package com.travelplanner.repository

import com.travelplanner.model.Station
import org.junit.Assert.*
import org.junit.Test

class StationRepositoryTest {

    private val testStations = listOf(
        Station("Clapham Junction", "CLJ"),
        Station("London Waterloo", "WAT"),
        Station("Wimbledon", "WIM"),
        Station("Aberdeen", "ABD")
    )

    private val repository = StationRepository(null, testStations)

    @Test
    fun testGetSuggestions_PrefixMatch() {
        val suggestions = repository.getSuggestions("Clap")
        assertEquals(1, suggestions.size)
        assertEquals("Clapham Junction", suggestions[0].name)
        assertEquals("CLJ", suggestions[0].crs)
    }

    @Test
    fun testGetSuggestions_CrsMatch() {
        val suggestions = repository.getSuggestions("WAT")
        assertEquals(1, suggestions.size)
        assertEquals("London Waterloo", suggestions[0].name)
    }

    @Test
    fun testGetSuggestions_NoQueryReturnsEmpty() {
        val suggestions = repository.getSuggestions("")
        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun testGetClosestMatch_ExactMatch() {
        val match = repository.getClosestMatch("Wimbledon")
        assertNotNull(match)
        assertEquals("Wimbledon", match?.name)
    }

    @Test
    fun testGetClosestMatch_FuzzyTypoMatch() {
        // Typo: "Claphum" instead of "Clapham Junction"
        val match = repository.getClosestMatch("Claphum")
        assertNotNull(match)
        assertEquals("Clapham Junction", match?.name)
    }

    @Test
    fun testGetClosestMatch_AnotherFuzzyTypoMatch() {
        // Typo: "Waterlow" instead of "London Waterloo"
        val match = repository.getClosestMatch("Waterlow")
        assertNotNull(match)
        assertEquals("London Waterloo", match?.name)
    }
}
