package utils

import edu.unh.cs753.utils.SearchUtils
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.Term
import org.apache.lucene.search.*
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
                .groupingBy { it }
                .eachCount()
                .map { (term, freq) ->
                    term to (1.0 + Math.log10(freq.toDouble())) * getInverseTermFreq(searcher, term)  }
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
            .map { (term, freq) ->
                term to (0.5 + (0.5 * freq) / maxFreq.toDouble()) }
            .map { (term, augFreq) ->
                term to augFreq * getProbIDF(searcher, term) }
            .map { (term, augProbIdf) ->
                BoostQuery(TermQuery(Term("text", term)), augProbIdf.toFloat()) }
            .fold(BooleanQuery.Builder()) { builder, termQuery ->
                builder.add(BooleanClause(termQuery, BooleanClause.Occur.SHOULD)) }
            .build()
    }

}