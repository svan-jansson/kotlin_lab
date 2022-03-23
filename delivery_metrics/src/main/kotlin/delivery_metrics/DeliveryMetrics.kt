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

object DeliveryMetrics : PrometheusMetrics() {
    val deliveryEvents by
            counter("delivery-events", "Events produced by the delivery system") {
                DeliveryEventLabels()
            }

    val weightCounter by
            counter("weight-delivered-by-drone", "Total weight of deliveries per drone") {
                WeightCounterLabels()
            }

    val jvm by submetrics(DefaultJvmMetrics())
}
