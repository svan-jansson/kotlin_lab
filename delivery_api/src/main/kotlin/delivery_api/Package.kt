package delivery_api

import java.util.*

data class Package(val id: String, val contents: Collection<String>, val weight: Double) {
    companion object {
        fun random(): Package {
            val items = Item.randomList()
            return Package(
                id = UUID.randomUUID().toString(),
                contents = items.map { it.name },
                weight = items.map { it.weight }.sum()
            )
        }
    }

    override fun toString(): String {
        return gson.toJson(this)
    }
}
