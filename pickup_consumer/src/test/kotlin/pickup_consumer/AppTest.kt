package pickup_consumer

import kotlin.test.*

class ItemTest {
    @Test
    fun pickup_details_can_parse_from_json_string() {
        val json =
                """
        {
            "id": "abc",
            "contents": ["a", "b", "c"],
            "weight": 200.22
        }
        """.trimIndent()

        val sut = PickupDetails.fromJson(json)

        assertEquals("abc", sut.id)
        assertEquals("a", sut.contents.elementAt(0))
        assertEquals("b", sut.contents.elementAt(1))
        assertEquals("c", sut.contents.elementAt(2))
        assertEquals(200.22, sut.weight)
    }
}
