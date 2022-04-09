package delivery_api

import java.util.*

data class Item(val name: String, val weight: Double) {
    companion object {
        val rng = Random(1L)

        fun randomList(): Collection<Item> {
            val possibleItemsAndWeights =
                    mapOf(
                            "deodorant" to 0.15,
                            "diapers" to 0.5,
                            "towel" to 0.3,
                            "toothbrush" to 0.01,
                            "lipstick" to 0.01,
                            "shampoo" to 0.25
                    )
            val pick = rng.nextInt(6) + 1

            return sequence {
                        repeat(pick) {
                            val index = rng.nextInt(possibleItemsAndWeights.size)
                            val item = possibleItemsAndWeights.entries.elementAt(index)

                            yield(Item(item.key, item.value))
                        }
                    }
                    .toList()
        }
    }
}
