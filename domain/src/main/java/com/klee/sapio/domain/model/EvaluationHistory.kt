package com.klee.sapio.domain.model

data class EvaluationHistory(val evaluations: List<Evaluation>) {

    val current: Evaluation get() = evaluations.last()

    val hasTrend: Boolean get() = evaluations.size > 1

    companion object {

        fun of(evaluations: List<Evaluation>): EvaluationHistory? {
            if (evaluations.isEmpty()) {
                return null
            }

            return EvaluationHistory(evaluations.sortedBy { it.updatedAt })
        }
    }
}
