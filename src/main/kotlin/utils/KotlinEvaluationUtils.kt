package utils

import edu.unh.cs753.utils.EvaluationUtils
import edu.unh.cs753.utils.SearchUtils
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.Term
import org.apache.lucene.search.*
import java.io.File
import kotlin.math.log
import kotlin.math.pow
typealias queryRankings = HashMap<String, HashMap<String, Int>>
typealias runRankings = List<Pair<String, queryRankings>>

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


    private fun getInverseTermFreq(searcher: IndexSearcher, term: String, field: String = "text"): Double {
        val nDocs = searcher.indexReader.numDocs()
        val df = searcher.indexReader.docFreq(Term(field, term))
        return Math.log10(nDocs.toDouble() / df.toDouble())
    }

    private fun getProbIDF(searcher: IndexSearcher, term: String, field: String = "text"): Double {
        val nDocs = searcher.indexReader.numDocs()
        val df = searcher.indexReader.docFreq(Term(field, term))
        return Math.max(0.0, Math.log10((nDocs.toDouble() - df.toDouble()) / df.toDouble()))
    }

    fun queryStandard(text: String, searcher: IndexSearcher) =
            SearchUtils.createTokenList(text, StandardAnalyzer())
                .map { term -> TermQuery(Term("text", term)) }
                .fold(BooleanQuery.Builder()) { builder, boostedQuery ->
                    builder.add(BooleanClause(boostedQuery, BooleanClause.Occur.SHOULD)) }
                .build()

    fun queryLTN(text: String, searcher: IndexSearcher) =
            SearchUtils.createTokenList(text, StandardAnalyzer())
                // |-> Get token frequencies
                .groupingBy { it }
                .eachCount()

                // |-> Apply tf-idf
                .map { (term, freq) ->
                    term to (1.0 + Math.log10(freq.toDouble())) * getInverseTermFreq(searcher, term)  }

                // |-> Use final weights to create boosted queries
                .map { (term, tfidf) ->
                    BoostQuery(TermQuery(Term("text", term)), tfidf.toFloat()) }
                .fold(BooleanQuery.Builder()) { builder, boostedQuery ->
                    builder.add(BooleanClause(boostedQuery, BooleanClause.Occur.SHOULD)) }
                .build()

    fun queryBNN(text: String, searcher: IndexSearcher) =
            SearchUtils.createTokenList(text, StandardAnalyzer())
                .distinct()
                .map { term ->
                    TermQuery(Term("text", term)) }
                .fold(BooleanQuery.Builder()) { builder, termQuery ->
                    builder.add(BooleanClause(termQuery, BooleanClause.Occur.SHOULD)) }
                .build()

    fun queryAPC(text: String, searcher: IndexSearcher): BooleanQuery {
        val termFreqs = SearchUtils.createTokenList(text, StandardAnalyzer())
            .groupingBy { it }
            .eachCount()

        val maxFreq = termFreqs.values.max() ?: 1
        return termFreqs

            // |-> Apply Augment Frequency to terms
            .map { (term, freq) ->
                term to (0.5 + (0.5 * freq) / maxFreq.toDouble()) }

            // |->  Then multiply by probabilistic IDF for these terms
            .map { (term, augFreq) ->
                term to augFreq * getProbIDF(searcher, term) }

            // |-> Then normalize using cosine
            .let { results: List<Pair<String, Double>> ->
                val cosineNorm = 1.0 /
                        results.sumByDouble { (_, freq) -> freq.pow(2.0) }.pow(0.5)
                results.map { (term, freq) -> term to freq * cosineNorm }}

            // |-> Turn final term weights into boosted queries
            .map { (term, augProbIdf) ->
                BoostQuery(TermQuery(Term("text", term)), augProbIdf.toFloat()) }
            .fold(BooleanQuery.Builder()) { builder, termQuery ->
                builder.add(BooleanClause(termQuery, BooleanClause.Occur.SHOULD)) }
            .build()
    }


//    fun spearman() {
//        EvaluationUtils.
//
//    }

    public fun getRuns(resultsLoc: String) =
        File(resultsLoc)
            .listFiles()
            .map { run ->
                run.nameWithoutExtension to EvaluationUtils.parseRunFile(run) }


    fun doSpearman(runsToCompare: runRankings, bm25Run: runRankings, queryType: String) {

    }

    private fun compareToBm25(bm25: queryRankings, run: queryRankings) {

    }

}

fun main(args: Array<String>) {
    KotlinEvaluationUtils.getRuns("/home/hcgs/Desktop/projects/assignments/cs753_team2_assignment_3/results")
}