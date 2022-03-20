package pickup_producer

import kotlin.test.*

class ItemTest {
    @Test
    fun return_random_list_of_items() {
        repeat(100) {
            val sut = Item.randomList()

            assertTrue(sut.size > 0, "random items cannot be zero")
        }
    }
}
