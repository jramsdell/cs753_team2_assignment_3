package utils

import kotlin.math.log
import kotlin.math.pow

object KotlinEvaluationUtils {
    fun getNDCG(qrels: HashMap<String, HashMap<String, Int>>, runfiles: HashMap<String, ArrayList<String>>) =
        qrels
            .entries
            .map { (query, relevantDocs) ->
                val idealScore =
                        relevantDocs
                            .entries
                            .sortedByDescending { it.value }
                            .take(20)
                            .withIndex()
                            .sumByDouble { (index, entry) ->
                                calcNDCG(entry.value, index + 1)}

                val retrievedDocs = runfiles[query]

                if (retrievedDocs != null) {
                    retrievedDocs.take(20)
                        .map { pid -> relevantDocs[pid] ?: 0 }
                        .withIndex()
                        .sumByDouble { (index, grade) ->
                            calcNDCG(grade, index + 1)}
                        .div(idealScore)
                } else { 0.0 } }
            .average()


    private fun calcNDCG(grade: Int, rank: Int) =
            (2.0.pow(grade) - 1)
                .div(log(1.0 + rank, 2.0))



}