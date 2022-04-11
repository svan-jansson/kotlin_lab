package delivery_metrics

import dev.evo.prometheus.LabelSet
import dev.evo.prometheus.PrometheusMetrics
import dev.evo.prometheus.jvm.DefaultJvmMetrics

class DeliveryEventLabels : LabelSet() {
    var topic by label()
}

class WeightCounterLabels : LabelSet() {
    var id by label()
    var type by label()
}

class DeliveryCounterLabels : LabelSet() {
    var id by label()
    var type by label()
}

class DroneActiveLabels : LabelSet() {
    var id by label()
    var type by label()
}

object DeliveryMetrics : PrometheusMetrics() {
    val deliveryEvents by
    counter("delivery_events", "Events produced by the delivery system") {
        DeliveryEventLabels()
    }

    val weightCounter by
    counter("weight_delivered_by_drone", "Total weight of deliveries per drone") {
        WeightCounterLabels()
    }

    val deliveryCounter by
    counter("deliveries_by_drone", "Count of deliveries made by a drone") {
        DeliveryCounterLabels()
    }

    val droneActiveGauge by
    gauge("drone_active", "Current active status of drone") {
        DroneActiveLabels()
    }

    val jvm by submetrics(DefaultJvmMetrics())
}
