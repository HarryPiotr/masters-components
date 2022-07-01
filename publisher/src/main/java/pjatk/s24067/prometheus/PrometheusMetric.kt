package pjatk.s24067.prometheus

import java.util.*

data class PrometheusMetric(
        val name: String?,
        var labels: MutableMap<String, String> = HashMap<String, String>(),
        var value: Double = Double.NaN
) {
    constructor() : this("", HashMap<String, String>(), Double.NaN)

    fun withName(name: String) = copy(name = name)
    fun withLabels(labels: MutableMap<String, String>) = copy(labels = labels)
    fun withLabel(labelName: String, labelValue: String) : PrometheusMetric {
        val result = copy()
        result.labels.put(labelName, labelValue)
        return result
    }
    fun withValue(value: Double) = copy(value = value)

    override fun toString() : String{
        val sb = StringBuilder()
        sb.append(name)
        sb.append("{")
        labels.entries.map { (key, value) -> "$key=\"$value\"" }.joinToString(separator = ", ").forEach { sb.append(it) }
        sb.append("} ")
        sb.append(value)
        return sb.toString()
    }

}