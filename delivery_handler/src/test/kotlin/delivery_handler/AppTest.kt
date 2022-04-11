package delivery_handler

import arrow.core.None
import arrow.core.Some
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        val actual = Package.fromJson(json)

        assertTrue(actual is Some<Package>)

        actual
            .tap { assertEquals("abc", it.id) }
            .tap { assertEquals("a", it.contents.elementAt(0)) }
            .tap { assertEquals("b", it.contents.elementAt(1)) }
            .tap { assertEquals("c", it.contents.elementAt(2)) }
            .tap { assertEquals(200.22, it.weight) }
    }

    @Test
    fun pickup_details_returns_option_none_for_invalid_json() {
        val json = """
        { "invalid json }
        """.trimIndent()

        val actual = Package.fromJson(json)

        assertTrue(actual is None)
    }
}
